/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jvss.git;

import org.jvss.git.VssUtil.RecursionStatus;
import org.jvss.git.VssUtil.VssFileCallback;
import org.jvss.git.VssUtil.VssProjectCallback;
import org.jvss.logical.VssAction.VssActionType;
import org.jvss.logical.VssAction.VssNamedAction;
import org.jvss.logical.VssDatabase;
import org.jvss.logical.VssFile;
import org.jvss.logical.VssItem;
import org.jvss.logical.VssProject;
import org.jvss.logical.VssRevision;
import org.jvss.physical.RecordException;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Enumerates revisions in a VSS database.
 */
public class RevisionAnalyzer
{
   private final Logger logger;

   private String excludeFiles;

   private final VssDatabase database;

   private final LinkedList<VssProject> rootProjects = new LinkedList<VssProject>();

   private final Map<Date, List<Revision>> sortedRevisions = new TreeMap<Date, List<Revision>>();

   private final Set<String> processedFiles = new HashSet<String>();

   private final Set<String> destroyedFiles = new HashSet<String>();

   private int projectCount;

   private int fileCount;

   private int revisionCount;

   public RevisionAnalyzer(Logger logger, VssDatabase database)
   {
      this.logger = logger;
      this.database = database;
   }

   public boolean isDestroyed(String physicalName)
   {
      return destroyedFiles.contains(physicalName);
   }

   /**
    * @return the excludeFiles
    */
   public String getExcludeFiles()
   {
      return excludeFiles;
   }

   /**
    * @param excludeFiles
    *           the excludeFiles to set
    */
   public void setExcludeFiles(String excludeFiles)
   {
      this.excludeFiles = excludeFiles;
   }

   /**
    * @return the projectCount
    */
   public int getProjectCount()
   {
      return projectCount;
   }

   /**
    * @param projectCount
    *           the projectCount to set
    */
   public void setProjectCount(int projectCount)
   {
      this.projectCount = projectCount;
   }

   /**
    * @return the fileCount
    */
   public int getFileCount()
   {
      return fileCount;
   }

   /**
    * @param fileCount
    *           the fileCount to set
    */
   public void setFileCount(int fileCount)
   {
      this.fileCount = fileCount;
   }

   /**
    * @return the revisionCount
    */
   public int getRevisionCount()
   {
      return revisionCount;
   }

   /**
    * @param revisionCount
    *           the revisionCount to set
    */
   public void setRevisionCount(int revisionCount)
   {
      this.revisionCount = revisionCount;
   }

   /**
    * @return the database
    */
   public VssDatabase getDatabase()
   {
      return database;
   }

   /**
    * @return the rootProjects
    */
   public LinkedList<VssProject> getRootProjects()
   {
      return rootProjects;
   }

   /**
    * @return the sortedRevisions
    */
   public Map<Date, List<Revision>> getSortedRevisions()
   {
      return sortedRevisions;
   }

   /**
    * @return the processedFiles
    */
   public Set<String> getProcessedFiles()
   {
      return processedFiles;
   }

   /**
    * @return the destroyedFiles
    */
   public Set<String> getDestroyedFiles()
   {
      return destroyedFiles;
   }

   public void addItem(VssProject project)
   {
      if (project == null)
      {
         throw new NullPointerException("project");
      }
      else if (project.getDatabase() != database)
      {
         throw new NullPointerException("Project database mismatch  project");
      }

      rootProjects.addLast(project);
      //TODO fix me
      final PathMatcher[] exclusionMatcher = new PathMatcher[1];
      if (excludeFiles != null && excludeFiles.length() > 0)
      {
         String[] excludeFileArray = excludeFiles.split(";");
         exclusionMatcher[0] = new PathMatcher(excludeFileArray);
      }

      //       workQueue.AddLast(delegate(object work)
      //       {
      logger.WriteSectionSeparator();
      //LogStatus(work, "Building revision list");

      logger.WriteLine(String.format("Root project: %s", project.getPath()));
      logger.WriteLine(String.format("Excluded files: %s", excludeFiles));

      final int[] excludedProjects = new int[]{0};
      final int[] excludedFiles = new int[]{0};
      //var stopwatch = Stopwatch.StartNew();
      long start = System.currentTimeMillis();
      VssUtil.recurseItems(project, new VssProjectCallback()
      {

         @Override
         public RecursionStatus call(VssProject subproject)
         {
            //               // TODO Auto-generated method stub
            //               return null;

            //                   if (workQueue.IsAborting)
            //                   {
            //                       return RecursionStatus.Abort;
            //                   }

            String path = subproject.getPath();
            if (exclusionMatcher[0] != null && exclusionMatcher[0].Matches(path))
            {
               logger.WriteLine("Excluding project {0}", path);
               excludedProjects[0] += 1;
               return RecursionStatus.Skip;
            }

            processItem(subproject, path, exclusionMatcher[0]);
            ++projectCount;
            return RecursionStatus.Continue;
         }
      }, new VssFileCallback()
      {

         @Override
         public RecursionStatus call(VssProject subproject, VssFile file)
         {
            //            if (workQueue.IsAborting)
            //            {
            //               return RecursionStatus.Abort;
            //            }

            String path = file.GetPath(subproject);
            if (exclusionMatcher[0] != null && exclusionMatcher[0].Matches(path))
            {
               logger.WriteLine("Excluding file {0}", path);
               excludedFiles[0] += 1;
               return RecursionStatus.Skip;
            }

            // only process shared files once (projects are never shared)
            if (!processedFiles.contains(file.getPhysicalName()))
            {
               processedFiles.add(file.getPhysicalName());
               processItem(file, path, exclusionMatcher[0]);
               ++fileCount;
            }
            return RecursionStatus.Continue;
         }
      });
      //stopwatch.Stop();

      logger.WriteSectionSeparator();
      logger.WriteLine(String.format("Analysis complete in %d msec", System.currentTimeMillis() - start));
      logger.WriteLine(String.format("Projects: %d ( %d excluded)", projectCount, excludedProjects[0]));
      logger.WriteLine(String.format("Files: %d (%d excluded)", fileCount, excludedFiles[0]));
      logger.WriteLine(String.format("Revisions: %d", revisionCount));
      //  });
   }

   private void processItem(VssItem item, String path, PathMatcher exclusionMatcher)
   {
      try
      {
         for (VssRevision vssRevision : item.getRevisions())
         {

            //logger.WriteLine("ProcessItem<<<");
            //logger.WriteLine(vssRevision.getAction());
            //logger.WriteLine(vssRevision.getAction());
            //logger.WriteLine("ProcessItem>>>>" + vssRevision);
            VssActionType actionType = vssRevision.getAction().type();
            //            if (!(vssRevision.getAction() instanceof VssNamedAction))
            //            {
            //               System.err.println("not an instance of VssNamedAction");
            //               continue;
            //            }

            //if (namedAction != null)
            if (vssRevision.getAction() instanceof VssNamedAction)
            {
               VssNamedAction namedAction = (VssNamedAction)vssRevision.getAction();
               if (actionType == VssActionType.Destroy)
               {
                  // track destroyed files so missing history can be anticipated
                  // (note that Destroy actions on shared files simply delete
                  // that copy, so destroyed files can't be completely ignored)
                  destroyedFiles.add(namedAction.name().getPhysicalName());
               }

               String targetPath = path + VssDatabase.ProjectSeparator + namedAction.name().getLogicalName();
               if (exclusionMatcher != null && exclusionMatcher.Matches(targetPath))
               {
                  // project action targets an excluded file
                  continue;
               }
            }

            Revision revision =
               new Revision(vssRevision.getDate(), vssRevision.getUser(), item.getItemName(), vssRevision.getVersion(),
                  vssRevision.getComment(), vssRevision.getAction());

            List<Revision> revisionSet = sortedRevisions.get(vssRevision.getDate());
            if (revisionSet == null)
            {
               revisionSet = new LinkedList<Revision>();
               sortedRevisions.put(vssRevision.getDate(), revisionSet);
            }
            revisionSet.add(revision);
            ++revisionCount;
         }
      }
      catch (RecordException e)
      {
         String message =
            String.format("Failed to read revisions for {0} ({1}): {2}", path, item.getPhysicalName(), "");
         //LogException(e, message);
         //ReportError(message);
      }
   }
}
