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
package org.jvss.physical;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

/**
 * VSS header record for a file.
 */
public class FileHeaderRecord extends ItemHeaderRecord
{

   /**
    * Flags enumeration for a VSS file.
    */
   public enum FileFlags {
      None(0), Locked(0x01), Binary(0x02), LatestOnly(0x04), Shared(0x20), CheckedOut(0x40);

      private final int value;

      private FileFlags(int value)
      {
         this.value = value;
      }

      public int getValue()
      {
         return value;
      }

      public static FileFlags valueOf(int value)
      {
         switch (value)
         {
            case 0 :
               return None;
            case 0x01 :
               return Locked;
            case 0x02 :
               return Binary;
            case 0x04 :
               return LatestOnly;
            case 0x20 :
               return Shared;
            case 0x40 :
               return CheckedOut;
            default :
               throw new IllegalArgumentException(value + " is not a valid FileFlags");
         }
      }
   }

   private FileFlags flags;

   private String branchFile;

   private int branchOffset;

   private int projectOffset;

   private int branchCount;

   private int projectCount;

   private int firstCheckoutOffset;

   private int lastCheckoutOffset;

   private int dataCrc;

   private Date lastRevDateTime;

   private Date modificationDateTime;

   private Date creationDateTime;

   public FileHeaderRecord()
   {
      super(ItemType.FILE);
   }

   /**
    * @return the flags
    */
   public FileFlags getFlags()
   {
      return flags;
   }

   /**
    * @return the branchFile
    */
   public String getBranchFile()
   {
      return branchFile;
   }

   /**
    * @return the branchOffset
    */
   public int getBranchOffset()
   {
      return branchOffset;
   }

   /**
    * @return the projectOffset
    */
   public int getProjectOffset()
   {
      return projectOffset;
   }

   /**
    * @return the branchCount
    */
   public int getBranchCount()
   {
      return branchCount;
   }

   /**
    * @return the projectCount
    */
   public int getProjectCount()
   {
      return projectCount;
   }

   /**
    * @return the firstCheckoutOffset
    */
   public int getFirstCheckoutOffset()
   {
      return firstCheckoutOffset;
   }

   /**
    * @return the lastCheckoutOffset
    */
   public int getLastCheckoutOffset()
   {
      return lastCheckoutOffset;
   }

   /**
    * @return the dataCrc
    */
   public int getDataCrc()
   {
      return dataCrc;
   }

   /**
    * @return the lastRevDateTime
    */
   public Date getLastRevDateTime()
   {
      return lastRevDateTime;
   }

   /**
    * @return the modificationDateTime
    */
   public Date getModificationDateTime()
   {
      return modificationDateTime;
   }

   /**
    * @return the creationDateTime
    */
   public Date getCreationDateTime()
   {
      return creationDateTime;
   }

   /**
    * @see org.jvss.physical.ItemHeaderRecord#read(org.jvss.physical.BufferReader,
    *      org.jvss.physical.RecordHeader)
    */
   @Override
   public void read(BufferReader reader, RecordHeader header)
   {
      super.read(reader, header);

      flags = FileFlags.valueOf(reader.readInt16());
      branchFile = reader.readString(8);
      reader.skip(2); // reserved; always 0
      branchOffset = reader.readInt32();
      projectOffset = reader.readInt32();
      branchCount = reader.readInt16();
      projectCount = reader.readInt16();
      firstCheckoutOffset = reader.readInt32();
      lastCheckoutOffset = reader.readInt32();
      dataCrc = reader.readInt32();
      reader.skip(8); // reserved; always 0
      lastRevDateTime = reader.readDateTime();
      modificationDateTime = reader.readDateTime();
      creationDateTime = reader.readDateTime();
      // remaining appears to be trash
   }

   /**
    * @see org.jvss.physical.ItemHeaderRecord#dump(java.io.PrintStream)
    */
   @Override
   public void dump(PrintStream writer) throws IOException
   {
      super.dump(writer);

      writer.println(String.format("  Flags: {0}", flags));
      writer.println(String.format("  Branched from file: {0}", branchFile));
      writer.println(String.format("  Branch offset: {0:X6}", branchOffset));
      writer.println(String.format("  Branch count: {0}", branchCount));
      writer.println(String.format("  Project offset: {0:X6}", projectOffset));
      writer.println(String.format("  Project count: {0}", projectCount));
      writer.println(String.format("  First/last checkout offset: {0:X6}/{1:X6}", firstCheckoutOffset,
         lastCheckoutOffset));
      writer.println(String.format("  Data CRC: {0:X8}", dataCrc));
      writer.println(String.format("  Last revision time: {0}", lastRevDateTime));
      writer.println(String.format("  Modification time: {0}", modificationDateTime));
      writer.println(String.format("  Creation time: {0}", creationDateTime));
   }
}
