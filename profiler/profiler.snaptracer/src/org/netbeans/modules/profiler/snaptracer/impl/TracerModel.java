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

package org.netbeans.modules.profiler.snaptracer.impl;

import java.io.IOException;
import org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode;
import org.netbeans.modules.profiler.snaptracer.PackageStateHandler;
import org.netbeans.modules.profiler.snaptracer.ProbeStateHandler;
import org.netbeans.modules.profiler.snaptracer.TracerPackage;
import org.netbeans.modules.profiler.snaptracer.TracerProbe;
import org.netbeans.modules.profiler.snaptracer.TracerProbeDescriptor;
import org.netbeans.modules.profiler.snaptracer.impl.timeline.TimelineSupport;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.profiler.snaptracer.Positionable;
import org.openide.util.Exceptions;

/**
 *
 * @author Jiri Sedlacek
 */
public final class TracerModel {

    private static final Logger LOGGER = Logger.getLogger(TracerController.class.getName());

    private final IdeSnapshot snapshot;

    private final Map<TracerPackage, List<TracerProbe>> probesCache = new HashMap<>();
    private final Map<TracerProbe, TracerProbeDescriptor> descriptorsCache = new HashMap<>();

    private final Set<Listener> listeners = new HashSet<>();

    private final TimelineSupport timelineSupport;


    // --- Constructor ---------------------------------------------------------

    public TracerModel(IdeSnapshot snapshot) {
        this.snapshot = snapshot;
        timelineSupport = new TimelineSupport(new TimelineSupport.DescriptorResolver() {
            public TracerProbeDescriptor getDescriptor(TracerProbe p) {
                return TracerModel.this.getDescriptor(p);
            }
        }, snapshot);
    }


    // --- DataSource ----------------------------------------------------------

    IdeSnapshot getSnapshot() {
        return snapshot;
    }
    
    int getSamplesCount() {
        return snapshot.getSamplesCount();
    }

    long firstTimestamp() {
        return getTimestamp(0);
    }

    long lastTimestamp() {
        return getTimestamp(getSamplesCount() - 1);
    }

    long getTimestamp(int sampleIndex) {
        try {
            return snapshot.getTimestamp(sampleIndex) / 1000000;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return -1;
        }
    }


    // --- Packages ------------------------------------------------------------

    List<TracerPackage> getPackages() {
        try {
            return TracerSupportImpl.getInstance().getPackages(snapshot);
        } catch (Throwable t) {
            LOGGER.log(Level.INFO, "Package exception in getPackages", t); // NOI18N
            return null;
        }
    }


    // --- Probes --------------------------------------------------------------

    void addDescriptor(final TracerPackage p,
                       final TracerProbeDescriptor d) {
        TracerSupportImpl.getInstance().perform(new Runnable() {
            public void run() { addProbe(p, d); }
        });
    }

    void removeDescriptor(final TracerPackage p,
                          final TracerProbeDescriptor d) {
        TracerSupportImpl.getInstance().perform(new Runnable() {
            public void run() { removeProbe(p, d); }
        });
    }
    
    void addDescriptors(final TracerPackage p,
                       final TracerProbeDescriptor[] da) {
        for (TracerProbeDescriptor d : da) addProbe(p, d);
    }

    void removeDescriptors(final TracerPackage p,
                          final TracerProbeDescriptor[] da) {
        for (TracerProbeDescriptor d : da) removeProbe(p, d);
    }

    TracerProbeDescriptor getDescriptor(TracerProbe p) {
        synchronized(descriptorsCache) {
            return descriptorsCache.get(p);
        }
    }

    // Must be called in EDT
    List<TracerProbe> getDefinedProbes() {
        List<TracerProbe> probes = new ArrayList<>();
        probes.addAll(timelineSupport.getProbes());
        return probes;
    }

    Set<Map.Entry<TracerPackage, List<TracerProbe>>> getDefinedProbeSets() {
        Comparator<Map.Entry<TracerPackage, List<TracerProbe>>> comp =
                new Comparator<Map.Entry<TracerPackage, List<TracerProbe>>>() {
            public int compare(Entry<TracerPackage, List<TracerProbe>> o1,
                               Entry<TracerPackage, List<TracerProbe>> o2) {
                return Positionable.STRONG_COMPARATOR.compare(o1.getKey(), o2.getKey());
            }
        };
        Set<Map.Entry<TracerPackage, List<TracerProbe>>> probes = new TreeSet<>(comp);
        synchronized(probesCache) { probes.addAll(probesCache.entrySet()); }
        return probes;
    }

    boolean areProbesDefined() {
        synchronized(probesCache) { return !probesCache.isEmpty(); }
    }


    private void addProbe(TracerPackage p, TracerProbeDescriptor d) {
        TracerProbe r = p.getProbe(d);
        synchronized(descriptorsCache) {
            descriptorsCache.put(r, d);
        }
        synchronized(probesCache) {
            List<TracerProbe> probes = probesCache.get(p);
            if (probes == null) {
                probes = new ArrayList<>();
                probesCache.put(p, probes);
            }
            probes.add(r);
        }

        timelineSupport.addProbe(r);

        notifyProbeAdded(p, r);
        fireProbeAdded(r);
    }

    private void removeProbe(TracerPackage p, TracerProbeDescriptor d) {
        TracerProbe probe = null;
        boolean probesDefined = true;

        synchronized(descriptorsCache) {
            Iterator<Map.Entry<TracerProbe, TracerProbeDescriptor>> iter =
                    descriptorsCache.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<TracerProbe, TracerProbeDescriptor> entry = iter.next();
                if (entry.getValue() == d) {
                    probe = entry.getKey();
                    break;
                }
            }
            descriptorsCache.remove(probe);
        }
        synchronized(probesCache) {
            List<TracerProbe> probes = probesCache.get(p);
            probes.remove(probe);
            if (probes.isEmpty()) {
                probesCache.remove(p);
                probesDefined = !probesCache.isEmpty();
            }
        }

        timelineSupport.removeProbe(probe);

        notifyProbeRemoved(p, probe);
        fireProbeRemoved(probe, probesDefined);
    }

    private void notifyProbeAdded(TracerPackage p, TracerProbe r) {
        PackageStateHandler ph = p.getStateHandler();
        if (ph != null) try {
            ph.probeAdded(r, snapshot);
        } catch (Throwable t) {
            LOGGER.log(Level.INFO, "Package exception in probeAdded", t); // NOI18N
        }

        ProbeStateHandler rh = r.getStateHandler();
        if (rh != null) try {
            rh.probeAdded(snapshot);
        } catch (Throwable t) {
            LOGGER.log(Level.INFO, "Probe exception in probeAdded", t); // NOI18N
        }
    }

    private void notifyProbeRemoved(TracerPackage p, TracerProbe r) {
        PackageStateHandler ph = p.getStateHandler();
        if (ph != null) try {
            ph.probeRemoved(r, snapshot);
        } catch (Throwable t) {
            LOGGER.log(Level.INFO, "Package exception in probeRemoved", t); // NOI18N
        }

        ProbeStateHandler rh = r.getStateHandler();
        if (rh != null) try {
            rh.probeRemoved(snapshot);
        } catch (Throwable t) {
            LOGGER.log(Level.INFO, "Probe exception in probeRemoved", t); // NOI18N
        }
    }


    // --- Events support ------------------------------------------------------

    void addListener(Listener listener) {
        synchronized(listeners) { listeners.add(listener); }
    }

    void removeListener(Listener listener) {
        synchronized(listeners) { listeners.remove(listener); }
    }

    private void fireProbeAdded(final TracerProbe probe) {
        final Set<Listener> toNotify = new HashSet<>();
        synchronized(listeners) { toNotify.addAll(listeners); }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (Listener listener : toNotify)
                    listener.probeAdded(probe);
            }
        });
        
    }

    private void fireProbeRemoved(final TracerProbe probe, final boolean probesDefined) {
        final Set<Listener> toNotify = new HashSet<>();
        synchronized(listeners) { toNotify.addAll(listeners); }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (Listener listener : toNotify)
                    listener.probeRemoved(probe, probesDefined);
            }
        });
    }

    static interface Listener {

        public void probeAdded(TracerProbe probe);

        public void probeRemoved(TracerProbe probe, boolean probesDefined);

    }


    // --- Timeline ------------------------------------------------------------

    TimelineSupport getTimelineSupport() {
        return timelineSupport;
    }

    List<Integer> getIntervals(PrestimeCPUCCTNode node) throws IOException {
        TimelineSupport support = getTimelineSupport();
        final int startIndex = Math.min(support.getStartIndex(), support.getEndIndex());
        final int endIndex = Math.max(support.getStartIndex(), support.getEndIndex());
        
        return getSnapshot().getIntervals(startIndex,endIndex,node);
    }

}
