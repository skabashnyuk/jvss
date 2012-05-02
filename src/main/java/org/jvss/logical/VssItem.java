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
import org.jvss.physical.FileHeaderRecord;
import org.jvss.physical.ItemFile;
import org.jvss.physical.RevisionRecord;
import org.jvss.physical.RevisionRecord.Action;

import java.util.Iterator;

/**
 * Represents an abstract VSS item, which is a project or file.
 */
public abstract class VssItem
{
   protected final VssDatabase database;

   protected final VssItemName itemName;

   protected final String physicalPath;

   private ItemFile itemFile;

   public VssDatabase getDatabase()
   {
      return database;
   }

   public VssItemName getItemName()
   {
      return itemName;
   }

   public boolean isProject()
   {
      return itemName.isProject();
   }

   public String getName()
   {
      return itemName.getLogicalName();
   }

   public String getPhysicalName()
   {
      return itemName.getPhysicalName();
   }

   public String getPhysicalPath()
   {
      return physicalPath;
   }

   public String getDataPath()
   {
      return physicalPath + getItemFile().getHeader().getDataExt();
   }

   public int getRevisionCount()
   {
      return getItemFile().getHeader().getRevisions();
   }

   public Iterable<VssRevision> getRevisions()
   {
      return new VssRevisions(this);
   }

   public VssRevision GetRevision(int version)
   {
      ItemFile itemFile = getItemFile();
      if (version < 1 || version > itemFile.getHeader().getRevisions())
      {
         throw new IllegalArgumentException("version" + version + " Invalid version number");
      }

      // check whether version was before branch
      if (version < itemFile.getHeader().getFirstRevision())
      {
         if (!isProject())
         {
            FileHeaderRecord fileHeader = (FileHeaderRecord)itemFile.getHeader();
            return database.GetItemPhysical(fileHeader.getBranchFile()).GetRevision(version);
         }
         else
         {
            // should never happen; projects cannot branch
            throw new IllegalArgumentException("version" + version + "Undefined version");
         }
      }

      RevisionRecord revisionRecord = itemFile.GetFirstRevision();
      while (revisionRecord != null && revisionRecord.getRevision() < version)
      {
         revisionRecord = itemFile.GetNextRevision(revisionRecord);
      }
      if (revisionRecord == null)
      {
         throw new IllegalArgumentException("Version not found" + "version");
      }
      return CreateRevision(revisionRecord);
   }

   protected ItemFile getItemFile()
   {
      if (itemFile == null)
      {
         itemFile = new ItemFile(physicalPath, database.getEncoding());
      }
      return itemFile;
   }

   protected void setItemFile(ItemFile value)
   {
      itemFile = value;
   }

   protected VssItem(VssDatabase database, VssItemName itemName, String physicalPath)
   {
      this.database = database;
      this.itemName = itemName;
      this.physicalPath = physicalPath;
   }

   protected VssRevision CreateRevision(RevisionRecord revision)
   {
      CommentRecord comment = null;
      if (revision.getCommentLength() > 0 && revision.getCommentOffset() > 0)
      {
         comment = new CommentRecord();
         getItemFile().ReadRecord(comment, revision.getCommentOffset());
      }
      else if (revision.getAction() == Action.Label && revision.getLabelCommentLength() > 0
         && revision.getLabelCommentOffset() > 0)
      {
         comment = new CommentRecord();
         getItemFile().ReadRecord(comment, revision.getLabelCommentOffset());
      }
      return createRevision(revision, comment);
   }

   protected abstract VssRevision createRevision(RevisionRecord revision, CommentRecord comment);

   public static class VssRevisions implements Iterable<VssRevision>
   {
      private final VssItem item;

      public VssRevisions(VssItem item)
      {
         this.item = item;
      }

      /**
       * @see java.lang.Iterable#iterator()
       */
      @Override
      public Iterator<VssRevision> iterator()
      {
         return new VssRevisionEnumerator(item);
      }
   }

   protected static class VssRevisionEnumerator implements Iterator<VssRevision>
   {

      private final VssItem item;

      private RevisionRecord revisionRecord;

      private VssRevision revision;

      private boolean beforeFirst = true;

      private VssRevisionEnumerator(VssItem item)
      {
         this.item = item;
      }

      /**
       * @see java.util.Iterator#hasNext()
       */
      @Override
      public boolean hasNext()
      {
         revision = null;
         if (beforeFirst)
         {
            revisionRecord = item.getItemFile().GetFirstRevision();
            beforeFirst = false;
         }
         else if (revisionRecord != null)
         {
            revisionRecord = item.getItemFile().GetNextRevision(revisionRecord);
         }
         return revisionRecord != null;
      }

      /**
       * @see java.util.Iterator#next()
       */
      @Override
      public VssRevision next()
      {
         if (revisionRecord == null)
         {
            throw new IllegalStateException();
         }

         if (revision == null)
         {
            revision = item.CreateRevision(revisionRecord);
         }

         return revision;
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
