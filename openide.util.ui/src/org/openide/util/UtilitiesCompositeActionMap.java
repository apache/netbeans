/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

    public int size() {
        return keys().length;
    }

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

    public Object[] allKeys() {
        return keys(true);
    }

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
    public void remove(Object key) {
    }

    public void setParent(ActionMap map) {
    }

    public void clear() {
    }

    public void put(Object key, Action action) {
    }

    public ActionMap getParent() {
        return null;
    }
}
