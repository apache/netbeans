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
package org.netbeans.modules.parsing.nb.indexing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.parsing.impl.indexing.implspi.ActiveDocumentProvider;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service = ActiveDocumentProvider.class, position = 30_000)
public final class NbActiveDocumentProvider implements ActiveDocumentProvider, PropertyChangeListener {

    private final AtomicBoolean listens;
    private final List<ActiveDocumentListener> listeners;
    //Threading: accessed only from EDT
    private Reference<JTextComponent> activeComponentRef;

    public NbActiveDocumentProvider() {
        this.listens = new AtomicBoolean();
        this.listeners = new CopyOnWriteArrayList<>();
    }

    @Override
    @CheckForNull
    public Document getActiveDocument() {
        final JTextComponent last = EditorRegistry.lastFocusedComponent();
        return last == null ?
            null :
            last.getDocument();
    }

    @Override
    public Set<? extends Document> getActiveDocuments() {
        final Set<Document> res = Collections.newSetFromMap(new IdentityHashMap<Document, Boolean>());
        for (JTextComponent jtc : EditorRegistry.componentList()) {
            final Document doc = jtc.getDocument();
            if (doc != null) {
                res.add(doc);
            }
        }
        return res;
    }

    @Override
    public void addActiveDocumentListener(@NonNull final ActiveDocumentListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        attachListener();
        listeners.add(listener);
    }

    @Override
    public void removeActiveDocumentListener(@NonNull final ActiveDocumentListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        listeners.remove(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        assert SwingUtilities.isEventDispatchThread() : "Changes in focused editor component should be delivered on AWT"; //NOI18N
        Document deactivate = null, activate = null;
        List<? extends JTextComponent> components = Collections.<JTextComponent>emptyList();
        boolean steady = false;
        final String propName = evt.getPropertyName();
        if (propName == null) {
            components = EditorRegistry.componentList();
        } else if (propName.equals(EditorRegistry.FOCUS_LOST_PROPERTY) ||
                   propName.equals(EditorRegistry.LAST_FOCUSED_REMOVED_PROPERTY)) {
            if (evt.getOldValue() instanceof JTextComponent) {
                Object newValue = evt.getNewValue();
                if (!(newValue instanceof JTextComponent) || ((JTextComponent)newValue).getClientProperty("AsTextField") == null) {
                    deactivate = ((JTextComponent) evt.getOldValue()).getDocument();
                }
            }
        } else if (propName.equals(EditorRegistry.COMPONENT_REMOVED_PROPERTY)) {
            if (evt.getOldValue() instanceof JTextComponent) {
                JTextComponent jtc = (JTextComponent) evt.getOldValue();
                components = Collections.singletonList(jtc);
                steady = true;
            }
        } else if (propName.equals(EditorRegistry.FOCUS_GAINED_PROPERTY)) {
            if (evt.getNewValue() instanceof JTextComponent) {
                JTextComponent jtc = (JTextComponent) evt.getNewValue();
                if (jtc.getClientProperty("AsTextField") == null) {
                    activate = jtc.getDocument();
                    JTextComponent activeComponent = activeComponentRef == null ? null : activeComponentRef.get();
                    if (activeComponent != jtc) {
                        if (activeComponent != null) {
                            components = Collections.singletonList(activeComponent);
                        }
                        activeComponentRef = new WeakReference<>(jtc);
                    }
                }
            }
        } else if (propName.equals(EditorRegistry.FOCUSED_DOCUMENT_PROPERTY)) {
            JTextComponent jtc = EditorRegistry.focusedComponent();
            if (jtc == null) {
                jtc = EditorRegistry.lastFocusedComponent();
            }
            if (jtc != null) {
                components = Collections.singletonList(jtc);
            }
            deactivate = (Document) evt.getOldValue();
            activate = (Document) evt.getNewValue();
        }

        fire(
            deactivate,
            activate,
            map(
                components,
                new F<JTextComponent,Document>() {
                    @Override
                    public Document apply(JTextComponent jtc) {
                        return jtc.getDocument();
                    }
                }),
            steady);
    }

    private void attachListener() {
        if (listens.compareAndSet(false, true)) {
            EditorRegistry.addPropertyChangeListener(WeakListeners.propertyChange(this, EditorRegistry.class));
        }
    }

    private void fire(
        @NullAllowed final Document deactivated,
        @NullAllowed final Document activated,
        @NonNull final Collection<? extends Document> toRefresh,
        final boolean steady) {
        final ActiveDocumentEvent event = new ActiveDocumentEvent(
            this,
            deactivated,
            activated,
            toRefresh,
            steady);
        for (ActiveDocumentListener l : listeners) {
            l.activeDocumentChanged(event);
        }
    }

    private static <P,R> Collection<? extends R> map(
        @NonNull final Collection<? extends P> elements,
        @NonNull final F<? super P, ? extends R> fnc) {
        final Collection<R> result = new ArrayList<>(elements.size());
        for (P p : elements) {
            result.add(fnc.apply(p));
        }
        return result;
    }

    private static interface F<P,R> {
        R apply(P p);
    }
}
