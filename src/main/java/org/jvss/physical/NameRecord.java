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

import java.io.IOException;
import java.io.PrintStream;

/**
 * VSS record containing the logical names of an object in particular contexts.
 */
public class NameRecord extends VssRecord
{
   /**
    * Enumeration of the kinds of VSS logical item names.
    */
   public enum NameKind {
      Dos(1), // DOS 8.3 filename
      Long(2), // Win95/NT long filename
      MacOS(3), // Mac OS 9 and earlier 31-character filename
      Project(10); // VSS project name

      public static NameKind valueOf(int value)
      {
         switch (value)
         {
            case 1 :
               return Dos;
            case 2 :
               return Long;
            case 3 :
               return MacOS;
            case 10 :
               return Project;
            default :
               throw new IllegalArgumentException(value + " is not a valid NameKind");
         }
      }

      private final int value;

      private NameKind(int value)
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

   /**
    * @see org.jvss.physical.VssRecord#getSignature()
    */
   @Override
   public String getSignature()
   {
      return SIGNATURE;
   }

   public final String SIGNATURE = "SN";

   int kindCount;

   NameKind[] kinds;

   String[] names;

   /**
    * @return the kindCount
    */
   public int getKindCount()
   {
      return kindCount;
   }

   public int indexOf(NameKind kind)
   {
      for (int i = 0; i < kindCount; ++i)
      {
         if (kinds[i] == kind)
         {
            return i;
         }
      }
      return -1;
   }

   public NameKind getKind(int index)
   {
      return kinds[index];
   }

   public String getName(int index)
   {
      return names[index];
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

      kindCount = reader.readInt16();
      reader.skip(2); // unknown
      kinds = new NameKind[kindCount];
      names = new String[kindCount];
      int baseOffset = reader.getOffset() + kindCount * 4;
      for (int i = 0; i < kindCount; ++i)
      {
         kinds[i] = NameKind.valueOf(reader.readInt16());
         int nameOffset = reader.readInt16();
         int saveOffset = reader.getOffset();
         try
         {
            reader.setOffset(baseOffset + nameOffset);
            names[i] = reader.readString(reader.getRemaining());
         }
         finally
         {
            reader.setOffset(saveOffset);
         }
      }
   }

   /**
    * @see org.jvss.physical.VssRecord#dump(java.io.PrintWriter)
    */
   @Override
   public void dump(PrintStream writer) throws IOException
   {
      for (int i = 0; i < kindCount; ++i)
      {
         writer.println(String.format("  {0} name: {1}", kinds[i], names[i]));
      }
   }
}
