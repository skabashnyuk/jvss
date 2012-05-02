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
import org.jvss.physical.FileHeaderRecord.FileFlags;
import org.jvss.physical.RevisionRecord;

import java.util.Date;

/**
 * Represents a VSS file.
 */
public class VssFile extends VssItem
{
   public boolean isLocked()
   {
      return (header().getFlags().getValue() & FileFlags.Locked.getValue()) != 0;
   }

   public boolean isBinary()
   {
      return (header().getFlags().getValue() & FileFlags.Binary.getValue()) != 0;
   }

   public boolean isLatestOnly()
   {
      return (header().getFlags().getValue() & FileFlags.LatestOnly.getValue()) != 0;
   }

   public boolean isShared()
   {
      return (header().getFlags().getValue() & FileFlags.Shared.getValue()) != 0;
   }

   public boolean isCheckedOut()
   {
      return (header().getFlags().getValue() & FileFlags.CheckedOut.getValue()) != 0;
   }

   public int getCrc()
   {
      return header().getDataCrc();
   }

   public Date getLastRevised()
   {
      return header().getLastRevDateTime();
   }

   public Date getLastModified()
   {
      return header().getModificationDateTime();
   }

   public Date getCreated()
   {
      return header().getCreationDateTime();
   }

   public Iterable<VssRevision> getRevisions()
   {
      return new VssRevisions(this);
   }

   public VssFileRevision getRevision(int version)
   {
      return (VssFileRevision)super.GetRevision(version);
   }

   private FileHeaderRecord header()
   {
      return (FileHeaderRecord)getItemFile().getHeader();
   }

   protected VssFile(VssDatabase database, VssItemName itemName, String physicalPath)

   {
      super(database, itemName, physicalPath);
   }

   public String GetPath(VssProject project)
   {
      return project.getPath() + VssDatabase.ProjectSeparator + getName();
   }

   /**
    * @see org.jvss.logical.VssItem#createRevision(org.jvss.physical.RevisionRecord,
    *      org.jvss.physical.CommentRecord)
    */
   @Override
   protected VssRevision createRevision(RevisionRecord revision, CommentRecord comment)
   {
      return new VssFileRevision(this, revision, comment);
   }
}
