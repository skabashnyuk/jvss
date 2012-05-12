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

import org.jvss.physical.CommentRecord;
import org.jvss.physical.DeltaOperation;
import org.jvss.physical.DeltaRecord;
import org.jvss.physical.DeltaStream;
import org.jvss.physical.DeltaUtil;
import org.jvss.physical.ItemFile;
import org.jvss.physical.RecordNotFoundException;
import org.jvss.physical.RevisionRecord;
import org.jvss.physical.RevisionRecord.BranchRevisionRecord;
import org.jvss.physical.RevisionRecord.EditRevisionRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Represents a revision of a VSS file.
 */
public class VssFileRevision extends VssRevision
{
   public VssFile getFile()
   {
      return (VssFile)item;
   }

   public InputStream getContents() throws IOException
   {

      File vssFile = new File(item.getDataPath().toLowerCase());

      if (!vssFile.exists())
      {
         throw new RecordNotFoundException("File " + vssFile.getAbsolutePath() + " not found");
      }

      InputStream dataFile = new FileInputStream(item.getDataPath().toLowerCase());

      ItemFile itemFile = item.getItemFile();
      RevisionRecord lastRev = itemFile.GetLastRevision();
      if (lastRev != null)
      {
         List<DeltaOperation> deltaOps = null;
         while (lastRev != null && lastRev.getRevision() > this.getVersion())
         {

            if (lastRev instanceof BranchRevisionRecord)
            {
               BranchRevisionRecord branchRev = (BranchRevisionRecord)lastRev;
               int branchRevId = branchRev.getRevision();
               String itemPath = item.getDatabase().GetDataPath(branchRev.getBranchFile());
               itemFile = new ItemFile(itemPath, item.getDatabase().getEncoding());
               lastRev = itemFile.GetLastRevision();
               while (lastRev != null && lastRev.getRevision() >= branchRevId)
               {
                  lastRev = itemFile.getPreviousRevision(lastRev);
               }
            }
            else
            {

               if (lastRev instanceof EditRevisionRecord)
               {
                  EditRevisionRecord editRev = (EditRevisionRecord)lastRev;
                  DeltaRecord delta = itemFile.getPreviousDelta(editRev);
                  if (delta != null)
                  {
                     List<DeltaOperation> curDeltaOps = delta.getOperations();
                     deltaOps = deltaOps == null ? curDeltaOps : DeltaUtil.merge(deltaOps, curDeltaOps);
                  }
               }
               lastRev = itemFile.getPreviousRevision(lastRev);
            }
         }

         if (deltaOps != null)
         {
            dataFile = new DeltaStream(dataFile, deltaOps);
         }
      }

      return dataFile;
   }

   protected VssFileRevision(VssItem item, RevisionRecord revision, CommentRecord comment)
   {
      super(item, revision, comment);
   }

}
