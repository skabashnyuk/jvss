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

import org.jvss.physical.FileHeaderRecord;

/**
 * Represents a VSS file.
 */
public class VssFile extends VssItem
{
   public boolean isLocked()
   {
       return (Header.Flags & FileFlags.Locked) != 0; 
   }

   public bool IsBinary
   {
       get { return (Header.Flags & FileFlags.Binary) != 0; }
   }

   public bool IsLatestOnly
   {
       get { return (Header.Flags & FileFlags.LatestOnly) != 0; }
   }

   public bool IsShared
   {
       get { return (Header.Flags & FileFlags.Shared) != 0; }
   }

   public bool IsCheckedOut
   {
       get { return (Header.Flags & FileFlags.CheckedOut) != 0; }
   }

   public uint Crc
   {
       get { return Header.DataCrc; }
   }

   public DateTime LastRevised
   {
       get { return Header.LastRevDateTime; }
   }

   public DateTime LastModified
   {
       get { return Header.ModificationDateTime; }
   }

   public DateTime Created
   {
       get { return Header.CreationDateTime; }
   }

   public new IEnumerable<VssFileRevision> Revisions
   {
       get { return new VssRevisions<VssFile, VssFileRevision>(this); }
   }

   public new VssFileRevision GetRevision(int version)
   {
       return (VssFileRevision)base.GetRevision(version);
   }

   private FileHeaderRecord header()
   {
      return getItemFile().get
//        return (FileHeaderRecord)  getItemFile().get; 
   }

   internal VssFile(VssDatabase database, VssItemName itemName, string physicalPath)
       : base(database, itemName, physicalPath)
   {
   }

   public string GetPath(VssProject project)
   {
       return project.Path + VssDatabase.ProjectSeparator + Name;
   }

   protected override VssRevision CreateRevision(RevisionRecord revision, CommentRecord comment)
   {
       return new VssFileRevision(this, revision, comment);
   }
}
