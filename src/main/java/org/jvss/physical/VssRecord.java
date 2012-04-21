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
import java.io.PrintWriter;

/**
 * Base class for VSS records.
 */
public abstract class VssRecord
{
   protected String signature;

   protected RecordHeader header;

   public void read(BufferReader reader, RecordHeader header)
   {
      this.header = header;
   }

   /**
    * @return the signature
    */
   public String getSignature()
   {
      return signature;
   }

   /**
    * @return the header
    */
   public RecordHeader getHeader()
   {
      return header;
   }

   public abstract void dump(PrintWriter writer) throws IOException;
}
