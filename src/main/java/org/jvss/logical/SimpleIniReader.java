/*
 * Copyright 2009 HPDI, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jvss.logical;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * A very simple .INI file reader that does not require or support sections.
 * TODO use {@link Properties}
 */
public class SimpleIniReader
{
   private final String filename;

   private final Map<String, String> entries = new HashMap<String, String>();

   /**
    * @param filename
    */
   public SimpleIniReader(String filename)
   {
      super();
      this.filename = filename;
   }

   public void parse() throws IOException
   {
      entries.clear();
      BufferedReader in = null;
      try
      {
         in = new BufferedReader(new FileReader(filename));
         String strLine;

         while ((strLine = in.readLine()) != null)
         {
            strLine = strLine.trim();
            if (strLine.length() > 0 && !strLine.startsWith(";"))
            {
               int separator = strLine.indexOf('=');
               if (separator > 0)
               {
                  String key = strLine.substring(0, separator).trim();
                  String value = strLine.substring(separator + 1).trim();
                  entries.put(key, value);
               }
            }
         }
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }

   }

   public Set<String> keys()
   {
      return entries.keySet();
   }

   public String getValue(String key)
   {
      return entries.get(key);
   }

   public String getValue(String key, String defaultValue)
   {
      return entries.containsKey(key) ? entries.get(key) : defaultValue;
   }
}
