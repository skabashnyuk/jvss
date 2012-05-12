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

import org.jvss.physical.DeltaSimulator.FromLogCallback;
import org.jvss.physical.DeltaSimulator.FromSuccessorCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility methods for merging and applying reverse-delta operations.
 */
public class DeltaUtil
{
   public static List<DeltaOperation> merge(List<DeltaOperation> lastRevision, List<DeltaOperation> priorRevision)
   {
      final LinkedList<DeltaOperation> result = new LinkedList<DeltaOperation>();
      DeltaSimulator merger = new DeltaSimulator(lastRevision);
      for (DeltaOperation operation : priorRevision)
      {
         switch (operation.getCommand())
         {
            case WriteLog :
               result.add(operation);
               break;
            case WriteSuccessor :
               System.out.println("DeltaUtil>>>" + lastRevision.size() + "==" + priorRevision.size());
               System.out.println("seek>>>" + operation.getOffset());
               System.out.println("length>>>" + operation.getLength());
               merger.seek(operation.getOffset());
               merger.read(operation.getLength(), new FromLogCallback()
               {

                  @Override
                  public int fromLog(byte[] data, int offset, int count)
                  {
                     result.addLast(DeltaOperation.writeLog(data, offset, count));
                     return count;
                  }
               }, new FromSuccessorCallback()
               {

                  @Override
                  public int fromSuccessor(int offset, int count)
                  {
                     result.addLast(DeltaOperation.writeSuccessor(offset, count));
                     return count;
                  }
               });
         }

      }
      System.out.println("DeltaUtil<<<" + result.size());
      return result;
   }

   public static void apply(List<DeltaOperation> operations, InputStream input, OutputStream output) throws IOException
   {
      int COPY_BUFFER_SIZE = 4096;
      byte[] copyBuffer = null;
      for (DeltaOperation operation : operations)
      {
         switch (operation.getCommand())
         {
            case WriteLog :
               output.write(operation.getData(), operation.getOffset(), operation.getLength());
               break;
            case WriteSuccessor :
               input.skip(operation.getOffset());
               if (copyBuffer == null)
               {
                  copyBuffer = new byte[COPY_BUFFER_SIZE];
               }
               int remaining = operation.getLength();
               int offset = 0;
               while (remaining > 0)
               {
                  int count = input.read(copyBuffer, offset, remaining);
                  if (count <= 0)
                  {
                     throw new IOException("Unexpected end of current revision file");
                  }
                  offset += count;
                  remaining -= count;
               }
               output.write(copyBuffer, 0, offset);
               break;
         }
      }
      output.flush();
   }
}
