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
package org.jvss.logical;

import java.nio.charset.Charset;

/**
 * Factory for obtaining VssDatabase instances.
 */
public class VssDatabaseFactory
{
   private final String path;

   private String encoding = Charset.defaultCharset().name();

   /**
    * @param encoding
    *           the encoding to set
    */
   public void setEncoding(String encoding)
   {
      this.encoding = encoding;
   }

   /**
    * @return the path
    */
   public String getPath()
   {
      return path;
   }

   /**
    * @return the encoding
    */
   public String getEncoding()
   {
      return encoding;
   }

   public VssDatabaseFactory(String path)
   {
      this.path = path;
   }

   public VssDatabase Open()
   {
      return new VssDatabase(path, encoding);
   }
}
