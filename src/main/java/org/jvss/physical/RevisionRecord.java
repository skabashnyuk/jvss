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
import java.util.Date;

/**
 * VSS record representing a project/file revision.
 */
public class RevisionRecord extends VssRecord
{
   /**
    * Enumeration of physical VSS revision actions.
    * 
    */
   public enum Action {

      // project actions
      Label(0), CreateProject(1), AddProject(2), AddFile(3), DestroyProject(4), DestroyFile(5), DeleteProject(6), DeleteFile(
         7), RecoverProject(8), RecoverFile(9), RenameProject(10), RenameFile(11), MoveFrom(12), MoveTo(13), ShareFile(
         14), BranchFile(15),

      // file actions
      CreateFile(16), EditFile(17), CreateBranch(19),

      // archive actions
      ArchiveProject(23), RestoreProject(25);

      private final int value;

      private Action(int value)
      {
         this.value = value;
      }

      public int getValue()
      {
         return value;
      }

      public static Action valueOf(int value)
      {
         switch (value)
         {

            case 0 :
               return Label;
            case 1 :
               return CreateProject;
            case 2 :
               return AddProject;
            case 3 :
               return AddFile;
            case 4 :
               return DestroyProject;
            case 5 :
               return DestroyFile;
            case 6 :
               return DeleteProject;
            case 7 :
               return DeleteFile;
            case 8 :
               return RecoverProject;
            case 9 :
               return RecoverFile;
            case 10 :
               return RenameProject;
            case 11 :
               return RenameFile;
            case 12 :
               return MoveFrom;
            case 13 :
               return MoveTo;
            case 14 :
               return ShareFile;
            case 15 :
               return BranchFile;
            case 16 :
               return CreateFile;
            case 17 :
               return EditFile;
            case 19 :
               return CreateBranch;
            case 23 :
               return ArchiveProject;
            case 25 :
               return RestoreProject;
            default :
               throw new IllegalArgumentException(value + " is not a valid Action");
         }
      }

   }

   public final static String SIGNATURE = "EL";

   protected int prevRevOffset;

   protected Action action;

   protected int revision;

   protected Date dateTime;

   protected String user;

   protected String label;

   protected int commentOffset; // or next revision if no comment

   protected int labelCommentOffset; // or label comment

   protected int commentLength;

   protected int labelCommentLength;

   /**
    * @return the signature
    */
   public String getSignature()
   {
      return SIGNATURE;
   }

   public static Action peekAction(BufferReader reader)
   {
      int saveOffset = reader.getOffset();
      try
      {
         reader.skip(4);
         return Action.valueOf(reader.readInt16());
      }
      finally
      {
         reader.setOffset(saveOffset);
      }
   }

   /**
    * @see org.jvss.physical.VssRecord#dump(java.io.PrintWriter)
    */
   @Override
   public void dump(PrintWriter writer) throws IOException
   {
      // TODO Auto-generated method stub

   }

}
