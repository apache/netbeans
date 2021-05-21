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
package org.netbeans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class NetigsoHandle {
    private final ModuleManager mgr;
    // @GuardedBy("toEnable")
    private final ArrayList<Module> toEnable = new ArrayList<Module>();
    // @GuardedBy("NetigsoFramework.class")    
    private NetigsoFramework framework;
    // @GuardedBy("this")
    private List<NetigsoModule> toInit = new ArrayList<NetigsoModule>();
    
    NetigsoHandle(ModuleManager mgr) {
        this.mgr = mgr;
    }
    
    final NetigsoFramework getDefault() {
        return getDefault(null);
    }

    private NetigsoFramework getDefault(Lookup lkp) {
        synchronized (NetigsoFramework.class) {
            if (framework != null) {
                return framework;
            }
        }
        
        NetigsoFramework created = null;
        if (lkp != null) {
            NetigsoFramework prototype = lkp.lookup(NetigsoFramework.class);
            if (prototype == null) {
                throw new IllegalStateException("No NetigsoFramework found, is org.netbeans.core.netigso module enabled?"); // NOI18N
            }
            created = prototype.bindTo(mgr);
        }
        
        synchronized (NetigsoFramework.class) {
            if (framework == null && created != null) {
                framework = created;
            }
            return framework;
        }
    }
    /** Used on shutdown */
    final void shutdownFramework() {
        NetigsoFramework f;
        synchronized (NetigsoFramework.class) {
            f = framework;
            framework = null;
            toInit = new ArrayList<NetigsoModule>();
            synchronized (toEnable) {
                toEnable.clear();
            }
        }
        if (f != null) {
            f.shutdown();
        }
    }

    final void willEnable(List<Module> newlyEnabling) {
        synchronized (toEnable) {
            toEnable.addAll(newlyEnabling);
        }
    }

    final Set<Module> turnOn(ClassLoader findNetigsoFrameworkIn, Collection<Module> allModules) throws InvalidException {
        boolean found = false;
        if (getDefault() == null) {
            synchronized (toEnable) {
                for (Module m : toEnable) {
                    if (m instanceof NetigsoModule) {
                        found = true;
                        break;
                    }
                }
            }
        } else {
            found = true;
        }
        if (!found) {
            return Collections.emptySet();
        }
        final Lookup lkp = Lookups.metaInfServices(findNetigsoFrameworkIn);
        getDefault(lkp).prepare(lkp, allModules);
        synchronized (toEnable) {
            toEnable.clear();
            toEnable.trimToSize();
        }
        delayedInit(mgr);
        Set<String> cnbs = getDefault().start(allModules);
        if (cnbs == null) {
            return Collections.emptySet();
        }

        Set<Module> additional = new HashSet<Module>();
        for (Module m : allModules) {
            if (!m.isEnabled() && cnbs.contains(m.getCodeNameBase())) {
                additional.add(m);
            }
        }
        return additional;
    }

    private boolean delayedInit(ModuleManager mgr) throws InvalidException {
        List<NetigsoModule> init;
        synchronized (this) {
            init = toInit;
            toInit = null;
            if (init == null || init.isEmpty()) {
                return true;
            }
        }
        Set<NetigsoModule> problematic = new HashSet<NetigsoModule>();
        for (NetigsoModule nm : init) {
            try {
                nm.start();
            } catch (IOException ex) {
                nm.setEnabled(false);
                InvalidException invalid = new InvalidException(nm, ex.getMessage());
                nm.setProblem(invalid);
                problematic.add(nm);
            }
        }
        if (!problematic.isEmpty()) {
            mgr.getEvents().log(Events.FAILED_INSTALL_NEW, problematic);
        }
        
        return problematic.isEmpty();
    }

    synchronized void classLoaderUp(NetigsoModule nm) throws IOException {
        if (toInit != null) {
            toInit.add(nm);
            return;
        }
        List<Module> clone;
        synchronized (toEnable) {
            @SuppressWarnings("unchecked")
            List<Module> cloneTmp = (List<Module>) toEnable.clone();
            clone = cloneTmp;
            toEnable.clear();
        }
        if (!clone.isEmpty()) {
            getDefault().prepare(Lookup.getDefault(), clone);
        }
        nm.start();
    }

    synchronized void classLoaderDown(NetigsoModule nm) {
        if (toInit != null) {
            toInit.remove(nm);
        }
    }

    final void startFramework() {
        if (getDefault() != null) {
            getDefault().start();
        }
    }


    final ClassLoader findFallbackLoader() {
        NetigsoFramework f = getDefault();
        if (f == null) {
            return null;
        }
        
        ClassLoader frameworkLoader = f.findFrameworkClassLoader();
        
        Class<?>[] stack = TopSecurityManager.getStack();
        for (int i = 0; i < stack.length; i++) {
            ClassLoader sl = stack[i].getClassLoader();
            if (sl == null) {
                continue;
            }
            if (sl.getClass().getClassLoader() == frameworkLoader) {
                return stack[i].getClassLoader();
            }
        }
        return null;
    }
    
}
