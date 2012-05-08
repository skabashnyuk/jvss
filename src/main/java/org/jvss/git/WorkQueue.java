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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Extends the simple work queue with support for tracking worker status and
 * exceptions.
 */
public class WorkQueue extends SimpleWorkQueue
{
//   //private final static ManualResetEvent idleEvent = new ManualResetEvent(true);
//   //88private final static Stopwatch stopwatch = new Stopwatch();
//   private final static LinkedList<Exception> workExceptions = new LinkedList<Exception>();
//   private final static Map<Object, String> workStatuses = new HashMap<Object, String>();
//   private Object lastStatusWork;
//   private String lastStatus;
//
//   public String getLastStatus()
//   {
//       return lastStatus;
//   }
//
//   public WorkQueue()
//   {
//   }
//
//   public WorkQueue(int maxThreads)
//
//   {
//      super(maxThreads);
//   }
////
////   public TimeSpan ActiveTime
////   {
////       get { return stopwatch.Elapsed; }
////   }
////
////   public WaitHandle IdleEvent
////   {
////       get { return idleEvent; }
////   }
////
////   public event EventHandler Idle;
////
////   public void WaitIdle()
////   {
////       idleEvent.WaitOne();
////   }
//
//   public Collection<Exception> fetchExceptions()
//   {
//       synchronized (workExceptions)
//       {
//           if (workExceptions.size() > 0)
//           {
//               List<Exception> result = new ArrayList<Exception>(workExceptions);
//               workExceptions.clear();
//               return result;
//           }
//       }
//       return null;
//   }
//
//   public String getStatus(Object work)
//   {
//       String result;
//       synchronized (workStatuses)
//       {
//           workStatuses.TryGetValue(work, out result);
//       }
//       return result;
//   }
//
//   public void SetStatus(object work, string status)
//   {
//       lock (workStatuses)
//       {
//           // only allow status to be set if key is already present,
//           // so we know that it will be removed in OnStop
//           if (workStatuses.ContainsKey(work))
//           {
//               workStatuses[work] = status;
//               if (string.IsNullOrEmpty(status))
//               {
//                   WorkStatusCleared(work);
//               }
//               else
//               {
//                   lastStatusWork = work;
//                   lastStatus = status;
//               }
//           }
//       }
//   }
//
//   public void ClearStatus(object work)
//   {
//       SetStatus(work, null);
//   }
//
//   protected override void OnActive()
//   {
//       base.OnActive();
//       idleEvent.Reset();
//       stopwatch.Start();
//   }
//
//   protected override void OnIdle()
//   {
//       base.OnIdle();
//       stopwatch.Stop();
//       idleEvent.Set();
//
//       var handler = Idle;
//       if (handler != null)
//       {
//           handler(this, EventArgs.Empty);
//       }
//   }
//
//   protected override void OnStart(WaitCallback work)
//   {
//       base.OnStart(work);
//       lock (workStatuses)
//       {
//           workStatuses[work] = null;
//       }
//   }
//
//   protected override void OnStop(WaitCallback work)
//   {
//       base.OnStop(work);
//       lock (workStatuses)
//       {
//           workStatuses.Remove(work);
//           WorkStatusCleared(work);
//       }
//   }
//
//   protected override void OnException(WaitCallback work, Exception e)
//   {
//       base.OnException(work, e);
//       lock (workExceptions)
//       {
//           workExceptions.AddLast(e);
//       }
//   }
//
//   // Assumes work status lock is held.
//   private void WorkStatusCleared(object work)
//   {
//       if (work == lastStatusWork)
//       {
//           lastStatusWork = null;
//           lastStatus = null;
//
//           foreach (var entry in workStatuses)
//           {
//               if (!string.IsNullOrEmpty(entry.Value))
//               {
//                   lastStatusWork = entry.Key;
//                   lastStatus = entry.Value;
//                   break;
//               }
//           }
//       }
//   }
}
