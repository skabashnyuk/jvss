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

import java.util.Date;
import java.util.LinkedList;

/**
 * Reconstructs changesets from independent revisions.
 */
public class ChangesetBuilder
{
   private final RevisionAnalyzer revisionAnalyzer;

   private final LinkedList<Changeset> changesets = new LinkedList<Changeset>();

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

   public void buildChangesets()
   {
       workQueue.AddLast(delegate(object work)
       {
           logger.WriteSectionSeparator();
           LogStatus(work, "Building changesets");

           var stopwatch = Stopwatch.StartNew();
           var pendingChangesByUser = new Dictionary<string, Changeset>();
           foreach (var dateEntry in revisionAnalyzer.SortedRevisions)
           {
               var dateTime = dateEntry.Key;
               foreach (Revision revision in dateEntry.Value)
               {
                   // determine target of project revisions
                   var actionType = revision.Action.Type;
                   var namedAction = revision.Action as VssNamedAction;
                   var targetFile = revision.Item.PhysicalName;
                   if (namedAction != null)
                   {
                       targetFile = namedAction.Name.PhysicalName;
                   }

                   // Create actions are only used to obtain initial item comments;
                   // items are actually created when added to a project
                   var creating = actionType == VssActionType.Create ||
                       actionType == VssActionType.Branch && !revision.Item.IsProject;

                   // Share actions are never conflict (which is important,
                   // since Share always precedes Branch)
                   var nonconflicting = creating || actionType == VssActionType.Share;

                   // look up the pending change for user of this revision
                   // and flush changes past time threshold
                   var pendingUser = revision.User;
                   Changeset pendingChange = null;
                   LinkedList<string> flushedUsers = null;
                   foreach (var userEntry in pendingChangesByUser)
                   {
                       var user = userEntry.Key;
                       var change = userEntry.Value;

                       // flush change if file conflict or past time threshold
                       var flush = false;
                       var timeDiff = revision.DateTime - change.DateTime;
                       if (timeDiff > anyCommentThreshold)
                       {
                           if (HasSameComment(revision, change.Revisions.Last.Value))
                           {
                               string message;
                               if (timeDiff < sameCommentThreshold)
                               {
                                   message = "Using same-comment threshold";
                               }
                               else
                               {
                                   message = "Same comment but exceeded threshold";
                                   flush = true;
                               }
                               logger.WriteLine("NOTE: {0} ({1} second gap):",
                                   message, timeDiff.TotalSeconds);
                           }
                           else
                           {
                               flush = true;
                           }
                       }
                       else if (!nonconflicting && change.TargetFiles.Contains(targetFile))
                       {
                           logger.WriteLine("NOTE: Splitting changeset due to file conflict on {0}:",
                               targetFile);
                           flush = true;
                       }

                       if (flush)
                       {
                           AddChangeset(change);
                           if (flushedUsers == null)
                           {
                               flushedUsers = new LinkedList<string>();
                           }
                           flushedUsers.AddLast(user);
                       }
                       else if (user == pendingUser)
                       {
                           pendingChange = change;
                       }
                   }
                   if (flushedUsers != null)
                   {
                       foreach (string user in flushedUsers)
                       {
                           pendingChangesByUser.Remove(user);
                       }
                   }

                   // if no pending change for user, create a new one
                   if (pendingChange == null)
                   {
                       pendingChange = new Changeset();
                       pendingChange.User = pendingUser;
                       pendingChangesByUser[pendingUser] = pendingChange;
                   }

                   // update the time of the change based on the last revision
                   pendingChange.DateTime = revision.DateTime;

                   // add the revision to the change
                   pendingChange.Revisions.AddLast(revision);

                   // track target files in changeset to detect conflicting actions
                   if (!nonconflicting)
                   {
                       pendingChange.TargetFiles.Add(targetFile);
                   }

                   // build up a concatenation of unique revision comments
                   var revComment = revision.Comment;
                   if (revComment != null)
                   {
                       revComment = revComment.Trim();
                       if (revComment.Length > 0)
                       {
                           if (string.IsNullOrEmpty(pendingChange.Comment))
                           {
                               pendingChange.Comment = revComment;
                           }
                           else if (!pendingChange.Comment.Contains(revComment))
                           {
                               pendingChange.Comment += "\n" + revComment;
                           }
                       }
                   }
               }
           }

           // flush all remaining changes
           foreach (var change in pendingChangesByUser.Values)
           {
               AddChangeset(change);
           }
           stopwatch.Stop();

           logger.WriteSectionSeparator();
           logger.WriteLine("Found {0} changesets in {1:HH:mm:ss}",
               changesets.Count, new DateTime(stopwatch.ElapsedTicks));
       });
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
