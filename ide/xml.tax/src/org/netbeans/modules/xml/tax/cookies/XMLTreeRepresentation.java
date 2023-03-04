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
package org.netbeans.modules.xml.tax.cookies;

import org.xml.sax.*;

import org.openide.*;

import org.netbeans.tax.*;
import org.netbeans.modules.xml.sync.*;

/**
 * Manages relations between tree model ant tree editor.
 *
 * @author  Petr Kuzel
 * @version
 */
public class XMLTreeRepresentation extends TreeRepresentation {

    /** Creates new XMLTreeRepresentation */
    public XMLTreeRepresentation(TreeEditorCookieImpl editor, Synchronizator sync) {
        super(editor, sync);
    }    

    /**
     * Update the representation without marking it as modified.
     */
    public void update(Object change) {

        if (change instanceof InputSource) {
            InputSource update = (InputSource) change;
            editor.updateTree(update);
        } else {
            throw new RuntimeException("TreeRepresentation does not support: " + change.getClass()); // NOI18N
        }
    }


    /**
     * Is this representation modified since last sync?
     */
    public boolean isModified() {
        return false;
    }
    
}
