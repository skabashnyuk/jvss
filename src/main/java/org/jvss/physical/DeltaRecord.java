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

import org.jvss.physical.DeltaOperation.DeltaCommand;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * VSS record representing a reverse-delta for a file revision.
 */
public class DeltaRecord extends VssRecord
{

   public final static String SIGNATURE = "FD";

   private final List<DeltaOperation> operations = new LinkedList<DeltaOperation>();

   /**
    * @see org.jvss.physical.VssRecord#getSignature()
    */
   @Override
   public String getSignature()
   {
      return SIGNATURE;
   }

   /**
    * 
    * @see org.jvss.physical.VssRecord#read(org.jvss.physical.BufferReader,
    *      org.jvss.physical.RecordHeader)
    */
   @Override
   public void read(BufferReader reader, RecordHeader header)
   {
      super.read(reader, header);

      while (true)
      {
         DeltaOperation operation = new DeltaOperation();
         operation.read(reader);
         if (operation.getCommand() == DeltaCommand.Stop)
         {
            break;
         }
         operations.add(operation);
      }
   }

   /**
    * @see org.jvss.physical.VssRecord#dump(java.io.PrintWriter)
    */
   @Override
   public void dump(PrintWriter writer) throws IOException
   {
      for (DeltaOperation operation : operations)
      {
         operation.dump(writer);
      }
   }
}
