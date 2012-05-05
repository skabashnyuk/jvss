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
package org.jvss.git;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copies the contents of one stream to another.
 */
public class StreamCopier
{
   private final static int DEFAULT_BUFFER_SIZE = 4096;

   private byte[] buffer;

   private final int bufferSize;

   public StreamCopier()

   {
      this(DEFAULT_BUFFER_SIZE);
   }

   public StreamCopier(int bufferSize)
   {
      this.bufferSize = bufferSize;
   }

   public long Copy(InputStream inputStream, OutputStream outputStream) throws IOException
   {
      if (buffer == null)
      {
         buffer = new byte[bufferSize];
      }
      long copied = 0;
      while (true)
      {
         int count = inputStream.read(buffer, 0, buffer.length);
         if (count <= 0)
         {
            break;
         }
         outputStream.write(buffer, 0, count);
         copied += count;
      }
      return copied;
   }
}
