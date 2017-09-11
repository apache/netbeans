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
    
    private final static RequestProcessor RP = new RequestProcessor(AntProjectChildren.class);
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
