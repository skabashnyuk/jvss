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
 * Represents the header of a VSS record.
 */
public class RecordHeader
{

   public static final int LENGTH = 8;

   private int offset;

   private int length;

   private String signature;

   private short fileCrc;

   private short actualCrc;

   /**
    * @return the offset
    */
   public int getOffset()
   {
      return offset;
   }

   /**
    * @return the length
    */
   public int getLength()
   {
      return length;
   }

   /**
    * @return the signature
    */
   public String getSignature()
   {
      return signature;
   }

   /**
    * @return the fileCrc
    */
   public short getFileCrc()
   {
      return fileCrc;
   }

   /**
    * @return the actualCrc
    */
   public short getActualCrc()
   {
      return actualCrc;
   }

   /**
    * @return the isCrcValid
    */
   public boolean isCrcValid()
   {
      return fileCrc == actualCrc;
   }

   public void CheckSignature(String expected)
   {
      if (!signature.equals(expected))
      {
         throw new RecordNotFoundException(String.format("Unexpected record signature: expected={0}, actual={1}",
            expected, signature));
      }
   }

   public void CheckCrc()
   {
      if (!isCrcValid())
      {
         throw new RecordCrcException(this, String.format("CRC error in {0} record: expected={1}, actual={2}",
            signature, fileCrc, actualCrc));
      }
   }

   public void Read(BufferReader reader)
   {
      offset = reader.getOffset();
      length = reader.readInt32();
      signature = reader.readSignature(2);
      fileCrc = reader.readInt16();
      actualCrc = reader.crc16(length);
   }

   public void dump(PrintStream writer) throws IOException
   {
      writer.format("Signature: {0} - Length: {1} - Offset: {2:X6} - CRC: {3:X4} ({5}: {4:X4})", signature, length,
         offset, fileCrc, actualCrc, isCrcValid() ? "valid" : "INVALID");
      writer.println();
   }
}
