/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.ddl.util;

import java.text.ParseException;
import java.util.Enumeration;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.openide.util.MapFormat;

/**
* The message formatter, It handles [] brackets as optional keys.
*
* @author   Slavek Psenicka
*/

public class CommandFormatter {
    /** Parsed items */
    Vector items;

    /** Formats pattern using arguments map
    * Returns formatted string.
    * @param pattern String to be formatted
    * @param arguments Argument map
    */
    public static String format(String pattern, Map arguments) throws ParseException, IllegalArgumentException {
        CommandFormatter temp = new CommandFormatter(pattern);
        return temp.format(arguments);
    }

    /** Constructor
    * @param pattern String to be formatted
    */
    public CommandFormatter(String pattern) throws ParseException {
        this(new StringTokenizer(pattern, "[]", true));
    }

    /** Constructor
    * @param tok Used tokenizer
    */
    private CommandFormatter(StringTokenizer tok) throws ParseException {
        items = scan(tok);
    }

    /** Scans data for [] brackets and prepares data for formatting
    * using MapFormat utility
    */
    private Vector scan(StringTokenizer tok) throws ParseException {
        Vector objvec = new Vector();
        while (tok.hasMoreTokens()) {
            String token = (String) tok.nextElement();
            Object obj = token;
            if (token.equals("["))
                obj = (Object)scan(tok);
            else if (token.equals("]"))
                break;
            objvec.add(obj);
        }

        return objvec;
    }

    /** Formats parsed string using given argument map
    * @param arguments Argument map
    */
    public String format(Map arguments) throws IllegalArgumentException {
        return format(items, arguments);
    }

    /** Formats given string vector using given argument map.
    * Used internally only.
    * @param arguments Argument map
    */
    private String format(Vector itemvec, Map arguments) throws IllegalArgumentException {
        String retstr = "";
        Enumeration items_e = itemvec.elements();
        while (items_e.hasMoreElements()) {
            Object e_item = items_e.nextElement();
            if (e_item instanceof Vector) {
                try {
                    e_item = format((Vector)e_item, arguments);
                } catch (Exception e) {
                    //PENDING
                }
            }

            if (e_item instanceof String) {
                MapFormat fmt = new MapFormat(arguments);
                fmt.setThrowExceptionIfKeyWasNotFound(true);
                String e_msg = fmt.format((String)e_item);
                if (e_msg != null)
                    retstr = retstr + e_msg;
                else
                    throw new IllegalArgumentException();
            }
        }

        return retstr;
    }
}
