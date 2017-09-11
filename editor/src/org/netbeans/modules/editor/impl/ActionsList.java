/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.editor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JSeparator;
import org.openide.awt.AcceleratorBinding;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Utilities;

/**
 *
 * @author Vita Stejskal
 */
public class ActionsList {

    // -J-Dorg.netbeans.modules.editor.impl.ActionsList.level=FINE
    private static final Logger LOG = Logger.getLogger(ActionsList.class.getName());
    
    private final List<Object> all;
    private final List<Action> actions;

    /**
     * Create a new <code>ActionList</code> instance. The <code>ActionList</code>
     * converts a list of objects (keys) to the list of <code>Action</code>s
     * or other instances that can potentially be used in actions based UI such
     * as popup menus, toolbars, etc. The other instances can be anything, but
     * usually they are things like <code>JSeparator</code>, <code>DataFolder</code>
     * or plain <code>String</code> with the name of an editor action.
     * 
     * @param keys The list of objects to convert to <code>Action</code>s
     * @param ignoreFolders <code>true</code> if the conversion should skipp folders
     * @param prohibitSeparatorsAndActionNames Treat separators and references to actions
     *  by using their Action.NAME as errors. This is useful for
     */
    protected ActionsList(List<FileObject> keys, boolean ignoreFolders,
            boolean prohibitSeparatorsAndActionNames)
    {
        Pair p = convertImpl(keys == null ? Collections.<FileObject>emptyList() : keys, ignoreFolders,
                prohibitSeparatorsAndActionNames);
        this.all = p.all;
        this.actions = p.actions;
    }

    public List<Object> getAllInstances() {
        return all;
    }

    public List<Action> getActionsOnly() {
        return actions;
    }

    public static List<Object> convert(List<FileObject> keys, boolean prohibitSeparatorsAndActionNames) {
        return convertImpl(keys, false, prohibitSeparatorsAndActionNames).all;
    }
    
    private static class Pair {
        List<Object> all;
        List<Action> actions;
    }
    
    private static Pair convertImpl(List<FileObject> keys, boolean ignoreFolders,
            boolean prohibitSeparatorsAndActionNames)
    {
        List<Object> all = new ArrayList<Object>();
        List<Action> actions = new ArrayList<Action>();

        for (FileObject item : keys) {
            DataObject dob;
            try {
                dob = DataObject.find(item);
                if (dob == null && prohibitSeparatorsAndActionNames) {
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.info("ActionsList: DataObject is null for item=" + item + "\n"); //NOI18N
                    }
                }
            } catch (DataObjectNotFoundException dnfe) {
                if (prohibitSeparatorsAndActionNames) {
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.info("ActionsList: DataObject not found for item=" + item + "\n"); //NOI18N
                    }
                } else {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "DataObject not found for action fileObject=" + item); //NOI18N
                    }
                }
                continue; // ignore
            }

            Object toAdd = null;
            InstanceCookie ic = dob.getLookup().lookup(InstanceCookie.class);
            if (prohibitSeparatorsAndActionNames && ic == null) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("ActionsList: InstanceCookie not found for item=" + item + "\n"); //NOI18N
                }
                continue;
            }
            if (ic != null) {
                try {
                    if (!isSeparator(ic)) {
                        toAdd = ic.instanceCreate();
                        if (toAdd == null && prohibitSeparatorsAndActionNames) {
                            if (LOG.isLoggable(Level.INFO)) {
                                LOG.info("ActionsList: InstanceCookie.instanceCreate() null for item=" + item + "\n"); //NOI18N
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.log(Level.INFO, "Can't instantiate object", e); //NOI18N
                    continue;
                }
            } else if (dob instanceof DataFolder) {
                toAdd = dob;
            } else {
                toAdd = dob.getName();
            }

            // Filter out the same succeding items
            if (all.size() > 0) {
                Object lastOne = all.get(all.size() - 1);
                if (Utilities.compareObjects(lastOne, toAdd)) {
                    continue;
                }
                if (isSeparator(lastOne) && isSeparator(toAdd)) {
                    continue;
                }
            }
            
            if (toAdd instanceof Action) {
                Action action = (Action) toAdd;
                actions.add(action);
                AcceleratorBinding.setAccelerator(action, item);
            } else if (isSeparator(toAdd)) {
                if (prohibitSeparatorsAndActionNames) {
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.info("ActionsList: Separator for item=" + item + "\n"); //NOI18N
                    }
                }
                actions.add(null);
            }
            all.add(toAdd);
        }

        Pair p = new Pair();
        p.all = all.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(all);
        p.actions = actions.isEmpty() ? Collections.<Action>emptyList() : Collections.unmodifiableList(actions);
        return p;
    }
    
    private static boolean isSeparator(Object o) {
        return o == null || o instanceof JSeparator;
    }
    
    private static boolean isSeparator(InstanceCookie o) throws Exception {
        return (o instanceof InstanceCookie.Of && ((InstanceCookie.Of) o).instanceOf(JSeparator.class))
            || JSeparator.class.isAssignableFrom(o.instanceClass());
    }
}
