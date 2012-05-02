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

/**
 * Represents the name of a VSS item.
 */
public class VssItemName
{
   private final String logicalName;

   private final String physicalName;

   private final boolean isProject;

   /**
    * @param logicalName
    * @param physicalName
    * @param isProject
    */
   public VssItemName(String logicalName, String physicalName, boolean isProject)
   {
      super();
      this.logicalName = logicalName;
      this.physicalName = physicalName;
      this.isProject = isProject;
   }

   /**
    * @return The current logical name of the item. Note that the logical name
    *         can change over the history of the item.
    */
   public String getLogicalName()
   {
      return logicalName;
   }

   /**
    * @return The physical name of the item (e.g. AAAAAAAA). This name never
    *         changes.
    */
   public String getPhysicalName()
   {
      return physicalName;
   }

   /**
    * @return Indicates whether this item is a project or a file.
    */
   public boolean isProject()
   {
      return isProject;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return (isProject ? "$" : "") + logicalName + "(" + physicalName + ")";
   }

}
