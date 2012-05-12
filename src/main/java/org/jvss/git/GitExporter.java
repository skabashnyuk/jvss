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

import org.jvss.git.VssPathMapper.VssFileInfo;
import org.jvss.git.VssPathMapper.VssItemInfo;
import org.jvss.git.VssPathMapper.VssProjectInfo;
import org.jvss.logical.VssAction.VssActionType;
import org.jvss.logical.VssAction.VssArchiveAction;
import org.jvss.logical.VssAction.VssBranchAction;
import org.jvss.logical.VssAction.VssLabelAction;
import org.jvss.logical.VssAction.VssMoveFromAction;
import org.jvss.logical.VssAction.VssMoveToAction;
import org.jvss.logical.VssAction.VssNamedAction;
import org.jvss.logical.VssAction.VssPinAction;
import org.jvss.logical.VssAction.VssRenameAction;
import org.jvss.logical.VssAction.VssRestoreAction;
import org.jvss.logical.VssDatabase;
import org.jvss.logical.VssFile;
import org.jvss.logical.VssFileRevision;
import org.jvss.logical.VssItemName;
import org.jvss.logical.VssProject;
import org.jvss.logical.VssRevision;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Replays and commits changesets into a new Git repository.
 */
public class GitExporter
{
   private final static String DefaultComment = "Vss2Git";

   private final VssDatabase database;

   private final RevisionAnalyzer revisionAnalyzer;

   private final ChangesetBuilder changesetBuilder;

   private final StreamCopier streamCopier = new StreamCopier();

   private final HashSet<String> tagsUsed = new HashSet<String>();

   private final String emailDomain = "localhost";

   private final String commitEncoding = "UTF-8";

   private final boolean forceAnnotatedTags = true;

   private final Logger logger;

   private final GitCommandHandler git;

   public GitExporter(Logger logger, RevisionAnalyzer revisionAnalyzer, ChangesetBuilder changesetBuilder,
      GitCommandHandler git)
   {
      this.git = git;
      this.logger = logger;
      this.database = revisionAnalyzer.getDatabase();
      this.revisionAnalyzer = revisionAnalyzer;
      this.changesetBuilder = changesetBuilder;
   }

   public void exportToGit(String repoPath)
   {
      //       workQueue.AddLast(delegate(object work)
      //       {
      //  var stopwatch = Stopwatch.StartNew();

      logger.WriteSectionSeparator();
      logger.WriteLine("Initializing Git repository");

      File repoDir = new File(repoPath);
      // create repository directory if it does not exist
      if (!repoDir.exists())
      {
         //TODO check creation
         repoDir.mkdirs();
      }

      //var git = new GitCommandHandler(repoPath, logger);
      //git.CommitEncoding = commitEncoding;

      //           while (!git.FindExecutable())
      //           {
      //               var button = MessageBox.Show("Git not found in PATH. " +
      //                   "If you need to modify your PATH variable, please " +
      //                   "restart the program for the changes to take effect.",
      //                   "Error", MessageBoxButtons.RetryCancel, MessageBoxIcon.Error);
      //               if (button == DialogResult.Cancel)
      //               {
      //                   workQueue.Abort();
      //                   return;
      //               }
      //           } 

      //TODO retry
      if (!git.init())
      {
         return;
      }
      //           if (!RetryCancel(delegate { git.Init(); }))
      //           {
      //               return;
      //           }

      //           if (commitEncoding!= "utf-8")
      //           {
      //               AbortRetryIgnore(delegate
      //               {
      //                   git.SetConfig("i18n.commitencoding", commitEncoding.WebName);
      //               });
      //           }

      VssPathMapper pathMapper = new VssPathMapper();

      // create mappings for root projects
      for (VssProject rootProject : revisionAnalyzer.getRootProjects())
      {
         String rootPath = VssPathMapper.getWorkingPath(repoPath, rootProject.getPath());
         pathMapper.setProjectPath(rootProject.getPhysicalName(), rootPath, rootProject.getPath());
      }

      // replay each changeset
      int changesetId = 1;
      List<Changeset> changesets = changesetBuilder.getChangesets();
      int commitCount = 0;
      int tagCount = 0;
      //var replayStopwatch = new Stopwatch();
      LinkedList<Revision> labels = new LinkedList<Revision>();
      tagsUsed.clear();
      for (Changeset changeset : changesets)
      {
         String changesetDesc = String.format("changeset %d from %tF", changesetId, changeset.getDateTime());

         // replay each revision in changeset
         //LogStatus(work, "Replaying " + changesetDesc);
         logger.WriteLine("Replaying " + changesetDesc);
         labels.clear();
         //replayStopwatch.Start();
         boolean needCommit = false;
         try
         {
            needCommit = replayChangeset(pathMapper, changeset, git, labels);
         }
         finally
         {
            //replayStopwatch.Stop();
         }

         //               if (workQueue.IsAborting)
         //               {
         //                   return;
         //               }

         // commit changes
         if (needCommit)
         {
            logger.WriteLine("Committing " + changesetDesc);
            if (commitChangeset(git, changeset))
            {
               ++commitCount;
            }
         }

         //               if (workQueue.IsAborting)
         //               {
         //                   return;
         //               }

         // create tags for any labels in the changeset
         if (labels.size() > 0)
         {
            for (Revision label : labels)
            {
               String labelName = ((VssLabelAction)label.getAction()).getLabel();
               if (labelName == null || labelName.length() == 0)
               {
                  logger.WriteLine("NOTE: Ignoring empty label");
               }
               else if (commitCount == 0)
               {
                  logger.WriteLine("NOTE: Ignoring label '%s' before initial commit", labelName);
               }
               else
               {
                  String tagName = getTagFromLabel(labelName);

                  String tagMessage = "Creating tag " + tagName;
                  if (tagName != labelName)
                  {
                     tagMessage += " for label '" + labelName + "'";
                  }
                  logger.WriteLine(tagMessage);

                  // annotated tags require (and are implied by) a tag message;
                  // tools like Mercurial's git converter only import annotated tags
                  String tagComment = label.getComment();
                  if ((tagComment == null || tagComment.length() == 0) && forceAnnotatedTags)
                  {
                     // use the original VSS label as the tag message if none was provided
                     tagComment = labelName;
                  }

                  //if (AbortRetryIgnore(
                  //  delegate
                  //{
                  if (git.tag(tagName, label.getUser(), getEmail(label.getUser()), tagComment, label.getDateTime()))
                  {
                     //;
                     //}
                     //}))
                     //{
                     ++tagCount;
                  }
               }
            }
         }

         ++changesetId;
      }

      //stopwatch.Stop();

      //           logger.WriteSectionSeparator();
      //           logger.WriteLine("Git export complete in {0:HH:mm:ss}", new DateTime(stopwatch.ElapsedTicks));
      //           logger.WriteLine("Replay time: {0:HH:mm:ss}", new DateTime(replayStopwatch.ElapsedTicks));
      //           logger.WriteLine("Git time: {0:HH:mm:ss}", new DateTime(git.ElapsedTime.Ticks));
      //           logger.WriteLine("Git commits: %s", commitCount);
      //           logger.WriteLine("Git tags: %s", tagCount);
      //});
   }

   private boolean replayChangeset(VssPathMapper pathMapper, Changeset changeset, GitCommandHandler git,
      LinkedList<Revision> labels)
   {
      boolean needCommit = false;
      for (Revision revision : changeset.getRevisions())
      {
         //           if (workQueue.IsAborting)
         //           {
         //               break;
         //           }

         //AbortRetryIgnore(delegate
         //{
         needCommit |= replayRevision(pathMapper, revision, git, labels);
         // });
      }
      return needCommit;
   }

   private boolean replayRevision(VssPathMapper pathMapper, Revision revision, GitCommandHandler git,
      LinkedList<Revision> labels)
   {
      boolean needCommit = false;
      VssActionType actionType = revision.getAction().type();
      if (revision.getItem().isProject())
      {
         // note that project path (and therefore target path) can be
         // null if a project was moved and its original location was
         // subsequently destroyed
         VssItemName project = revision.getItem();
         String projectName = project.getLogicalName();
         String projectPath = pathMapper.getProjectPath(project.getPhysicalName());
         String projectDesc = projectPath;
         if (projectPath == null)
         {
            projectDesc = revision.getItem().toString();
            logger.WriteLine(String.format("NOTE: %s is currently unmapped", project));
         }

         VssItemName target = null;
         String targetPath = null;

         if (revision.getAction() instanceof VssNamedAction)
         {
            VssNamedAction namedAction = (VssNamedAction)revision.getAction();
            if (namedAction != null)
            {
               target = namedAction.name();
               if (projectPath != null)
               {
                  targetPath = new File(projectPath, target.getLogicalName()).getAbsolutePath();
               }
            }
         }

         boolean isAddAction = false;
         boolean writeProject = false;
         boolean writeFile = false;
         VssItemInfo itemInfo = null;
         switch (actionType)
         {
            case Label :
               // defer tagging until after commit
               labels.addLast(revision);
               break;

            case Create :
               // ignored; items are actually created when added to a project
               break;

            case Add :
            case Share :
               logger.WriteLine(String.format("%s: %s %s", projectDesc, actionType, target.getLogicalName()));
               itemInfo = pathMapper.addItem(project, target);
               isAddAction = true;
               break;

            case Recover :
               logger.WriteLine(String.format("%s: %s %s", projectDesc, actionType, target.getLogicalName()));
               itemInfo = pathMapper.recoverItem(project, target);
               isAddAction = true;
               break;

            case Delete :
            case Destroy : {
               logger.WriteLine(String.format("%s: %s %s", projectDesc, actionType, target.getLogicalName()));
               itemInfo = pathMapper.deleteItem(project, target);
               if (targetPath != null && !itemInfo.isDestroyed())
               {
                  if (target.isProject())
                  {

                     if (IoUtil.isExists(targetPath))
                     {
                        if (((VssProjectInfo)itemInfo).containsFiles())
                        {
                           git.remove(targetPath, true);
                           needCommit = true;
                        }
                        else
                        {
                           // git doesn't care about directories with no files
                           IoUtil.delete(targetPath);
                        }
                     }
                  }
                  else
                  {
                     if (IoUtil.isExists(targetPath))
                     {
                        // not sure how it can happen, but a project can evidently
                        // contain another file with the same logical name, so check
                        // that this is not the case before deleting the file
                        if (pathMapper.projectContainsLogicalName(project, target))
                        {
                           logger.WriteLine(String.format("NOTE: %s contains another file named %s; not deleting file",
                              projectDesc, target.getLogicalName()));
                        }
                        else
                        {
                           IoUtil.delete(targetPath);
                           needCommit = true;
                        }
                     }
                  }
               }
            }
               break;

            case Rename : {
               VssRenameAction renameAction = (VssRenameAction)revision.getAction();
               logger.WriteLine(String.format("%s: %s %s to %s", projectDesc, actionType,
                  renameAction.getOriginalName(), target.getLogicalName()));
               itemInfo = pathMapper.renameItem(target);
               if (targetPath != null && !itemInfo.isDestroyed())
               {
                  String sourcePath = new File(projectPath, renameAction.getOriginalName()).getAbsolutePath();
                  if (target.isProject() ? IoUtil.isExists(sourcePath) : IoUtil.isExists(sourcePath))
                  {
                     // renaming a file or a project that contains files?
                     if (itemInfo instanceof VssProjectInfo)
                     {
                        VssProjectInfo projectInfo = (VssProjectInfo)itemInfo;
                        if (projectInfo.containsFiles())
                        {
                           //TODO gi
                           caseSensitiveRename(sourcePath, targetPath, new GitMover(git));
                           needCommit = true;
                        }
                        else
                        {
                           // git doesn't care about directories with no files
                           caseSensitiveRename(sourcePath, targetPath, new FsMover());
                        }
                     }
                     else
                     {
                        caseSensitiveRename(sourcePath, targetPath, new FsMover());
                     }

                  }
                  else
                  {
                     logger.WriteLine("NOTE: Skipping rename because %s does not exist", sourcePath);
                  }
               }
            }
               break;

            case MoveFrom :
            // if both MoveFrom & MoveTo are present (e.g.
            // one of them has not been destroyed), only one
            // can succeed, so check that the source exists
            {
               VssMoveFromAction moveFromAction = (VssMoveFromAction)revision.getAction();
               //                       logger.WriteLine("%s: Move from %s to %s",
               //                           projectDesc, moveFromAction.OriginalProject, targetPath ?? target.LogicalName);
               String sourcePath = pathMapper.getProjectPath(target.getPhysicalName());
               VssProjectInfo projectInfo =
                  pathMapper.moveProjectFrom(project, target, moveFromAction.getOriginalProject());
               if (targetPath != null && !projectInfo.isDestroyed())
               {
                  if (sourcePath != null && IoUtil.isExists(sourcePath))
                  {
                     if (projectInfo.containsFiles())
                     {
                        git.move(sourcePath, targetPath);
                        needCommit = true;
                     }
                     else
                     {

                        // git doesn't care about directories with no files
                        //Directory.Move(sourcePath, targetPath);
                        IoUtil.move(sourcePath, targetPath);
                     }
                  }
                  else
                  {
                     // project was moved from a now-destroyed project
                     writeProject = true;
                  }
               }
            }
               break;

            case MoveTo : {
               // handle actual moves in MoveFrom; this just does cleanup of destroyed projects
               VssMoveToAction moveToAction = (VssMoveToAction)revision.getAction();
               //                       logger.WriteLine("%s: Move to %s from %s",
               //                           projectDesc, moveToAction.NewProject, targetPath ?? target.LogicalName);
               VssProjectInfo projectInfo = pathMapper.moveProjectTo(project, target, moveToAction.getNewProject());
               if (projectInfo.isDestroyed() && targetPath != null && IoUtil.isExists(targetPath))
               {
                  // project was moved to a now-destroyed project; remove empty directory
                  //Directory.Delete(targetPath, true);
                  IoUtil.delete(targetPath);
               }
            }
               break;

            case Pin : {
               VssPinAction pinAction = (VssPinAction)revision.getAction();
               if (pinAction.isPinned())
               {
                  logger.WriteLine(String.format("%s: Pin %s", projectDesc, target.getLogicalName()));
                  itemInfo = pathMapper.pinItem(project, target);
               }
               else
               {
                  logger.WriteLine(String.format("%s: Unpin %s", projectDesc, target.getLogicalName()));
                  itemInfo = pathMapper.unpinItem(project, target);
                  writeFile = !itemInfo.isDestroyed();
               }
            }
               break;

            case Branch : {
               VssBranchAction branchAction = (VssBranchAction)revision.getAction();
               logger.WriteLine(String.format("%s: %s %s", projectDesc, actionType, target.getLogicalName()));
               itemInfo = pathMapper.branchFile(project, target, branchAction.getSource());
               // branching within the project might happen after branching of the file
               writeFile = true;
            }
               break;

            case Archive :
            // currently ignored
            {
               VssArchiveAction archiveAction = (VssArchiveAction)revision.getAction();
               logger.WriteLine(String.format("%s: Archive %s to %s (ignored)", projectDesc, target.getLogicalName(),
                  archiveAction.getArchivePath()));
            }
               break;

            case Restore : {
               VssRestoreAction restoreAction = (VssRestoreAction)revision.getAction();
               logger.WriteLine(String.format("%s: Restore %s from archive %s", projectDesc, target.getLogicalName(),
                  restoreAction.getArchivePath()));
               itemInfo = pathMapper.addItem(project, target);
               isAddAction = true;
            }
               break;
         }

         if (targetPath != null)
         {
            if (isAddAction)
            {
               if (revisionAnalyzer.isDestroyed(target.getPhysicalName())
                  && !database.ItemExists(target.getPhysicalName()))
               {
                  logger.WriteLine(String.format("NOTE: Skipping destroyed file: %s", targetPath));
                  itemInfo.setDestroyed(true);
               }
               else if (target.isProject())
               {
                  //Directory.CreateDirectory(targetPath);
                  new File(targetPath).mkdirs();
                  writeProject = true;
               }
               else
               {
                  writeFile = true;
               }
            }

            if (writeProject && pathMapper.isProjectRooted(target.getPhysicalName()))
            {
               // create all contained subdirectories
               for (VssProjectInfo projectInfo : pathMapper.getAllProjects(target.getPhysicalName()))
               {
                  logger.WriteLine(String.format("%s: Creating subdirectory %s", projectDesc,
                     projectInfo.getLogicalName()));
                  //Directory.CreateDirectory(projectInfo.GetPath());
                  new File(projectInfo.getPath()).mkdirs();
               }

               // write current rev of all contained files
               for (VssFileInfo fileInfo : pathMapper.getAllFiles(target.getPhysicalName()))
               {
                  if (writeRevision(pathMapper, actionType, fileInfo.getPhysicalName(), fileInfo.getVersion(),
                     target.getPhysicalName(), git))
                  {
                     // one or more files were written
                     needCommit = true;
                  }
               }
            }
            else if (writeFile)
            {
               // write current rev to working path
               int version = pathMapper.getFileVersion(target.getPhysicalName());
               if (writeRevisionTo(target.getPhysicalName(), version, targetPath))
               {
                  // add file explicitly, so it is visible to subsequent git operations
                  git.add(targetPath);
                  needCommit = true;
               }
            }
         }
      }
      // item is a file, not a project
      else if (actionType == VssActionType.Edit || actionType == VssActionType.Branch)
      {
         // if the action is Branch, the following code is necessary only if the item
         // was branched from a file that is not part of the migration subset; it will
         // make sure we start with the correct revision instead of the first revision

         VssItemName target = revision.getItem();

         // update current rev
         pathMapper.setFileVersion(target, revision.getVersion());

         // write current rev to all sharing projects
         writeRevision(pathMapper, actionType, target.getPhysicalName(), revision.getVersion(), null, git);
         needCommit = true;
      }
      return needCommit;
   }

   private boolean commitChangeset(GitCommandHandler git, Changeset changeset)
   {
      boolean result = false;
      //AbortRetryIgnore(delegate
      //{
      String comment = changeset.getComment();
      result =
         git.addAll()
            && git.commit(changeset.getUser(), getEmail(changeset.getUser()), comment != null ? comment
               : DefaultComment, changeset.getDateTime());
      //});
      return result;
   }

   //   private boolean RetryCancel(ThreadStart work)
   //   {
   //      return AbortRetryIgnore(work, MessageBoxButtons.RetryCancel);
   //   }
   //
   //   private boolean AbortRetryIgnore(ThreadStart work)
   //   {
   //      return AbortRetryIgnore(work, MessageBoxButtons.AbortRetryIgnore);
   //   }
   //
   //   private boolean AbortRetryIgnore(ThreadStart work, MessageBoxButtons buttons)
   //   {
   //      boolean retry;
   //      do
   //      {
   //         try
   //         {
   //            work();
   //            return true;
   //         }
   //         catch (Exception e)
   //         {
   //            var message = LogException(e);
   //
   //            message += "\nSee log file for more information.";
   //
   //            var button = MessageBox.Show(message, "Error", buttons, MessageBoxIcon.Error);
   //            switch (button)
   //            {
   //               case DialogResult.Retry :
   //                  retry = true;
   //                  break;
   //               case DialogResult.Ignore :
   //                  retry = false;
   //                  break;
   //               default :
   //                  retry = false;
   //                  workQueue.Abort();
   //                  break;
   //            }
   //         }
   //      }
   //      while (retry);
   //      return false;
   //   }

   private String getEmail(String user)
   {
      // TODO: user-defined mapping of user names to email addresses
      return user.toLowerCase().replace(' ', '.') + "@" + emailDomain;
   }

   private String getTagFromLabel(String label)
   {
      // git tag names must be valid filenames, so replace sequences of
      // invalid characters with an underscore
      String baseTag = label.replace("[^A-Za-z0-9_-]+", "_");

      // git tags are global, whereas VSS tags are local, so ensure
      // global uniqueness by appending a number; since the file system
      // may be case-insensitive, ignore case when hashing tags
      String tag = baseTag;
      for (int i = 2; !tagsUsed.add(tag.toUpperCase()); ++i)
      {
         tag = baseTag + "-" + i;
      }

      return tag;
   }

   private boolean writeRevision(VssPathMapper pathMapper, VssActionType actionType, String physicalName, int version,
      String underProject, GitCommandHandler git)
   {
      boolean needCommit = false;
      Iterable<String> paths = pathMapper.getFilePaths(physicalName, underProject);
      for (String path : paths)
      {
         logger.WriteLine(String.format("%s: %s revision %s", path, actionType, version));
         if (writeRevisionTo(physicalName, version, path))
         {
            // add file explicitly, so it is visible to subsequent git operations
            git.add(path);
            needCommit = true;
         }
      }
      return needCommit;
   }

   private boolean writeRevisionTo(String physical, int version, String destPath)
   {
      VssFile item;
      VssFileRevision revision;
      InputStream contents;
      try
      {
         item = (VssFile)database.GetItemPhysical(physical);
         revision = item.getRevision(version);
         contents = revision.getContents();
      }
      catch (Exception e)
      {
         // log an error for missing data files or versions, but keep processing
         //var message = ExceptionFormatter.Format(e);
         logger.WriteLine(String.format("ERROR: %s", e.getLocalizedMessage()));
         logger.WriteLine(e);
         return false;
      }

      // propagate exceptions here (e.g. disk full) to abort/retry/ignore
      //using (contents)
      //{
      IoUtil.writeStream(contents, destPath);
      //}

      // try to use the first revision (for this branch) as the create time,
      // since the item creation time doesn't seem to be meaningful
      Date createDateTime = item.getCreated();
      Iterator<VssRevision> revEnum = item.getRevisions().iterator();
      if (revEnum.hasNext())
      {
         createDateTime = revEnum.next().getDate();
      }

      // set file creation and update timestamps
      File destPathFile = new File(destPath);
      //destPathFile.setLastModified(revision.getDate().getTime());
      destPathFile.setLastModified(revision.getDate().getTime());
      //File.SetCreationTimeUtc(destPath, TimeZoneInfo.ConvertTimeToUtc(createDateTime));
      //File.SetLastWriteTimeUtc(destPath, TimeZoneInfo.ConvertTimeToUtc(revision.DateTime));

      return true;
   }

   //   private void WriteStream(Stream inputStream, string path)
   //   {
   //       Directory.CreateDirectory(Path.GetDirectoryName(path));
   //
   //       using (var outputStream = new FileStream(
   //           path, FileMode.Create, FileAccess.Write, FileShare.None))
   //       {
   //           streamCopier.Copy(inputStream, outputStream);
   //       }
   //   }

   private interface RenameDelegate
   {
      void rename(String sourcePath, String destPath);
   }

   private class FsMover implements RenameDelegate
   {

      /**
       * @see org.jvss.git.GitExporter.RenameDelegate#rename(java.lang.String,
       *      java.lang.String)
       */
      @Override
      public void rename(String sourcePath, String destPath)
      {
         IoUtil.move(sourcePath, destPath);
      }

   }

   private class GitMover implements RenameDelegate
   {
      private final GitCommandHandler git;

      /**
       * @param git
       */
      public GitMover(GitCommandHandler git)
      {
         super();
         this.git = git;
      }

      /**
       * @see org.jvss.git.GitExporter.RenameDelegate#rename(java.lang.String,
       *      java.lang.String)
       */
      @Override
      public void rename(String sourcePath, String destPath)
      {
         git.move(sourcePath, destPath);
      }

   }

   private void caseSensitiveRename(String sourcePath, String destPath, RenameDelegate renamer)
   {
      //      if (sourcePath.equalsIgnoreCase(destPath))
      //      {
      //         // workaround for case-only renames on case-insensitive file systems:
      //         //TODO implement me
      //         new Exception().printStackTrace();
      //         //         var sourceDir = Path.GetDirectoryName(sourcePath);
      //         //         var sourceFile = Path.GetFileName(sourcePath);
      //         //         var destDir = Path.GetDirectoryName(destPath);
      //         //         var destFile = Path.GetFileName(destPath);
      //         //
      //         //         if (sourceDir != destDir)
      //         //         {
      //         //            // recursively rename containing directories that differ : case
      //         //            CaseSensitiveRename(sourceDir, destDir, renamer);
      //         //
      //         //            // fix up source path based on renamed directory
      //         //            sourcePath = Path.Combine(destDir, sourceFile);
      //         //         }
      //         //
      //         //         if (sourceFile != destFile)
      //         //         {
      //         //            // use temporary filename to rename files that differ in case
      //         //            var tempPath = sourcePath + ".mvtmp";
      //         //            CaseSensitiveRename(sourcePath, tempPath, renamer);
      //         //            CaseSensitiveRename(tempPath, destPath, renamer);
      //         //         }
      //      }
      //      else
      //      {
      renamer.rename(sourcePath, destPath);
      //}
   }
}
