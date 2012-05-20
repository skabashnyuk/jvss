package org.jvss.git;

import org.jvss.logical.VssDatabase;
import org.jvss.logical.VssDatabaseFactory;
import org.jvss.logical.VssItem;
import org.jvss.logical.VssPathException;
import org.jvss.logical.VssProject;

/**
 *
 */
public class MainImporter
{

   public static void main(String[] args)
   {
      try
      {
         //OpenLog(logTextBox.Text);
         Logger logger = new Logger();
         logger.setDisableOutput(true);
         //logger.WriteLine("VSS2Git version {0}", Assembly.GetExecutingAssembly().GetName().Version);

         //WriteSettings();

         //            Encoding encoding = Encoding.Default;
         //            EncodingInfo encodingInfo;
         //            if (codePages.TryGetValue(encodingComboBox.SelectedIndex, out encodingInfo))
         //            {
         //                encoding = encodingInfo.GetEncoding();
         //            }
         //
         //            logger.WriteLine("VSS encoding: {0} (CP: {1}, IANA: {2})",
         //                    encoding.EncodingName, encoding.CodePage, encoding.WebName);
         //            logger.WriteLine("Comment transcoding: {0}",
         //                    transcodeCheckBox.Checked ? "enabled" : "disabled");

         VssDatabaseFactory df = new VssDatabaseFactory("/home/sj/java/tmp/vss");
         df.setEncoding("Cp1251");
         VssDatabase db = df.Open();

         String path = "$";//vssProjectTextBox.Text;
         VssItem item;
         try
         {
            item = db.GetItem(path);
         }
         catch (VssPathException ex)
         {
            //                MessageBox.Show(ex.Message, "Invalid project path",
            //                        MessageBoxButtons.OK, MessageBoxIcon.Error);
            return;
         }

         VssProject project = (VssProject)item;
         if (project == null)
         {
            //                MessageBox.Show(path + " is not a project", "Invalid project path",
            //                        MessageBoxButtons.OK, MessageBoxIcon.Error);
            return;
         }

         RevisionAnalyzer revisionAnalyzer = new RevisionAnalyzer(logger, db);
         //            if (!string.IsNullOrEmpty(excludeTextBox.Text))
         //            {
         //                revisionAnalyzer.ExcludeFiles = excludeTextBox.Text;
         //            }
         revisionAnalyzer.addItem(project);

         ChangesetBuilder changesetBuilder = new ChangesetBuilder(revisionAnalyzer, logger);
         changesetBuilder.setAnyCommentThreshold(30000);//30sec
         changesetBuilder.setSameCommentThreshold(600000);//SameCommentThreshold = TimeSpan.FromSeconds((double)sameCommentUpDown.Value);
         changesetBuilder.buildChangesets();
         String outGit = "/home/sj/java/tmp/git";
         IoUtil.delete(outGit);
         logger.setDisableOutput(false);
         if (outGit != null)
         {
            GitCommandHandler git =new GitWrapper(outGit, "git", "", false, "UTF-8");
            GitExporter gitExporter = new GitExporter(logger, revisionAnalyzer, changesetBuilder, git);
            //                if (!string.IsNullOrEmpty(domainTextBox.Text))
            //                {
            //                    gitExporter.EmailDomain = domainTextBox.Text;
            //                }
            //                if (!transcodeCheckBox.Checked)
            //                {
            //                    gitExporter.CommitEncoding = encoding;
            //                }
            gitExporter.exportToGit(outGit);//ExportToGit(outDirTextBox.Text);
         }

         //            workQueue.Idle += delegate
         //            {
         //                logger.Dispose();
         //                logger = Logger.Null;
         //            };
         //
         //            statusTimer.Enabled = true;
         //            goButton.Enabled = false;
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         //ShowException(ex);
      }

   }
}
