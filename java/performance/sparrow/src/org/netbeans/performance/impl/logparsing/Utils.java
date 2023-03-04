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
/*
 * Utils.java
 *
 * Created on October 8, 2002, 3:04 PM
 */

package org.netbeans.performance.impl.logparsing;
import java.util.*;
import org.netbeans.performance.impl.*;


/** Miscellaneous utility methods for parsing logs.
 *
 * @author  Tim Boudreau
 */
public final class Utils {

    /**Utility method to split a string into multiple pieces based on a separator string.
     * Throws an illegal argument exception if the separator is not
     * present in the passed value string.  Useful for handling name
     * value pairs and the like.
     */
    public static final String[] splitString (String value, String sp) {
        if (value.indexOf (sp) == -1) {
            throw new IllegalArgumentException ("Can't split string " + value + " on character " + sp);
        }
        ArrayList results = new ArrayList (3);
        StringTokenizer sk = new StringTokenizer (value, sp);
        while (sk.hasMoreElements()) {
            results.add (sk.nextElement());
        }
        String[] result = new String[results.size()];
        result = (String[]) results.toArray (result);
        return result;
    }
    
    /**Utility method to split a string into two pieces based on a separator string.
     * Throws an illegal argument exception if the separator is not
     * present in the passed value string.  Useful for handling name
     * value pairs and the like.
     */
    public static final String[] splitStringInTwo (String value, String sp) {
        String[] result = new String[2];
        int idx = value.indexOf (sp);
        if (idx == -1) throw new IllegalArgumentException ("String \"" + value + "\" does not contain the separator \"" + sp + "\"");
        result[0] = value.substring (0,idx);
        result[1] = value.substring (idx + sp.length());
        return result;
    }
}
