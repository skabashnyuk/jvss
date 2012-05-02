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

   /**
    * @return the revision
    */
   public RevisionRecord getRevision()
   {
      return revision;
   }

   /**
    * @return the comment
    */
   public CommentRecord getComment()
   {
      return comment;
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
            return new VssDestroyAction(db.getItemName(destroy.getName(), destroy.getPhysical()));
         }
         case Hpdi.VssPhysicalLib.Action.RenameProject :
         case Hpdi.VssPhysicalLib.Action.RenameFile : {
            var rename = (RenameRevisionRecord)revision;
            return new VssRenameAction(db.GetItemName(rename.Name, rename.Physical), db.GetFullName(rename.OldName));
         }
         case Hpdi.VssPhysicalLib.Action.MoveFrom : {
            var moveFrom = (MoveRevisionRecord)revision;
            return new VssMoveFromAction(db.GetItemName(moveFrom.Name, moveFrom.Physical), moveFrom.ProjectPath);
         }
         case Hpdi.VssPhysicalLib.Action.MoveTo : {
            var moveTo = (MoveRevisionRecord)revision;
            return new VssMoveToAction(db.GetItemName(moveTo.Name, moveTo.Physical), moveTo.ProjectPath);
         }
         case Hpdi.VssPhysicalLib.Action.ShareFile : {
            var share = (ShareRevisionRecord)revision;
            return new VssShareAction(db.GetItemName(share.Name, share.Physical), share.ProjectPath);
         }
         case Hpdi.VssPhysicalLib.Action.BranchFile :
         case Hpdi.VssPhysicalLib.Action.CreateBranch : {
            var branch = (BranchRevisionRecord)revision;
            var name = db.GetFullName(branch.Name);
            return new VssBranchAction(new VssItemName(name, branch.Physical, branch.Name.IsProject), new VssItemName(
               name, branch.BranchFile, branch.Name.IsProject));
         }
         case Hpdi.VssPhysicalLib.Action.EditFile : {
            return new VssEditAction(item.PhysicalName);
         }
         case Hpdi.VssPhysicalLib.Action.CreateProject :
         case Hpdi.VssPhysicalLib.Action.CreateFile : {
            var create = (CommonRevisionRecord)revision;
            return new VssCreateAction(db.GetItemName(create.Name, create.Physical));
         }
         case Hpdi.VssPhysicalLib.Action.AddProject :
         case Hpdi.VssPhysicalLib.Action.AddFile : {
            var add = (CommonRevisionRecord)revision;
            return new VssAddAction(db.GetItemName(add.Name, add.Physical));
         }
         case Hpdi.VssPhysicalLib.Action.DeleteProject :
         case Hpdi.VssPhysicalLib.Action.DeleteFile : {
            var delete = (CommonRevisionRecord)revision;
            return new VssDeleteAction(db.GetItemName(delete.Name, delete.Physical));
         }
         case Hpdi.VssPhysicalLib.Action.RecoverProject :
         case Hpdi.VssPhysicalLib.Action.RecoverFile : {
            var recover = (CommonRevisionRecord)revision;
            return new VssRecoverAction(db.GetItemName(recover.Name, recover.Physical));
         }
         case Hpdi.VssPhysicalLib.Action.ArchiveProject : {
            var archive = (ArchiveRevisionRecord)revision;
            return new VssArchiveAction(db.GetItemName(archive.Name, archive.Physical), archive.ArchivePath);
         }
         case Hpdi.VssPhysicalLib.Action.RestoreProject : {
            var archive = (ArchiveRevisionRecord)revision;
            return new VssRestoreAction(db.GetItemName(archive.Name, archive.Physical), archive.ArchivePath);
         }
         default :
            throw new ArgumentException("Unknown revision action: " + revision.Action);
      }
   }
}
