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
package org.netbeans.modules.cnd.navigation.overrides;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 * After org.netbeans.modules.java.editor.overridden.AnnotationsHolder by
 */
public class AnnotationsHolder implements PropertyChangeListener, Runnable {

    //private static final Logger LOGGER = Logger.getLogger(AnnotationsHolder.class.getName());

    /**
     * Maps file objects to AnnotationsHolder instances.
     * Synchronized by itself
     */
    private static final Map<DataObject, AnnotationsHolder> file2holders = new HashMap<DataObject, AnnotationsHolder>();
    
    public static AnnotationsHolder get(DataObject dao) {
        synchronized (file2holders) {
            AnnotationsHolder holder = file2holders.get(dao);
            if (holder != null) {
                return holder;
            }
            EditorCookie.Observable ec = dao.getLookup().lookup(EditorCookie.Observable.class);
            if (ec == null) {
                return null;
            }
            file2holders.put(dao, holder = new AnnotationsHolder(dao, ec));
            return holder;
        }
    }

    public static void clearIfNeed(DataObject dao) {
        AnnotationsHolder holder;
        synchronized (file2holders) {
            holder = file2holders.remove(dao);
            if (holder != null) {
                holder.cancelled.set(true);
            }
        }
        if (holder != null) {
            holder.ec.removePropertyChangeListener(holder);
            holder.task.schedule(0);
        }
    }

    private final DataObject file;
    private final EditorCookie.Observable ec;
    private final RequestProcessor.Task task;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    /**
     * Contains annotations that has been already attached to the document.
     * Accessed ONLY WITHIN AWT THREAD => needs no synchronization.
     */
    private final List<BaseAnnotation> attachedAnnotations = new ArrayList<BaseAnnotation>();

    /**
     * Annotations that are to be attached to the document.
     * Synchronized by pendingAnnotationsLock.
     */
    private final List<BaseAnnotation> pendingAnnotations = new ArrayList<BaseAnnotation>();

    private final ReentrantReadWriteLock attachedAnnotationsLock = new ReentrantReadWriteLock();
    
    private AnnotationsHolder(DataObject file, EditorCookie.Observable ec) {
        this.file = file;
        this.ec   = ec;        
        Logger.getLogger("TIMER").log(Level.FINE, "Overridden AnnotationsHolder", new Object[] {file.getPrimaryFile(), this}); //NOI18N

        task = new RequestProcessor("C/C++ Annotations Holder", 1).create(this); //NOI18N

        ec.addPropertyChangeListener(this);        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                checkForReset();
            }
        });
     }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (EditorCookie.Observable.PROP_OPENED_PANES.endsWith(evt.getPropertyName()) || evt.getPropertyName() == null) {
            checkForReset();
        }
    }

    @Override
    public void run() {
        //long currentTimeMillis = System.currentTimeMillis();
        // First, clear old annotations.
        // This should be done even if annotations are to be updated again.
        Collection<BaseAnnotation> toAdd;
        synchronized (pendingAnnotations) {
            attachedAnnotationsLock.readLock().lock();
            try {
                for (BaseAnnotation a : attachedAnnotations) {
                    a.detachImpl();
                }
            } finally {
                attachedAnnotationsLock.readLock().unlock();
            }
            //System.err.println("Removing " + attachedAnnotations.size()+ " annotations.");
            attachedAnnotationsLock.writeLock().lock();
            try {
                attachedAnnotations.clear();
            } finally {
                attachedAnnotationsLock.writeLock().unlock();
            }
            // Remember pendingAnnotations and set it to null
            toAdd = new ArrayList<BaseAnnotation>(pendingAnnotations);
            pendingAnnotations.clear();
        }
        if (toAdd.isEmpty()) {
            return;
        }
        for (BaseAnnotation a : toAdd) {
            if (cancelled.get()) {
                //System.err.println("Cancelled after adding " + attachedAnnotations.size() + " from " + toAdd.size() + "; took " + (System.currentTimeMillis() - currentTimeMillis) + "ms");
                return;
            }
            if (a.attach()) {
                attachedAnnotationsLock.writeLock().lock();
                try {
                   attachedAnnotations.add(a);
                } finally {
                    attachedAnnotationsLock.writeLock().unlock();
                }
            }
        }
        //System.err.println("Adding " + toAdd.size() + " annotations took " + (System.currentTimeMillis() - currentTimeMillis) + "ms");
    }
        
    private void checkForReset() {
        assert SwingUtilities.isEventDispatchThread();        
        if (ec.getOpenedPanes() == null) {
            //reset:
            AnnotationsHolder remove;
            synchronized (file2holders) {
                remove = file2holders.remove(file);
                cancelled.set(true);
            }    
            ec.removePropertyChangeListener(this);
            if (remove != null) {
                task.schedule(0);
            }
        } else {
            cancelled.set(false);
        }
    }
    
    void setNewAnnotations(Collection<BaseAnnotation> annotations2set) {
        CndUtils.assertNonUiThread();
        synchronized (pendingAnnotations) {
            pendingAnnotations.clear();
            pendingAnnotations.addAll(annotations2set);
        }
        task.schedule(0);
    }

    /**
     * Gets annotations that have been attached to the document.
     * Should be called ONLY FROM AWT THREAD
     */
    public List<BaseAnnotation> getAttachedAnnotations() {
        CndUtils.assertUiThread();
        attachedAnnotationsLock.readLock().lock();
        try {
            return new ArrayList<BaseAnnotation>(attachedAnnotations);
        } finally {
            attachedAnnotationsLock.readLock().unlock();
        }
    }
}
