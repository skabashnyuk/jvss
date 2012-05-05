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

/**
 * Simple work queue over a bounded number of thread-pool threads.
 */
public class SimpleWorkQueue
{
   //   private final List<WaitCallback> workQueue = new LinkedList<WaitCallback>();
   //   private final int maxThreads;
   //   private int activeThreads = 0;
   //   private boolean suspended = false;
   //   private volatile boolean aborting = false;
   //
   //   public SimpleWorkQueue()
   //   {
   //       this.maxThreads = Environment.ProcessorCount;
   //   }
   //
   //   public SimpleWorkQueue(int maxThreads)
   //   {
   //       this.maxThreads = maxThreads;
   //   }
   //
   //   public bool IsIdle
   //   {
   //       get { return activeThreads == 0; }
   //   }
   //
   //   public bool IsFullyActive
   //   {
   //       get { return activeThreads == maxThreads; }
   //   }
   //
   //   public bool IsSuspended
   //   {
   //       get { return suspended; }
   //   }
   //
   //   public bool IsAborting
   //   {
   //       get { return aborting; }
   //   }
   //
   //   // Adds work to the head of the work queue. Useful for workers that
   //   // want to reschedule themselves on suspend.
   //   public void AddFirst(WaitCallback work)
   //   {
   //       lock (workQueue)
   //       {
   //           workQueue.AddFirst(work);
   //           StartWorker();
   //       }
   //   }
   //
   //   // Adds work to the tail of the work queue.
   //   public void AddLast(WaitCallback work)
   //   {
   //       lock (workQueue)
   //       {
   //           workQueue.AddLast(work);
   //           StartWorker();
   //       }
   //   }
   //
   //   // Clears pending work without affecting active work.
   //   public void ClearPending()
   //   {
   //       lock (workQueue)
   //       {
   //           workQueue.Clear();
   //       }
   //   }
   //
   //   // Stops processing of pending work.
   //   public void Suspend()
   //   {
   //       lock (workQueue)
   //       {
   //           suspended = true;
   //       }
   //   }
   //
   //   // Resumes processing of pending work after being suspended.
   //   public void Resume()
   //   {
   //       lock (workQueue)
   //       {
   //           suspended = false;
   //           while (activeThreads < workQueue.Count)
   //           {
   //               StartWorker();
   //           }
   //       }
   //   }
   //
   //   // Signals active workers to abort and clears pending work.
   //   public void Abort()
   //   {
   //       lock (workQueue)
   //       {
   //           if (activeThreads > 0)
   //           {
   //               // flag active workers to stop; last will reset the flag
   //               aborting = true;
   //           }
   //
   //           // to avoid non-determinism, always clear the queue
   //           workQueue.Clear();
   //       }
   //   }
   //
   //   protected virtual void OnActive()
   //   {
   //   }
   //
   //   protected virtual void OnIdle()
   //   {
   //       // auto-reset abort flag
   //       aborting = false;
   //   }
   //
   //   protected virtual void OnStart(WaitCallback work)
   //   {
   //   }
   //
   //   protected virtual void OnStop(WaitCallback work)
   //   {
   //   }
   //
   //   protected virtual void OnException(WaitCallback work, Exception e)
   //   {
   //   }
   //
   //   // Assumes work queue lock is held.
   //   private void StartWorker()
   //   {
   //       if (activeThreads < maxThreads && !suspended)
   //       {
   //           if (++activeThreads == 1)
   //           {
   //               // hook for transition from Idle to Active
   //               OnActive();
   //           }
   //           ThreadPool.QueueUserWorkItem(Worker);
   //       }
   //   }
   //
   //   private void Worker(object state)
   //   {
   //       while (true)
   //       {
   //           WaitCallback work;
   //           lock (workQueue)
   //           {
   //               var head = workQueue.First;
   //               if (head == null || suspended)
   //               {
   //                   if (--activeThreads == 0)
   //                   {
   //                       // hook for transition from Active to Idle
   //                       OnIdle();
   //                   }
   //                   return;
   //               }
   //               work = head.Value;
   //               workQueue.RemoveFirst();
   //           }
   //
   //           // hook for worker initialization
   //           OnStart(work);
   //           try
   //           {
   //               work(work);
   //           }
   //           catch (Exception e)
   //           {
   //               // hook for worker exceptions
   //               OnException(work, e);
   //           }
   //           finally
   //           {
   //               // hook for worker cleanup
   //               OnStop(work);
   //           }
   //       }
   //   }
}
