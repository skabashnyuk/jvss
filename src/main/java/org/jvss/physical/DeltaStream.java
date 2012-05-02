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

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Provides a seekable input stream over a file revision based on the latest
 * revision content and a set of reverse-delta operations.
 */
public class DeltaStream extends InputStream
{

   private final InputStream baseStream;

   private final DeltaSimulator simulator;

   private int length = -1;

   public DeltaStream(InputStream stream, List<DeltaOperation> operations)
   {
      baseStream = stream;
      simulator = new DeltaSimulator(operations);
   }

   /**
    * @see java.io.InputStream#read()
    */
   @Override
   public int read() throws IOException
   {
      throw new NotImplementedException();
   }

   /**
    * @see java.io.InputStream#available()
    */
   @Override
   public int available() throws IOException
   {
      if (length < 0)
      {
         length = 0;
         for (DeltaOperation operation : simulator.getOperations())
         {
            length += operation.getLength();
         }
      }
      return length;
   }

   /**
    * @see java.io.InputStream#read(byte[], int, int)
    */
   @Override
   public int read(byte[] buffer, int offset, int count) throws IOException
   {
      //final int[] bytesRead = new int[1];

      //      simulator.read(count, new FromLogCallback()
      //      {
      //
      //         @Override
      //         public int fromLog(byte[] opData, int opOffset, int opCount)
      //         {
      //            System.arraycopy(opData, opOffset, buffer, offset, opCount);
      //            offset += opCount;
      //            count -= opCount;
      //            bytesRead[0] += opCount;
      //            return opCount;
      //         }
      //
      //      }, new FromSuccessorCallback()
      //      {
      //
      //         @Override
      //         public int fromSuccessor(int opOffset, int opCount)
      //         {
      //            baseStream.skip(opOffset); //TODO check this  baseStream.Seek(opOffset, SeekOrigin.Begin);
      //            int opBytesRead = baseStream.read(buffer, offset, opCount);
      //            offset += opBytesRead;
      //            count -= opBytesRead;
      //            bytesRead[0] += opBytesRead;
      //            return opBytesRead;
      //         }
      //      });

      return 0;//simulator.read(buffer, offset, count);//bytesRead[0];
   }
}
