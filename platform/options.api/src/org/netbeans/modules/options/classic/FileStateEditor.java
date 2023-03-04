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

package org.netbeans.modules.options.classic;

import org.netbeans.beaninfo.editors.ListImageEditor;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.explorer.propertysheet.PropertyEnv;

import java.lang.reflect.InvocationTargetException;

import org.netbeans.modules.options.classic.SettingChildren.FileStateProperty;

/**
 *
 * @author  Vitezslav Stejskal
 */
class FileStateEditor extends ListImageEditor {

    private String action_define = null;
    private String action_revert = null;
    private String action_delete = null;
    
    private Node.Property<?> prop = null;

    /** Creates new FileStatePropertyEditor */
    public FileStateEditor () {
        super ();
        
        action_define = NbBundle.getMessage (FileStateEditor.class, "LBL_action_define");
        action_revert = NbBundle.getMessage (FileStateEditor.class, "LBL_action_revert");
        action_delete = NbBundle.getMessage (FileStateEditor.class, "LBL_action_delete");
    }

    @Override
    public void attachEnv (PropertyEnv env) {
        super.attachEnv (env);
        
        try {
            prop = (Node.Property)env.getFeatureDescriptor ();
        } catch (ClassCastException cce) {
            ClassCastException cce2 = new ClassCastException("Expected a Node.Property but got a " + env.getFeatureDescriptor() + " descriptor " + env.getFeatureDescriptor().getClass().getName());
            throw cce2;
        }
    }
    
    @Override
    public String getAsText () {
        return null;
    }

    @Override
    public void setAsText (String str) throws java.lang.IllegalArgumentException {        
        try {
            Integer value = null;
            if (action_define.equals (str)) {
                value  = Integer.valueOf (SettingChildren.FileStateProperty.ACTION_DEFINE);
            }
            if (action_revert.equals (str)) {
                value = Integer.valueOf (FileStateProperty.ACTION_REVERT);
            }
            if (action_delete.equals (str)) {
                value = Integer.valueOf (FileStateProperty.ACTION_DELETE);                
            }
            if (value != null) {
                doSetValue(prop, value);
                super.setValue(value);                
            }
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }

    private <T> void doSetValue(Node.Property<T> prop, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        prop.setValue(prop.getValueType().cast(value));
    }
    
    @Override
    public String[] getTags () {
        Integer val = (Integer) getValue ();

        if (SettingChildren.PROP_LAYER_MODULES.equals (prop.getName ())) {
            return new String [] {
                action_revert
            };
        }
        if (val != null &&
            val.intValue () == FileStateManager.FSTATE_IGNORED) {
            return new String [] {
                action_define,
                action_revert,
                action_delete
            };
        }
        return new String [] {
            action_define
        };
    }
}
