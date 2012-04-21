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
import java.io.PrintWriter;

/**
 * Base class for item VSS header records.
 */
public class ItemHeaderRecord extends VssRecord
{
   /**
    * Enumeration indicating whether an item is a project or a file.
    */
   public enum ItemType {
      PROJECT(1), FILE(2);

      private final int value;

      private ItemType(int value)
      {
         this.value = value;
      }

      public int getValue()
      {
         return value;
      }

      public static ItemType valueOf(int value)
      {
         switch (value)
         {
            case 1 :
               return PROJECT;
            case 2 :
               return FILE;
            default :
               throw new IllegalArgumentException(value + " is not a valid ItemType");
         }
      }
   }

   public final static String SIGNATURE = "DH";

   protected ItemType itemType;

   protected int revisions;

   protected VssName name;

   protected int firstRevision;

   protected String dataExt;

   protected int firstRevOffset;

   protected int lastRevOffset;

   protected int eofOffset;

   protected int rightsOffset;

   /**
    * @see org.jvss.physical.VssRecord#getSignature()
    */
   @Override
   public String getSignature()
   {
      return SIGNATURE;
   }

   public ItemHeaderRecord(ItemType itemType)
   {
      this.itemType = itemType;
   }

   /**
    * @return the itemType
    */
   public ItemType getItemType()
   {
      return itemType;
   }

   /**
    * @return the revisions
    */
   public int getRevisions()
   {
      return revisions;
   }

   /**
    * @return the name
    */
   public VssName getName()
   {
      return name;
   }

   /**
    * @return the firstRevision
    */
   public int getFirstRevision()
   {
      return firstRevision;
   }

   /**
    * @return the dataExt
    */
   public String getDataExt()
   {
      return dataExt;
   }

   /**
    * @return the firstRevOffset
    */
   public int getFirstRevOffset()
   {
      return firstRevOffset;
   }

   /**
    * @return the lastRevOffset
    */
   public int getLastRevOffset()
   {
      return lastRevOffset;
   }

   /**
    * @return the eofOffset
    */
   public int getEofOffset()
   {
      return eofOffset;
   }

   /**
    * @return the rightsOffset
    */
   public int getRightsOffset()
   {
      return rightsOffset;
   }

   /**
    * 
    * @see org.jvss.physical.VssRecord#read(org.jvss.physical.BufferReader,
    *      org.jvss.physical.RecordHeader)
    */
   @Override
   public void read(BufferReader reader, RecordHeader header)
   {
      super.read(reader, header);

      itemType = ItemType.valueOf(reader.readInt16());
      revisions = reader.readInt16();
      name = reader.readName();
      firstRevision = reader.readInt16();
      dataExt = reader.readString(2);
      firstRevOffset = reader.readInt32();
      lastRevOffset = reader.readInt32();
      eofOffset = reader.readInt32();
      rightsOffset = reader.readInt32();
      reader.skip(16); // reserved; always 0
   }

   /**
    * @see org.jvss.physical.VssRecord#dump(java.io.PrintWriter)
    */
   @Override
   public void dump(PrintWriter writer) throws IOException
   {
      writer.println(String.format("  Item Type: {0} - Revisions: {1} - Name: {2}", itemType.getValue(), revisions,
         name.shortName()));
      writer.println(String.format("  Name offset: {0:X6}", name.nameFileOffset()));
      writer.println(String.format("  First revision: #{0:D3}", firstRevision));
      writer.println(String.format("  Data extension: {0}", dataExt));
      writer.println(String.format("  First/last rev offset: {0:X6}/{1:X6}", firstRevOffset, lastRevOffset));
      writer.println(String.format("  EOF offset: {0:X6}", eofOffset));
      writer.println(String.format("  Rights offset: {0:X8}", rightsOffset));
   }

}
