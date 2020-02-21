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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
