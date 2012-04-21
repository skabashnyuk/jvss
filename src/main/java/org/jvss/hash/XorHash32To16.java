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
 * 16-bit hash function based on XORing the upper and lower words of a 32-bit
 * hash.
 */
public class XorHash32To16 implements Hash16
{

   private final Hash32 hash32;

   public XorHash32To16(Hash32 hash32)
   {
      this.hash32 = hash32;
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
      int value32 = hash32.compute(bytes, offset, limit);
      return (short)(value32 ^ value32 >> 16);
   }

}
