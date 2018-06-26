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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.deployment.plugins.spi.support;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.plugins.spi.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Factory for lookup capable of merging content from registered 
 * {@link org.netbeans.spi.project.LookupProvider} instances.
 * @author phejl, mkleint
 * @since 1.50
 */
public final class LookupProviderSupport {
    
    private LookupProviderSupport() {
    }
    
    /**
     * Creates a platform lookup instance that combines the content from multiple sources.
     * A convenience factory method for implementors of
     * {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform}.
     * 
     * @param baseLookup initial, base content of the project lookup created by the plugin
     * @param folderPath the path in the System Filesystem that is used as root for lookup composition, as for {@link Lookups#forPath}.
     *        The content of the folder is assumed to be {@link LookupProvider} instances.
     * @return a lookup to be used in platform
     */ 
    public static Lookup createCompositeLookup(Lookup baseLookup, String folderPath) {
        return new DelegatingLookupImpl(baseLookup, folderPath);
    }
    
    static class DelegatingLookupImpl extends ProxyLookup implements LookupListener {
        private Lookup baseLookup;
        private Lookup.Result<LookupProvider> providerResult;
        private LookupListener providerListener;
        private List<LookupProvider> old = Collections.emptyList();
        private List<Lookup> currentLookups;

        //#68623: the proxy lookup fires changes only if someone listens on a particular template:
        private List<Lookup.Result<?>> results = new ArrayList<Lookup.Result<?>>();
        
        public DelegatingLookupImpl(Lookup base, String path) {
            this(base, Lookups.forPath(path), path);
        }
        
        public DelegatingLookupImpl(Lookup base, Lookup providerLookup, String path) {
            super();
            assert base != null;
            baseLookup = base;
            providerResult = providerLookup.lookup(new Lookup.Template<LookupProvider>(LookupProvider.class));
            assert isAllJustLookupProviders(providerLookup) : 
                "Layer content at " + path + " contains other than LookupProvider instances! See messages.log file for more details."; //NOI18N
            doDelegate(providerResult.allInstances());
            providerListener = new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    doDelegate(providerResult.allInstances());
                }
            };
            providerResult.addLookupListener(
                WeakListeners.create(LookupListener.class, providerListener, providerResult));
        }
        
        //just for assertion evaluation.
        private boolean isAllJustLookupProviders(Lookup lkp) {
            Lookup.Result<Object> res = lkp.lookupResult(Object.class);
            Set<Class<?>> set = res.allClasses();
            for (Class clzz : set) {
                if (!LookupProvider.class.isAssignableFrom(clzz)) {
                    Logger.getLogger(LookupProviderSupport.class.getName()).warning("" + clzz.getName() + " is not instance of LookupProvider."); //NOI18N
                    return false;
                }
            }
            return true;
        }
        
        
        public void resultChanged(LookupEvent ev) {
            doDelegate(providerResult.allInstances());
        }
        
        
        private synchronized void doDelegate(Collection<? extends LookupProvider> providers) {
            //unregister listeners from the old results:
            for (Lookup.Result<?> r : results) {
                r.removeLookupListener(this);
            }
            
            List<Lookup> newLookups = new ArrayList<Lookup>();
            for (LookupProvider elem : providers) {
                if (old.contains(elem)) {
                    int index = old.indexOf(elem);
                    newLookups.add(currentLookups.get(index));
                } else {
                    Lookup newone = elem.createAdditionalLookup(baseLookup);
                    assert newone != null;
                    newLookups.add(newone);
                }
            }
            old = new ArrayList<LookupProvider>(providers);
            currentLookups = newLookups;
            newLookups.add(baseLookup);
            Lookup lkp = new ProxyLookup(newLookups.toArray(new Lookup[newLookups.size()]));
            
            setLookups(lkp);
        }
    }
    
}
