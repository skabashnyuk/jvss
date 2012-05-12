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

import java.util.Iterator;

/**
 *
 */
public class IEnumerator<T>
{
   private final Iterator<T> iterator;

   private T current;

   /**
    * @param iterator
    */
   public IEnumerator(Iterator<T> iterator)
   {
      super();
      this.iterator = iterator;
      moveNext();
   }

   public T current()
   {
      return current;
   }

   public boolean moveNext()
   {
      if (iterator.hasNext())
      {
         current = iterator.next();
      }
      else
      {
         current = null;
      }
      return current != null;
   }

   /**
    * @return
    */
   public boolean hasNext()
   {
      return iterator.hasNext();
   }
}
