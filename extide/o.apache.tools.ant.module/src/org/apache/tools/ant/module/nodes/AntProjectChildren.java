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

package org.apache.tools.ant.module.nodes;

import java.io.IOException;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

final class AntProjectChildren extends Children.Keys<TargetLister.Target> implements ChangeListener, Comparator<TargetLister.Target> {
    
    private static final RequestProcessor RP = new RequestProcessor(AntProjectChildren.class);
    private static Collator SORTER = Collator.getInstance();
    
    private final AntProjectCookie cookie;
    private SortedSet<TargetLister.Target> allTargets;
    
    public AntProjectChildren (AntProjectCookie cookie) {
        super ();
        this.cookie = cookie;
    }
    
    @Override
    protected void addNotify () {
        super.addNotify ();
        RP.post(new Runnable() {

            @Override
            public void run() {
                refreshKeys(true);
            }
        });
        
        cookie.addChangeListener (this);
    }

    @Override
    protected void removeNotify () {
        super.removeNotify ();
        setKeys(Collections.<TargetLister.Target>emptySet());
        synchronized (this) {
            allTargets = null;
        }
        cookie.removeChangeListener (this);
    }

    private void refreshKeys(boolean createKeys) {
        try {
            Set<TargetLister.Target> _allTargets = TargetLister.getTargets(cookie);
            Collection<TargetLister.Target> keys;
            synchronized (this) {
                if (allTargets == null && !createKeys) {
                    // Aynch refresh after removeNotify; ignore. (#44428)
                    return;
                }
                allTargets = new TreeSet<TargetLister.Target>(this);
                allTargets.addAll(_allTargets);
                Iterator<TargetLister.Target> it = allTargets.iterator();
                while (it.hasNext()) {
                    TargetLister.Target t = it.next();
                    if (t.isOverridden()) {
                        // Don't include these.
                        it.remove();
                    }
                }
                keys = allTargets;
            }
            if (keys != null) { // #65235
                setKeys(keys);
            }
        } catch (IOException e) {
            // XXX should mark the project node as being somehow in error
            AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
            setKeys(Collections.<TargetLister.Target>emptySet());
        }
    }
    
    protected Node[] createNodes(TargetLister.Target key) {
        return new Node[] {new AntTargetNode(cookie, key)};
    }
    
    public void stateChanged (ChangeEvent ev) {
        refreshKeys(false);
    }
    
    public int compare(TargetLister.Target t1, TargetLister.Target t2) {
        int x = SORTER.compare(t1.getName(), t2.getName());
        if (x != 0 || t1 == t2) {
            return x;
        } else {
            // #44491: was not displaying overridden targets.
            return System.identityHashCode(t1) - System.identityHashCode(t2);
        }
    }
    
}
