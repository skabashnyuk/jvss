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

import java.io.IOException;
import java.io.PrintStream;

/**
 * VSS record for representing an item stored in particular project.
 */
public class ProjectEntryRecord extends VssRecord
{
   /**
    * Flags enumeration for items in project.
    */
   public enum ProjectEntryFlags {
      None(0), Deleted(0x01), Binary(0x02), LatestOnly(0x04), Shared(0x08);

      public static ProjectEntryFlags valueOf(int value)
      {
         switch (value)
         {
            case 0 :
               return None;
            case 1 :
               return Deleted;
            case 2 :
               return Binary;
            case 3 :
               return LatestOnly;
            case 10 :
               return Shared;
            default :
               throw new IllegalArgumentException(value + " is not a valid ProjectEntryFlags");
         }
      }

      private final int value;

      private ProjectEntryFlags(int value)
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

   public final String SIGNATURE = "JP";

   protected ItemType itemType;

   protected ProjectEntryFlags flags;

   protected VssName name;

   protected int pinnedVersion;

   protected String physical;

   /**
    * @see org.jvss.physical.VssRecord#read(org.jvss.physical.BufferReader,
    *      org.jvss.physical.RecordHeader)
    */
   @Override
   public void read(BufferReader reader, RecordHeader header)
   {
      super.read(reader, header);
      itemType = ItemType.valueOf(reader.readInt16());
      flags = ProjectEntryFlags.valueOf(reader.readInt16());
      name = reader.readName();
      pinnedVersion = reader.readInt16();
      physical = reader.readString(10);
   }

   /**
    * @see org.jvss.physical.VssRecord#dump(java.io.PrintWriter)
    */
   @Override
   public void dump(PrintStream writer) throws IOException
   {
      writer.println(String.format("  Item Type: {0} - Name: {1} ({2})", itemType, name.shortName(), physical));
      writer.println(String.format("  Flags: {0}", flags));
      writer.println(String.format("  Pinned version: {0}", pinnedVersion));
   }

   /**
    * @return the itemType
    */
   public ItemType getItemType()
   {
      return itemType;
   }

   /**
    * @return the flags
    */
   public ProjectEntryFlags getFlags()
   {
      return flags;
   }

   /**
    * @return the name
    */
   public VssName getName()
   {
      return name;
   }

   /**
    * @return the pinnedVersion
    */
   public int getPinnedVersion()
   {
      return pinnedVersion;
   }

   /**
    * @return the physical
    */
   public String getPhysical()
   {
      return physical;
   }
}
