/*
 * Copyright 2009 HPDI, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jvss.logical;

import org.jvss.physical.ItemFile;
import org.jvss.physical.ItemHeaderRecord.ItemType;
import org.jvss.physical.NameFile;
import org.jvss.physical.NameRecord;
import org.jvss.physical.NameRecord.NameKind;
import org.jvss.physical.ProjectHeaderRecord;
import org.jvss.physical.VssName;

import java.io.File;
import java.io.IOException;

/**
 * Represents a VSS database and provides access to the items it contains.
 */
public class VssDatabase
{
   public static final String RootProjectName = "$";

   public static final String RootProjectFile = "aaaaaaaa";

   public static final String ProjectSeparatorChar = "/";

   public static final String ProjectSeparator = "/";

   private final String basePath;

   private final String iniPath;

   private final String dataPath;

   private final NameFile nameFile;

   private final VssProject rootProject;

   private final String encoding;

   /**
    * @return the basePath
    */
   public String getBasePath()
   {
      return basePath;
   }

   /**
    * @return the iniPath
    */
   public String getIniPath()
   {
      return iniPath;
   }

   /**
    * @return the dataPath
    */
   public String getDataPath()
   {
      return dataPath;
   }

   /**
    * @return the nameFile
    */
   public NameFile getNameFile()
   {
      return nameFile;
   }

   /**
    * @return the rootProject
    */
   public VssProject getRootProject()
   {
      return rootProject;
   }

   /**
    * @return the encoding
    */
   public String getEncoding()
   {
      return encoding;
   }

   public VssItem GetItem(String logicalPath)
   {
      //       var segments = logicalPath.Split(new char[] { ProjectSeparatorChar },
      //           StringSplitOptions.RemoveEmptyEntries);
      String[] segments = logicalPath.split(ProjectSeparatorChar);
      int index = segments[0] == RootProjectName ? 1 : 0;
      VssProject project = rootProject;
      while (index < segments.length)
      {
         String name = segments[index++];

         VssProject subproject = project.findProject(name);
         if (subproject != null)
         {
            project = subproject;
            continue;
         }

         VssFile file = project.findFile(name);
         if (file != null)
         {
            if (index == segments.length)
            {
               return file;
            }
            else
            {
               //var currentPath = String..Join(ProjectSeparator, segments, 0, index);
               //TODO join Strings
               throw new VssPathException(String.format("{0} is not a project", segments));
            }
         }

         throw new VssPathException(String.format("{0} not found in {1}", name, project.getPath()));
      }
      return project;
   }

   public VssItem GetItemPhysical(String physicalName)
   {
      physicalName = physicalName.toUpperCase();

      if (physicalName == RootProjectFile)
      {
         return rootProject;
      }

      String physicalPath = GetDataPath(physicalName);
      ItemFile itemFile = new ItemFile(physicalPath, encoding);
      boolean isProject = itemFile.getHeader().getItemType() == ItemType.PROJECT;
      String logicalName = GetFullName(itemFile.getHeader().getName());
      VssItemName itemName = new VssItemName(logicalName, physicalName, isProject);
      VssItem item;
      if (isProject)
      {
         String parentFile = ((ProjectHeaderRecord)itemFile.getHeader()).getParentFile();
         VssProject parent = (VssProject)GetItemPhysical(parentFile);
         String logicalPath = BuildPath(parent, logicalName);
         item = new VssProject(this, itemName, physicalPath, logicalPath);
      }
      else
      {
         item = new VssFile(this, itemName, physicalPath);
      }
      item.setItemFile(itemFile);
      return item;
   }

   public boolean ItemExists(String physicalName)
   {
      String physicalPath = GetDataPath(physicalName);
      return new File(physicalPath.toLowerCase()).exists();
   }

   public VssDatabase(String path, String encoding) throws IOException
   {
      this.basePath = path;
      this.encoding = encoding;

      iniPath = new File(path, "srcsafe.ini").getAbsolutePath();
      SimpleIniReader iniReader = new SimpleIniReader(iniPath);
      iniReader.parse();

      dataPath = new File(path, iniReader.getValue("Data_Path", "data")).getAbsolutePath();

      String namesPath = new File(dataPath, "names.dat").getAbsolutePath();
      nameFile = new NameFile(namesPath, encoding);

      rootProject = OpenProject(null, RootProjectFile, RootProjectName);
   }

   public VssProject OpenProject(VssProject parent, String physicalName, String logicalName)
   {
      VssItemName itemName = new VssItemName(logicalName, physicalName, true);
      String logicalPath = BuildPath(parent, logicalName);
      String physicalPath = GetDataPath(physicalName);
      return new VssProject(this, itemName, physicalPath, logicalPath);
   }

   public VssFile OpenFile(String physicalName, String logicalName)
   {
      VssItemName itemName = new VssItemName(logicalName, physicalName, false);
      String physicalPath = GetDataPath(physicalName);
      return new VssFile(this, itemName, physicalPath);
   }

   public static String BuildPath(VssProject parent, String logicalName)
   {
      return parent != null ? parent.getPath() + ProjectSeparator + logicalName : logicalName;
   }

   public String GetDataPath(String physicalName)
   {
      return new File(new File(dataPath, physicalName.substring(0, 1)).getAbsolutePath(), physicalName)
         .getAbsolutePath();
   }

   public String GetFullName(VssName name)
   {
      if (name.nameFileOffset() != 0)
      {
         NameRecord nameRecord = nameFile.GetName(name.nameFileOffset());
         int nameIndex = nameRecord.indexOf(name.isProject() ? NameKind.Project : NameKind.Long);
         if (nameIndex >= 0)
         {
            return nameRecord.getName(nameIndex);
         }
      }
      return name.shortName();
   }

   public VssItemName GetItemName(VssName name, String physicalName)
   {
      return new VssItemName(GetFullName(name), physicalName, name.isProject());
   }

}
