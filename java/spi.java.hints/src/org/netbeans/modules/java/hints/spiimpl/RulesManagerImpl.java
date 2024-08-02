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

package org.netbeans.modules.java.hints.spiimpl;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.providers.spi.ClassPathBasedHintProvider;
import org.netbeans.modules.java.hints.providers.spi.ElementBasedHintProvider;
import org.netbeans.modules.java.hints.providers.spi.HintProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=RulesManager.class)
public class RulesManagerImpl extends RulesManager {

    private final Map<HintMetadata, Collection<HintDescription>> globalHints = new HashMap<>();

    public RulesManagerImpl() {
        reload();
    }
    
    @Override
    public void reload() {
        globalHints.clear();

        for (HintProvider p : Lookup.getDefault().lookupAll(HintProvider.class)) {
            Map<HintMetadata, ? extends Collection<? extends HintDescription>> pHints = p.computeHints();

            if (pHints != null) {
                for (Entry<HintMetadata, ? extends Collection<? extends HintDescription>> e : pHints.entrySet()) {
                    globalHints.put(e.getKey(), new ArrayList<>(e.getValue()));
                }
            }
        }
    }
    
    private final Map<ClasspathInfo, Reference<Holder>> compoundPathCache = new WeakHashMap<>();
    
    /**
     * Holds a reference to a composite CP created from the ClasspathInfo. Attaches as listener
     * to the ClasspathInfo, so it should live at least as so long as the original ClasspathInfo.
     * Does not reference CPInfo, so it may be stored as a WHM value - but it references CPInfo components.
     * GC may free Holders together with their original CPInfos.
     */
    private static final class Holder implements ChangeListener {
        private final ClassPath compound;
        
        public Holder(ClasspathInfo cpInfo) {
            cpInfo.addChangeListener(this);
            ClassPath[] cps = new ClassPath[] {
                cpInfo.getClassPath(PathKind.BOOT),
                cpInfo.getClassPath(PathKind.COMPILE),
                cpInfo.getClassPath(PathKind.SOURCE)
            };
            compound = ClassPathSupport.createProxyClassPath(cps);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
        }
        
    }

    @Override
    public Map<HintMetadata, ? extends Collection<? extends HintDescription>> readHints(CompilationInfo info, Collection<? extends ClassPath> from, AtomicBoolean cancel) {
        Map<HintMetadata, Collection<HintDescription>> result = new HashMap<>(globalHints);

        if (info != null) {
            for (ElementBasedHintProvider provider : Lookup.getDefault().lookupAll(ElementBasedHintProvider.class)) {
                sortByMetadata(provider.computeHints(info), result);
            }
        }
        
        ClassPath compound;
        
        if (from != null) {
            // not cached, probably not invoked that much
            compound = ClassPathSupport.createProxyClassPath(from.toArray(ClassPath[]::new));
        } else {
            OK: if (info != null) {
                synchronized (compoundPathCache) {
                    ClasspathInfo cpInfo = info.getClasspathInfo();
                    Reference<Holder> cpRef = compoundPathCache.get(cpInfo);
                    if (cpRef != null) {
                        Holder cp = cpRef.get();
                        if (cp != null) {
                            compound = cp.compound;
                            break OK;
                        }
                    }
                    Holder h = new Holder(cpInfo);
                    compoundPathCache.put(cpInfo, new WeakReference<>(h));
                    compound = h.compound;
                }
            } else {
                compound = ClassPathSupport.createClassPath(new FileObject[0]);
            }
        }

        for (ClassPathBasedHintProvider p : Lookup.getDefault().lookupAll(ClassPathBasedHintProvider.class)) {
            Collection<? extends HintDescription> hints = p.computeHints(compound, cancel);

            if (hints == null || (cancel != null && cancel.get())) return null;
            
            sortByMetadata(hints, result);
        }

        return result;
    }

    public static void sortByMetadata(Collection<? extends HintDescription> listedHints, Map<HintMetadata, Collection<HintDescription>> into) {
        for (HintDescription hd : listedHints) {
            into.computeIfAbsent(hd.getMetadata(), k -> new ArrayList<>())
                .add(hd);
        }
    }

}
