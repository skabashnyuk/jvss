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
package org.jvss.dump;

import org.jvss.logical.VssDatabase;
import org.jvss.logical.VssDatabaseFactory;
import org.jvss.physical.ItemFile;
import org.jvss.physical.ItemHeaderRecord.ItemType;
import org.jvss.physical.NameFile;
import org.jvss.physical.NameRecord;
import org.jvss.physical.RevisionRecord;
import org.jvss.physical.RevisionRecord.Action;
import org.jvss.physical.VssRecord;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Dumps pretty much everything in the VSS database to the console.
 */
public class Program
{
   private final static String Separator = "------------------------------------------------------------";

   /**
    * @param args
    * @throws IOException
    */
   public static void main(String[] args) throws IOException
   {
      boolean invalidArg = false;
      int argIndex = 0;

      String repoPath = "/home/sj/java/tmp/vss";//args[argIndex];
      VssDatabaseFactory df = new VssDatabaseFactory(repoPath);
      VssDatabase db = df.Open();

      System.out.println("File hierarchy:");
      System.out.println(Separator);
      TreeDumper tree = new TreeDumper(System.out);
      tree.setIncludeRevisions(false);
      tree.dumpProject(db.getRootProject());
      System.out.println();

      System.out.println("Log file contents:");
      for (char c = 'a'; c <= 'z'; ++c)
      {

         String[] dataPaths = new File(db.getDataPath(), "" + c).list(new FilenameFilter()
         {

            @Override
            public boolean accept(File dir, String name)
            {

               return name.matches("*.");
            }
         });
         //           String[] dataPaths = Directory.GetFiles(
         //               new File(db.getDataPath(), c), "*.");
         for (String dataPath : dataPaths)
         {
            String dataFile = dataPath.toUpperCase();
            boolean orphaned = !tree.getPhysicalNames().contains(dataFile);
            System.out.println(Separator);
            System.out.format("{0}{1}", dataPath, orphaned ? " (orphaned)" : "");
            System.out.println();
            dumpLogFile(dataPath);
         }
      }
      System.out.println();

      System.out.println("Name file contents:");
      System.out.println(Separator);
      File namePath = new File(db.getDataPath(), "names.dat");
      System.out.println(namePath.getAbsolutePath());
      dumpNameFile(namePath.getAbsolutePath());
      System.out.println();

      System.out.println(Separator);
      System.out.format("Project actions: {0}", formatCollection(projectActions));
      System.out.println();
      System.out.format("File actions: {0}", formatCollection(fileActions));
      System.out.println();
   }

   private static Set<Action> projectActions = new HashSet<Action>();

   private static Set<Action> fileActions = new HashSet<Action>();

   private static String formatCollection(Set<Action> collection)
   {
      StringBuilder buf = new StringBuilder();
      for (Action action : collection)
      {
         if (buf.length() > 0)
         {
            buf.append(", ");
         }
         buf.append(action);
      }

      return buf.toString();
   }

   private static void dumpLogFile(String filename)
   {
      try
      {
         ItemFile itemFile = new ItemFile(filename, "Cp1251");
         itemFile.getHeader().getHeader().dump(System.out);
         itemFile.getHeader().dump(System.out);
         VssRecord record = itemFile.GetNextRecord(true);
         while (record != null)
         {
            record.getHeader().dump(System.out);
            record.dump(System.out);
            RevisionRecord revision = (RevisionRecord)record;
            if (revision != null)
            {
               if (itemFile.getHeader().getItemType() == ItemType.PROJECT)
               {
                  projectActions.add(revision.getAction());
               }
               else
               {
                  fileActions.add(revision.getAction());
               }
            }
            record = itemFile.GetNextRecord(true);
         }
      }
      catch (Exception e)
      {
         System.out.format("ERROR: {0}", e.getMessage());
      }
   }

   private static void dumpNameFile(String filename)
   {
      try
      {
         NameFile nameFile = new NameFile(filename, "Cp1251");
         nameFile.getHeader().getHeader().dump(System.out);
         nameFile.getHeader().dump(System.out);
         NameRecord name = nameFile.getNextName();
         while (name != null)
         {
            name.getHeader().dump(System.out);
            name.dump(System.out);
            name = nameFile.getNextName();
         }
      }
      catch (Exception e)
      {
         System.out.format("ERROR: {0}", e.getMessage());
      }
   }
}
