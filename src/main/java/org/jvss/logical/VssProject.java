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
 * Represents a VSS project.
 */
public class VssProject extends VssItem
{
   private final String logicalPath;

   

   /**
    * @return the logicalPath
    */
   public String getLogicalPath()
   {
      return logicalPath;
   }

   public String getPath()
   {
      return logicalPath;
   }
   
   public IEnumerable<VssProject> Projects
   {
       get { return new VssProjects(this); }
   }

   public IEnumerable<VssFile> Files
   {
       get { return new VssFiles(this); }
   }

   public new IEnumerable<VssProjectRevision> Revisions
   {
       get { return new VssRevisions<VssProject, VssProjectRevision>(this); }
   }

   public new VssProjectRevision GetRevision(int version)
   {
       return (VssProjectRevision)base.GetRevision(version);
   }

   public VssProject FindProject(string name)
   {
       foreach (VssProject subproject in Projects)
       {
           if (name == subproject.Name)
           {
               return subproject;
           }
       }
       return null;
   }

   public VssFile FindFile(string name)
   {
       foreach (VssFile file in Files)
       {
           if (name == file.Name)
           {
               return file;
           }
       }
       return null;
   }

   public VssItem FindItem(string name)
   {
       var project = FindProject(name);
       if (project != null)
       {
           return project;
       }
       return FindFile(name);
   }

   internal VssProject(VssDatabase database, VssItemName itemName,
       string physicalPath, string logicalPath)
       : base(database, itemName, physicalPath)
   {
       this.logicalPath = logicalPath;
   }

   protected override VssRevision CreateRevision(RevisionRecord revision, CommentRecord comment)
   {
       return new VssProjectRevision(this, revision, comment);
   }

   private class VssProjects : IEnumerable<VssProject>
   {
       private readonly VssProject project;

       internal VssProjects(VssProject project)
       {
           this.project = project;
       }

       public IEnumerator<VssProject> GetEnumerator()
       {
           return new VssItemEnumerator<VssProject>(project, ItemTypes.Project, project.DataPath);
       }

       IEnumerator IEnumerable.GetEnumerator()
       {
           return this.GetEnumerator();
       }
   }

   private class VssFiles : IEnumerable<VssFile>
   {
       private readonly VssProject project;

       internal VssFiles(VssProject project)
       {
           this.project = project;
       }

       public IEnumerator<VssFile> GetEnumerator()
       {
           return new VssItemEnumerator<VssFile>(project, ItemTypes.File, project.DataPath);
       }

       IEnumerator IEnumerable.GetEnumerator()
       {
           return this.GetEnumerator();
       }
   }

   private enum ItemTypes
   {
//       None = 0,
//       Project = ItemType.Project,
//       File = ItemType.File,
//       Any = Project | File
      None ,
               Project,
               File ,
               Any ;
   }

   private class VssItemEnumerator<T> : IEnumerator<T>
       where T : VssItem
   {
       private readonly VssProject project;
       private readonly ItemTypes itemTypes;
       private readonly ProjectEntryFile entryFile;
       private ProjectEntryRecord entryRecord;
       private VssItem entryItem;
       private bool beforeFirst = true;

       internal VssItemEnumerator(VssProject project, ItemTypes itemTypes, string entryFilePath)
       {
           this.project = project;
           this.itemTypes = itemTypes;
           entryFile = new ProjectEntryFile(entryFilePath, project.Database.Encoding);
       }

       public void Dispose()
       {
       }

       public void Reset()
       {
           beforeFirst = true;
       }

       public bool MoveNext()
       {
           entryItem = null;
           do
           {
               entryRecord = beforeFirst ? entryFile.GetFirstEntry() : entryFile.GetNextEntry();
               beforeFirst = false;
           }
           while (entryRecord != null && ((int)itemTypes & (int)entryRecord.ItemType) == 0);
           return entryRecord != null;
       }

       public T Current
       {
           get
           {
               if (entryRecord == null)
               {
                   throw new InvalidOperationException();
               }

               if (entryItem == null)
               {
                   var physicalName = entryRecord.Physical.ToUpper();
                   var logicalName = project.database.GetFullName(entryRecord.Name);
                   if (entryRecord.ItemType == ItemType.Project)
                   {
                       entryItem = project.database.OpenProject(project, physicalName, logicalName);
                   }
                   else
                   {
                       entryItem = project.database.OpenFile(physicalName, logicalName);
                   }
               }

               return (T)entryItem;
           }
       }

       object IEnumerator.Current
       {
           get { return this.Current; }
       }
   }
}
}
