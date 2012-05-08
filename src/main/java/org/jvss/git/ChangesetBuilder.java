/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jvss.git;

import org.jvss.logical.VssAction.VssActionType;
import org.jvss.logical.VssAction.VssNamedAction;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Reconstructs changesets from independent revisions.
 */
public class ChangesetBuilder
{
   private final RevisionAnalyzer revisionAnalyzer;

   private final LinkedList<Changeset> changesets = new LinkedList<Changeset>();

   private long anyCommentThreshold;

   private long sameCommentThreshold;

   /**
    * @return the anyCommentThreshold
    */
   public long getAnyCommentThreshold()
   {
      return anyCommentThreshold;
   }

   /**
    * @return the sameCommentThreshold
    */
   public long getSameCommentThreshold()
   {
      return sameCommentThreshold;
   }

   /**
    * @param sameCommentThreshold
    *           the sameCommentThreshold to set
    */
   public void setSameCommentThreshold(long sameCommentThreshold)
   {
      this.sameCommentThreshold = sameCommentThreshold;
   }

   /**
    * @param anyCommentThreshold
    *           the anyCommentThreshold to set
    */
   public void setAnyCommentThreshold(long anyCommentThreshold)
   {
      this.anyCommentThreshold = anyCommentThreshold;
   }

   //   
   //   private TimeSpan anyCommentThreshold = TimeSpan.FromSeconds(30);
   //   public TimeSpan AnyCommentThreshold
   //   {
   //       get { return anyCommentThreshold; }
   //       set { anyCommentThreshold = value; }
   //   }
   //
   //
   //   private TimeSpan sameCommentThreshold = TimeSpan.FromMinutes(10);
   //   public TimeSpan SameCommentThreshold
   //   {
   //       get { return sameCommentThreshold; }
   //       set { sameCommentThreshold = value; }
   //   }
   //WorkQueue workQueue, Logger logger, 
   public ChangesetBuilder(RevisionAnalyzer revisionAnalyzer)

   {
      this.revisionAnalyzer = revisionAnalyzer;
   }

   /**
    * @return the changesets
    */
   public LinkedList<Changeset> getChangesets()
   {
      return changesets;
   }

   public void buildChangesets()
   {
      //       workQueue.AddLast(delegate(object work)
      //       {
      //           logger.WriteSectionSeparator();
      //           LogStatus(work, "Building changesets");

      //           var stopwatch = Stopwatch.StartNew();
      Map<String, Changeset> pendingChangesByUser = new HashMap<String, Changeset>();
      for (Map.Entry<Date, List<Revision>> dateEntry : revisionAnalyzer.getSortedRevisions().entrySet())
      {
         Date dateTime = dateEntry.getKey();
         for (Revision revision : dateEntry.getValue())
         {
            // determine target of project revisions
            VssActionType actionType = revision.getAction().type();
            VssNamedAction namedAction = (VssNamedAction)revision.getAction();
            String targetFile = revision.getItem().getPhysicalName();
            if (namedAction != null)
            {
               targetFile = namedAction.name().getPhysicalName();
            }

            // Create actions are only used to obtain initial item comments;
            // items are actually created when added to a project
            boolean creating =
               actionType == VssActionType.Create || actionType == VssActionType.Branch
                  && !revision.getItem().isProject();

            // Share actions are never conflict (which is important,
            // since Share always precedes Branch)
            boolean nonconflicting = creating || actionType == VssActionType.Share;

            // look up the pending change for user of this revision
            // and flush changes past time threshold
            String pendingUser = revision.getUser();
            Changeset pendingChange = null;
            LinkedList<String> flushedUsers = null;
            for (Entry<String, Changeset> userEntry : pendingChangesByUser.entrySet())
            {
               String user = userEntry.getKey();
               Changeset change = userEntry.getValue();

               // flush change if file conflict or past time threshold
               boolean flush = false;
               long timeDiff = revision.getDateTime().getTime() - change.getDateTime().getTime();
               if (timeDiff > anyCommentThreshold)
               {
                  if (hasSameComment(revision, change.getRevisions().getLast()))
                  {
                     String message;
                     if (timeDiff < sameCommentThreshold)
                     {
                        message = "Using same-comment threshold";
                     }
                     else
                     {
                        message = "Same comment but exceeded threshold";
                        flush = true;
                     }
                     //                               logger.WriteLine("NOTE: {0} ({1} second gap):",
                     //                                   message, timeDiff.TotalSeconds);
                  }
                  else
                  {
                     flush = true;
                  }
               }
               else if (!nonconflicting && change.getTargetFiles().contains(targetFile))
               {
                  //                           logger.WriteLine("NOTE: Splitting changeset due to file conflict on {0}:",
                  //                               targetFile);
                  flush = true;
               }

               if (flush)
               {
                  addChangeset(change);
                  if (flushedUsers == null)
                  {
                     flushedUsers = new LinkedList<String>();
                  }
                  flushedUsers.addLast(user);
               }
               else if (user == pendingUser)
               {
                  pendingChange = change;
               }
            }
            if (flushedUsers != null)
            {
               for (String user : flushedUsers)
               {
                  pendingChangesByUser.remove(user);
               }
            }

            // if no pending change for user, create a new one
            if (pendingChange == null)
            {
               pendingChange = new Changeset();
               pendingChange.setUser(pendingUser);
               pendingChangesByUser.put(pendingUser, pendingChange);
            }

            // update the time of the change based on the last revision
            pendingChange.setDateTime(revision.getDateTime());

            // add the revision to the change
            pendingChange.getRevisions().addLast(revision);

            // track target files in changeset to detect conflicting actions
            if (!nonconflicting)
            {
               pendingChange.getTargetFiles().add(targetFile);
            }

            // build up a concatenation of unique revision comments
            String revComment = revision.getComment();
            if (revComment != null)
            {
               revComment = revComment.trim();
               if (revComment.length() > 0)
               {
                  if (pendingChange.getComment() == null || pendingChange.getComment().length() == 0)
                  {
                     pendingChange.setComment(revComment);
                  }
                  else if (!pendingChange.getComment().contains(revComment))
                  {
                     pendingChange.setComment(pendingChange.getComment() + "\n" + revComment);
                  }
               }
            }
         }
      }

      // flush all remaining changes
      for (Changeset change : pendingChangesByUser.values())
      {
         addChangeset(change);
      }
      //stopwatch.Stop();

      //           logger.WriteSectionSeparator();
      //           logger.WriteLine("Found {0} changesets in {1:HH:mm:ss}",
      //               changesets.Count, new DateTime(stopwatch.ElapsedTicks));
      //});
   }

   private boolean hasSameComment(Revision rev1, Revision rev2)
   {
      return !(rev1.getComment() == null || rev1.getComment().length() == 0)
         && rev1.getComment().equals(rev2.getComment());
   }

   private void addChangeset(Changeset change)
   {
      changesets.addLast(change);
      int changesetId = changesets.size();
      dumpChangeset(change, changesetId);
   }

   private void dumpChangeset(Changeset changeset, int changesetId)
   {
      Date firstRevTime = changeset.getRevisions().get(0).getDateTime();
      long changeDuration = changeset.getDateTime().getTime() - firstRevTime.getTime();
      //       logger.WriteSectionSeparator();
      //       logger.WriteLine("Changeset {0} - {1} ({2} secs) {3} {4} files",
      //           changesetId, changeset.DateTime, changeDuration.TotalSeconds, changeset.User,
      //           changeset.Revisions.Count);
      //       if (!string.IsNullOrEmpty(changeset.Comment))
      //       {
      //           logger.WriteLine(changeset.Comment);
      //       }
      //       logger.WriteLine();
      //       foreach (var revision in changeset.Revisions)
      //       {
      //           logger.WriteLine("  {0} {1}@{2} {3}",
      //               revision.DateTime, revision.Item, revision.Version, revision.Action);
      //       }
   }
}
