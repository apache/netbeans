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
package org.openide.util;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import javax.swing.*;


/** ActionMap that is composed from all Components up to the ExplorerManager.Provider
*
* @author   Jaroslav Tulach
*/
final class UtilitiesCompositeActionMap extends ActionMap {
    private Component component;

    public UtilitiesCompositeActionMap(Component c) {
        this.component = c;
    }

    @Override
    public int size() {
        return keys().length;
    }

    @Override
    public Action get(Object key) {
        Component c = component;

        for (;;) {
            if (c instanceof JComponent) {
                javax.swing.ActionMap m = ((JComponent) c).getActionMap();

                if (m != null) {
                    Action a = m.get(key);

                    if (a != null) {
                        return a;
                    }
                }
            }

            if (c instanceof Lookup.Provider) {
                break;
            }

            c = c.getParent();

            if (c == null) {
                break;
            }
        }

        return null;
    }

    @Override
    public Object[] allKeys() {
        return keys(true);
    }

    @Override
    public Object[] keys() {
        return keys(false);
    }

    private Object[] keys(boolean all) {
        java.util.HashSet<Object> keys = new java.util.HashSet<Object>();

        Component c = component;

        for (;;) {
            if (c instanceof JComponent) {
                javax.swing.ActionMap m = ((JComponent) c).getActionMap();

                if (m != null) {
                    java.util.List<Object> l;

                    Object[] keyList = null;
                    if (all) {
                        keyList = m.allKeys();
                    } else {
                        keyList = m.keys();
                    }

                    if (keyList != null) {
                        keys.addAll(java.util.Arrays.asList(keyList));
                    }
                }
            }

            if (c instanceof Lookup.Provider) {
                break;
            }

            c = c.getParent();

            if (c == null) {
                break;
            }
        }

        return keys.toArray();
    }

    // 
    // Not implemented
    //
    @Override
    public void remove(Object key) {
    }

    @Override
    public void setParent(ActionMap map) {
    }

    @Override
    public void clear() {
    }

    @Override
    public void put(Object key, Action action) {
    }

    @Override
    public ActionMap getParent() {
        return null;
    }
}
