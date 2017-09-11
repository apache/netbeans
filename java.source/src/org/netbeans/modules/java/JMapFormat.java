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

package org.netbeans.modules.java;

import java.util.*;

import org.openide.util.MapFormat;

/**
* The message formatter, which uses map's keys in place of numbers.
* This class extends functionality of MessageFormat by allowing the user to
* specify strings in place of MessageFormatter's numbers. It then uses given
* map to translate keys to values. It also handles conditional expressions.
*
* You will usually use this formatter as follows:
* 	<code>JMapFormat.format("Hello {name}", map);</code>
*
* For conditional expression:
*       <code>JMapFormat.format("Hello {name,old_suffix,new_suffix,no_match}"); </code>
*  Last three parameters are optional. At first, name is translated. If it then has
*  suffix old_suffix it is replaced by te new_suffix else whole parrent is replaced by
*  no_match string.
*  
* Notes: if map does not contain value for key specified, it substitutes
* the value by <code>"null"</code> word to qualify that something goes wrong.
*
* @author   Slavek Psenicka, Martin Ryzl
* @version  1.0, March 11. 1999
*/

public class JMapFormat extends MapFormat {

    private String cdel = ","; // NOI18N

    static final long serialVersionUID =5503640004816201285L;
    public JMapFormat(Map map) {
        super(map);
    }

    protected Object processKey(String key) {
        //    System.out.println("key = " + key); // NOI18N
        StringTokenizer st = new StringTokenizer(key, cdel, true);
        String data[] = new String[4];
        String temp;

        for(int i = 0; (i < 4) && st.hasMoreTokens(); ) {
            temp = st.nextToken();
            if (temp.equals("$")) i++; // NOI18N
            else data[i] = temp;
        }

        Object obj = super.processKey(data[0]);
        if (obj instanceof String) {
            if (data[1] != null) {
                String name = (String)obj;
                if (name.endsWith(data[1])) {
                    if (data[2] == null) data[2] = ""; // NOI18N
                    return name.substring(0, name.length() - data[1].length()) + data[2];
                } else {
                    if (data[3] != null) return data[3];
                    else return obj;
                }
            }
        }
        return obj;
    }

    public String getCondDelimiter() {
        return cdel;
    }

    public void setCondDelimiter(String cdel) {
        this.cdel = cdel;
    }

}
