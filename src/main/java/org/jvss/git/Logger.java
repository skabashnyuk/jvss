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

import org.slf4j.LoggerFactory;

/**
 * Writes log messages
 */
public class Logger
{

   private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Logger.class);

   private final static String sectionSeparator = "------------------------------------------------------------";

   private boolean disableOutput = false;

   /**
    * @return the disableOutput
    */
   public boolean isDisableOutput()
   {
      return disableOutput;
   }

   /**
    * @param disableOutput
    *           the disableOutput to set
    */
   public void setDisableOutput(boolean disableOutput)
   {
      this.disableOutput = disableOutput;
   }

   public void Write(boolean value)
   {
      Write(Boolean.toString(value));
   }

   public void Write(char value)
   {
      Write("" + value);
   }

   public void Write(char[] buffer)
   {
      Write(buffer, 0, buffer.length);
   }

   public void Write(double value)
   {
      Write(Double.toString(value));
   }

   public void Write(float value)
   {
      Write(Float.toString(value));
   }

   public void Write(int value)
   {
      Write(Integer.toString(value));
   }

   public void Write(long value)
   {
      Write(Long.toString(value));
   }

   public void Write(Object value)
   {
      Write(value.toString());
   }

   public void Write(String value)
   {
      WriteInternal(value);
   }

   public void Write(String format, Object[] arg)
   {
      Write(String.format(format, arg));
   }

   public void Write(char[] buffer, int index, int count)
   {
      WriteInternal(buffer, index, count);
   }

   public void WriteLine()
   {
      Write("\n");
   }

   public void WriteLine(boolean value)
   {
      Write(Boolean.toString(value));
      WriteLine();
   }

   public void WriteLine(char value)
   {
      WriteInternal("" + value);
      WriteLine();
   }

   public void WriteLine(char[] buffer)
   {
      WriteInternal(buffer, 0, buffer.length);
      WriteLine();
   }

   public void WriteLine(double value)
   {
      WriteInternal(Double.toString(value));
      WriteLine();
   }

   public void WriteLine(float value)
   {
      WriteInternal(Float.toString(value));
      WriteLine();
   }

   public void WriteLine(int value)
   {
      WriteInternal(Integer.toString(value));
      WriteLine();
   }

   public void WriteLine(long value)
   {
      WriteInternal(Long.toString(value));
      WriteLine();
   }

   public void WriteLine(Object value)
   {
      WriteInternal(value.toString());
      WriteLine();
   }

   public void WriteLine(String value)
   {
      WriteInternal(value);
      WriteLine();
   }

   public void WriteLine(String format, Object... arg)
   {
      WriteInternal(String.format(format, arg));
      WriteLine();
   }

   public void WriteLine(char[] buffer, int index, int count)
   {
      WriteInternal(buffer, index, count);
      WriteLine();
   }

   public void WriteSectionSeparator()
   {
      WriteLine(sectionSeparator);
   }

   private void WriteInternal(String value)
   {
      //var bytes = encoding.GetBytes(value);
      //baseStream.Write(bytes, 0, bytes.Length);
      if (!disableOutput)
      {
         System.out.print(value);
      }
   }

   private void WriteInternal(char[] buffer, int index, int count)
   {
      //var bytes = encoding.GetBytes(buffer, index, count);
      //baseStream.Write(bytes, 0, bytes.Length);
      if (!disableOutput)
      {
         System.out.print(new String(buffer, index, count));
      }
   }
}
