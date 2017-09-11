/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
