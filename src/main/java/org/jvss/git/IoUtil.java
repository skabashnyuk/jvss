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

import java.io.File;

/**
 *
 */
public class IoUtil
{
   public static boolean isExists(String path)
   {
      return new File(path).exists();
   }

   public static void delete(String path)
   {
      delete(new File(path));
   }

   public static void delete(File f)
   {
      if (f.isDirectory())
      {
         for (File c : f.listFiles())
         {
            delete(c);
         }
      }
      if (!f.delete())
      {
         throw new RuntimeException("Failed to delete file: " + f);
      }
   }
}
