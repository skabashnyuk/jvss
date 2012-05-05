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

import org.jvss.logical.VssAction;
import org.jvss.logical.VssItemName;

import java.util.Date;

/**
 * Represents a single revision to a file or directory.
 */
public class Revision
{
   private final Date dateTime;

   private final String user;

   private final VssItemName item;

   private final int version;

   private final String comment;

   private final VssAction action;

   public Revision(Date dateTime, String user, VssItemName item, int version, String comment, VssAction action)
   {
      this.dateTime = dateTime;
      this.user = user;
      this.item = item;
      this.version = version;
      this.comment = comment;
      this.action = action;
   }

   /**
    * @return the dateTime
    */
   public Date getDateTime()
   {
      return dateTime;
   }

   /**
    * @return the user
    */
   public String getUser()
   {
      return user;
   }

   /**
    * @return the item
    */
   public VssItemName getItem()
   {
      return item;
   }

   /**
    * @return the version
    */
   public int getVersion()
   {
      return version;
   }

   /**
    * @return the comment
    */
   public String getComment()
   {
      return comment;
   }

   /**
    * @return the action
    */
   public VssAction getAction()
   {
      return action;
   }

}
