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

import java.util.regex.Pattern;

/**
 * Determines whether a path matches a set of glob patterns.
 */
public class PathMatcher
{
   public final static String AnyPathPattern = "**";

   public final static String AnyNamePattern = "*";

   public final static String AnyNameCharPattern = "?";

   private static final char[] directorySeparators = {'/', '\\'};

   private final Pattern regex;

   public PathMatcher(String pattern)
   {
      //       regex = new Regex(ConvertPattern(pattern),
      //           RegexOptions.IgnoreCase | RegexOptions.Singleline);
      regex = Pattern.compile(ConvertPattern(pattern), Pattern.CASE_INSENSITIVE);
   }

   public PathMatcher(String[] patterns)
   {
      regex = Pattern.compile(ConvertPatterns(patterns), Pattern.CASE_INSENSITIVE);
   }

   public boolean Matches(String path)
   {
      return regex.matcher(path).matches();
   }

   private static String ConvertPattern(String glob)
   {
      StringBuilder buf = new StringBuilder(glob.length() * 2);
      ConvertPatternInto(glob, buf);
      return buf.toString();
   }

   private static String ConvertPatterns(String[] globs)
   {
      StringBuilder buf = new StringBuilder();
      for (String glob : globs)
      {
         if (buf.length() > 0)
         {
            buf.append('|');
         }
         ConvertPatternInto(glob, buf);
      }
      return buf.toString();
   }

   private static void ConvertPatternInto(String glob, StringBuilder buf)
   {
      for (int i = 0; i < glob.length(); ++i)
      {
         char c = glob.charAt(i);
         switch (c)
         {
            case '.' :
            case '$' :
            case '^' :
            case '{' :
            case '[' :
            case '(' :
            case '|' :
            case ')' :
            case '+' :
               // escape regex operators
               buf.append('\\');
               buf.append(c);
               break;
            case '/' :
            case '\\' :
               // accept either directory separator
               buf.append("[/\\]");
               break;
            case '*' :
               if (i + 1 < glob.length() && glob.charAt(i + 1) == '*')
               {
                  // match any path
                  buf.append(".*");
                  ++i;
               }
               else
               {
                  // match any name
                  buf.append("[^/\\]*");
               }
               break;
            case '?' :
               // match any name char
               buf.append("[^/\\]");
               break;
            default :
               // passthrough char
               buf.append(c);
               break;
         }
      }
      buf.append('$');
   }
}
