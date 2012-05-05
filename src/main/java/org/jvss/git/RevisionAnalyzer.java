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

import org.jvss.logical.VssDatabase;
import org.jvss.logical.VssProject;

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
   private String excludeFiles;

   private final VssDatabase database;

   private final LinkedList<VssProject> rootProjects = new LinkedList<VssProject>();

   private final Map<Date, List<Revision>> sortedRevisions = new TreeMap<Date, List<Revision>>();

   private final Set<String> processedFiles = new HashSet<String>();

   private final Set<String> destroyedFiles = new HashSet<String>();

   private int projectCount;

   private int fileCount;

   private int revisionCount;

   public RevisionAnalyzer(VssDatabase database)
   {
      this.database = database;
   }

   public boolean isDestroyed(String physicalName)
   {
      return destroyedFiles.contains(physicalName);
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

       PathMatcher exclusionMatcher = null;
       if (excludeFiles!=null && excludeFiles.length()>0)
       {
           String[] excludeFileArray = excludeFiles.split(";");
           exclusionMatcher = new PathMatcher(excludeFileArray);
       }

       workQueue.AddLast(delegate(object work)
       {
           logger.WriteSectionSeparator();
           LogStatus(work, "Building revision list");

           logger.WriteLine("Root project: {0}", project.Path);
           logger.WriteLine("Excluded files: {0}", excludeFiles);

           int excludedProjects = 0;
           int excludedFiles = 0;
           var stopwatch = Stopwatch.StartNew();
           VssUtil.RecurseItems(project,
               delegate(VssProject subproject)
               {
                   if (workQueue.IsAborting)
                   {
                       return RecursionStatus.Abort;
                   }

                   var path = subproject.Path;
                   if (exclusionMatcher != null && exclusionMatcher.Matches(path))
                   {
                       logger.WriteLine("Excluding project {0}", path);
                       ++excludedProjects;
                       return RecursionStatus.Skip;
                   }

                   ProcessItem(subproject, path, exclusionMatcher);
                   ++projectCount;
                   return RecursionStatus.Continue;
               },
               delegate(VssProject subproject, VssFile file)
               {
                   if (workQueue.IsAborting)
                   {
                       return RecursionStatus.Abort;
                   }

                   var path = file.GetPath(subproject);
                   if (exclusionMatcher != null && exclusionMatcher.Matches(path))
                   {
                       logger.WriteLine("Excluding file {0}", path);
                       ++excludedFiles;
                       return RecursionStatus.Skip;
                   }

                   // only process shared files once (projects are never shared)
                   if (!processedFiles.Contains(file.PhysicalName))
                   {
                       processedFiles.Add(file.PhysicalName);
                       ProcessItem(file, path, exclusionMatcher);
                       ++fileCount;
                   }
                   return RecursionStatus.Continue;
               });
           stopwatch.Stop();

           logger.WriteSectionSeparator();
           logger.WriteLine("Analysis complete in {0:HH:mm:ss}", new DateTime(stopwatch.ElapsedTicks));
           logger.WriteLine("Projects: {0} ({1} excluded)", projectCount, excludedProjects);
           logger.WriteLine("Files: {0} ({1} excluded)", fileCount, excludedFiles);
           logger.WriteLine("Revisions: {0}", revisionCount);
       });
   }

   private void ProcessItem(VssItem item, string path, PathMatcher exclusionMatcher)
   {
       try
       {
           foreach (VssRevision vssRevision in item.Revisions)
           {
               var actionType = vssRevision.Action.Type;
               var namedAction = vssRevision.Action as VssNamedAction;
               if (namedAction != null)
               {
                   if (actionType == VssActionType.Destroy)
                   {
                       // track destroyed files so missing history can be anticipated
                       // (note that Destroy actions on shared files simply delete
                       // that copy, so destroyed files can't be completely ignored)
                       destroyedFiles.Add(namedAction.Name.PhysicalName);
                   }

                   var targetPath = path + VssDatabase.ProjectSeparator + namedAction.Name.LogicalName;
                   if (exclusionMatcher != null && exclusionMatcher.Matches(targetPath))
                   {
                       // project action targets an excluded file
                       continue;
                   }
               }

               Revision revision = new Revision(vssRevision.DateTime,
                   vssRevision.User, item.ItemName, vssRevision.Version,
                   vssRevision.Comment, vssRevision.Action);

               ICollection<Revision> revisionSet;
               if (!sortedRevisions.TryGetValue(vssRevision.DateTime, out revisionSet))
               {
                   revisionSet = new LinkedList<Revision>();
                   sortedRevisions[vssRevision.DateTime] = revisionSet;
               }
               revisionSet.Add(revision);
               ++revisionCount;
           }
       }
       catch (RecordException e)
       {
           var message = string.Format("Failed to read revisions for {0} ({1}): {2}",
               path, item.PhysicalName, ExceptionFormatter.Format(e));
           LogException(e, message);
           ReportError(message);
       }
   }
}
