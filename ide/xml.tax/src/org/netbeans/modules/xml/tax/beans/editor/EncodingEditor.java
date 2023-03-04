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
package org.netbeans.modules.xml.tax.beans.editor;

import org.netbeans.tax.TreeUtilities;

/**
 *
 * @author  Vladimir Zboril
 * @author  Libor Kramolis
 * @version 0.2
 */
public class EncodingEditor extends NullChoicePropertyEditor {

    /** */
    private static String[] items;


    //
    // init
    //

    /** Creates new EncodingEditor */
    public EncodingEditor () {
        super (getItems());
    }


    //
    // EnhancedPropertyEditor
    //

    /**
     */
    public boolean supportsEditingTaggedValues () {
        return true;
    }


    //
    // itself
    //
    
    /**
     */
    public static String[] getItems () {
        if ( items == null ) {
            String[] engs = (String[]) TreeUtilities.getSupportedEncodings().toArray (new String[0]);
            items = new String[engs.length + 1];
            
            items[0] = DEFAULT_NULL;
            for (int i = 0; i < engs.length; i++) {
                items[i + 1] = engs[i];
            }
        }
        return items;
    }

}
