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

package org.netbeans.spi.project.support;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 * Factory for lookup capable of merging content from registered 
 * {@link org.netbeans.spi.project.LookupProvider} instances.
 * @author mkleint
 * @since org.netbeans.modules.projectapi 1.12
 */
public final class LookupProviderSupport {
    
    private LookupProviderSupport() {
    }
    
    /**
     * Creates a project lookup instance that combines the content from multiple sources. 
     * A convenience factory method for implementors of Project.
     * <p>The pattern {@code Projects/TYPE/Lookup} is conventional for the folder path, and required if
     * {@link org.netbeans.spi.project.LookupProvider.Registration},
     * {@link org.netbeans.spi.project.LookupMerger.Registration}, or
     * {@link org.netbeans.spi.project.ProjectServiceProvider} are used.
     * 
     * @param baseLookup initial, base content of the project lookup created by the project owner
     * @param folderPath the path in the System Filesystem that is used as root for lookup composition, as for {@link Lookups#forPath}.
     *        The content of the folder is assumed to be {@link LookupProvider} instances.
     * @return a lookup to be used in project
     */ 
    public static Lookup createCompositeLookup(Lookup baseLookup, String folderPath) {
        return new DelegatingLookupImpl(baseLookup, Lookups.forPath(folderPath), folderPath);
    }
    /**
     * Creates a project lookup instance that combines the content from multiple sources. 
     * A convenience factory method for implementors of Project.
     * <p>The pattern {@code Projects/TYPE/Lookup} is conventional for the folder path, and required if
     * {@link org.netbeans.spi.project.LookupProvider.Registration},
     * {@link org.netbeans.spi.project.LookupMerger.Registration}, or
     * {@link org.netbeans.spi.project.ProjectServiceProvider} are used.
     * 
     * @param baseLookup initial, base content of the project lookup created by the project owner
     * @param providers lookup containing the {@link LookupProvider} instances, typically created by aggregating multiple folder paths (see {@link Lookups#forPath})
     * @return a lookup to be used in project
     * @since 1.49
     */
    public static Lookup createCompositeLookup(Lookup baseLookup, Lookup providers) {
        return new DelegatingLookupImpl(baseLookup, providers, "<multiplePaths>");
    }
    
    /**
     * Factory method for creating {@link org.netbeans.spi.project.LookupMerger} instance that merges
     * {@link org.netbeans.api.project.Sources} instances in the project lookup. 
     * Allows to compose the {@link org.netbeans.api.project.Sources}
     * content from multiple sources.
     * @return instance to include in project lookup
     */
    public static LookupMerger<Sources> createSourcesMerger() {
        return new SourcesMerger();
    }

    /**
     * Factory method for creating {@link org.netbeans.spi.project.LookupMerger} instance that merges
     * {@link org.netbeans.spi.project.ActionProvider} instances in the project lookup.
     * The first {@link org.netbeans.spi.project.ActionProvider} which supports the command and is
     * enabled on it_will perform it.
     * @return instance to include in project lookup
     * @since 1.38
     */
    public static LookupMerger<ActionProvider> createActionProviderMerger() {
        return new ActionProviderMerger();
    }

    /**
     * Factory method for creating {@link org.netbeans.spi.project.LookupMerger} instance that merges
     * {@link SharabilityQueryImplementation2} instances in the project lookup.
     * The first non {@link SharabilityQuery.Sharability#UNKNOWN} result returned by the {@link SharabilityQueryImplementation2}s
     * included in the project's {@link Lookup} is returned.
     * @return instance to include in project lookup
     * @since 1.64
     */
    public static LookupMerger<SharabilityQueryImplementation2> createSharabilityQueryMerger() {
        return new SharabilityQueryMerger();
    }

    private static class SourcesMerger implements LookupMerger<Sources> {
        public @Override Class<Sources> getMergeableClass() {
            return Sources.class;
        }

        public @Override Sources merge(Lookup lookup) {
            return new SourcesImpl(lookup);
        }
    }

    private static class SourcesImpl implements Sources, ChangeListener, LookupListener {
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private final Lookup.Result<Sources> delegates;
        private Sources[] currentDelegates;
        
        @SuppressWarnings("LeakingThisInConstructor")
        SourcesImpl(Lookup lookup) {
            delegates = lookup.lookupResult(Sources.class);
            delegates.addLookupListener(this);
        }

        public @Override SourceGroup[] getSourceGroups(String type) {
            assert delegates != null;
            Sources[] _currentDelegates;
            synchronized (this) {
                if (currentDelegates == null) {
                    Collection<? extends Sources> instances = delegates.allInstances();
                    currentDelegates = instances.toArray(new Sources[0]);
                    for (Sources ns : currentDelegates) {
                        ns.addChangeListener(this);
                    }
                }
                _currentDelegates = currentDelegates;
            }
            Collection<SourceGroup> result = new ArrayList<SourceGroup>();
            for (Sources ns : _currentDelegates) {
                SourceGroup[] sourceGroups = ns.getSourceGroups(type);
                if (sourceGroups != null) {
                    for (SourceGroup sourceGroup : sourceGroups) {
                        if (sourceGroup == null) {
                            Exceptions.printStackTrace(new NullPointerException(ns + " returns null source group!"));
                        } else {
                            result.add(sourceGroup);
                        }
                    }
                }
            }
            return result.toArray(new SourceGroup[0]);
        }

        @Override public synchronized void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        @Override public synchronized void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        public @Override void stateChanged(ChangeEvent e) {
            changeSupport.fireChange();
        }

        public @Override void resultChanged(LookupEvent ev) {
            synchronized (this) {
                if (currentDelegates != null) {
                    for (Sources old : currentDelegates) {
                        old.removeChangeListener(this);
                    }
                    currentDelegates = null;
                }
            }
            changeSupport.fireChange();
        }
    }

    private static final class ActionProviderMerger implements LookupMerger<ActionProvider> {
        @Override
        public Class<ActionProvider> getMergeableClass() {
            return ActionProvider.class;
        }

        @Override
        public ActionProvider merge(final Lookup lookup) {
            return new MergedActionProvider(lookup);
        }
    }

    private static final class MergedActionProvider implements ActionProvider, LookupListener {

        private final Lookup.Result<ActionProvider> lkpResult;
        @SuppressWarnings("VolatileArrayField")
        private volatile String[] actionNamesCache;

        @SuppressWarnings("LeakingThisInConstructor")
        private MergedActionProvider(final Lookup lkp) {
            this.lkpResult = lkp.lookupResult(ActionProvider.class);
            this.lkpResult.addLookupListener(this);
        }

        @Override
        public String[] getSupportedActions() {
            String[] result = actionNamesCache;
            if (result == null) {
                final Set<String> actionNames = new LinkedHashSet <String>();
                for (ActionProvider ap : lkpResult.allInstances()) {
                    actionNames.addAll(Arrays.asList(ap.getSupportedActions()));
                }
                result = actionNames.toArray(new String[0]);
                actionNamesCache = result;
            }
            assert result != null;
            return result;
        }

        @Override
        public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
            boolean found = false;
            for (ActionProvider ap : lkpResult.allInstances()) {
                if (Arrays.asList(ap.getSupportedActions()).contains(command)) {
                    if (ap.isActionEnabled(command, context)) {
                        return true;
                    } else {
                        found = true;
                    }
                }
            }
            if (found) {
                return false;
            } else {
                throw new IllegalArgumentException("Misimplemented command '" + command + "' in " + Arrays.toString(lkpResult.allInstances().toArray()));
            }
        }

        @Override
        public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
            for (ActionProvider ap : lkpResult.allInstances()) {
                if (Arrays.asList(ap.getSupportedActions()).contains(command) &&
                    ap.isActionEnabled(command, context)) {
                    ap.invokeAction(command, context);
                    return;
                }
            }
            throw new IllegalArgumentException(String.format(command));
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            actionNamesCache = null;
        }

    }

    private static final class SharabilityQueryMerger implements LookupMerger<SharabilityQueryImplementation2> {

        @Override
        public Class<SharabilityQueryImplementation2> getMergeableClass() {
            return SharabilityQueryImplementation2.class;
        }

        @Override
        public SharabilityQueryImplementation2 merge(Lookup lookup) {
            return new MergedSharabilityQueryImplementation2(lookup);
        }
    }

    private static final class MergedSharabilityQueryImplementation2 implements SharabilityQueryImplementation2 {
        private final Lookup.Result<? extends SharabilityQueryImplementation2> lkpResult;

        MergedSharabilityQueryImplementation2(@NonNull final Lookup lkp) {
            this.lkpResult = lkp.lookupResult(SharabilityQueryImplementation2.class);
        }

        @Override
        public SharabilityQuery.Sharability getSharability(URI uri) {
            for (SharabilityQueryImplementation2 impl : lkpResult.allInstances()) {
                SharabilityQuery.Sharability res = impl.getSharability(uri);
                if (res != SharabilityQuery.Sharability.UNKNOWN) {
                    return res;
                }
            }
            return SharabilityQuery.Sharability.UNKNOWN;
        }
    }
}
