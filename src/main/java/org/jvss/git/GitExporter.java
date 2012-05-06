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

import org.jvss.git.VssPathMapper.VssItemInfo;
import org.jvss.git.VssPathMapper.VssProjectInfo;
import org.jvss.logical.VssAction.VssActionType;
import org.jvss.logical.VssAction.VssLabelAction;
import org.jvss.logical.VssAction.VssMoveFromAction;
import org.jvss.logical.VssAction.VssNamedAction;
import org.jvss.logical.VssAction.VssRenameAction;
import org.jvss.logical.VssDatabase;
import org.jvss.logical.VssFile;
import org.jvss.logical.VssFileRevision;
import org.jvss.logical.VssItemName;
import org.jvss.logical.VssProject;

import java.io.File;
import java.util.HashSet;
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
   
   
   

   public GitExporter(RevisionAnalyzer revisionAnalyzer, ChangesetBuilder changesetBuilder, GitCommandHandler git )
   {  
       this.git = git;
      this.logger = new Logger();
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
           //LogStatus(work, "Initializing Git repository");
           
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
           if(!git.init()){
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
           for (VssProject  rootProject : revisionAnalyzer.getRootProjects())
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
//               var changesetDesc = string.Format(CultureInfo.InvariantCulture,
//                   "changeset {0} from {1}", changesetId, changeset.DateTime);

               // replay each revision in changeset
               //LogStatus(work, "Replaying " + changesetDesc);
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
                   //LogStatus(work, "Committing " + changesetDesc);
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
                       if (labelName == null || labelName.length()==0)
                       {
                           logger.WriteLine("NOTE: Ignoring empty label");
                       }
                       else if (commitCount == 0)
                       {
                           logger.WriteLine("NOTE: Ignoring label '{0}' before initial commit", labelName);
                       }
                       else
                       {
                            String tagName = getTagFromLabel(labelName);

                           String tagMessage = "Creating tag " + tagName;
                           if (tagName != labelName)
                           {
                               tagMessage += " for label '" + labelName + "'";
                           }
                           //LogStatus(work, tagMessage);

                           // annotated tags require (and are implied by) a tag message;
                           // tools like Mercurial's git converter only import annotated tags
                           String tagComment = label.getComment();
                           if ((tagComment==null ||tagComment.length()==0) && forceAnnotatedTags)
                           {
                               // use the original VSS label as the tag message if none was provided
                               tagComment = labelName;
                           }

                           //if (AbortRetryIgnore(
                             //  delegate
                               //{
                                   if(git.tag(tagName, label.getUser(),getEmail(label.getUser()),
                                       tagComment, label.getDateTime()))
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
//           logger.WriteLine("Git commits: {0}", commitCount);
//           logger.WriteLine("Git tags: {0}", tagCount);
       //});
   }

   private boolean replayChangeset(VssPathMapper pathMapper, Changeset changeset, GitCommandHandler git, LinkedList<Revision> labels)
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

   private boolean replayRevision(VssPathMapper pathMapper, Revision revision,
       GitCommandHandler git, LinkedList<Revision> labels)
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
               logger.WriteLine("NOTE: {0} is currently unmapped", project);
           }

           VssItemName target = null;
           String targetPath = null;
           VssNamedAction namedAction = (VssNamedAction)revision.getAction() ;
           if (namedAction != null)
           {
               target = namedAction.name();
               if (projectPath != null)
               {
                   targetPath = new File(projectPath, target.getLogicalName()).getAbsolutePath();
               }
           }

           boolean isAddAction = false;
           boolean writeProject = false;
           boolean writeFile = false;
           VssItemInfo itemInfo = null;
           switch (actionType)
           {
               case VssActionType.Label:
                   // defer tagging until after commit
                   labels.addLast(revision);
                   break;

               case VssActionType.Create:
                   // ignored; items are actually created when added to a project
                   break;

               case VssActionType.Add:
               case VssActionType.Share:
                   logger.WriteLine("{0}: {1} {2}", projectDesc, actionType, target.getLogicalName());
                   itemInfo = pathMapper.addItem(project, target);
                   isAddAction = true;
                   break;

               case VssActionType.Recover:
                   logger.WriteLine("{0}: {1} {2}", projectDesc, actionType, target.getLogicalName());
                   itemInfo = pathMapper.recoverItem(project, target);
                   isAddAction = true;
                   break;

               case VssActionType.Delete:
               case VssActionType.Destroy:
                   {
                       logger.WriteLine("{0}: {1} {2}", projectDesc, actionType, target.getLogicalName());
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
                                       logger.WriteLine("NOTE: {0} contains another file named {1}; not deleting file",
                                           projectDesc, target.getLogicalName());
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

               case VssActionType.Rename:
                   {
                       var renameAction = (VssRenameAction)revision.Action;
                       logger.WriteLine("{0}: {1} {2} to {3}",
                           projectDesc, actionType, renameAction.OriginalName, target.LogicalName);
                       itemInfo = pathMapper.RenameItem(target);
                       if (targetPath != null && !itemInfo.Destroyed)
                       {
                           var sourcePath = Path.Combine(projectPath, renameAction.OriginalName);
                           if (target.IsProject ? Directory.Exists(sourcePath) : File.Exists(sourcePath))
                           {
                               // renaming a file or a project that contains files?
                               var projectInfo = itemInfo as VssProjectInfo;
                               if (projectInfo == null || projectInfo.ContainsFiles())
                               {
                                   CaseSensitiveRename(sourcePath, targetPath, git.Move);
                                   needCommit = true;
                               }
                               else
                               {
                                   // git doesn't care about directories with no files
                                   CaseSensitiveRename(sourcePath, targetPath, Directory.Move);
                               }
                           }
                           else
                           {
                               logger.WriteLine("NOTE: Skipping rename because {0} does not exist", sourcePath);
                           }
                       }
                   }
                   break;

               case VssActionType.MoveFrom:
                   // if both MoveFrom & MoveTo are present (e.g.
                   // one of them has not been destroyed), only one
                   // can succeed, so check that the source exists
                   {
                       var moveFromAction = (VssMoveFromAction)revision.Action;
                       logger.WriteLine("{0}: Move from {1} to {2}",
                           projectDesc, moveFromAction.OriginalProject, targetPath ?? target.LogicalName);
                       var sourcePath = pathMapper.GetProjectPath(target.PhysicalName);
                       var projectInfo = pathMapper.MoveProjectFrom(
                           project, target, moveFromAction.OriginalProject);
                       if (targetPath != null && !projectInfo.Destroyed)
                       {
                           if (sourcePath != null && Directory.Exists(sourcePath))
                           {
                               if (projectInfo.ContainsFiles())
                               {
                                   git.Move(sourcePath, targetPath);
                                   needCommit = true;
                               }
                               else
                               {
                                   // git doesn't care about directories with no files
                                   Directory.Move(sourcePath, targetPath);
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

               case VssActionType.MoveTo:
                   {
                       // handle actual moves in MoveFrom; this just does cleanup of destroyed projects
                       var moveToAction = (VssMoveToAction)revision.Action;
                       logger.WriteLine("{0}: Move to {1} from {2}",
                           projectDesc, moveToAction.NewProject, targetPath ?? target.LogicalName);
                       var projectInfo = pathMapper.MoveProjectTo(
                           project, target, moveToAction.NewProject);
                       if (projectInfo.Destroyed && targetPath != null && Directory.Exists(targetPath))
                       {
                           // project was moved to a now-destroyed project; remove empty directory
                           Directory.Delete(targetPath, true);
                       }
                   }
                   break;

               case VssActionType.Pin:
                   {
                       var pinAction = (VssPinAction)revision.Action;
                       if (pinAction.Pinned)
                       {
                           logger.WriteLine("{0}: Pin {1}", projectDesc, target.LogicalName);
                           itemInfo = pathMapper.PinItem(project, target);
                       }
                       else
                       {
                           logger.WriteLine("{0}: Unpin {1}", projectDesc, target.LogicalName);
                           itemInfo = pathMapper.UnpinItem(project, target);
                           writeFile = !itemInfo.Destroyed;
                       }
                   }
                   break;

               case VssActionType.Branch:
                   {
                       var branchAction = (VssBranchAction)revision.Action;
                       logger.WriteLine("{0}: {1} {2}", projectDesc, actionType, target.LogicalName);
                       itemInfo = pathMapper.BranchFile(project, target, branchAction.Source);
                       // branching within the project might happen after branching of the file
                       writeFile = true;
                   }
                   break;

               case VssActionType.Archive:
                   // currently ignored
                   {
                       var archiveAction = (VssArchiveAction)revision.Action;
                       logger.WriteLine("{0}: Archive {1} to {2} (ignored)",
                           projectDesc, target.LogicalName, archiveAction.ArchivePath);
                   }
                   break;

               case VssActionType.Restore:
                   {
                       var restoreAction = (VssRestoreAction)revision.Action;
                       logger.WriteLine("{0}: Restore {1} from archive {2}",
                           projectDesc, target.LogicalName, restoreAction.ArchivePath);
                       itemInfo = pathMapper.AddItem(project, target);
                       isAddAction = true;
                   }
                   break;
           }

           if (targetPath != null)
           {
               if (isAddAction)
               {
                   if (revisionAnalyzer.IsDestroyed(target.PhysicalName) &&
                       !database.ItemExists(target.PhysicalName))
                   {
                       logger.WriteLine("NOTE: Skipping destroyed file: {0}", targetPath);
                       itemInfo.Destroyed = true;
                   }
                   else if (target.IsProject)
                   {
                       Directory.CreateDirectory(targetPath);
                       writeProject = true;
                   }
                   else
                   {
                       writeFile = true;
                   }
               }

               if (writeProject && pathMapper.IsProjectRooted(target.PhysicalName))
               {
                   // create all contained subdirectories
                   for (var projectInfo : pathMapper.GetAllProjects(target.PhysicalName))
                   {
                       logger.WriteLine("{0}: Creating subdirectory {1}",
                           projectDesc, projectInfo.LogicalName);
                       Directory.CreateDirectory(projectInfo.GetPath());
                   }

                   // write current rev of all contained files
                   for (var fileInfo : pathMapper.GetAllFiles(target.PhysicalName))
                   {
                       if (WriteRevision(pathMapper, actionType, fileInfo.PhysicalName,
                           fileInfo.Version, target.PhysicalName, git))
                       {
                           // one or more files were written
                           needCommit = true;
                       }
                   }
               }
               else if (writeFile)
               {
                   // write current rev to working path
                   int version = pathMapper.GetFileVersion(target.PhysicalName);
                   if (WriteRevisionTo(target.PhysicalName, version, targetPath))
                   {
                       // add file explicitly, so it is visible to subsequent git operations
                       git.Add(targetPath);
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

           var target = revision.Item;

           // update current rev
           pathMapper.SetFileVersion(target, revision.Version);

           // write current rev to all sharing projects
           WriteRevision(pathMapper, actionType, target.PhysicalName,
               revision.Version, null, git);
           needCommit = true;
       }
       return needCommit;
   }

   private boolean commitChangeset(GitCommandHandler git, Changeset changeset)
   {
       var result = false;
       AbortRetryIgnore(delegate
       {
           result = git.AddAll() &&
               git.Commit(changeset.User, GetEmail(changeset.User),
               changeset.Comment ?? DefaultComment, changeset.DateTime);
       });
       return result;
   }

   private boolean RetryCancel(ThreadStart work)
   {
       return AbortRetryIgnore(work, MessageBoxButtons.RetryCancel);
   }

   private boolean AbortRetryIgnore(ThreadStart work)
   {
       return AbortRetryIgnore(work, MessageBoxButtons.AbortRetryIgnore);
   }

   private boolean AbortRetryIgnore(ThreadStart work, MessageBoxButtons buttons)
   {
       boolean retry;
       do
       {
           try
           {
               work();
               return true;
           }
           catch (Exception e)
           {
               var message = LogException(e);

               message += "\nSee log file for more information.";

               var button = MessageBox.Show(message, "Error", buttons, MessageBoxIcon.Error);
               switch (button)
               {
                   case DialogResult.Retry:
                       retry = true;
                       break;
                   case DialogResult.Ignore:
                       retry = false;
                       break;
                   default:
                       retry = false;
                       workQueue.Abort();
                       break;
               }
           }
       } while (retry);
       return false;
   }

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

   private boolean WriteRevision(VssPathMapper pathMapper, VssActionType actionType,
       string physicalName, int version, string underProject, GitCommandHandler git)
   {
       var needCommit = false;
       var paths = pathMapper.GetFilePaths(physicalName, underProject);
       for (string path : paths)
       {
           logger.WriteLine("{0}: {1} revision {2}", path, actionType, version);
           if (WriteRevisionTo(physicalName, version, path))
           {
               // add file explicitly, so it is visible to subsequent git operations
               git.Add(path);
               needCommit = true;
           }
       }
       return needCommit;
   }

   private boolean WriteRevisionTo(string physical, int version, string destPath)
   {
       VssFile item;
       VssFileRevision revision;
       Stream contents;
       try
       {
           item = (VssFile)database.GetItemPhysical(physical);
           revision = item.GetRevision(version);
           contents = revision.GetContents();
       }
       catch (Exception e)
       {
           // log an error for missing data files or versions, but keep processing
           var message = ExceptionFormatter.Format(e);
           logger.WriteLine("ERROR: {0}", message);
           logger.WriteLine(e);
           return false;
       }

       // propagate exceptions here (e.g. disk full) to abort/retry/ignore
       using (contents)
       {
           WriteStream(contents, destPath);
       }

       // try to use the first revision (for this branch) as the create time,
       // since the item creation time doesn't seem to be meaningful
       var createDateTime = item.Created;
       using (var revEnum = item.Revisions.GetEnumerator())
       {
           if (revEnum.MoveNext())
           {
               createDateTime = revEnum.Current.DateTime;
           }
       }

       // set file creation and update timestamps
       File.SetCreationTimeUtc(destPath, TimeZoneInfo.ConvertTimeToUtc(createDateTime));
       File.SetLastWriteTimeUtc(destPath, TimeZoneInfo.ConvertTimeToUtc(revision.DateTime));

       return true;
   }

   private void WriteStream(Stream inputStream, string path)
   {
       Directory.CreateDirectory(Path.GetDirectoryName(path));

       using (var outputStream = new FileStream(
           path, FileMode.Create, FileAccess.Write, FileShare.None))
       {
           streamCopier.Copy(inputStream, outputStream);
       }
   }

   private delegate void RenameDelegate(string sourcePath, string destPath);

   private void CaseSensitiveRename(string sourcePath, string destPath, RenameDelegate renamer)
   {
       if (sourcePath.Equals(destPath, StringComparison.OrdinalIgnoreCase))
       {
           // workaround for case-only renames on case-insensitive file systems:

           var sourceDir = Path.GetDirectoryName(sourcePath);
           var sourceFile = Path.GetFileName(sourcePath);
           var destDir = Path.GetDirectoryName(destPath);
           var destFile = Path.GetFileName(destPath);

           if (sourceDir != destDir)
           {
               // recursively rename containing directories that differ : case
               CaseSensitiveRename(sourceDir, destDir, renamer);

               // fix up source path based on renamed directory
               sourcePath = Path.Combine(destDir, sourceFile);
           }

           if (sourceFile != destFile)
           {
               // use temporary filename to rename files that differ in case
               var tempPath = sourcePath + ".mvtmp";
               CaseSensitiveRename(sourcePath, tempPath, renamer);
               CaseSensitiveRename(tempPath, destPath, renamer);
           }
       }
       else
       {
           renamer(sourcePath, destPath);
       }
   }
}
