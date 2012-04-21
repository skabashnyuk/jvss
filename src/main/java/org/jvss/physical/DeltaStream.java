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

   private final int length = -1;

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
      // TODO Auto-generated method stub
      return 0;
   }

}
