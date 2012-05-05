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
    * @return the prevRevOffset
    */
   public int getPrevRevOffset()
   {
      return prevRevOffset;
   }

   /**
    * @param prevRevOffset
    *           the prevRevOffset to set
    */
   public void setPrevRevOffset(int prevRevOffset)
   {
      this.prevRevOffset = prevRevOffset;
   }

   /**
    * @return the action
    */
   public Action getAction()
   {
      return action;
   }

   /**
    * @param action
    *           the action to set
    */
   public void setAction(Action action)
   {
      this.action = action;
   }

   /**
    * @return the revision
    */
   public int getRevision()
   {
      return revision;
   }

   /**
    * @param revision
    *           the revision to set
    */
   public void setRevision(int revision)
   {
      this.revision = revision;
   }

   /**
    * @return the dateTime
    */
   public Date getDateTime()
   {
      return dateTime;
   }

   /**
    * @param dateTime
    *           the dateTime to set
    */
   public void setDateTime(Date dateTime)
   {
      this.dateTime = dateTime;
   }

   /**
    * @return the user
    */
   public String getUser()
   {
      return user;
   }

   /**
    * @param user
    *           the user to set
    */
   public void setUser(String user)
   {
      this.user = user;
   }

   /**
    * @return the label
    */
   public String getLabel()
   {
      return label;
   }

   /**
    * @param label
    *           the label to set
    */
   public void setLabel(String label)
   {
      this.label = label;
   }

   /**
    * @return the commentOffset
    */
   public int getCommentOffset()
   {
      return commentOffset;
   }

   /**
    * @param commentOffset
    *           the commentOffset to set
    */
   public void setCommentOffset(int commentOffset)
   {
      this.commentOffset = commentOffset;
   }

   /**
    * @return the labelCommentOffset
    */
   public int getLabelCommentOffset()
   {
      return labelCommentOffset;
   }

   /**
    * @param labelCommentOffset
    *           the labelCommentOffset to set
    */
   public void setLabelCommentOffset(int labelCommentOffset)
   {
      this.labelCommentOffset = labelCommentOffset;
   }

   /**
    * @return the commentLength
    */
   public int getCommentLength()
   {
      return commentLength;
   }

   /**
    * @param commentLength
    *           the commentLength to set
    */
   public void setCommentLength(int commentLength)
   {
      this.commentLength = commentLength;
   }

   /**
    * @return the labelCommentLength
    */
   public int getLabelCommentLength()
   {
      return labelCommentLength;
   }

   /**
    * @param labelCommentLength
    *           the labelCommentLength to set
    */
   public void setLabelCommentLength(int labelCommentLength)
   {
      this.labelCommentLength = labelCommentLength;
   }

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
    * @see org.jvss.physical.VssRecord#read(org.jvss.physical.BufferReader,
    *      org.jvss.physical.RecordHeader)
    */
   @Override
   public void read(BufferReader reader, RecordHeader header)
   {
      super.read(reader, header);
      prevRevOffset = reader.readInt32();
      action = Action.valueOf(reader.readInt16());
      revision = reader.readInt16();
      dateTime = reader.readDateTime();
      user = reader.readString(32);
      label = reader.readString(32);
      commentOffset = reader.readInt32();
      labelCommentOffset = reader.readInt32();
      commentLength = reader.readInt16();
      labelCommentLength = reader.readInt16();
   }

   /**
    * @see org.jvss.physical.VssRecord#dump(java.io.PrintStream)
    */
   @Override
   public void dump(PrintStream writer) throws IOException
   {
      writer.println(String.format("  Prev rev offset: %06X", prevRevOffset));
      writer.println(String.format("  #%03d %s by '%s' at %4$td.%4$tm.%4$tY %4$tH:%4$tM:%4$tS", revision, action, user,
         dateTime));
      writer.println(String.format("  Label: %s", label));
      writer.println(String.format("  Comment: length %s, offset %06X", commentLength, commentOffset));
      writer.println(String.format("  Label comment: length %s, offset %06X", labelCommentLength, labelCommentOffset));

      //      writer.WriteLine("  Prev rev offset: {0:X6}", prevRevOffset);
      //      writer.WriteLine("  #{0:D3} {1} by '{2}' at {3}",
      //          revision, action, user, dateTime);
      //      writer.WriteLine("  Label: {0}", label);
      //      writer.WriteLine("  Comment: length {0}, offset {1:X6}", commentLength, commentOffset);
      //      writer.WriteLine("  Label comment: length {0}, offset {1:X6}", labelCommentLength, labelCommentOffset);

   }

   public static class CommonRevisionRecord extends RevisionRecord
   {
      VssName name;

      String physical;

      /**
       * @return the name
       */
      public VssName getName()
      {
         return name;
      }

      /**
       * @return the physical
       */
      public String getPhysical()
      {
         return physical;
      }

      /**
       * @see org.jvss.physical.VssRecord#read(org.jvss.physical.BufferReader,
       *      org.jvss.physical.RecordHeader)
       */
      @Override
      public void read(BufferReader reader, RecordHeader header)
      {
         super.read(reader, header);

         name = reader.readName();
         physical = reader.readString(10);
      }

      /**
       * @see org.jvss.physical.VssRecord#dump(java.io.PrintStream)
       */
      @Override
      public void dump(PrintStream writer) throws IOException
      {
         super.dump(writer);
         writer.println(String.format("  Name: %s (%s)", name.shortName(), physical));
      }
   }

   public static class DestroyRevisionRecord extends RevisionRecord
   {
      VssName name;

      short unkShort;

      String physical;

      /**
       * @return the name
       */
      public VssName getName()
      {
         return name;
      }

      /**
       * @return the physical
       */
      public String getPhysical()
      {
         return physical;
      }

      /**
       * @see org.jvss.physical.VssRecord#read(org.jvss.physical.BufferReader,
       *      org.jvss.physical.RecordHeader)
       */
      @Override
      public void read(BufferReader reader, RecordHeader header)
      {
         super.read(reader, header);

         name = reader.readName();
         unkShort = reader.readInt16(); // 0 or 1
         physical = reader.readString(10);
      }

      /**
       * @see org.jvss.physical.VssRecord#dump(java.io.PrintStream)
       */
      @Override
      public void dump(PrintStream writer) throws IOException
      {
         super.dump(writer);

         writer.println(String.format("  Name: %s (%s)", name.shortName(), physical));
      }
   }

   public static class RenameRevisionRecord extends RevisionRecord
   {
      VssName name;

      VssName oldName;

      String physical;

      /**
       * @return the name
       */
      public VssName getName()
      {
         return name;
      }

      /**
       * @return the oldName
       */
      public VssName getOldName()
      {
         return oldName;
      }

      /**
       * @return the physical
       */
      public String getPhysical()
      {
         return physical;
      }

      /**
       * @see org.jvss.physical.VssRecord#read(org.jvss.physical.BufferReader,
       *      org.jvss.physical.RecordHeader)
       */
      @Override
      public void read(BufferReader reader, RecordHeader header)
      {
         super.read(reader, header);

         name = reader.readName();
         oldName = reader.readName();
         physical = reader.readString(10);
      }

      /**
       * @see org.jvss.physical.VssRecord#dump(java.io.PrintStream)
       */
      @Override
      public void dump(PrintStream writer) throws IOException
      {

         writer.println(String.format("  Name: %s -> %s (%s)", oldName.shortName(), name.shortName(), physical));
      }
   }

   public static class MoveRevisionRecord extends RevisionRecord
   {
      String projectPath;

      VssName name;

      String physical;

      /**
       * @return the projectPath
       */
      public String getProjectPath()
      {
         return projectPath;
      }

      /**
       * @return the name
       */
      public VssName getName()
      {
         return name;
      }

      /**
       * @return the physical
       */
      public String getPhysical()
      {
         return physical;
      }

      /**
       * @see org.jvss.physical.VssRecord#read(org.jvss.physical.BufferReader,
       *      org.jvss.physical.RecordHeader)
       */
      @Override
      public void read(BufferReader reader, RecordHeader header)
      {
         super.read(reader, header);
         projectPath = reader.readString(260);
         name = reader.readName();
         physical = reader.readString(10);
      }

      /**
       * @see org.jvss.physical.VssRecord#dump(java.io.PrintStream)
       */
      @Override
      public void dump(PrintStream writer) throws IOException
      {
         super.dump(writer);
         writer.println(String.format("  Project path: %s", projectPath));
         writer.println(String.format("  Name: %s (%s)", name.shortName(), physical));
      }
   }

   public static class ShareRevisionRecord extends RevisionRecord
   {
      String projectPath;

      VssName name;

      short unpinnedRevision; // -1: shared, 0: pinned; >0 unpinned version

      short pinnedRevision; // >0: pinned version, ==0 unpinned

      short unkShort;

      String physical;

      /**
       * @return the projectPath
       */
      public String getProjectPath()
      {
         return projectPath;
      }

      /**
       * @return the name
       */
      public VssName getName()
      {
         return name;
      }

      /**
       * @return the unpinnedRevision
       */
      public short getUnpinnedRevision()
      {
         return unpinnedRevision;
      }

      /**
       * @return the pinnedRevision
       */
      public short getPinnedRevision()
      {
         return pinnedRevision;
      }

      /**
       * @return the unkShort
       */
      public short getUnkShort()
      {
         return unkShort;
      }

      /**
       * @return the physical
       */
      public String getPhysical()
      {
         return physical;
      }

      /**
       * @see org.jvss.physical.VssRecord#read(org.jvss.physical.BufferReader,
       *      org.jvss.physical.RecordHeader)
       */
      @Override
      public void read(BufferReader reader, RecordHeader header)
      {
         super.read(reader, header);
         projectPath = reader.readString(260);
         name = reader.readName();
         unpinnedRevision = reader.readInt16();
         pinnedRevision = reader.readInt16();
         unkShort = reader.readInt16(); // often seems to increment
         physical = reader.readString(10);
      }

      /**
       * @see org.jvss.physical.VssRecord#dump(java.io.PrintStream)
       */
      @Override
      public void dump(PrintStream writer) throws IOException
      {
         super.dump(writer);

         writer.println(String.format("  Project path: %s", projectPath));
         writer.println(String.format("  Name: %s (%s)", name.shortName(), physical));
         if (unpinnedRevision == 0)
         {
            writer.println(String.format("  Pinned at revision %s", pinnedRevision));
         }
         else if (unpinnedRevision > 0)
         {
            writer.println(String.format("  Unpinned at revision %s", unpinnedRevision));
         }
      }
   }

   public static class BranchRevisionRecord extends RevisionRecord
   {
      VssName name;

      String physical;

      String branchFile;

      /**
       * @return the name
       */
      public VssName getName()
      {
         return name;
      }

      /**
       * @return the physical
       */
      public String getPhysical()
      {
         return physical;
      }

      /**
       * @return the branchFile
       */
      public String getBranchFile()
      {
         return branchFile;
      }

      /**
       * @see org.jvss.physical.VssRecord#read(org.jvss.physical.BufferReader,
       *      org.jvss.physical.RecordHeader)
       */
      @Override
      public void read(BufferReader reader, RecordHeader header)
      {
         super.read(reader, header);

         name = reader.readName();
         physical = reader.readString(10);
         branchFile = reader.readString(10);
      }

      /**
       * @see org.jvss.physical.VssRecord#dump(java.io.PrintStream)
       */
      @Override
      public void dump(PrintStream writer) throws IOException
      {

         writer.println(String.format("  Name: %s (%s)", name.shortName(), physical));
         writer.println(String.format("  Branched from file: %s", branchFile));
      }
   }

   public static class EditRevisionRecord extends RevisionRecord
   {
      int prevDeltaOffset;

      String projectPath;

      /**
       * @return the prevDeltaOffset
       */
      public int getPrevDeltaOffset()
      {
         return prevDeltaOffset;
      }

      /**
       * @return the projectPath
       */
      public String getProjectPath()
      {
         return projectPath;
      }

      /**
       * @see org.jvss.physical.VssRecord#read(org.jvss.physical.BufferReader,
       *      org.jvss.physical.RecordHeader)
       */
      @Override
      public void read(BufferReader reader, RecordHeader header)
      {
         super.read(reader, header);

         prevDeltaOffset = reader.readInt32();
         reader.skip(4); // reserved; always 0
         projectPath = reader.readString(260);
      }

      /**
       * @see org.jvss.physical.VssRecord#dump(java.io.PrintStream)
       */
      @Override
      public void dump(PrintStream writer) throws IOException
      {

         writer.println(String.format("  Prev delta offset: %s", prevDeltaOffset));
         writer.println(String.format("  Project path: %s", projectPath));
      }
   }

   public static class ArchiveRevisionRecord extends RevisionRecord
   {
      VssName name;

      String physical;

      String archivePath;

      /**
       * @return the name
       */
      public VssName getName()
      {
         return name;
      }

      /**
       * @return the physical
       */
      public String getPhysical()
      {
         return physical;
      }

      /**
       * @return the archivePath
       */
      public String getArchivePath()
      {
         return archivePath;
      }

      /**
       * @see org.jvss.physical.VssRecord#read(org.jvss.physical.BufferReader,
       *      org.jvss.physical.RecordHeader)
       */
      @Override
      public void read(BufferReader reader, RecordHeader header)
      {
         super.read(reader, header);

         name = reader.readName();
         physical = reader.readString(10);
         reader.skip(2); // 0?
         archivePath = reader.readString(260);
         reader.skip(4); // ?
      }

      /**
       * @see org.jvss.physical.VssRecord#dump(java.io.PrintStream)
       */
      @Override
      public void dump(PrintStream writer) throws IOException
      {

         writer.println(String.format("  Name: %s (%s)", name.shortName(), physical));
         writer.println(String.format("  Archive path: %s", archivePath));
      }
   }
}
