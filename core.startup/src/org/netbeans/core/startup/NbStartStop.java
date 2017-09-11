/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.core.startup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.netbeans.Util;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class NbStartStop implements LookupListener {
    private static final RequestProcessor RP = new RequestProcessor("On Start/Stop", 8); // NOI18N
    
    private final Map<String, RequestProcessor.Task> onStart = new HashMap<String, RequestProcessor.Task>();
    private final Lookup lkpStart;
    private final Lookup lkpStop;
    
    private Lookup.Result<Runnable> resStart;
    

    NbStartStop(Lookup lkp, Lookup lkp2) {
        lkpStart = lkp;
        lkpStop = lkp2;
    }
    
    void initialize() {
        for (Lookup.Item<Runnable> item : onStart().allItems()) {
            synchronized (onStart) {
                RequestProcessor.Task already = onStart.get(item.getId());
                if (already == null) {
                    Runnable r = item.getInstance();
                    if (r != null) {
                        onStart.put(item.getId(), RP.post(r));
                    }
                }
            }
        }
        
    }

    private synchronized Lookup.Result<Runnable> onStart() {
        if (resStart == null) {
            Lookup lkp = lkpStart != null ? lkpStart : Lookups.forPath("Modules/Start"); // NOI18N
            resStart = lkp.lookupResult(Runnable.class);
            resStart.addLookupListener(this);
        }
        return resStart;
    }
    private Lookup onStop() {
        return lkpStop != null ? lkpStop : Lookups.forPath("Modules/Stop"); // NOI18N
    }

    void waitOnStart() {
        RequestProcessor.Task[] all;
        synchronized (onStart) {
            Collection<RequestProcessor.Task> values = onStart.values();
            all = values.toArray(new RequestProcessor.Task[values.size()]);
        }
        for (RequestProcessor.Task t : all) {
            t.waitFinished();
        }
    }
    
    boolean closing(List<? extends ModuleInfo> modules) {
        for (Callable<?> c : onStop().lookupAll(Callable.class)) { // NOI18N
            if (!modules.contains(Modules.getDefault().ownerOf(c.getClass()))) {
                continue;
            }
            try {
                if (Boolean.FALSE.equals(c.call())) {
                    Util.err.log(Level.FINE, "{0} refused to close", c.getClass()); // NOI18N
                    return false;
                }
            } catch (Exception ex) {
                Util.err.log(Level.FINE, c.getClass() + " thrown an exception", ex); // NOI18N
                return false;
            }
        }
        return true;
    }

    List<Task> startClose(List<? extends ModuleInfo> modules) {
        List<Task> waitFor = new ArrayList<Task>();
        for (Runnable r : onStop().lookupAll(Runnable.class)) { // NOI18N
            if (!modules.contains(Modules.getDefault().ownerOf(r.getClass()))) {
                continue;
            }
            waitFor.add(RP.post(r));
        }
        return waitFor;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        initialize();
    }
}
