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

import java.io.File;

/**
 * Represents an abstract VSS item, which is a project or file.
 */
public class VssItem
{
   protected final VssDatabase database;

   protected final VssItemName itemName;

   protected final String physicalPath;

   private File itemFile;
   
   
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
       return physicalPath + ItemFile.Header.DataExt; 
   }

   public int RevisionCount
   {
       get { return ItemFile.Header.Revisions; }
   }

   public IEnumerable<VssRevision> Revisions
   {
       get { return new VssRevisions<VssItem, VssRevision>(this); }
   }

   public VssRevision GetRevision(int version)
   {
       var itemFile = ItemFile;
       if (version < 1 || version > itemFile.Header.Revisions)
       {
           throw new ArgumentOutOfRangeException("version", version, "Invalid version number");
       }

       // check whether version was before branch
       if (version < itemFile.Header.FirstRevision)
       {
           if (!IsProject)
           {
               var fileHeader = (FileHeaderRecord)itemFile.Header;
               return database.GetItemPhysical(fileHeader.BranchFile).GetRevision(version);
           }
           else
           {
               // should never happen; projects cannot branch
               throw new ArgumentOutOfRangeException("version", version, "Undefined version");
           }
       }

       var revisionRecord = itemFile.GetFirstRevision();
       while (revisionRecord != null && revisionRecord.Revision < version)
       {
           revisionRecord = itemFile.GetNextRevision(revisionRecord);
       }
       if (revisionRecord == null)
       {
           throw new ArgumentException("Version not found", "version");
       }
       return CreateRevision(revisionRecord);
   }

   internal ItemFile ItemFile
   {
       get
       {
           if (itemFile == null)
           {
               itemFile = new ItemFile(physicalPath, database.Encoding);
           }
           return itemFile;
       }
       set
       {
           itemFile = value;
       }
   }

   internal VssItem(VssDatabase database, VssItemName itemName, string physicalPath)
   {
       this.database = database;
       this.itemName = itemName;
       this.physicalPath = physicalPath;
   }

   protected VssRevision CreateRevision(RevisionRecord revision)
   {
       CommentRecord comment = null;
       if (revision.CommentLength > 0 && revision.CommentOffset > 0)
       {
           comment = new CommentRecord();
           ItemFile.ReadRecord(comment, revision.CommentOffset);
       }
       else if (revision.Action == VssPhysicalLib.Action.Label &&
           revision.LabelCommentLength > 0 && revision.LabelCommentOffset > 0)
       {
           comment = new CommentRecord();
           ItemFile.ReadRecord(comment, revision.LabelCommentOffset);
       }
       return CreateRevision(revision, comment);
   }

   protected abstract VssRevision CreateRevision(RevisionRecord revision, CommentRecord comment);

   protected class VssRevisions<ItemT, RevisionT> : IEnumerable<RevisionT>
       where ItemT : VssItem
       where RevisionT : VssRevision
   {
       private readonly ItemT item;

       internal VssRevisions(ItemT item)
       {
           this.item = item;
       }

       public IEnumerator<RevisionT> GetEnumerator()
       {
           return new VssRevisionEnumerator<ItemT, RevisionT>(item);
       }

       IEnumerator IEnumerable.GetEnumerator()
       {
           return this.GetEnumerator();
       }
   }

   private class VssRevisionEnumerator<ItemT, RevisionT> : IEnumerator<RevisionT>
       where ItemT : VssItem
       where RevisionT : VssRevision
   {
       private readonly ItemT item;
       private RevisionRecord revisionRecord;
       private RevisionT revision;
       private bool beforeFirst = true;

       internal VssRevisionEnumerator(ItemT item)
       {
           this.item = item;
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
           revision = null;
           if (beforeFirst)
           {
               revisionRecord = item.ItemFile.GetFirstRevision();
               beforeFirst = false;
           }
           else if (revisionRecord != null)
           {
               revisionRecord = item.ItemFile.GetNextRevision(revisionRecord);
           }
           return revisionRecord != null;
       }

       public RevisionT Current
       {
           get
           {
               if (revisionRecord == null)
               {
                   throw new InvalidOperationException();
               }

               if (revision == null)
               {
                   revision = (RevisionT)item.CreateRevision(revisionRecord);
               }

               return revision;
           }
       }

       object IEnumerator.Current
       {
           get { return this.Current; }
       }
   }
}
}
