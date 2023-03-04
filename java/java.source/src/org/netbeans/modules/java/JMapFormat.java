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
