/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.spiimpl;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
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

    private final Map<HintMetadata, Collection<HintDescription>> globalHints = new HashMap<HintMetadata, Collection<HintDescription>>();

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
                    globalHints.put(e.getKey(), new ArrayList<HintDescription>(e.getValue()));
                }
            }
        }
    }
    
    private final Map<ClasspathInfo, Reference<Holder>> compoundPathCache = new WeakHashMap<>();
    
    /**
     * Holds a refernce to a composite CP created from the ClasspathInfo. Attaches as listener
     * to the ClasspathInfo, so it should live at least as so long as the original ClasspathInfo.
     * Does not reference CPInfo, so it may be stored as a WHM value - but it references CPInfo components.
     * GC may free Holders together with their original CPInfos.
     */
    private static final class Holder implements ChangeListener {
        private final ClassPath compound;
        
        public Holder(ClasspathInfo cpInfo) {
            cpInfo.addChangeListener(this);
            LinkedList<ClassPath> cps = new LinkedList<ClassPath>();
            cps.add(cpInfo.getClassPath(PathKind.BOOT));
            cps.add(cpInfo.getClassPath(PathKind.COMPILE));
            cps.add(cpInfo.getClassPath(PathKind.SOURCE));
            compound = ClassPathSupport.createProxyClassPath(cps.toArray(new ClassPath[0]));
        }

        @Override
        public void stateChanged(ChangeEvent e) {
        }
        
    }

    @Override
    public Map<HintMetadata, ? extends Collection<? extends HintDescription>> readHints(CompilationInfo info, Collection<? extends ClassPath> from, AtomicBoolean cancel) {
        Map<HintMetadata, Collection<HintDescription>> result = new HashMap<HintMetadata, Collection<HintDescription>>(globalHints);

        if (info != null) {
            for (ElementBasedHintProvider provider : Lookup.getDefault().lookupAll(ElementBasedHintProvider.class)) {
                sortByMetadata(provider.computeHints(info), result);
            }
        }
        
        ClassPath compound;
        
        if (from != null) {
            // not cached, probably not invoked that much
            compound = ClassPathSupport.createProxyClassPath(from.toArray(new ClassPath[0]));
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
            Collection<HintDescription> h = into.get(hd.getMetadata());

            if (h == null) {
                into.put(hd.getMetadata(), h = new ArrayList<HintDescription>());
            }

            h.add(hd);
        }
    }

}
