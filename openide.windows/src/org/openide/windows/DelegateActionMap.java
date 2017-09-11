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
package org.openide.windows;

import java.awt.Component;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import org.netbeans.modules.openide.windows.GlobalActionContextImpl;


// This is almost copy of org.openide.util.UtilitiesCompositeActionMap.

/** ActionMap that delegates to current action map of provided component.
 * Used in <code>TopComopnent</code> lookup.
 * <p><b>Note: This action map is 'passive', i.e putting new mappings
 * into it makes no effect. Could be changed later.</b>
 *
 * @author Peter Zavadsky
 */
final class DelegateActionMap extends ActionMap {
    private Reference<JComponent> component;
    private ActionMap delegate;

    public DelegateActionMap(JComponent c) {
        setComponent(c);
    }

    public DelegateActionMap(TopComponent c, ActionMap delegate) {
        setComponent(c);
        this.delegate = delegate;
    }

    @Override
    public int size() {
        return keys().length;
    }

    @Override
    public Action get(Object key) {
        ActionMap m;

        if (delegate == null) {
            JComponent comp = getComponent();
            if (comp == null) {
                m = null;
            } else {
                m = comp.getActionMap();
            }
        } else {
            m = delegate;
        }

        if (m != null) {
            Action a = m.get(key);

            if (a != null) {
                return a;
            }
        }

        Component owner = GlobalActionContextImpl.findFocusOwner();
        Action found = null;

        while ((owner != null) && (owner != getComponent())) {
            if ((found == null) && (owner instanceof JComponent)) {
                m = ((JComponent) owner).getActionMap();

                if (m != null) {
                    found = m.get(key);
                }
            }

            owner = owner.getParent();
        }

        return (owner == getComponent()) ? found : null;
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
        Set<Object> keys = new HashSet<Object>();

        
        ActionMap m;

        if (delegate == null) {
            JComponent comp = getComponent();
            if (comp == null) {
                m = null;
            } else {
                m = comp.getActionMap();
            }
        } else {
            m = delegate;
        }

        if (m != null) {
            List<Object> l;

            if (all) {
                Object[] allKeys = m.allKeys();
                if( null == allKeys ) {
                    l = Collections.EMPTY_LIST;
                } else {
                    l = Arrays.asList(m.allKeys());
                }
            } else {
                l = Arrays.asList(m.keys());
            }

            keys.addAll(l);
        }
        
        Component owner = GlobalActionContextImpl.findFocusOwner();
        List<JComponent> hierarchy = new ArrayList<JComponent>();
        while ((owner != null) && (owner != getComponent())) {
            if (owner instanceof JComponent) {
                hierarchy.add((JComponent)owner);
            }
            owner = owner.getParent();
        }
        if (owner == getComponent()) {
            for (JComponent c : hierarchy) {
                ActionMap am = c.getActionMap();
                if (am == null) {
                    continue;
                }
                Object[] fk = all ? am.allKeys() : am.keys();
                if (fk != null) {
                    keys.addAll(Arrays.asList(fk));
                }
            }
        }
        return keys.toArray();
    }

    // 
    // Not implemented
    //
    @Override
    public void remove(Object key) {
        if (delegate != null) {
            delegate.remove(key);
        }
    }

    @Override
    public void setParent(ActionMap map) {
        if (delegate != null) {
            delegate.setParent(map);
            GlobalActionContextImpl.blickActionMap(new ActionMap());
        }
    }

    @Override
    public void clear() {
        if (delegate != null) {
            delegate.clear();
        }
    }

    @Override
    public void put(Object key, Action action) {
        if (delegate != null) {
            delegate.put(key, action);
        }
    }

    @Override
    public ActionMap getParent() {
        return (delegate == null) ? null : delegate.getParent();
    }

    @Override
    public String toString() {
        return super.toString() + " for " + this.getComponent();
    }

    JComponent getComponent() {
        return component.get();
    }

    private void setComponent(JComponent component) {
        this.component = new WeakReference<JComponent>(component);
    }
}
