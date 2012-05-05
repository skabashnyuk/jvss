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
 * Exception thrown while executing an external process.
 */
public class ProcessException extends RuntimeException
{
   private final String executable;

   private final String arguments;

   public ProcessException(String message, String executable, String arguments)

   {
      super(message);
      this.executable = executable;
      this.arguments = arguments;
   }

   public ProcessException(String message, Exception innerException, String executable, String arguments)

   {
      super(message, innerException);
      this.executable = executable;
      this.arguments = arguments;
   }

   /**
    * @return the executable
    */
   public String getExecutable()
   {
      return executable;
   }

   /**
    * @return the arguments
    */
   public String getArguments()
   {
      return arguments;
   }

}
