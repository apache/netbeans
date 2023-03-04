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

package org.netbeans.modules.parsing.impl.indexing;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.parsing.spi.indexing.IndexabilityQueryImplementation;
import org.netbeans.modules.parsing.spi.indexing.IndexabilityQueryImplementation.IndexabilityQueryContext;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Determine whether files should be skipped by the netbeans indexing infrastructure.
 *
 * @see org.netbeans.modules.parsing.spi.indexing.IndexabilityQueryImplementation
 */
final class IndexabilityQuery {
    private static final Logger LOG = Logger.getLogger(IndexabilityQuery.class.getName());

    private static final IndexabilityQueryContextAccessor CONTEXT_CREATOR = IndexabilityQueryContextAccessor.getInstance();
    private static final IndexabilityQuery INSTANCE = new IndexabilityQuery();

    private final ResultListener resultListener = new ResultListener();
    private final IqiChangedListener vqiListener = new IqiChangedListener();

    private final List<ChangeListener> listeners = new ArrayList<>();
    private volatile List<IndexabilityQueryImplementation> cachedIqiInstances = null;
    private Lookup.Result<IndexabilityQueryImplementation> iqiResult = null;

    /**
     * Get instance of IndexabilityQuery.
     * @return instance of IndexabilityQuery
     */
    public static final IndexabilityQuery getInstance() {
        return INSTANCE;
    }

    private IndexabilityQuery() {
    }

    public boolean preventIndexing(FileObject fileObject) {
        IndexabilityQueryContext iqc = CONTEXT_CREATOR.createContext(fileObject.toURL(), null, null);
        for (IndexabilityQueryImplementation iqi : getIqiInstances()) {
            if (iqi.preventIndexing(iqc)) {
                return true;
            }
        }
        return false;
    }

    public boolean preventIndexing (String indexerName, URL indexable, URL rootUrl)  {
        IndexabilityQueryContext iqc = CONTEXT_CREATOR.createContext(indexable, indexerName, rootUrl);
        for (IndexabilityQueryImplementation iqi : getIqiInstances()) {
            if (iqi.preventIndexing(iqc)) {
                return true;
            }
        }
        return false;
    }

    public String getState() {
        return getIqiInstances()
                .stream()
                .map(iqi -> iqi.getName() + "-" + iqi.getVersion() + "-" + iqi.getStateIdentifier())
                .collect(Collectors.joining(","));
    }

    private Set<String> decodeState(String input) {
        try {
            return Arrays.stream(input.split(","))
                    .map(s -> s.trim())
                    .collect(Collectors.toSet());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Failed to parse IndexabilityQuery state from '" + input + "'", ex);
            return Collections.EMPTY_SET;
        }
    }

    public boolean isSameState(String reference) {
        return Objects.equals(decodeState(reference), decodeState(getState()));
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
            lists = new ArrayList<>(listeners);
        }
        for (ChangeListener listener : lists) {
            try {
                listener.stateChanged(event);
            } catch (RuntimeException x) {
                Exceptions.printStackTrace(x);
            }
        }
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    private synchronized List<IndexabilityQueryImplementation> getIqiInstances() {
        if (cachedIqiInstances == null) {
            iqiResult = Lookup.getDefault().lookupResult(IndexabilityQueryImplementation.class);
            iqiResult.addLookupListener(resultListener);
            setupChangeListeners(null, new ArrayList<>(iqiResult.allInstances()));
        }
        return cachedIqiInstances;
    }

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    private synchronized void setupChangeListeners(final List<IndexabilityQueryImplementation> oldVqiInstances, final List<IndexabilityQueryImplementation> newVqiInstances) {
        if (oldVqiInstances != null) {
            Set<IndexabilityQueryImplementation> removed = new HashSet<>(oldVqiInstances);
            removed.removeAll(newVqiInstances);
            for (IndexabilityQueryImplementation vqi : removed) {
                vqi.removeChangeListener(vqiListener);
            }
        }

        Set<IndexabilityQueryImplementation> added = new HashSet<>(newVqiInstances);
        if (oldVqiInstances != null) {
            added.removeAll(oldVqiInstances);
        }
        for (IndexabilityQueryImplementation vqi : added) {
            vqi.addChangeListener(vqiListener);
        }

        cachedIqiInstances = newVqiInstances;
    }

    private class ResultListener implements LookupListener {
        @Override
        public void resultChanged(LookupEvent ev) {
            setupChangeListeners(cachedIqiInstances, new ArrayList<>(iqiResult.allInstances()));
            fireChange(new ChangeEvent(IndexabilityQuery.this));
        }
    }

    private class IqiChangedListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            fireChange(e);
        }
    }
}
