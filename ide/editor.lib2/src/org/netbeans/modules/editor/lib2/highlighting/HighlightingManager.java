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

package org.netbeans.modules.editor.lib2.highlighting;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ReleasableHighlightsContainer;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ProxyLookup;

/**
 * Highlighting manager maintains all the highlighting layers instances.
 * <br>
 * It divides them into two groups according to their z-order and fixedSize attributes.
 * Top layers that have fixedSize set to true are one group. The rest is the other group.
 * <br>
 * View hierarchy only rebuilds views if the second group of layers (bottom ones) changes.
 * If the top group changes the view hierarchy only triggers repaint of affected part.
 *
 * @author Vita Stejskal, Miloslav Metelka
 */
public final class HighlightingManager {

    // -J-Dorg.netbeans.modules.editor.lib2.highlighting.HighlightingManager.level=FINE
    private static final Logger LOG = Logger.getLogger(HighlightingManager.class.getName());
    
    private final Highlighting highlighting;
    
    public static synchronized HighlightingManager getInstance(JTextComponent pane) {
        HighlightingManager highlightingManager = (HighlightingManager) pane.getClientProperty(HighlightingManager.class);
        if (highlightingManager == null) {
            highlightingManager = new HighlightingManager(pane);
            pane.putClientProperty(HighlightingManager.class, highlightingManager);
        }
        return highlightingManager;
    }
    
    /**
     * Get bottom highlighting layers (according to z-order) that are generally assumed to change the metrics
     * and so the views must be rebuilt when these highlights get changed.
     * @param pane non-null pane.
     * @return non-null highlights container.
     */
    public HighlightsContainer getBottomHighlights() {
        return highlighting.bottomHighlights();
    }
    
    /**
     * Get top highlighting layers (they are above the bottom layers according to z-order)
     * that must not change the metrics and so the views do not need to be rebuilt
     * when these highlights get changed (the affected area just gets repainted).
     * @param pane non-null pane.
     * @return non-null highlights container.
     */
    public HighlightsContainer getTopHighlights() {
        return highlighting.topHighlights();
    }
    
    /**
     * Find highlighting layer that uses a given container.
     *
     * @param container non-null container.
     * @return layer that uses the container or null if none does.
     */
    public HighlightsLayer findLayer(HighlightsContainer container) {
        return highlighting.findLayer(container);
    }

    /**
     * This is primarily for testing purposes - the resulting container is not cached.
     * 
     * @param filter valid filter or null.
     * @return 
     */
    HighlightsContainer getHighlights(HighlightsLayerFilter filter) {
        return highlighting.filteredHighlights(filter);
    }
    
    public void addChangeListener(ChangeListener listener) {
        highlighting.changeListeners.add(listener);
    }
    
    public void removeChangeListener(ChangeListener listener) {
        highlighting.changeListeners.remove(listener);
    }
    
    /**
     * Called in tests to quickly replace all existing layers with a single testing container
     * to get controlled attribute set chunking for view hierarchy tests.
     * 
     * @param testSingleContainer non-null testing container or null to return to regular behavior.
     */
    public void testSetSingleContainer(HighlightsContainer testSingleContainer) {
        highlighting.testSingleContainer = testSingleContainer;
        highlighting.rebuildAllLayers();
    }

    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------

    /** Creates a new instance of HighlightingManager */
    private HighlightingManager(JTextComponent pane) {
        highlighting = new Highlighting(this, pane);
    }
    
    private static final class Highlighting implements PropertyChangeListener {
    
        private static final String PROP_MIME_TYPE = "mimeType"; //NOI18N
        private static final String PROP_DOCUMENT = "document"; //NOI18N
        private static final String PROP_HL_INCLUDES = "HighlightsLayerIncludes"; //NOI18N
        private static final String PROP_HL_EXCLUDES = "HighlightsLayerExcludes"; //NOI18N

        // The factories changes tracking
        private Lookup.Result<HighlightsLayerFactory> factories = null;
        private final LookupListener factoriesTracker = new LookupListener() {
            public @Override void resultChanged(LookupEvent ev) {
                rebuildAllLayers();
            }
        };
        private LookupListener weakFactoriesTracker = null;

        // The FontColorSettings changes tracking
        private Lookup.Result<FontColorSettings> settings = null;
        private final LookupListener settingsTracker = new LookupListener() {
            public @Override void resultChanged(LookupEvent ev) {
//                System.out.println("Settings tracker for '" + (lastKnownMimePaths == null ? "null" : lastKnownMimePaths[0].getPath()) + "'");
                rebuildAllLayers();
            }
        };
        private LookupListener weakSettingsTracker = null;

        private final HighlightingManager manager; // For proper change firing
        private final JTextComponent pane;
        private HighlightsLayerFilter paneFilter;
        private Reference<Document> lastKnownDocumentRef;
        private MimePath [] lastKnownMimePaths = null;
        private boolean inRebuildAllLayers = false;
        
        private List<? extends HighlightsLayer> sortedLayers;
        private DirectMergeContainer bottomHighlights;
        private DirectMergeContainer topHighlights;
        List<ChangeListener> changeListeners = new CopyOnWriteArrayList<ChangeListener>();
        
        /**
         * Single container for unit testing of view hierarchy.
         */
        HighlightsContainer testSingleContainer;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public Highlighting(HighlightingManager manager, JTextComponent pane) {
            this.manager = manager;
            this.pane = pane;
            updatePaneFilter();
            this.pane.addPropertyChangeListener(WeakListeners.propertyChange(this, pane));
            rebuildAll();
        }
        
        private synchronized void updatePaneFilter() {
            paneFilter = new RegExpFilter(pane.getClientProperty(PROP_HL_INCLUDES), pane.getClientProperty(PROP_HL_EXCLUDES));
        }

        synchronized HighlightsContainer bottomHighlights() {
            return bottomHighlights;
        }
        
        synchronized HighlightsContainer topHighlights() {
            return topHighlights;
        }
        
        synchronized HighlightsContainer filteredHighlights(HighlightsLayerFilter filter) {
            // Get the containers
            List<? extends HighlightsLayer> layers = (filter == null)
                    ? sortedLayers
                    : filter.filterLayers(sortedLayers);
            ArrayList<HighlightsContainer> containers = new ArrayList<HighlightsContainer>(layers.size());
            for (HighlightsLayer layer : layers) {
                HighlightsLayerAccessor layerAccessor =
                        HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);

                containers.add(layerAccessor.getContainer());
            }
            return new DirectMergeContainer(containers.toArray(new HighlightsContainer[0]), true);
                
        }
        
        synchronized HighlightsLayer findLayer(HighlightsContainer container) {
            for (HighlightsLayer layer : sortedLayers) {
                if (HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer).getContainer() == container) {
                    return layer;
                }
            }
            return null;
        }

        // ----------------------------------------------------------------------
        //  PropertyChangeListener implementation
        // ----------------------------------------------------------------------

        public @Override void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == null || PROP_DOCUMENT.equals(evt.getPropertyName())) {
                updatePaneFilter();
                Document doc = pane.getDocument();
                if (doc != null) {
                    doc.render(new Runnable() {
                        @Override
                        public void run() {
                            rebuildAll();
                        }
                    });
                }
            }

            if (PROP_HL_INCLUDES.equals(evt.getPropertyName()) || PROP_HL_EXCLUDES.equals(evt.getPropertyName())) {
                updatePaneFilter();
                rebuildAllLayers();
            }
        }
        
        // ----------------------------------------------------------------------
        //  Private implementation
        // ----------------------------------------------------------------------

        private MimePath [] getAllDocumentMimePath() {
            Document doc = pane.getDocument();
            String mainMimeType;

            Object propMimeType = doc.getProperty(PROP_MIME_TYPE);
            if (propMimeType != null) {
                mainMimeType = propMimeType.toString();
            } else {
                mainMimeType = pane.getUI().getEditorKit(pane).getContentType();
            }

            return new MimePath [] { MimePath.parse(mainMimeType) };
        }
        
        private synchronized void rebuildAll() {
            // Get the new set of mime path
            MimePath [] mimePaths = getAllDocumentMimePath();

            Document lastKnownDocument = lastKnownDocumentRef == null ? null : lastKnownDocumentRef.get();

            // Recalculate factories and all containers if needed
            if (!Utilities.compareObjects(lastKnownDocument, pane.getDocument()) ||
                !Arrays.equals(lastKnownMimePaths, mimePaths)
            ) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("rebuildAll: lastKnownDocument = " + simpleToString(lastKnownDocument) + //NOI18N
                            ", document = " + simpleToString(pane.getDocument()) + //NOI18N
                            ", lastKnownMimePaths = " + mimePathsToString(lastKnownMimePaths) + //NOI18N
                            ", mimePaths = " + mimePathsToString(mimePaths) + "\n"); //NOI18N
                }
                
                // Unregister listeners
                if (factories != null && weakFactoriesTracker != null) {
                    factories.removeLookupListener(weakFactoriesTracker);
                    weakFactoriesTracker = null;
                }
                if (settings != null && weakSettingsTracker != null) {
                    settings.removeLookupListener(weakSettingsTracker);
                    weakSettingsTracker = null;
                }

                if (mimePaths != null) {
                    ArrayList<Lookup> lookups = new ArrayList<Lookup>();
                    for(MimePath mimePath : mimePaths) {
                        lookups.add(MimeLookup.getLookup(mimePath));
                    }

                    ProxyLookup lookup = new ProxyLookup(lookups.toArray(new Lookup[0]));
                    factories = lookup.lookup(new Lookup.Template<HighlightsLayerFactory>(HighlightsLayerFactory.class));
                    settings = lookup.lookup(new Lookup.Template<FontColorSettings>(FontColorSettings.class));
                } else {
                    factories = null;
                    settings = null;
                }
                
                // Start listening again
                if (factories != null) {
                    weakFactoriesTracker = WeakListeners.create(LookupListener.class, factoriesTracker, factories);
                    factories.addLookupListener(weakFactoriesTracker);
                    factories.allItems(); // otherwise we won't get any events at all
                }
                if (settings != null) {
                    weakSettingsTracker = WeakListeners.create(LookupListener.class, settingsTracker, settings);
                    settings.addLookupListener(weakSettingsTracker);
                    settings.allItems(); // otherwise we won't get any events at all
                }

                lastKnownDocument = pane.getDocument();
                lastKnownMimePaths = mimePaths;
                
                rebuildAllLayers();
            }
        }
        
        private void fireChangeListeners() {
            ChangeEvent evt = new ChangeEvent(manager);
            for (ChangeListener l : changeListeners) {
                l.stateChanged(evt);
            }
        }
        
        synchronized void rebuildAllLayers() {
            Document doc = pane.getDocument();
            if (doc != null) {
                doc.render(new Runnable() {
                    @Override
                    public void run() {
                        rebuildAllLayersImpl();
                    }
                });
            }
        }
        
        void rebuildAllLayersImpl() {
            if (inRebuildAllLayers) {
                return;
            }
            DirectMergeContainer origTopHighlights;
            DirectMergeContainer origBottomHighlights;
            inRebuildAllLayers = true;
            try {
                Document doc = pane.getDocument();
                Collection<? extends HighlightsLayerFactory> all = factories.allInstances();
                HashMap<String, HighlightsLayer> layers = new HashMap<String, HighlightsLayer>();

                HighlightsLayerFactory.Context context = HighlightingSpiPackageAccessor.get().createFactoryContext(doc, pane);

                for(HighlightsLayerFactory factory : all) {
                    HighlightsLayer [] factoryLayers = factory.createLayers(context);
                    if (factoryLayers == null) {
                        continue;
                    }

                    for(HighlightsLayer layer : factoryLayers) {
                        HighlightsLayerAccessor layerAccessor = 
                            HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);

                        String layerTypeId = layerAccessor.getLayerTypeId();
                        if (!layers.containsKey(layerTypeId)) {
                            layers.put(layerTypeId, layer);
                        }
                    }
                }

                // Sort the layers by their z-order
                try {
                    sortedLayers = HighlightingSpiPackageAccessor.get().sort(layers.values());
                } catch (TopologicalSortException tse) {
                    ErrorManager.getDefault().notify(tse);
                    @SuppressWarnings("unchecked") //NOI18N
                    List<? extends HighlightsLayer> sl
                            = (List<? extends HighlightsLayer>)tse.partialSort();
                    sortedLayers = sl;
                }
                // Filter layers by pane's filter - retains order
                List<? extends HighlightsLayer> origSortedLayers = sortedLayers;
                sortedLayers = paneFilter.filterLayers(sortedLayers);
                if (origSortedLayers.size() != sortedLayers.size()) { // Release filtered out layers
                    origSortedLayers = new ArrayList<>(origSortedLayers);
                    origSortedLayers.removeAll(sortedLayers);
                    for (HighlightsLayer layer : origSortedLayers) {
                        HighlightsContainer container = HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer).getContainer();
                        if (container instanceof ReleasableHighlightsContainer) {
                            ((ReleasableHighlightsContainer)container).released();
                        }
                    }
                }

                int topStartIndex = 0;
                for (int i = 0; i < sortedLayers.size(); i++) {
                    HighlightsLayer layer = sortedLayers.get(i);
                    HighlightsLayerAccessor layerAccessor =
                            HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);
                    if (!layerAccessor.isFixedSize()) {
                        topStartIndex = i + 1; // Top layers can only be above this one
                    }
                }

                // Get the containers
                ArrayList<HighlightsContainer> layerContainers = new ArrayList<HighlightsContainer>();
                for(HighlightsLayer layer : sortedLayers) {
                    HighlightsLayerAccessor layerAccessor = 
                        HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);

                    layerContainers.add(layerAccessor.getContainer());
                }
                List<? extends HighlightsContainer> bottomContainers = layerContainers.subList(0, topStartIndex);
                List<? extends HighlightsContainer> topContainers = layerContainers.subList(topStartIndex, sortedLayers.size());

                if (LOG.isLoggable(Level.FINER)) {
                    StringBuilder sb = new StringBuilder(300);
                    dumpInfo(sb, doc, lastKnownMimePaths);
                    dumpLayers(sb, "Bottom", sortedLayers.subList(0, topStartIndex));
                    dumpLayers(sb, "Top", sortedLayers.subList(topStartIndex, sortedLayers.size()));
                    LOG.finer(sb.toString());
                }
                
                if (testSingleContainer != null) { // Use just the one single container
                    bottomContainers = Collections.singletonList(testSingleContainer);
                    topContainers = Collections.emptyList();
                }

                origBottomHighlights = bottomHighlights;
                origTopHighlights = topHighlights;
                bottomHighlights = new DirectMergeContainer(bottomContainers.toArray(new HighlightsContainer[0]), true);
                topHighlights = new DirectMergeContainer(topContainers.toArray(new HighlightsContainer[0]), true);
            } finally {
                inRebuildAllLayers = false;
            }
            if (origBottomHighlights != null) {
                origBottomHighlights.released();
            }
            if (origTopHighlights != null) {
                origTopHighlights.released();
            }
            fireChangeListeners();
        }
        
        private static void dumpInfo(StringBuilder sb, Document doc, MimePath [] mimePaths) {
            sb.append(" HighlighsLayers:\n"); //NOI18N
            sb.append(" * document : "); //NOI18N
            sb.append(doc.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(doc)));
            Object streamDescriptor = doc.getProperty(Document.StreamDescriptionProperty);
            sb.append(" [").append(streamDescriptor == null ? "no stream descriptor" : streamDescriptor.toString()).append(']');
            sb.append("\n"); //NOI18N

            sb.append(" * mime paths : \n"); //NOI18N
            for(MimePath mimePath : mimePaths) {
                sb.append("    "); //NOI18N
                sb.append(mimePath.getPath());
                sb.append("\n"); //NOI18N
            }
        }
            
        private static void dumpLayers(StringBuilder sb, String prefix, List<? extends HighlightsLayer> layers) {
            sb.append(prefix).append(" layers:\n"); //NOI18N
            int digitCount = ArrayUtilities.digitCount(layers.size());
            for (int i = 0; i < layers.size(); i++) {
                HighlightsLayer layer = layers.get(i);
                HighlightsLayerAccessor layerAccessor = 
                    HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);

                sb.append("  ");
                ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
                sb.append(layerAccessor.getLayerTypeId());
                sb.append('['); //NOI18N
                sb.append(layerAccessor.getZOrder().toString()); //NOI18N
                sb.append(layerAccessor.isFixedSize() ? ",Fixed" : ",NonFixed");
                sb.append(']'); //NOI18N
                sb.append('@'); //NOI18N
                sb.append(Integer.toHexString(System.identityHashCode(layer)));
                sb.append("\n"); //NOI18N
            }
        }

    } // End of Highlighting class
    
    private static final class RegExpFilter implements HighlightsLayerFilter {
        
        private final List<Pattern> includes;
        private final List<Pattern> excludes;
        
        public RegExpFilter(Object includes, Object excludes) {
            this.includes = buildPatterns(includes);
            this.excludes = buildPatterns(excludes);
        }

        @Override
        public List<? extends HighlightsLayer> filterLayers(List<? extends HighlightsLayer> layers) {
            List<? extends HighlightsLayer> includedLayers;
            
            if (includes.isEmpty()) {
                includedLayers = layers;
            } else {
                includedLayers = filter(layers, includes, true);
            }
            
            List<? extends HighlightsLayer> filteredLayers;
            if (excludes.isEmpty()) {
                filteredLayers = includedLayers;
            } else {
                filteredLayers = filter(includedLayers, excludes, false);
            }
            
            return filteredLayers;
        }

        private static List<? extends HighlightsLayer> filter(
            List<? extends HighlightsLayer> layers,
            List<Pattern> patterns,
            boolean includeMatches // true means include matching layers, false means include non-matching layers
        ) {
            List<HighlightsLayer> filtered = new ArrayList<HighlightsLayer>();
            
            for(HighlightsLayer layer : layers) {
                HighlightsLayerAccessor layerAccessor = 
                    HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);
                
                boolean matchesExcludes = false;
                for(Pattern pattern : patterns) {
                    boolean matches = pattern.matcher(layerAccessor.getLayerTypeId()).matches();
                    
                    if (matches && includeMatches) {
                        filtered.add(layer);
                    }
                                     
                    matchesExcludes = matches ? true : matchesExcludes;
                }
                
                if (!patterns.isEmpty() && !matchesExcludes && !includeMatches) {
                    filtered.add(layer);
                }
            }
            
            return filtered;
        }
        
        private static List<Pattern> buildPatterns(Object expressions) {
            List<Pattern> patterns = new ArrayList<Pattern>();
            
            if (expressions instanceof String) {
                try {
                    patterns.add(Pattern.compile((String) expressions));
                } catch (PatternSyntaxException e) {
                    LOG.log(Level.WARNING, "Ignoring invalid regexp for the HighlightsLayer filtering.", e); //NOI18N
                }
            } else if (expressions instanceof String[]) {
                for(String expression : (String []) expressions) {
                    try {
                        patterns.add(Pattern.compile(expression));
                    } catch (PatternSyntaxException e) {
                        LOG.log(Level.WARNING, "Ignoring invalid regexp for the HighlightsLayer filtering.", e); //NOI18N
                    }
                }
            }
            
            return patterns;
        }
    } // End of RegExpFilter class
    
    private static String simpleToString(Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
    }
    
    private static String mimePathsToString(MimePath... mimePaths) {
        if (mimePaths == null) {
            return "null";
        } else {
            StringBuilder sb = new StringBuilder();
            
            sb.append('{'); //NOI18N
            for(MimePath mp : mimePaths) {
                sb.append('\'').append(mp.getPath()).append('\''); //NOI18N
                sb.append(","); //NOI81N
            }
            sb.append('}'); //NOI18N
            
            return sb.toString();
        }
    }
}
