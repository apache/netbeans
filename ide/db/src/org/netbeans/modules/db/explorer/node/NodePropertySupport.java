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

package org.netbeans.modules.db.explorer.node;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author Rob Englander
 */
public class NodePropertySupport extends PropertySupport {
    public static final String CUSTOM_EDITOR = "NodePropertySupport.customEditor"; //NOI18N
    public static final String NODE = "NodePropertySupport.Node";       //NOI18N

    private BaseNode node;
    private String key;

    public NodePropertySupport(BaseNode node, String name, Class type, String displayName, String shortDescription, boolean writable) {
        super(name, type, displayName, shortDescription, true, writable);
        key = name;
        this.node = node;
        setValue(NODE, node);
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        Object result = node.getPropertyValue(key);
        if (result == null) {
            result = ""; // NOI18N
        }

        return result;
    }

    @Override
    public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        node.setPropertyValue(this, val);
    }

    /**
     * PropertyEditor can be set via setValue - it can be either instanciated or
     * a Class, that has a Default-Constructor and results in an object, that
     * implements PropertyEditor
     *
     * @return
     */
    @Override
    public PropertyEditor getPropertyEditor() {
        PropertyEditor result = null;
        Object potentialEditor = getValue(CUSTOM_EDITOR);

        if (potentialEditor instanceof PropertyEditor) {
            result = (PropertyEditor) potentialEditor;
        } else if (potentialEditor instanceof Class) {
            try {
                potentialEditor = ((Class) potentialEditor).getDeclaredConstructor().newInstance();
                if (!(potentialEditor instanceof PropertyEditor)) {
                    throw new IllegalArgumentException(
                            "Editor class does not derive from property editor"); //NOI18N
}
                return (PropertyEditor) potentialEditor;
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
        if (result == null) {
            result = super.getPropertyEditor();
        }
        return result;
    }
}
