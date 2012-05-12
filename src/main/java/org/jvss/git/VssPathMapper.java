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
import org.jvss.logical.VssItemName;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Tracks the names and locations of VSS projects and files as revisions are
 * replayed.
 */
public class VssPathMapper
{
   /**
    * 
    * Base class for representing VSS items.
    */
   class VssItemInfo
   {
      private final String physicalName;

      private String logicalName;

      private boolean destroyed;

      public VssItemInfo(String physicalName, String logicalName)
      {
         this.physicalName = physicalName;
         this.logicalName = logicalName;
      }

      /**
       * @return the physicalName
       */
      public String getPhysicalName()
      {
         return physicalName;
      }

      /**
       * @param logicalName
       *           the logicalName to set
       */
      public void setLogicalName(String logicalName)
      {
         this.logicalName = logicalName;
      }

      /**
       * @return the destroyed
       */
      public boolean isDestroyed()
      {
         return destroyed;
      }

      /**
       * @param destroyed
       *           the destroyed to set
       */
      public void setDestroyed(boolean destroyed)
      {
         this.destroyed = destroyed;
      }

      /**
       * @return the physicalname
       */
      public String getPhysicalname()
      {
         return physicalName;
      }

      /**
       * @return the logicalName
       */
      public String getLogicalName()
      {
         return logicalName;
      }

   }

   /**
    * Represents the current state of a VSS project.
    */
   class VssProjectInfo extends VssItemInfo
   {
      private VssProjectInfo parentInfo;

      private boolean isRoot;

      // valid only for root paths; used to resolve project specifiers
      private String originalVssPath;

      public VssProjectInfo getParent()
      {
         return parentInfo;
      }

      public void setParent(VssProjectInfo value)
      {
         if (parentInfo != value)
         {
            if (parentInfo != null)
            {
               parentInfo.removeItem(this);
            }
            parentInfo = value;
            if (parentInfo != null)
            {
               parentInfo.addItem(this);
            }
         }
      }

      /**
       * @return the isRoot
       */
      public boolean isRoot()
      {
         return isRoot;
      }

      /**
       * @param isRoot
       *           the isRoot to set
       */
      public void setRoot(boolean isRoot)
      {
         this.isRoot = isRoot;
      }

      /**
       * @return the originalVssPath
       */
      public String getOriginalVssPath()
      {
         return originalVssPath;
      }

      /**
       * @param originalVssPath
       *           the originalVssPath to set
       */
      public void setOriginalVssPath(String originalVssPath)
      {
         this.originalVssPath = originalVssPath;
      }

      public boolean isRooted()
      {
         VssProjectInfo project = this;
         while (project.parentInfo != null)
         {
            project = project.parentInfo;
         }
         return project.isRoot;
      }

      private final LinkedList<VssItemInfo> items = new LinkedList<VssItemInfo>();

      public Iterable<VssItemInfo> getItems()
      {
         return items;
      }

      public VssProjectInfo(String physicalName, String logicalName)
      {
         super(physicalName, logicalName);
      }

      public String getPath()
      {
         if (isRooted())
         {
            if (parentInfo != null)
            {
               return new File(parentInfo.getPath(), getLogicalName()).getAbsolutePath();
            }
            else
            {
               return getLogicalName();
            }
         }
         return null;
      }

      public boolean isSameOrSubproject(VssProjectInfo parentInfo)
      {
         VssProjectInfo project = this;
         while (project != null)
         {
            if (project == parentInfo)
            {
               return true;
            }
            project = project.parentInfo;
         }
         return false;
      }

      public void addItem(VssItemInfo item)
      {
         items.addLast(item);
      }

      public void removeItem(VssItemInfo item)
      {
         items.remove(item);
      }

      public boolean containsLogicalName(String logicalName)
      {
         for (VssItemInfo item : items)
         {
            if (item.getLogicalName().equals(logicalName))
            {
               return true;
            }
         }
         return false;
      }

      public boolean containsFiles()
      {
         LinkedList<VssProjectInfo> subprojects = new LinkedList<VssProjectInfo>();
         VssProjectInfo project = this;
         while (project != null)
         {
            for (VssItemInfo item : project.items)
            {

               if (item instanceof VssProjectInfo)
               {
                  VssProjectInfo subproject = (VssProjectInfo)item;
                  subprojects.addLast(subproject);
               }
               else
               {
                  return true;
               }
            }
            if (subprojects.size() != 0)
            {
               //TODO can be done in one remove
               project = subprojects.getFirst();
               subprojects.remove();
            }
            else
            {
               project = null;
            }
         }
         return false;
      }

      public Iterable<VssFileInfo> getAllFiles()
      {

         List<VssFileInfo> result = new ArrayList<VssFileInfo>();
         LinkedList<VssProjectInfo> subprojects = new LinkedList<VssProjectInfo>();
         VssProjectInfo project = this;
         while (project != null)
         {
            for (VssItemInfo item : project.getItems())
            {

               if (item instanceof VssProjectInfo)
               {
                  VssProjectInfo subproject = (VssProjectInfo)item;
                  subprojects.addLast(subproject);
               }
               else
               {
                  result.add((VssFileInfo)item);
               }
            }
            if (subprojects.size() != 0)
            {
               //TODO can be done in one remove
               project = subprojects.getFirst();
               subprojects.remove();
            }
            else
            {
               project = null;
            }
         }
         return result;
      }

      public Iterable<VssProjectInfo> getAllProjects()
      {
         List<VssProjectInfo> result = new ArrayList<VssProjectInfo>();
         LinkedList<VssProjectInfo> subprojects = new LinkedList<VssProjectInfo>();
         VssProjectInfo project = this;
         while (project != null)
         {
            for (VssItemInfo item : project.items)
            {

               if (item instanceof VssProjectInfo)
               {
                  VssProjectInfo subproject = (VssProjectInfo)item;
                  if (subproject != null)
                  {
                     result.add(subproject);
                  }
               }
            }
            //if (subprojects.getFirst() != null)
            if (subprojects.size() != 0)
            {
               project = subprojects.getFirst();
               subprojects.remove();
            }
            else
            {
               project = null;
            }
         }
         return result;
      }

   }

   /// <summary>
   /// Represents the current state of a VSS file.
   /// </summary>
   /// <author>Trevor Robinson</author>
   class VssFileInfo extends VssItemInfo
   {
      private final List<VssProjectInfo> projects = new ArrayList<VssProjectInfo>();

      public Iterable<VssProjectInfo> getProjects()
      {
         return projects;
      }

      private int version = 1;

      /**
       * @return the version
       */
      public int getVersion()
      {
         return version;
      }

      /**
       * @param version
       *           the version to set
       */
      public void setVersion(int version)
      {
         this.version = version;
      }

      public VssFileInfo(String physicalName, String logicalName)
      {
         super(physicalName, logicalName);
      }

      public void addProject(VssProjectInfo project)
      {
         projects.add(project);
      }

      public void removeProject(VssProjectInfo project)
      {
         projects.remove(project);
      }
   }

   // keyed by physical name
   private final static Map<String, VssProjectInfo> projectInfos = new HashMap<String, VssProjectInfo>();

   private final static Map<String, VssProjectInfo> rootInfos = new HashMap<String, VssProjectInfo>();

   private final static Map<String, VssFileInfo> fileInfos = new HashMap<String, VssFileInfo>();

   public boolean isProjectRooted(String project)
   {
      VssProjectInfo projectInfo = projectInfos.get(project);
      if (projectInfo != null)
      {
         return projectInfo.isRooted();
      }
      return false;
   }

   public String getProjectPath(String project)
   {
      VssProjectInfo projectInfo = projectInfos.get(project);
      if (projectInfo != null)
      {
         return projectInfo.getPath();
      }
      return null;
   }

   public void setProjectPath(String project, String path, String originalVssPath)
   {
      VssProjectInfo projectInfo = new VssProjectInfo(project, path);
      projectInfo.setRoot(true);
      projectInfo.setOriginalVssPath(originalVssPath);
      projectInfos.put(project, projectInfo);
      rootInfos.put(project, projectInfo);
   }

   public Iterable<VssFileInfo> getAllFiles(String project)
   {
      VssProjectInfo projectInfo = projectInfos.get(project);
      if (projectInfo != null)
      {
         return projectInfo.getAllFiles();
      }
      return null;
   }

   public Iterable<VssProjectInfo> getAllProjects(String project)
   {

      VssProjectInfo projectInfo = projectInfos.get(project);
      if (projectInfo != null)
      {
         return projectInfo.getAllProjects();
      }

      return null;
   }

   public Iterable<String> getFilePaths(String file, String underProject)
   {
      LinkedList<String> result = new LinkedList<String>();
      VssFileInfo fileInfo = fileInfos.get(file);

      if (fileInfo != null)
      {
         VssProjectInfo underProjectInfo = null;
         if (underProject != null)
         {
            if (!projectInfos.containsKey(underProject))
            {
               return result;
            }
         }
         for (VssProjectInfo project : fileInfo.getProjects())
         {
            if (underProjectInfo == null || project.isSameOrSubproject(underProjectInfo))
            {
               // ignore projects that are not rooted
               String projectPath = project.getPath();
               if (projectPath != null)
               {
                  String path = new File(projectPath, fileInfo.getLogicalName()).getAbsolutePath();
                  result.addLast(path);
               }
            }
         }
      }
      return result;
   }

   public int getFileVersion(String file)
   {
      VssFileInfo fileInfo = fileInfos.get(file);
      if (fileInfo != null)
      {
         return fileInfo.getVersion();
      }
      return 1;
   }

   public void setFileVersion(VssItemName name, int version)
   {
      VssFileInfo fileInfo = getOrCreateFile(name);
      fileInfo.setVersion(version);
   }

   public VssItemInfo addItem(VssItemName project, VssItemName name)
   {
      VssProjectInfo parentInfo = getOrCreateProject(project);
      VssItemInfo itemInfo;
      if (name.isProject())
      {
         VssProjectInfo projectInfo = getOrCreateProject(name);
         projectInfo.setParent(parentInfo);
         itemInfo = projectInfo;
      }
      else
      {
         VssFileInfo fileInfo = getOrCreateFile(name);
         fileInfo.addProject(parentInfo);
         parentInfo.addItem(fileInfo);
         itemInfo = fileInfo;
      }

      // update name of item in case it was created on demand by
      // an earlier unmapped item that was subsequently renamed
      itemInfo.setLogicalName(name.getLogicalName());

      return itemInfo;
   }

   public VssItemInfo renameItem(VssItemName name)
   {
      VssItemInfo itemInfo;
      if (name.isProject())
      {
         itemInfo = getOrCreateProject(name);
      }
      else
      {
         itemInfo = getOrCreateFile(name);
      }
      itemInfo.setLogicalName(name.getLogicalName());
      return itemInfo;
   }

   public VssItemInfo deleteItem(VssItemName project, VssItemName name)
   {
      VssProjectInfo parentInfo = getOrCreateProject(project);
      VssItemInfo itemInfo;
      if (name.isProject())
      {
         VssProjectInfo projectInfo = getOrCreateProject(name);
         projectInfo.setParent(null);
         itemInfo = projectInfo;
      }
      else
      {
         VssFileInfo fileInfo = getOrCreateFile(name);
         fileInfo.removeProject(parentInfo);
         parentInfo.removeItem(fileInfo);
         itemInfo = fileInfo;
      }
      return itemInfo;
   }

   public VssItemInfo recoverItem(VssItemName project, VssItemName name)
   {
      VssProjectInfo parentInfo = getOrCreateProject(project);
      VssItemInfo itemInfo;
      if (name.isProject())
      {
         VssProjectInfo projectInfo = getOrCreateProject(name);
         projectInfo.setParent(parentInfo);
         itemInfo = projectInfo;
      }
      else
      {
         VssFileInfo fileInfo = getOrCreateFile(name);
         fileInfo.addProject(parentInfo);
         parentInfo.addItem(fileInfo);
         itemInfo = fileInfo;
      }
      return itemInfo;
   }

   public VssItemInfo pinItem(VssItemName project, VssItemName name)
   {
      // pinning removes the project from the list of
      // sharing projects, so it no longer receives edits
      return deleteItem(project, name);
   }

   public VssItemInfo unpinItem(VssItemName project, VssItemName name)
   {
      // unpinning restores the project to the list of
      // sharing projects, so it receives edits
      return recoverItem(project, name);
   }

   public VssItemInfo branchFile(VssItemName project, VssItemName newName, VssItemName oldName)
   {
      assert !newName.isProject();
      assert !oldName.isProject();
      //Debug.Assert(!newName.IsProject);
      //Debug.Assert(!oldName.IsProject);

      // "branching a file" (in VSS parlance) essentially moves it from
      // one project to another (and could potentially change its name)
      VssProjectInfo parentInfo = getOrCreateProject(project);

      // remove filename from old project
      VssFileInfo oldFile = getOrCreateFile(oldName);
      oldFile.removeProject(parentInfo);
      parentInfo.removeItem(oldFile);

      // add filename to new project
      VssFileInfo newFile = getOrCreateFile(newName);
      newFile.addProject(parentInfo);
      parentInfo.addItem(newFile);

      // retain version number from old file
      newFile.setVersion(oldFile.getVersion());

      return newFile;
   }

   public VssProjectInfo moveProjectFrom(VssItemName project, VssItemName subproject, String oldProjectSpec)
   {
      //Debug.Assert(subproject.IsProject);
      assert subproject.isProject();

      VssProjectInfo parentInfo = getOrCreateProject(project);
      VssProjectInfo subprojectInfo = getOrCreateProject(subproject);
      subprojectInfo.setParent(parentInfo);
      return subprojectInfo;
   }

   public VssProjectInfo moveProjectTo(VssItemName project, VssItemName subproject, String newProjectSpec)
   {
      VssProjectInfo subprojectInfo = getOrCreateProject(subproject);
      int lastSlash = newProjectSpec.lastIndexOf('/');
      if (lastSlash > 0)
      {
         String newParentSpec = newProjectSpec.substring(0, lastSlash);
         VssProjectInfo parentInfo = resolveProjectSpec(newParentSpec);
         if (parentInfo != null)
         {
            // propagate the destroyed flag from the new parent
            subprojectInfo.setParent(parentInfo);
            subprojectInfo.setDestroyed(subprojectInfo.isDestroyed() | parentInfo.isDestroyed());
            //subprojectInfo.Destroyed |= parentInfo.Destroyed;
         }
         else
         {
            // if resolution fails, the target project has been destroyed
            // or is outside the set of projects being mapped
            subprojectInfo.setDestroyed(true);
         }
      }
      return subprojectInfo;
   }

   public boolean projectContainsLogicalName(VssItemName project, VssItemName name)
   {
      VssProjectInfo parentInfo = getOrCreateProject(project);
      return parentInfo.containsLogicalName(name.getLogicalName());
   }

   private VssProjectInfo getOrCreateProject(VssItemName name)
   {
      VssProjectInfo projectInfo = projectInfos.get(name.getPhysicalName());
      if (projectInfo == null)
      {
         projectInfo = new VssProjectInfo(name.getPhysicalName(), name.getLogicalName());
         projectInfos.put(name.getPhysicalName(), projectInfo);
      }

      return projectInfo;
   }

   private VssFileInfo getOrCreateFile(VssItemName name)
   {
      VssFileInfo fileInfo = fileInfos.get(name.getPhysicalName());
      if (fileInfo == null)
      {
         fileInfo = new VssFileInfo(name.getPhysicalName(), name.getLogicalName());
         fileInfos.put(name.getPhysicalName(), fileInfo);
      }
      return fileInfo;
   }

   private VssProjectInfo resolveProjectSpec(String projectSpec)
   {
      if (!projectSpec.startsWith("$/"))
      {
         System.out.println("Project spec must start with $/");
         //throw new IllegalArgumentException("Project spec must start with $/");
      }

      for (VssProjectInfo rootInfo : rootInfos.values())
      {

         if (projectSpec.startsWith(rootInfo.getOriginalVssPath()))
         {
            if (projectSpec.equals(rootInfo.getOriginalVssPath()))
            {
               return rootInfo;
            }

            int rootLength = rootInfo.getOriginalVssPath().length();
            if (!rootInfo.getOriginalVssPath().endsWith("/"))
            {
               ++rootLength;
            }
            String subpath = projectSpec.substring(rootLength);
            String[] subprojectNames = subpath.split("/");
            VssProjectInfo projectInfo = rootInfo;
            for (String subprojectName : subprojectNames)
            {
               boolean found = false;
               for (VssItemInfo item : projectInfo.getItems())
               {
                  if (item instanceof VssProjectInfo)
                  {
                     VssProjectInfo subprojectInfo = (VssProjectInfo)item;
                     if (subprojectInfo != null && subprojectInfo.getLogicalName().equals(subprojectName))
                     {
                        projectInfo = subprojectInfo;
                        found = true;
                        break;
                     }
                  }
               }
               if (!found)
               {
                  return null;
               }
            }
            return projectInfo;
         }
      }

      return null;
   }

   public static String getWorkingPath(String workingRoot, String vssPath)
   {
      if (vssPath.equals("$"))
      {
         return workingRoot;
      }

      if (vssPath.startsWith("$/"))
      {
         vssPath = vssPath.substring(2);
      }

      String relPath = vssPath.replace(VssDatabase.ProjectSeparatorChar, File.pathSeparator);
      return new File(workingRoot, relPath).getAbsolutePath();
   }
}
