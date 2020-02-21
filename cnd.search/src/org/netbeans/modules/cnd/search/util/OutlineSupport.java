/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.search.util;

import java.lang.ref.WeakReference;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import org.netbeans.swing.etable.ETable;
import org.netbeans.swing.etable.ETableColumnModel;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 */
public final class OutlineSupport implements TableColumnModelListener {

    private static final HashMap<Class<?>, WeakReference<OutlineSupport>> cache = new HashMap<Class<?>, WeakReference<OutlineSupport>>();
    private final Properties props = new Properties();
    private final RequestProcessor.Task saveTask;

    public static OutlineSupport get(Class<?> clazz) {
        OutlineSupport support;

        synchronized (cache) {
            WeakReference<OutlineSupport> ref = cache.get(clazz);
            if (ref != null) {
                support = ref.get();
                if (support != null) {
                    return support;
                }
            }

            support = new OutlineSupport(clazz);
            cache.put(clazz, new WeakReference<OutlineSupport>(support));
        }

        return support;
    }

    private OutlineSupport(final Class<?> clazz) {
        try {
            Preferences prefs = NbPreferences.forModule(clazz).node("OutlineSupport"); // NOI18N
            for (String key : prefs.keys()) {
                props.put(key, prefs.get(key, "")); // NOI18N
            }
        } catch (BackingStoreException ex) {
//            Exceptions.printStackTrace(ex);
        }

        saveTask = RequestProcessor.getDefault().create(new Runnable() {

            @Override
            public void run() {
                Preferences prefs = NbPreferences.forModule(clazz).node("OutlineSupport"); // NOI18N
                Properties toSave;

                synchronized (props) {
                    toSave = new Properties();
                    toSave.putAll(props);
                }

                for (Map.Entry<Object, Object> entry : toSave.entrySet()) {
                    prefs.put((String) entry.getKey(), (String) entry.getValue());
                }
                
                try {
                    prefs.flush();
                } catch (BackingStoreException ex) {
//                    Exceptions.printStackTrace(ex);
                }
            }
        }, true);
    }

    @Override
    public void columnAdded(TableColumnModelEvent e) {
        update(e);
    }

    @Override
    public void columnRemoved(TableColumnModelEvent e) {
        update(e);
    }

    @Override
    public void columnMoved(TableColumnModelEvent e) {
        update(e);
    }

    @Override
    public void columnMarginChanged(ChangeEvent e) {
        update(e);
    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e) {
    }

    private void update(EventObject e) {
        Object source = e.getSource();
        if (source instanceof ETableColumnModel) {
            ETableColumnModel model = (ETableColumnModel) source;
            synchronized (props) {
                model.writeSettings(props, ""); // NOI18N
            }
        }

        saveTask.schedule(1000 * 5);
    }

    public void installFor(ETable table) {
        ETableColumnModel columnsModel = (ETableColumnModel) table.getColumnModel();
        columnsModel.addColumnModelListener(WeakListeners.create(TableColumnModelListener.class, this, columnsModel));
        synchronized (props) {
            if (!props.isEmpty()) {
                columnsModel.readSettings(props, "", table); // NOI18N
            }
        }
    }
}
