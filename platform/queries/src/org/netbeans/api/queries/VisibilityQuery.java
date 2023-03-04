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

package org.netbeans.api.queries;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.netbeans.spi.queries.VisibilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;

/**
 * Determine whether files should be hidden in views presented to the user.
 * <p>
 * This query should be considered only as a recommendation. Particular views
 * may decide to display all files and ignore this query.
 * </p>
 * @see org.netbeans.spi.queries.VisibilityQueryImplementation
 * @author Radek Matous
 */
public final class VisibilityQuery {
    private static final VisibilityQuery INSTANCE = new VisibilityQuery();

    private final ResultListener resultListener = new ResultListener();
    private final VqiChangedListener vqiListener = new VqiChangedListener ();

    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private Lookup.Result<VisibilityQueryImplementation> vqiResult = null;
    private List<VisibilityQueryImplementation> cachedVqiInstances = null;

    /**
     * Get default instance of VisibilityQuery.
     * @return instance of VisibilityQuery
     */
    public static final VisibilityQuery getDefault() {
        return INSTANCE;
    }

    private VisibilityQuery() {
    }

    /**
     * Check whether a file is recommended to be visible.
     * Default return value is visible unless at least one VisibilityQueryImplementation
     * provider says hidden.
     * @param file a file which should be checked
     * @return true if it is recommended to show this file
     */
    public boolean isVisible(FileObject file) {
        Parameters.notNull("file", file);
        for (VisibilityQueryImplementation vqi : getVqiInstances()) {
            if (!vqi.isVisible(file)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check whether a file is recommended to be visible.
     * Default return value is visible unless at least one VisibilityQueryImplementation
     * provider says hidden.
     * @param file a file which should be checked
     * @return true if it is recommended to show this file
     * @since org.netbeans.modules.queries/1 1.12
     */
    public boolean isVisible(File file) {
        Parameters.notNull("file", file);
        for (VisibilityQueryImplementation vqi : getVqiInstances()) {
            if (vqi instanceof VisibilityQueryImplementation2) {
                if (!((VisibilityQueryImplementation2)vqi).isVisible(file)) {
                    return false;
                }
            } else {
                FileObject fo = FileUtil.toFileObject(file);
                if (fo != null) {
                    if (!vqi.isVisible(fo)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    

    /**
     * Add a listener to changes.
     * @param l a listener to add
     */
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    /**
     * Stop listening to changes.
     * @param l a listener to remove
     */
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private void fireChange(ChangeEvent event) {
        assert event != null;
        ArrayList<ChangeListener> lists;
        synchronized (listeners) {
            lists = new ArrayList<ChangeListener>(listeners);
        }
        for (ChangeListener listener : lists) {
            try {
                listener.stateChanged(event);
            } catch (RuntimeException x) {
                Exceptions.printStackTrace(x);
            }
        }
    }

    private synchronized List<VisibilityQueryImplementation> getVqiInstances() {
        if (cachedVqiInstances == null) {
            vqiResult = Lookup.getDefault().lookupResult(VisibilityQueryImplementation.class);
            vqiResult.addLookupListener(resultListener);
            setupChangeListeners(null, new ArrayList<VisibilityQueryImplementation>(vqiResult.allInstances()));
        }
        return cachedVqiInstances;
    }

    private synchronized void setupChangeListeners(final List<VisibilityQueryImplementation> oldVqiInstances, final List<VisibilityQueryImplementation> newVqiInstances) {
        if (oldVqiInstances != null) {
            Set<VisibilityQueryImplementation> removed = new HashSet<VisibilityQueryImplementation>(oldVqiInstances);
            removed.removeAll(newVqiInstances);
            for (VisibilityQueryImplementation vqi : removed) {
                vqi.removeChangeListener(vqiListener);
            }
        }

        Set<VisibilityQueryImplementation> added = new HashSet<VisibilityQueryImplementation>(newVqiInstances);
        if (oldVqiInstances != null) {
            added.removeAll(oldVqiInstances);
        }
        for (VisibilityQueryImplementation vqi : added) {
            vqi.addChangeListener(vqiListener);
        }

        cachedVqiInstances = newVqiInstances;
    }

    private class ResultListener implements LookupListener {
        @Override
        public void resultChanged(LookupEvent ev) {
            setupChangeListeners(cachedVqiInstances, new ArrayList<VisibilityQueryImplementation>(vqiResult.allInstances()));
            fireChange(new ChangeEvent(this));
        }
    }

    private class VqiChangedListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            fireChange(e);
        }
    }

}
