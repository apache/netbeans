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
package org.netbeans.conffile.ui;

import org.netbeans.conffile.MemoryValue;
import org.netbeans.conffile.LineSwitchContributor;
import org.netbeans.conffile.LineSwitchWriter;
import org.netbeans.conffile.OS;
import org.netbeans.conffile.ui.comp.AALabel;
import java.awt.Dimension;
import java.awt.Font;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;

/**
 * Models the set of -Xmx and -Xms values and can produce slider models to set
 * them without ending up with impossible values.
 *
 * @author Tim Boudreau
 */
final class MemorySettings implements LineSwitchContributor, IntConsumer {

    private MemoryValue xmx;
    private MemoryValue xms;
    private final List<MemoryValue> memoryChoices;
    private final List<WeakReference<Runnable>> listeners = new ArrayList<>();
    private long totalMemBytes;
    private final boolean hasGcSettings;

    MemorySettings(boolean hasGcSettings) {
        this(null, null, hasGcSettings);
    }

    MemorySettings(MemoryValue currentXmx, MemoryValue currentXms, boolean hasGcSettings) {
        this.hasGcSettings = hasGcSettings;
        totalMemBytes = OS.get().getMemorySize();
        if (totalMemBytes == 0) {
            totalMemBytes = 4L * MemoryValue.GIGABYTE;
        }
        memoryChoices = MemoryValue.jvmRange(totalMemBytes / 3, currentXmx, currentXms);
        xmx = currentXmx != null ? currentXmx
                : MemoryValue.findNearestAtOrBelow(totalMemBytes / 4, memoryChoices);
        xms = currentXms != null ? currentXms : MemoryValue.findNearestFraction(4, xmx, memoryChoices);

        // Ensure we can't have impossible ranges
        MemoryValue[] sort = new MemoryValue[]{xms, xmx};
        Arrays.sort(sort);
        xms = sort[0];
        xmx = sort[1];
    } // assume 4g if could not be dtermined

    public long totalMemoryInBytes() {
        return totalMemBytes;
    }

    public String memorySize() {
        long kb = 1024;
        long mb = kb * kb;
        long gb = kb * kb * kb;
        long tb = kb * kb * kb * kb;
        if (totalMemBytes == 0) {
            return Localization.ERR_NO_MEMORY_SIZE.toString();
        }
        DecimalFormat fmt = new DecimalFormat("###0.#");
        double val;
        if (totalMemBytes > tb) {
            // For completeness...
            if (totalMemBytes % tb == 0) {
                return Localization.TERABYTES.format(totalMemBytes / tb);
            }
            val = (double) totalMemBytes / (double) tb;
            return Localization.TERABYTES.format(fmt.format(val));
        } else if (totalMemBytes > gb) {
            if (totalMemBytes % gb == 0) {
                return Localization.GIGABYTES.format(totalMemBytes / gb);
            }
            val = (double) totalMemBytes / (double) gb;
            return Localization.GIGABYTES.format(fmt.format(val));
        } else if (totalMemBytes > mb) {
            if (totalMemBytes % mb == 0) {
                return Localization.MEGABYTES.format(totalMemBytes / mb);
            }
            val = (double) totalMemBytes / (double) mb;
            return Localization.MEGABYTES.format(fmt.format(val));
        } else if (totalMemBytes > kb) {
            // for completeness
            if (totalMemBytes % kb == 0) {
                return Localization.KILOBYTES.format(totalMemBytes / kb);
            }
            val = (double) totalMemBytes / (double) kb;
            return Localization.KILOBYTES.format(fmt.format(val));
        } else {
            return Localization.BYTES.format(totalMemBytes);
        }
    }

    List<MemoryValue> choices() {
        return memoryChoices;
    }

    public MemoryValue xmx() {
        return xmx;
    }

    public MemoryValue xms() {
        return xms;
    }

    void listen(Runnable r) {
        listeners.add(new WeakReference<>(r));
    }

    private boolean added;
    @SuppressWarnings({"UseOfObsoleteCollectionType", "deprecation"})
    private List<WeakReference<java.util.Hashtable<Integer, JComponent>>> dictionaries
            = new ArrayList<>();
    private ConfFileSettings settings;

    @SuppressWarnings({"UseOfObsoleteCollectionType", "deprecation"})
    Dictionary dictionary(ConfFileSettings settings) {
        this.settings = settings;
        float fontSize = (float) Math.max(7, settings.uiFont().getSize() - 4);
        Font f = settings.uiFont().deriveFont(fontSize);
        // no choice here
        @SuppressWarnings(value = {"UseOfObsoleteCollectionType", "deprecation"})
        java.util.Hashtable<Integer, JComponent> dictionary = new java.util.Hashtable<>(memoryChoices.size());
        for (int i = 0; i < memoryChoices.size(); i++) {
            AALabel lbl = new BiggerAALabel(memoryChoices.get(i).toString());
            lbl.setFont(f);
            dictionary.put(i, lbl);
        }
        if (!added) {
            added = true;
//            settings.onFontSizeRecomputed(this);
        }
        dictionaries.add(new WeakReference<>(dictionary));
        return dictionary;
    }

    static class BiggerAALabel extends AALabel {

        public BiggerAALabel(String text) {
            super(text);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension result = super.getPreferredSize();
            result.width += 5;
            return result;
        }
    }

    MemorySettings change() {
        for (Iterator<WeakReference<Runnable>> it = listeners.iterator(); it.hasNext();) {
            WeakReference<Runnable> w = it.next();
            Runnable r = w.get();
            if (r == null) {
                it.remove();
            } else {
                r.run();
            }
        }
        return this;
    }

    MemorySettings update(MemoryValue xmx, MemoryValue xms) {
        boolean changed = !this.xmx.equals(xmx) || !this.xms.equals(xms);
        if (changed) {
            this.xmx = xmx;
            this.xms = xms;
            return change();
        }
        return this;
    }

    void models(QuadConsumer<BoundedRangeModel, Supplier<String>, BoundedRangeModel, Supplier<String>> c) {
        MemorySlidersController mdls = new MemorySlidersController();
        c.accept(mdls.xmxModel, mdls::xmxName, mdls.xmsModel, mdls::xmsName);
    }

    @Override
    public void contribute(LineSwitchWriter writer) {
        writer.appendOrReplaceArguments(xmx.toMaxHeapString());
        writer.appendOrReplaceArguments(xms.toInitialHeapString());
        if (xmx.isGigabyteOrGreater()) {
            if (!hasGcSettings) {
                // Better choice of garbage collector for gigabyte heaps
                // XXX could try ZGC on JDK 12?
                writer.appendOrReplaceArguments("-J-XX:+UseG1GC");
                writer.appendOrReplaceArguments("-J-XX:MaxGCPauseMillis=150");
            }
            if (OS.get() == OS.LINUX) {
                // Useful for gigabyte heaps, if the kernel supports it,
                // harmless if it doesn't - the JVM uses madvise to allocate
                // in larger blocks of memory
                writer.appendOrReplaceArguments("-J-XX:+UseTransparentHugePages");
                // Note this option will probably use TLB mapping rather than
                // the newer transparent huge pages
                writer.appendOrReplaceArguments("-J-XX:+UseLargePagesInMetaspace");
            }
        }
    }

    @Override
    @SuppressWarnings({"UseOfObsoleteCollectionType", "deprecation"})
    public void accept(int value) {
        if (settings != null) {
            Font f = settings.uiFont().deriveFont((float) Math.max(7, settings.uiFont().getSize() - 7));
            for (Iterator<WeakReference<java.util.Hashtable<Integer, JComponent>>> it = dictionaries.iterator(); it.hasNext();) {
                WeakReference<java.util.Hashtable<Integer, JComponent>> item = it.next();
                java.util.Hashtable<Integer, JComponent> dict = item.get();
                if (dict == null) {
                    it.remove();
                } else {
                    dict.forEach((ignored, comp) -> {
                        comp.setFont(f);
                    });
                }
            }
        }
    }

    class MemorySlidersController {

        DefaultBoundedRangeModel xmsModel = new DefaultBoundedRangeModel();
        DefaultBoundedRangeModel xmxModel = new DefaultBoundedRangeModel();

        MemorySlidersController() {
            xmxModel.setRangeProperties(memoryChoices.indexOf(xmx), 1, 0, memoryChoices.size(), false);
            xmsModel.setRangeProperties(memoryChoices.indexOf(xms), 1, 0, memoryChoices.size(), false);
            xmxModel.addChangeListener(this::updateXmsForXmx);
            xmsModel.addChangeListener(this::updateForXmsChange);
        }

        String xmxName() {
            int index = xmxModel.getValue();
            return memoryChoices.get(index).toString();
        }

        String xmsName() {
            int index = xmsModel.getValue();
            return memoryChoices.get(index).toString();
        }

        void updateForXmsChange(ChangeEvent e) {
            int xmxValue = xmxModel.getValue();
            int xmsValue = xmsModel.getValue();
            if (xmsValue > xmxValue) {
                xmxValue = xmsValue;
                xmxModel.setValue(xmxValue);
            }
            MemoryValue xmx = memoryChoices.get(xmxValue);
            MemoryValue xms = memoryChoices.get(xmsValue);
            update(xmx, xms);
        }

        void updateXmsForXmx(ChangeEvent e) {
            int xmxValue = xmxModel.getValue();
            int xmsValue = xmsModel.getValue();
            if (xmsValue >= xmxValue) {
                xmsValue = Math.max(0, xmxValue - 1);
            }
            xmsModel.setRangeProperties(xmsValue, 1, 0, memoryChoices.size(), false);
            MemoryValue xmx = memoryChoices.get(xmxValue);
            MemoryValue xms = memoryChoices.get(xmsValue);
            update(xmx, xms);
        }
    }

}
