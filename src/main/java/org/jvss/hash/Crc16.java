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

/**
 * 16-bit CRC hash function.
 */
public class Crc16 implements Hash16
{

   // Commonly used polynomials.
   public final static int IBM = 0xA001; // reversed

   public final static int DNP = 0xA6BC; // reversed

   public final static int CCITT = 0x1021;

   private final short[] table;

   private final short initial;

   private final short finalV;

   private final boolean reverse;

   public Crc16(short poly, boolean reverse)
   {
      this(poly, reverse, (short)0, (short)0);
   }

   public Crc16(short poly, boolean reverse, short initial, short finalV)
   {
      this.table = generateTable(poly, reverse);
      this.initial = initial;
      this.finalV = finalV;
      this.reverse = reverse;
   }

   /**
    * @see org.jvss.hash.Hash16#compute(byte[])
    */
   @Override
   public short compute(byte[] bytes)
   {
      return compute(bytes, 0, bytes.length);
   }

   /**
    * @see org.jvss.hash.Hash16#compute(byte[], int, int)
    */
   @Override
   public short compute(byte[] bytes, int offset, int limit)
   {
      short crc = initial;
      while (offset < limit)
      {
         if (reverse)
         {
            crc = (short)(crc >> 8 ^ table[(byte)(crc ^ bytes[offset++])]);
         }
         else
         {
            crc = (short)(crc << 8 ^ table[crc >> 8 ^ bytes[offset++]]);
         }
      }
      return (short)(crc ^ finalV);
   }

   protected static short[] generateTable(short poly, boolean reverse)
   {
      short[] table = new short[256];
      short mask = (short)(reverse ? 1 : 0x8000);
      for (int i = 0; i < table.length; ++i)
      {
         short value = (short)(reverse ? i : i << 8);
         for (int j = 0; j < 8; ++j)
         {
            boolean xor = (value & mask) != 0;
            value = reverse ? (short)(value >> 1) : (short)(value << 1);
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
