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
package org.jvss.logical;

/**
 * Represents a VSS database and provides access to the items it contains.
 */
public class VssDatabase
{
   public static final String RootProjectName = "$";

   public static final String RootProjectFile = "AAAAAAAA";

   public static final String ProjectSeparatorChar = "/";

   public static final String ProjectSeparator = "/";

   private final String basePath;

   private final String iniPath;

   private final String dataPath;

   private final String nameFile;

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
   public String getNameFile()
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
      String[] = logicalPath.split(ProjectSeparatorChar);
       var index = segments[0] == RootProjectName ? 1 : 0;
       VssProject project = rootProject;
       while (index < segments.Length)
       {
           var name = segments[index++];

           var subproject = project.FindProject(name);
           if (subproject != null)
           {
               project = subproject;
               continue;
           }

           var file = project.FindFile(name);
           if (file != null)
           {
               if (index == segments.Length)
               {
                   return file;
               }
               else
               {
                   var currentPath = string.Join(ProjectSeparator, segments, 0, index);
                   throw new VssPathException(string.Format("{0} is not a project", currentPath));
               }
           }

           throw new VssPathException(string.Format("{0} not found in {1}", name, project.Path));
       }
       return project;
   }

   public VssItem GetItemPhysical(string physicalName)
   {
       physicalName = physicalName.ToUpper();

       if (physicalName == RootProjectFile)
       {
           return rootProject;
       }

       var physicalPath = GetDataPath(physicalName);
       var itemFile = new ItemFile(physicalPath, encoding);
       var isProject = itemFile.Header.ItemType == ItemType.Project;
       var logicalName = GetFullName(itemFile.Header.Name);
       var itemName = new VssItemName(logicalName, physicalName, isProject);
       VssItem item;
       if (isProject)
       {
           var parentFile = ((ProjectHeaderRecord)itemFile.Header).ParentFile;
           var parent = (VssProject)GetItemPhysical(parentFile);
           var logicalPath = BuildPath(parent, logicalName);
           item = new VssProject(this, itemName, physicalPath, logicalPath);
       }
       else
       {
           item = new VssFile(this, itemName, physicalPath);
       }
       item.ItemFile = itemFile;
       return item;
   }

   public bool ItemExists(string physicalName)
   {
       var physicalPath = GetDataPath(physicalName);
       return File.Exists(physicalPath);
   }

   private VssDatabase(String path, String encoding)
   {
       this.basePath = path;
       this.encoding = encoding;

       iniPath = Path.Combine(path, "srcsafe.ini");
       var iniReader = new SimpleIniReader(iniPath);
       iniReader.Parse();

       dataPath = Path.Combine(path, iniReader.GetValue("Data_Path", "data"));

       var namesPath = Path.Combine(dataPath, "names.dat");
       nameFile = new NameFile(namesPath, encoding);

       rootProject = OpenProject(null, RootProjectFile, RootProjectName);
   }

   internal VssProject OpenProject(VssProject parent, string physicalName, string logicalName)
   {
       var itemName = new VssItemName(logicalName, physicalName, true);
       var logicalPath = BuildPath(parent, logicalName);
       var physicalPath = GetDataPath(physicalName);
       return new VssProject(this, itemName, physicalPath, logicalPath);
   }

   internal VssFile OpenFile(string physicalName, string logicalName)
   {
       var itemName = new VssItemName(logicalName, physicalName, false);
       var physicalPath = GetDataPath(physicalName);
       return new VssFile(this, itemName, physicalPath);
   }

   private static string BuildPath(VssProject parent, string logicalName)
   {
       return parent != null ? parent.Path + ProjectSeparator + logicalName : logicalName;
   }

   internal string GetDataPath(string physicalName)
   {
       return Path.Combine(Path.Combine(dataPath, physicalName.Substring(0, 1)), physicalName);
   }

   internal string GetFullName(VssName name)
   {
       if (name.NameFileOffset != 0)
       {
           var nameRecord = nameFile.GetName(name.NameFileOffset);
           var nameIndex = nameRecord.IndexOf(name.IsProject ? NameKind.Project : NameKind.Long);
           if (nameIndex >= 0)
           {
               return nameRecord.GetName(nameIndex);
           }
       }
       return name.ShortName;
   }

   internal VssItemName GetItemName(VssName name, string physicalName)
   {
       return new VssItemName(GetFullName(name), physicalName, name.IsProject);
   }

}
