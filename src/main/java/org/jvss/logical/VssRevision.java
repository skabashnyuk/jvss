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
package org.jvss.logical;

import org.jvss.logical.VssAction.VssAddAction;
import org.jvss.logical.VssAction.VssArchiveAction;
import org.jvss.logical.VssAction.VssBranchAction;
import org.jvss.logical.VssAction.VssCreateAction;
import org.jvss.logical.VssAction.VssDeleteAction;
import org.jvss.logical.VssAction.VssDestroyAction;
import org.jvss.logical.VssAction.VssEditAction;
import org.jvss.logical.VssAction.VssLabelAction;
import org.jvss.logical.VssAction.VssMoveFromAction;
import org.jvss.logical.VssAction.VssMoveToAction;
import org.jvss.logical.VssAction.VssRecoverAction;
import org.jvss.logical.VssAction.VssRenameAction;
import org.jvss.logical.VssAction.VssRestoreAction;
import org.jvss.logical.VssAction.VssShareAction;
import org.jvss.physical.CommentRecord;
import org.jvss.physical.RevisionRecord;
import org.jvss.physical.RevisionRecord.ArchiveRevisionRecord;
import org.jvss.physical.RevisionRecord.BranchRevisionRecord;
import org.jvss.physical.RevisionRecord.CommonRevisionRecord;
import org.jvss.physical.RevisionRecord.DestroyRevisionRecord;
import org.jvss.physical.RevisionRecord.MoveRevisionRecord;
import org.jvss.physical.RevisionRecord.RenameRevisionRecord;
import org.jvss.physical.RevisionRecord.ShareRevisionRecord;

import java.util.Date;

/**
 * Base class for revisions to a VSS item.
 * 
 */
public class VssRevision
{
   protected final VssItem item;

   protected final VssAction action;

   protected final RevisionRecord revision;

   protected final CommentRecord comment;

   /**
    * @return the item
    */
   public VssItem getItem()
   {
      return item;
   }

   /**
    * @return the action
    */
   public VssAction getAction()
   {
      return action;
   }

   public int getVersion()
   {
      return revision.getRevision();
   }

   /**
    * @return the revision
    */
   public RevisionRecord getRevision()
   {
      return revision;
   }

   public Date getDate()
   {
      return revision.getDateTime();
   }

   public String getUser()
   {
      return revision.getUser();
   }

   public String getLabel()
   {
      return revision.getLabel();
   }

   public String getComment()
   {
      return comment != null ? comment.getComment() : null;
   }

   /**
    * @param item
    * @param revision
    * @param comment
    */
   protected VssRevision(VssItem item, RevisionRecord revision, CommentRecord comment)
   {
      super();
      this.item = item;
      this.revision = revision;
      this.comment = comment;
      this.action = createAction(revision, item);
   }

   private static VssAction createAction(RevisionRecord revision, VssItem item)
   {
      VssDatabase db = item.getDatabase();
      switch (revision.getAction())
      {
         case Label : {
            return new VssLabelAction(revision.getLabel());
         }
         case DestroyProject :
         case DestroyFile : {
            DestroyRevisionRecord destroy = (DestroyRevisionRecord)revision;
            return new VssDestroyAction(db.GetItemName(destroy.getName(), destroy.getPhysical()));
         }
         case RenameProject :
         case RenameFile : {
            RenameRevisionRecord rename = (RenameRevisionRecord)revision;
            return new VssRenameAction(db.GetItemName(rename.getName(), rename.getPhysical()), db.GetFullName(rename
               .getOldName()));
         }
         case MoveFrom : {
            MoveRevisionRecord moveFrom = (MoveRevisionRecord)revision;
            return new VssMoveFromAction(db.GetItemName(moveFrom.getName(), moveFrom.getPhysical()),
               moveFrom.getProjectPath());
         }
         case MoveTo : {
            MoveRevisionRecord moveTo = (MoveRevisionRecord)revision;
            return new VssMoveToAction(db.GetItemName(moveTo.getName(), moveTo.getPhysical()), moveTo.getProjectPath());
         }
         case ShareFile : {
            ShareRevisionRecord share = (ShareRevisionRecord)revision;
            return new VssShareAction(db.GetItemName(share.getName(), share.getPhysical()), share.getProjectPath());
         }
         case BranchFile :
         case CreateBranch : {
            BranchRevisionRecord branch = (BranchRevisionRecord)revision;
            String name = db.GetFullName(branch.getName());
            return new VssBranchAction(new VssItemName(name, branch.getPhysical(), branch.getName().isProject()),
               new VssItemName(name, branch.getBranchFile(), branch.getName().isProject()));
         }
         case EditFile : {
            return new VssEditAction(item.getPhysicalName());
         }
         case CreateProject :
         case CreateFile : {
            CommonRevisionRecord create = (CommonRevisionRecord)revision;
            return new VssCreateAction(db.GetItemName(create.getName(), create.getPhysical()));
         }
         case AddProject :
         case AddFile : {
            CommonRevisionRecord add = (CommonRevisionRecord)revision;
            return new VssAddAction(db.GetItemName(add.getName(), add.getPhysical()));
         }
         case DeleteProject :
         case DeleteFile : {
            CommonRevisionRecord delete = (CommonRevisionRecord)revision;
            return new VssDeleteAction(db.GetItemName(delete.getName(), delete.getPhysical()));
         }
         case RecoverProject :
         case RecoverFile : {
            CommonRevisionRecord recover = (CommonRevisionRecord)revision;
            return new VssRecoverAction(db.GetItemName(recover.getName(), recover.getPhysical()));
         }
         case ArchiveProject : {
            ArchiveRevisionRecord archive = (ArchiveRevisionRecord)revision;
            return new VssArchiveAction(db.GetItemName(archive.getName(), archive.getPhysical()),
               archive.getArchivePath());
         }
         case RestoreProject : {
            ArchiveRevisionRecord archive = (ArchiveRevisionRecord)revision;
            return new VssRestoreAction(db.GetItemName(archive.getName(), archive.getPhysical()),
               archive.getArchivePath());
         }
         default :
            throw new IllegalArgumentException("Unknown revision action: " + revision.getAction());
      }
   }
}
