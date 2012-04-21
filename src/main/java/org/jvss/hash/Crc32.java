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
package org.jvss.hash;

import java.util.zip.CRC32;

/**
 * 32-bit CRC hash function. TODO replace by {@link CRC32}
 */
public class Crc32 implements Hash32
{

   // Commonly used polynomials.
   public static final int IEEE = 0xEDB88320; // reversed

   private final int[] table;

   private final int initial;

   private final int finalV;

   public Crc32(int poly)

   {
      this(poly, 0, 0);
   }

   public Crc32(int poly, int initial, int finalV)
   {
      this.table = generateTable(poly);
      this.initial = initial;
      this.finalV = finalV;
   }

   /**
    * @see org.jvss.hash.Hash32#compute(byte[])
    */
   @Override
   public int compute(byte[] bytes)
   {
      return compute(bytes, 0, bytes.length);
   }

   /**
    * @see org.jvss.hash.Hash32#compute(byte[], int, int)
    */
   @Override
   public int compute(byte[] bytes, int offset, int limit)
   {
      int crc = initial;
      while (offset < limit)
      {
         crc = crc >> 8 ^ table[(byte)(crc ^ bytes[offset++])];
      }
      return crc ^ finalV;
   }

   protected static int[] generateTable(int poly)
   {
      int[] table = new int[256];
      for (int i = 0; i < table.length; ++i)
      {
         int value = i;
         for (int j = 0; j < 8; ++j)
         {
            boolean xor = (value & 1) != 0;
            value >>= 1;
            if (xor)
            {
               value ^= poly;
            }
         }
         table[i] = value;
      }
      return table;
   }
}
