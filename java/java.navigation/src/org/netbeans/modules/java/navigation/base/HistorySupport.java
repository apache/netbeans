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
package org.netbeans.modules.java.navigation.base;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.lang.model.element.TypeElement;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ElementHandle;
import org.openide.util.Mutex;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
public class HistorySupport {


    public static final String HISTORY = "history"; //NOI18N
    private static final int HISTORY_LENGTH = 25;

    //@GuardedBy("HistorySupport.class")
    private static Map<Class<?>,HistorySupport> instances = new HashMap<Class<?>, HistorySupport>();

    private final PropertyChangeSupport suppot;
    //@GuardedBy("this")
    private final Deque<Pair<URI, ElementHandle<TypeElement>>> history;

    private HistorySupport() {
        this.suppot = new PropertyChangeSupport(this);
        this.history = new ArrayDeque<Pair<URI, ElementHandle<TypeElement>>>();
    }

    public void addToHistory(@NonNull final Pair<URI, ElementHandle<TypeElement>> pair) {
        synchronized (this) {
            if (history.size() == HISTORY_LENGTH) {
                history.removeLast();
            }
            boolean contains = false;
            for (Pair<URI,ElementHandle<TypeElement>> p : history) {
                if (p.equals(pair)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                history.addFirst(pair);
            }
        }
        suppot.firePropertyChange(HISTORY, null, null);
    }

    @NonNull
    public synchronized List<? extends Pair<URI, ElementHandle<TypeElement>>> getHistory() {
        final SortedSet<Pair<URI, ElementHandle<TypeElement>>> sorted = new TreeSet<Pair<URI, ElementHandle<TypeElement>>>(new SimpleNameAndPackageComparator());
        for (Pair<URI,ElementHandle<TypeElement>> p : history) {
            sorted.add(p);
        }
        return Collections.unmodifiableList(new ArrayList<Pair<URI, ElementHandle<TypeElement>>>(sorted));
    }

    public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        assert listener != null;
        suppot.addPropertyChangeListener(listener);
    }


    public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        assert listener != null;
        suppot.removePropertyChangeListener(listener);
    }

    @NonNull
    public static synchronized HistorySupport getInstnace(@NonNull final Class<?> forClass) {
        Parameters.notNull("forClass", forClass);       //NOI18N
        HistorySupport history = instances.get(forClass);
        if (history == null) {
            history = new HistorySupport();
            instances.put(forClass, history);
        }
        return history;
    }

    public static ComboBoxModel createModel(
            @NonNull final HistorySupport support,
            @NullAllowed final String emptyMessage) {
        return new HistoryModel(support, emptyMessage);
    }


    public static ListCellRenderer createRenderer(@NonNull final HistorySupport support) {
        return new HistoryRenderer();
    }


    private static String getSimpleName(@NonNull final String fqn) {
        final int sepIndex = splitIndex(fqn);
        return sepIndex >= 0 ?
            fqn.substring(sepIndex+1):
            fqn;
    }

    private static String getEnclosing(@NonNull final String fqn) {
        final int sepIndex = splitIndex(fqn);
        return sepIndex >= 0 ?
            fqn.substring(0, sepIndex) :
            ""; //NOI18N
    }

    private static int splitIndex(String fqn) {
        int sepIndex = fqn.lastIndexOf('$');   //NOI18N
        if (sepIndex == -1) {
            sepIndex = fqn.lastIndexOf('.');   //NOI18N
        }
        return sepIndex;
    }

    private static class HistoryRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String toolTipText = null;
            if (value instanceof Pair && ((Pair)value).second() instanceof ElementHandle) {
                final String fqn =  ((ElementHandle)((Pair)value).second()).getQualifiedName();
                value = getSimpleName(fqn);
                toolTipText = fqn;
            }
            final Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setToolTipText(toolTipText);
            return c;
        }
    }

    private static class HistoryModel implements ComboBoxModel, PropertyChangeListener {

        private final List<ListDataListener> listeners;
        private final HistorySupport history;
        private final String emptyMessage;
        //@GuardedBy("this")
        private List<?> cache;
        private Object selectedItem;

        HistoryModel(
                @NonNull final HistorySupport history,
                @NonNull final String emptyMessage) {
            Parameters.notNull("history", history); //NOI18N
            listeners = new CopyOnWriteArrayList<ListDataListener>();
            this.history = history;
            this.emptyMessage = emptyMessage;
            this.history.addPropertyChangeListener(WeakListeners.propertyChange(this, history));
        }

        @Override
        public void setSelectedItem(Object anItem) {
            if (selectedItem == null ? anItem != null : !selectedItem.equals(anItem)) {
                this.selectedItem = anItem;
                fire();
            }
        }

        @Override
        public Object getSelectedItem() {
            return selectedItem;
        }

        @Override
        public int getSize() {
            return getCache().size();
        }

        @Override
        public Object getElementAt(int index) {
            return getCache().get(index);
        }

        @Override
        public void addListDataListener(@NonNull final ListDataListener l) {
            assert l != null;
            listeners.add(l);
        }

        @Override
        public void removeListDataListener(@NonNull final ListDataListener l) {
            assert l != null;
            listeners.remove(l);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (HISTORY.equals(evt.getPropertyName())) {
                synchronized (this) {
                    cache = null;
                }
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run() {
                        fire();
                    }
                });
            }
        }

        private void fire() {
            final ListDataEvent event = new ListDataEvent(
                    this,
                    -1,
                    -1,
                    Integer.MAX_VALUE);
            for (ListDataListener l : listeners) {
                l.contentsChanged(event);
            }
        }


        @NonNull
        private synchronized List<?> getCache() {
            if (cache == null) {
                cache = history.getHistory();
                if (cache.isEmpty() && emptyMessage != null) {
                    cache = Collections.singletonList(emptyMessage);
                    setSelectedItem(emptyMessage);
                }
            }
            return cache;
        }

    }

    private static class SimpleNameAndPackageComparator implements Comparator<Pair<URI,ElementHandle<TypeElement>>> {

        @Override
        public int compare(Pair<URI, ElementHandle<TypeElement>> o1, Pair<URI, ElementHandle<TypeElement>> o2) {
            final String q1 = o1.second().getQualifiedName();
            final String q2 = o2.second().getQualifiedName();
            final String simpleName1 = getSimpleName(q1);
            final String simpleName2 = getSimpleName(q2);
            int res = simpleName1.compareTo(simpleName2);
            if (res != 0) {
                return res;
            }
            final String pkg1 = getEnclosing(q1);
            final String pkg2 = getEnclosing(q2);
            return pkg1.compareTo(pkg2);
        }

    }

}
