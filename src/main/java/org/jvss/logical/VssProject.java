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

import org.jvss.physical.CommentRecord;
import org.jvss.physical.ItemHeaderRecord.ItemType;
import org.jvss.physical.ProjectEntryFile;
import org.jvss.physical.ProjectEntryRecord;
import org.jvss.physical.RevisionRecord;

import java.util.Iterator;

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

   public Iterable<VssProject> getProjects()
   {
      return new VssProjects(this);
   }

   public Iterable<VssFile> getFiles()
   {
      return new VssFiles(this);
   }

   public Iterable<VssRevision> getRevisions()
   {
      return new VssRevisions(this);
   }

   public VssProjectRevision getRevision(int version)
   {
      return (VssProjectRevision)super.GetRevision(version);
   }

   public VssProject findProject(String name)
   {
      for (VssProject subproject : getProjects())
      {
         if (name == subproject.getName())
         {
            return subproject;
         }
      }
      return null;
   }

   public VssFile findFile(String name)
   {
      for (VssFile file : getFiles())
      {
         if (name == file.getName())
         {
            return file;
         }
      }
      return null;
   }

   public VssItem findItem(String name)
   {
      VssProject project = findProject(name);
      if (project != null)
      {
         return project;
      }
      return findFile(name);
   }

   protected VssProject(VssDatabase database, VssItemName itemName, String physicalPath, String logicalPath)
   {
      super(database, itemName, physicalPath);
      this.logicalPath = logicalPath;
   }

   /**
    * 
    * @see org.jvss.logical.VssItem#createRevision(org.jvss.physical.RevisionRecord,
    *      org.jvss.physical.CommentRecord)
    */
   @Override
   protected VssRevision createRevision(RevisionRecord revision, CommentRecord comment)
   {
      return new VssProjectRevision(this, revision, comment);
   }

   private class VssProjects implements Iterable<VssProject>
   {
      private final VssProject project;

      /**
       * @param project
       */
      public VssProjects(VssProject project)
      {
         super();
         this.project = project;
      }

      /**
       * @see java.lang.Iterable#iterator()
       */
      @Override
      public Iterator<VssProject> iterator()
      {
         return new VssItemEnumerator<VssProject>(project, ItemTypes.Project, project.getDataPath());
      }
   }

   private class VssFiles implements Iterable<VssFile>
   {
      private final VssProject project;

      /**
       * @param project
       */
      public VssFiles(VssProject project)
      {
         super();
         this.project = project;
      }

      /**
       * @see java.lang.Iterable#iterator()
       */
      @Override
      public Iterator<VssFile> iterator()
      {
         return new VssItemEnumerator<VssFile>(project, ItemTypes.File, project.getDataPath());
      }
   }

   private enum ItemTypes {
      //       None = 0,
      //       Project = ItemType.Project,
      //       File = ItemType.File,
      //       Any = Project | File
      None(0), Project(ItemType.PROJECT.getValue()), File(ItemType.FILE.getValue()), Any(ItemType.PROJECT.getValue()
         | ItemType.FILE.getValue());

      private final int value;

      private ItemTypes(int value)
      {
         this.value = value;
      }

      /**
       * @return the value
       */
      public int getValue()
      {
         return value;
      }

   }

   private static class VssItemEnumerator<T extends VssItem> implements Iterator<T>
   {

      private final VssProject project;

      private final ItemTypes itemTypes;

      private final ProjectEntryFile entryFile;

      private ProjectEntryRecord entryRecord;

      private T entryItem;

      private boolean beforeFirst = true;

      private VssItemEnumerator(VssProject project, ItemTypes itemTypes, String entryFilePath)
      {
         this.project = project;
         this.itemTypes = itemTypes;
         this.entryFile = new ProjectEntryFile(entryFilePath, project.getDatabase().getEncoding());
      }

      /**
       * @see java.util.Iterator#hasNext()
       */
      @Override
      public boolean hasNext()
      {
         entryItem = null;
         do
         {
            entryRecord = beforeFirst ? entryFile.getFirstEntry() : entryFile.GetNextEntry();
            beforeFirst = false;
         }
         while (entryRecord != null && (itemTypes.getValue() & entryRecord.getItemType().getValue()) == 0);
         return entryRecord != null;
      }

      /**
       * @see java.util.Iterator#next()
       */
      @Override
      public T next()
      {
         if (entryRecord == null)
         {
            throw new IllegalStateException();
         }

         if (entryItem == null)
         {
            String physicalName = entryRecord.getPhysical().toUpperCase();
            String logicalName = project.database.GetFullName(entryRecord.getName());
            if (entryRecord.getItemType() == ItemType.PROJECT)
            {
               entryItem = (T)project.database.OpenProject(project, physicalName, logicalName);
            }
            else
            {
               entryItem = (T)project.database.OpenFile(physicalName, logicalName);
            }
         }

         return entryItem;
      }

      public void Reset()
      {
         beforeFirst = true;
      }

      /**
       * @see java.util.Iterator#remove()
       */
      @Override
      public void remove()
      {
      }

   }
}
