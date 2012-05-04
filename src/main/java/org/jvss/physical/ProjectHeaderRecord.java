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
package org.jvss.physical;

import java.io.IOException;
import java.io.PrintStream;

/**
 * VSS header record for a project.
 */
public class ProjectHeaderRecord extends ItemHeaderRecord
{
   private String parentProject;

   private String parentFile;

   private int totalItems;

   private int subprojects;

   /**
    * @return the parentProject
    */
   public String getParentProject()
   {
      return parentProject;
   }

   /**
    * @return the parentFile
    */
   public String getParentFile()
   {
      return parentFile;
   }

   /**
    * @return the totalItems
    */
   public int getTotalItems()
   {
      return totalItems;
   }

   /**
    * @return the subprojects
    */
   public int getSubprojects()
   {
      return subprojects;
   }

   public ProjectHeaderRecord()
   {
      super(ItemType.PROJECT);
   }

   /**
    * 
    * @see org.jvss.physical.ItemHeaderRecord#read(org.jvss.physical.BufferReader,
    *      org.jvss.physical.RecordHeader)
    */
   @Override
   public void read(BufferReader reader, RecordHeader header)
   {
      super.read(reader, header);

      parentProject = reader.readString(260);
      parentFile = reader.readString(8);
      reader.skip(4); // reserved; always 0
      totalItems = reader.readInt16();
      subprojects = reader.readInt16();
   }

   /**
    * @see org.jvss.physical.VssRecord#dump(java.io.PrintStream)
    */
   @Override
   public void dump(PrintStream writer) throws IOException
   {
      super.dump(writer);

      writer.println(String.format("  Parent project: %s", parentProject));
      writer.println(String.format("  Parent file: %s", parentFile));
      writer.println(String.format("  Total items: %s", totalItems));
      writer.println(String.format("  Subprojects: %s", subprojects));
   }

}
