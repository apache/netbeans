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
package org.netbeans.modules.visual.widget;

import org.netbeans.api.visual.widget.Widget;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleStateSet;
import javax.swing.*;
import java.awt.*;
import java.util.Locale;

/**
 * @author David Kaspar
 */
public class WidgetAccessibleContext extends AccessibleContext {

    private Widget widget;

    public WidgetAccessibleContext (Widget widget) {
        this.widget = widget;
    }

    public AccessibleRole getAccessibleRole () {
        return AccessibleRole.UNKNOWN;
    }

    public AccessibleStateSet getAccessibleStateSet () {
        return new AccessibleStateSet ();
    }

    public int getAccessibleIndexInParent () {
        return widget != widget.getScene () ? widget.getParentWidget ().getChildren ().indexOf (widget) : 0;
    }

    public int getAccessibleChildrenCount () {
        return widget.getChildren ().size ();
    }

    public Accessible getAccessibleChild (int i) {
        return widget.getChildren ().get (i);
    }

    public Locale getLocale () throws IllegalComponentStateException {
        JComponent view = widget.getScene ().getView ();
        return view != null ? view.getLocale () : Locale.getDefault ();
    }

    public void notifyChildAdded (Widget parent, Widget child) {
        if (parent == widget)
            firePropertyChange (AccessibleContext.ACCESSIBLE_CHILD_PROPERTY, null, child);
    }

    public void notifyChildRemoved (Widget parent, Widget child) {
        if (parent == widget)
            firePropertyChange (AccessibleContext.ACCESSIBLE_CHILD_PROPERTY, child, null);
    }

}
