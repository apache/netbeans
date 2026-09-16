/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.options.keymap;

import java.util.ArrayList;
import java.util.List;

public class XMLStorage {
    
    static StringBuffer generateHeader () {
        StringBuffer sb = new StringBuffer ();
        sb.append ("<?xml version=\"1.0\"?>\n\n");
        return sb;
    }
    
    static void generateFolderStart (
        StringBuffer sb, 
        String name, 
        Attribs attributes, 
        String indentation
    ) {
        sb.append (indentation).append ('<').append (name);
        if (attributes != null) {
            if (!attributes.oneLine) sb.append ('\n');
            else sb.append (' ');
            generateAttributes (sb, attributes, indentation + "    ");
            if (!attributes.oneLine) sb.append (indentation);
            sb.append (">\n");
        } else
            sb.append (">\n");
    }
    
    static void generateFolderEnd (StringBuffer sb, String name, String indentation) {
        sb.append (indentation).append ("</").append (name).append (">\n");
    }
    
    static void generateLeaf (
        StringBuffer sb, 
        String name, 
        Attribs attributes, 
        String indentation
    ) {
        sb.append (indentation).append ('<').append (name);
        if (attributes != null) {
            if (!attributes.oneLine) sb.append ('\n');
            else sb.append (' ');
            generateAttributes (sb, attributes, indentation + "    ");
            if (!attributes.oneLine) sb.append (indentation);
            sb.append ("/>\n");
        } else
            sb.append ("/>\n");
    }
    
    private static void generateAttributes (
        StringBuffer sb, 
        Attribs attributes, 
        String indentation
    ) {
        if (attributes == null) return;
        int i, k = attributes.names.size ();
        for (i = 0; i < k; i++) {
            if (!attributes.oneLine)
                sb.append (indentation);
            sb.append (attributes.names.get (i)).append ("=\"").
                append (attributes.values.get (i)).append ('\"');
            if (!attributes.oneLine)
                sb.append ("\n");
            else
            if (i < (k - 1))
                sb.append (' ');
        }
    }
    
    static class Attribs {
        private List<String> names = new ArrayList<String> ();
        private List<String> values = new ArrayList<String> ();
        private boolean oneLine;
        
        Attribs (boolean oneLine) {
            this.oneLine = oneLine;
        }
        
        void add (String name, String value) {
            int i = names.indexOf (name);
            if (i >= 0) {
                names.remove (i);
                values.remove (i);
            }
            names.add (name);
            values.add (value);
        }
        
        void clear () {
            names.clear ();
            values.clear ();
        }
    }
}
