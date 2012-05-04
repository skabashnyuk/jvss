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
 * VSS record representing a branch file.
 * 
 */
public class BranchRecord extends VssRecord
{
   public final static String SIGNATURE = "BF";

   private int prevBranchOffset;

   private String branchFile;

   /**
    * @return the signature
    */
   public String getSignature()
   {
      return SIGNATURE;
   }

   public void Read(BufferReader reader, RecordHeader header)
   {
      super.read(reader, header);

      prevBranchOffset = reader.readInt32();
      branchFile = reader.readString(12);
   }

   /**
    * @return the prevBranchOffset
    */
   public int getPrevBranchOffset()
   {
      return prevBranchOffset;
   }

   /**
    * @return the branchFile
    */
   public String getBranchFile()
   {
      return branchFile;
   }

   /**
    * @see org.jvss.physical.VssRecord#dump(java.io.PrintStream)
    */
   @Override
   public void dump(PrintStream writer) throws IOException
   {
      writer.println(String.format("  Prev branch offset: {0:X6}", prevBranchOffset));
      writer.println(String.format("  Branch file: {0}", branchFile));

   }

}
