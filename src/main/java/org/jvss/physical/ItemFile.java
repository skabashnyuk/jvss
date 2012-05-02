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
package org.jvss.physical;

import org.jvss.physical.ItemHeaderRecord.ItemType;

/**
 * Represents a file containing VSS project/file records.
 */
public class ItemFile extends VssRecordFile
{
   private final ItemHeaderRecord header;

   public ItemFile(String filename, String encoding)
   {
      super(filename, encoding);
      try
      {
         String fileSig = reader.readString(0x20);
         if (fileSig != "SourceSafe@Microsoft")
         {
            throw new BadHeaderException("Incorrect file signature");
         }

         ItemType fileType = ItemType.valueOf(reader.readInt16());
         int fileVersion = reader.readInt16();
         if (fileVersion != 6)
         {
            throw new BadHeaderException("Incorrect file version");
         }

         reader.skip(16); // reserved; always 0

         if (fileType == ItemType.PROJECT)
         {
            header = new ProjectHeaderRecord();
         }
         else
         {
            header = new FileHeaderRecord();
         }

         readRecord(header);
         if (header.getItemType() != fileType)
         {
            throw new BadHeaderException("Header record type mismatch");
         }
      }
      catch (EndOfBufferException e)
      {
         throw new BadHeaderException("Truncated header", e);
      }
   }
}
