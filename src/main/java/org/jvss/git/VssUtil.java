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

import org.jvss.logical.VssFile;
import org.jvss.logical.VssProject;

/**
 * Helper methods for working with VSS objects.
 */
public class VssUtil
{
   public static enum RecursionStatus {
      Continue, Skip, Abort
   }

   public static interface VssProjectCallback
   {
      RecursionStatus call(VssProject project);
   }

   public static interface VssFileCallback
   {
      RecursionStatus call(VssProject project, VssFile file);
   }

   public static RecursionStatus recurseItems(VssProject project, VssProjectCallback projectCallback,
      VssFileCallback fileCallback)
   {
      if (projectCallback != null)
      {
         RecursionStatus status = projectCallback.call(project);
         if (status != RecursionStatus.Continue)
         {
            return status;
         }
      }
      for (VssProject subproject : project.getProjects())
      {
         RecursionStatus status = recurseItems(subproject, projectCallback, fileCallback);
         if (status == RecursionStatus.Abort)
         {
            return status;
         }
      }
      for (VssFile file : project.getFiles())
      {
         RecursionStatus status = fileCallback.call(project, file);
         if (status == RecursionStatus.Abort)
         {
            return status;
         }
      }
      return RecursionStatus.Continue;
   }
}
