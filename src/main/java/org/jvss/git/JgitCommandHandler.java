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

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.InvalidTagNameException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.UnmergedPathException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author <a href="mailto:foo@bar.org">Foo Bar</a>
 * @version $Id: exo-jboss-codetemplates.xml 34360 2009-07-22 23:58:59Z
 *          aheritier $
 * 
 */
public class JgitCommandHandler implements GitCommandHandler
{

   private final String gitdir;

   private Repository repository;

   /**
    * @param gitdir
    */
   public JgitCommandHandler(String gitdir)
   {
      super();
      this.gitdir = gitdir;
   }

   /**
    * @see org.jvss.git.GitCommandHandler#init()
    */
   @Override
   public boolean init()
   {
      InitCommand command = Git.init();
      command.setBare(false);
      if (gitdir != null)
      {
         command.setDirectory(new File(gitdir));
      }
      repository = command.call().getRepository();

      return true;
   }

   /**
    * @see org.jvss.git.GitCommandHandler#setConfig(java.lang.String,
    *      java.lang.String)
    */
   @Override
   public void setConfig(String name, String value)
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.jvss.git.GitCommandHandler#add(java.lang.String)
    */
   @Override
   public boolean add(String path)
   {
      AddCommand addCmd = new Git(repository).add();
      addCmd.setUpdate(false);
      addCmd.addFilepattern(getPathInsideRepo(path));
      try
      {
         addCmd.call();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
         return false;
      }

      return true;
   }

   /**
    * @see org.jvss.git.GitCommandHandler#add(java.util.Collection)
    */
   @Override
   public boolean add(Collection<String> filepatterns)
   {
      AddCommand addCmd = new Git(repository).add();
      addCmd.setUpdate(false);
      for (String p : filepatterns)
      {
         addCmd.addFilepattern(getPathInsideRepo(p));
      }
      try
      {
         addCmd.call();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
         return false;
      }

      return true;

   }

   /**
    * @see org.jvss.git.GitCommandHandler#addAll()
    */
   @Override
   public boolean addAll()
   {
      //// TODO Auto-generated method stub
      //return false;
      return add(".");
   }

   /**
    * @see org.jvss.git.GitCommandHandler#remove(java.lang.String, boolean)
    */
   @Override
   public void remove(String path, boolean recursive)
   {
      RmCommand command = new Git(repository).rm();
      command.addFilepattern(getPathInsideRepo(path));
      try
      {
         command.call();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }

   /**
    * @see org.jvss.git.GitCommandHandler#move(java.lang.String,
    *      java.lang.String)
    */
   @Override
   public void move(String sourcePath, String destPath)
   {
      IoUtil.move(sourcePath, destPath);
      add(getPathInsideRepo(destPath));
      remove(getPathInsideRepo(sourcePath), true);
   }

   /**
    * @see org.jvss.git.GitCommandHandler#commit(java.lang.String,
    *      java.lang.String, java.lang.String, java.util.Date)
    */
   @Override
   public boolean commit(String authorName, String authorEmail, String comment, Date localTime)
   {
      CommitCommand commitCmd = new Git(repository).commit();
      commitCmd.setAll(true);
      commitCmd.setAuthor(new PersonIdent(authorName, authorEmail, localTime, TimeZone.getDefault()));
      commitCmd.setMessage(comment);
      try
      {
         RevCommit commit = commitCmd.call();
         return true;
      }
      catch (NoHeadException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (NoMessageException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (UnmergedPathException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (ConcurrentRefUpdateException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (JGitInternalException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (WrongRepositoryStateException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return false;
   }

   /**
    * @see org.jvss.git.GitCommandHandler#tag(java.lang.String,
    *      java.lang.String, java.lang.String, java.lang.String, java.util.Date)
    */
   @Override
   public boolean tag(String name, String taggerName, String taggerEmail, String comment, Date localTime)
   {
      Git git = new Git(repository);
      TagCommand command = git.tag().setForceUpdate(true).setMessage(comment).setName(name.replace(' ', '_'));
      command.setTagger(new PersonIdent(taggerName, taggerEmail, localTime, TimeZone.getDefault()));

      try
      {
         command.call();
         return true;
      }
      catch (JGitInternalException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (ConcurrentRefUpdateException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (InvalidTagNameException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (NoHeadException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return true;
   }

   public String getPathInsideRepo(String path)
   {
      if (path.startsWith(gitdir))
      {
         return path.substring(gitdir.length() + 1);
      }
      return path;
   }
}
