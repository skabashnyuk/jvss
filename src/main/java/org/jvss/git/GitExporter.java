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

import org.jvss.logical.VssDatabase;

import java.util.HashSet;

/**
 * Replays and commits changesets into a new Git repository.
 */
public class GitExporter
{
   private final static String DefaultComment = "Vss2Git";

   private final VssDatabase database;

   private final RevisionAnalyzer revisionAnalyzer;

   private final ChangesetBuilder changesetBuilder;

   private final StreamCopier streamCopier = new StreamCopier();

   private final HashSet<String> tagsUsed = new HashSet<String>();

   private final String emailDomain = "localhost";
}
