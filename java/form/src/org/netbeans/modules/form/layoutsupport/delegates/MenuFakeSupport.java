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

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.form.codestructure.CodeGroup;
import org.netbeans.modules.form.layoutsupport.AbstractLayoutSupport;

/**
 * Fake layout support for Swing menus allowing to treat menu components as
 * visual components/containers and generate correct code. Does not support dragging.
 */
public class MenuFakeSupport extends AbstractLayoutSupport {

    @Override
    public Class getSupportedClass() {
        return null;
    }

    @Override
    public boolean isDedicated() {
        return true;
    }

    @Override
    public boolean shouldHaveNode() {
        return false;
    }

    @Override
    protected void readLayoutCode(CodeGroup layoutCode) {
    }

    @Override
    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        for (int i=0; i < components.length; i++) {
            containerDelegate.add(components[i], i + index);
            // Issue 110587 - the default background of menu depends (on some l&f)
            // on the location of the menu (if it is top-level menu or not).
            // The background is changed when "ancestor" property change event
            // is fired. This usually happens when addNotify() is called.
            // Unfortunately, addNotify() is not called if the parent doesn't
            // have peer - this happens for model instances. The following
            // code fires the property change event explicitly to force
            // update of the background.
            PropertyChangeEvent event = new PropertyChangeEvent(components[i], "ancestor", null, container); // NOI18N
            for (PropertyChangeListener listener : components[i].getPropertyChangeListeners()) {
                listener.propertyChange(event);
            }
        }
    }
}
