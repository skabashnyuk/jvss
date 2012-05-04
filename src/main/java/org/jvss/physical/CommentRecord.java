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
 * VSS record representing a comment message.
 */
public class CommentRecord extends VssRecord
{

   public final static String SIGNATURE = "MC";

   private String comment;

   /**
    * @see org.jvss.physical.VssRecord#getSignature()
    */
   @Override
   public String getSignature()
   {
      return SIGNATURE;
   }

   /**
    * @return the comment
    */
   public String getComment()
   {
      return comment;
   }

   /**
    * @throws IOException
    * @see org.jvss.physical.VssRecord#dump(java.io.PrintWriter)
    */
   @Override
   public void dump(PrintStream writer) throws IOException
   {
      writer.println(String.format("  {0}", comment));

   }

   /**
    * @see org.jvss.physical.VssRecord#read(org.jvss.physical.BufferReader,
    *      org.jvss.physical.RecordHeader)
    */
   @Override
   public void read(BufferReader reader, RecordHeader header)
   {
      super.read(reader, header);
      comment = reader.readString(reader.getRemaining());
   }

}
