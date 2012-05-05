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

import java.util.Collection;
import java.util.Date;

/**
 * Handler of git commands
 */
public interface GitCommandHandler
{
   void init();

   void setConfig(String name, String value);

   boolean add(String path);

   boolean add(Collection<String> paths);

   boolean addAll();

   void remove(String path, boolean recursive);

   void move(String sourcePath, String destPath);

   boolean commit(String authorName, String authorEmail, String comment, Date localTime);

   void tag(String name, String taggerName, String taggerEmail, String comment, Date localTime);
}
