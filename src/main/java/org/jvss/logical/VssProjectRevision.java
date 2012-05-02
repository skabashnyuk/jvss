/*
 * Copyright 2009 HPDI, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jvss.logical;

import org.jvss.physical.CommentRecord;
import org.jvss.physical.RevisionRecord;

/**
 * Represents a revision of a VSS project.
 */
public class VssProjectRevision extends VssRevision
{

   /**
    * @param item
    * @param revision
    * @param comment
    */
   public VssProjectRevision(VssItem item, RevisionRecord revision, CommentRecord comment)
   {
      super(item, revision, comment);
   }

   public VssProject getProject()
   {
      return (VssProject)item;
   }

}
