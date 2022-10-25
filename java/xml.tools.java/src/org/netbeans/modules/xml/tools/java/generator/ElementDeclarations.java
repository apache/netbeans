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
package org.netbeans.modules.xml.tools.java.generator;

import java.util.*;

import org.netbeans.tax.*;

/**
 * Maps element declaration name => Entry.
 *
 * @author  Petr Kuzel
 * @version
 */
public class ElementDeclarations extends HashMap {

    /** Serial Version UID */
    private static final long serialVersionUID =2385299250969298335L;

    /**
     * Creates new ElementDeclarations from TreeElementDecl iterator.
     */
    public ElementDeclarations(Iterator<TreeElementDecl> it) {
        if (it == null) return;
        while (it.hasNext()) {
            TreeElementDecl next = (TreeElementDecl) it.next();
            put(next.getName(), new Entry(next.allowText(), next.allowElements()));
        }
    }
    
    /**
     * Get Entry by declaration name.
     */
    public final Entry getEntry(String element) {
        return (Entry) get(element);
    }
    
    /**
     * Entry represents one value keyed by element declaration name. 
     */
    public static class Entry {

        public static final int EMPTY = 0;
        public static final int DATA = 1;
        public static final int CONTAINER = 2;
        public static final int MIXED = 3;
        
        private int type;
        
        public Entry(boolean at, boolean ae) {
            type = at ? DATA : 0;
            type += ae ? CONTAINER : 0;
        }

        public int getType() {
            return type;
        }
    }
}
