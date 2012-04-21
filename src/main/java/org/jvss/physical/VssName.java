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

/**
 * Structure used to store a VSS project or file name.
 */
public class VssName
{
   private final short flags;

   private final String shortName;

   private final int nameFileOffset;

   public VssName(short flags, String shortName, int nameFileOffset)
   {
      this.flags = flags;
      this.shortName = shortName;
      this.nameFileOffset = nameFileOffset;
   }

   public boolean isProject()
   {
      return (flags & 1) != 0;
   }

   public String shortName()
   {
      return shortName;
   }

   public int nameFileOffset()
   {
      return nameFileOffset;
   }

}
