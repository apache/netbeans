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

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import java.beans.FeatureDescriptor;

import org.openide.nodes.Node;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
abstract class NullChoicePropertyEditor extends NullStringEditor implements EnhancedPropertyEditor {

    /** */
    private String[] tags;


    //
    // init
    //

    /** Creates new NullChoicePropertyEditor */
    public NullChoicePropertyEditor (String[] tags) {
        super();

        this.tags = tags;
    }


    //
    // PropertyEditor
    //    
    
    /**
     */
    public boolean supportsCustomEditor () {
        return false;
    }
    
    /**
     */
    public String[] getTags () {
        return tags;
    }
    

    //
    // EnhancedPropertyEditor
    //
    
    /**
     */
    public boolean hasInPlaceCustomEditor () {
        return false;
    }

    /**
     */
    public Component getInPlaceCustomEditor () {
        return null;
    }

    /**
     */
    public boolean supportsEditingTaggedValues () {
        return false;
    }

}
