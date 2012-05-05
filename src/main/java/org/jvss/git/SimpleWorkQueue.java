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

import java.util.LinkedList;

/**
 * Simple work queue over a bounded number of thread-pool threads.
 */
public class SimpleWorkQueue
{
   private final LinkedList<Runnable> workQueue = new LinkedList<Runnable>();

   private final int maxThreads;

   private int activeThreads = 0;

   private boolean suspended = false;

   private volatile boolean aborting = false;

   public SimpleWorkQueue()
   {
      this.maxThreads = Runtime.getRuntime().availableProcessors();
   }

   public SimpleWorkQueue(int maxThreads)
   {
      this.maxThreads = maxThreads;
   }

   public boolean isIdle()
   {
      return activeThreads == 0;
   }

   public boolean isFullyActive()
   {
      return activeThreads == maxThreads;
   }

   public boolean isSuspended()
   {
      return suspended;
   }

   public boolean isAborting()
   {
      return aborting;
   }

   // Adds work to the head of the work queue. Useful for workers that
   // want to reschedule themselves on suspend.
   public void addFirst(Runnable work)
   {
      synchronized (workQueue)
      {
         workQueue.addFirst(work);
         startWorker();
      }
   }

   // Adds work to the tail of the work queue.
   public void addLast(Runnable work)
   {
      synchronized (workQueue)
      {
         workQueue.addLast(work);
         startWorker();
      }
   }

   // Clears pending work without affecting active work.
   public void clearPending()
   {
      synchronized (workQueue)
      {
         workQueue.clear();
      }
   }

   // Stops processing of pending work.
   public void suspend()
   {
      synchronized (workQueue)
      {
         suspended = true;
      }
   }

   // Resumes processing of pending work after being suspended.
   public void resume()
   {
      synchronized (workQueue)
      {
         suspended = false;
         while (activeThreads < workQueue.size())
         {
            startWorker();
         }
      }
   }

   // Signals active workers to abort and clears pending work.
   public void abort()
   {
      synchronized (workQueue)
      {
         if (activeThreads > 0)
         {
            // flag active workers to stop; last will reset the flag
            aborting = true;
         }

         // to avoid non-determinism, always clear the queue
         workQueue.clear();
      }
   }

   protected void onActive()
   {
   }

   protected void onIdle()
   {
      // auto-reset abort flag
      aborting = false;
   }

   protected void onStart(Runnable work)
   {
   }

   protected void onStop(Runnable work)
   {
   }

   protected void onException(Runnable work, Exception e)
   {
   }

   // Assumes work queue lock is held.
   private void startWorker()
   {
      if (activeThreads < maxThreads && !suspended)
      {
         if (++activeThreads == 1)
         {
            // hook for transition from Idle to Active
            onActive();
         }
         //ThreadPool.QueueUserWorkItem(Worker);
      }
   }

   private void Worker(Object state)
   {
      while (true)
      {
         Runnable work;
         synchronized (workQueue)
         {
            Runnable head = workQueue.getFirst();
            if (head == null || suspended)
            {
               if (--activeThreads == 0)
               {
                  // hook for transition from Active to Idle
                  onIdle();
               }
               return;
            }
            work = head;
            workQueue.remove();
         }

         // hook for worker initialization
         onStart(work);
         try
         {
            //work(work);
         }
         catch (Exception e)
         {
            // hook for worker exceptions
            onException(work, e);
         }
         finally
         {
            // hook for worker cleanup
            onStop(work);
         }
      }
   }
}
