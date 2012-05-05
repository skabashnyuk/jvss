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

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Represents a set of revisions made by a particular person at a particular
 * time.
 */
public class Changeset
{
   private Date dateTime;

   private String user;

   private String comment;

   private final List<Revision> revisions = new LinkedList<Revision>();

   private final Set<String> targetFiles = new HashSet<String>();

   /**
    * @return the dateTime
    */
   public Date getDateTime()
   {
      return dateTime;
   }

   /**
    * @param dateTime
    *           the dateTime to set
    */
   public void setDateTime(Date dateTime)
   {
      this.dateTime = dateTime;
   }

   /**
    * @return the user
    */
   public String getUser()
   {
      return user;
   }

   /**
    * @param user
    *           the user to set
    */
   public void setUser(String user)
   {
      this.user = user;
   }

   /**
    * @return the comment
    */
   public String getComment()
   {
      return comment;
   }

   /**
    * @param comment
    *           the comment to set
    */
   public void setComment(String comment)
   {
      this.comment = comment;
   }

   /**
    * @return the revisions
    */
   public List<Revision> getRevisions()
   {
      return revisions;
   }

   /**
    * @return the targetFiles
    */
   public Set<String> getTargetFiles()
   {
      return targetFiles;
   }

}
