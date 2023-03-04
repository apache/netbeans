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
package org.netbeans.modules.xml.text;

import org.netbeans.modules.xml.*;
import org.netbeans.modules.xml.sync.*;

/**
 * Takes care about specifics of DTD text representation.
 * I.e. no update() is implemented.
 *
 * @author  Petr Kuzel
 * @version
 */
public class DTDTextRepresentation extends TextRepresentation {

    /** Creates new DTDTextRepresentation */
    public DTDTextRepresentation(TextEditorSupport editor, Synchronizator sync) {
        super(editor, sync);
    }

    /**
     * Update the representation without marking it as modified.
     */
    public void update(Object change) {

        if (change instanceof String) {
            String update = (String) change;
            updateText(update);
        }
    }
    
    /** Update tree from selected source.  */
    private void updateText(Object source) {
        //!!! tree is read only
    }
    
    /**
     * Is this representation modified since last sync?
     */
    public boolean isModified() {
        return false; //es.isModified();
    }
        
}
