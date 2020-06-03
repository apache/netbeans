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
