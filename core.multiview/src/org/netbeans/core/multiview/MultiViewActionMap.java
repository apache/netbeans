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

package org.netbeans.core.multiview;


import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import java.util.Arrays;
import org.openide.windows.TopComponent;


/** ActionMap that delegates to current action map of provided component and dynamically also the current element.
 * Used in <code>MultiViewTopComopnent</code> lookup.
 *
 * @author Milos Kleint
 */
final class MultiViewActionMap extends ActionMap {
    private ActionMap delegate;
    private ActionMap topComponentMap;
    private TopComponent component;
    
    private boolean preventRecursive = false;
    private Object LOCK = new Object();

    public MultiViewActionMap(TopComponent tc, ActionMap tcMap) {
        topComponentMap = tcMap;
        component = tc;
    }
    
    public void setDelegateMap(ActionMap map) {
        delegate = map;
    }
    
    public int size() {
        return keys ().length;
    }

    public Action get(Object key) {
        // the multiview's action map first.. for stuff like the closewindow and clonewindow from TopComponent.initActionMap
        javax.swing.ActionMap m = topComponentMap;
        if (m != null) {
            Action a = m.get (key);
            if (a != null) {
                return a;
            }
        }
        // delegate then
        m = delegate;
        if (m != null) {
            //this is needed because of Tc's DelegateActionMap which traverses up the component hierarchy.
            // .. results in calling this method again and again and again. -> stackoverflow.
            // this should break the evil cycle.
            synchronized (LOCK) {
                if (preventRecursive) {
                    preventRecursive = false;
                    return null;
                }
                Action a;
                preventRecursive = true;
                try {
                    a = m.get (key);
                } finally {
                    preventRecursive = false;
                }
                if (a != null) {
                    return a;
                }
            }
        }
        
        java.awt.Component owner = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        Action found = null;
        try {
            preventRecursive = true;
            while (owner != null && owner != component) {
                if (found == null && (owner instanceof JComponent)) {
                    m = ((JComponent)owner).getActionMap ();
                    if (m != null) {
                        if( m instanceof MultiViewActionMap && ((MultiViewActionMap)m).preventRecursive ) {
                            break;
                        }
                        found = m.get (key);
                    }
                }
                owner = owner.getParent ();
            }
        } finally {
            preventRecursive = false;
        }
        
        return owner == component ? found : null;
    }

    public Object[] allKeys() {
        return keys (true);
    }

    public Object[] keys() {
        return keys (false);
    }


    private Object[] keys(boolean all) {
        java.util.Set keys = new java.util.HashSet();

        if (delegate != null) {
            Object[] delegateKeys;
            if (all) {
                delegateKeys = delegate.allKeys();
            } else {
                delegateKeys = delegate.keys();
            }
            if( null != delegateKeys ) {
                keys.addAll(Arrays.asList(delegateKeys));
            }
        }
        
        if (topComponentMap != null) {
            java.util.List l;

            if (all) {
                l = Arrays.asList (topComponentMap.allKeys ());
            } else {
                l = Arrays.asList (topComponentMap.keys ());
            }

            keys.addAll (l);
        }

        return keys.toArray();
    }

    // 
    // Not implemented
    //
    public void remove(Object key) {
        topComponentMap.remove(key);
    }        

    public void setParent(ActionMap map) {
        topComponentMap.setParent(map);
    }

    public void clear() {
        topComponentMap.clear();
    }

    public void put(Object key, Action action) {
        topComponentMap.put (key, action);
    }

    public ActionMap getParent() {
        return topComponentMap.getParent();
    }
 
}    
