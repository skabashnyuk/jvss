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
import java.util.Date;

/**
 * VSS record representing a file checkout.
 * 
 */
public class CheckoutRecord extends VssRecord
{

   public final static String SIGNATURE = "CF";

   private String user;

   private Date dateTime;

   private String workingDir;

   private String machine;

   private String project;

   private String comment;

   private int revision;

   private int flags;

   private boolean exclusive;

   private int prevCheckoutOffset;

   private int thisCheckoutOffset;

   private int checkouts;

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

      user = reader.readString(32);
      dateTime = reader.readDateTime();
      workingDir = reader.readString(260);
      machine = reader.readString(32);
      project = reader.readString(260);
      comment = reader.readString(64);
      revision = reader.readInt16();
      flags = reader.readInt16();
      exclusive = (flags & 0x40) != 0;
      prevCheckoutOffset = reader.readInt32();
      thisCheckoutOffset = reader.readInt32();
      checkouts = reader.readInt32();
   }

   /**
    * @see org.jvss.physical.VssRecord#dump(java.io.PrintWriter)
    */
   @Override
   public void dump(PrintStream writer) throws IOException
   {
      writer.println(String.format("  User: {0} @ {1}", user, dateTime));
      writer.println(String.format("  Working: {0}", workingDir));
      writer.println(String.format("  Machine: {0}", machine));
      writer.println(String.format("  Project: {0}", project));
      writer.println(String.format("  Comment: {0}", comment));
      writer.println(String.format("  Revision: #{0:D3}", revision));
      writer.println(String.format("  Flags: {0:X4}{1}", flags, exclusive ? " (exclusive)" : ""));
      writer.println(String.format("  Prev checkout offset: {0:X6}", prevCheckoutOffset));
      writer.println(String.format("  This checkout offset: {0:X6}", thisCheckoutOffset));
      writer.println(String.format("  Checkouts: {0}", checkouts));

   }

   /**
    * @return the user
    */
   public String getUser()
   {
      return user;
   }

   /**
    * @return the dateTime
    */
   public Date getDateTime()
   {
      return dateTime;
   }

   /**
    * @return the workingDir
    */
   public String getWorkingDir()
   {
      return workingDir;
   }

   /**
    * @return the machine
    */
   public String getMachine()
   {
      return machine;
   }

   /**
    * @return the project
    */
   public String getProject()
   {
      return project;
   }

   /**
    * @return the comment
    */
   public String getComment()
   {
      return comment;
   }

   /**
    * @return the revision
    */
   public int getRevision()
   {
      return revision;
   }

   /**
    * @return the flags
    */
   public int getFlags()
   {
      return flags;
   }

   /**
    * @return the exclusive
    */
   public boolean isExclusive()
   {
      return exclusive;
   }

   /**
    * @return the prevCheckoutOffset
    */
   public int getPrevCheckoutOffset()
   {
      return prevCheckoutOffset;
   }

   /**
    * @return the thisCheckoutOffset
    */
   public int getThisCheckoutOffset()
   {
      return thisCheckoutOffset;
   }

   /**
    * @return the checkouts
    */
   public int getCheckouts()
   {
      return checkouts;
   }

}
