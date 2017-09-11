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

package threaddemo.views;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.looks.Look;
import org.openide.actions.DeleteAction;
import org.openide.actions.NewAction;
import org.openide.actions.OpenAction;
import org.openide.actions.RenameAction;
import org.openide.actions.SaveAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import threaddemo.data.DomProvider;
import threaddemo.data.PhadhailLookups;
import threaddemo.data.PhadhailNewType;
import threaddemo.locking.Locks;
import threaddemo.model.Phadhail;
import threaddemo.model.PhadhailEvent;
import threaddemo.model.PhadhailListener;
import threaddemo.model.PhadhailNameEvent;

/**
 * A look which wraps phadhails.
 * @author Jesse Glick
 */
final class PhadhailLook extends Look<Phadhail> implements PhadhailListener, LookupListener, ChangeListener {
    
    private static final Logger logger = Logger.getLogger(PhadhailLook.class.getName());
    
    private static final Map<Phadhail,Lookup.Result<Object>> phadhails2Results = new IdentityHashMap<Phadhail,Lookup.Result<Object>>();
    private static final Map<Lookup.Result,Phadhail> results2Phadhails = new IdentityHashMap<Lookup.Result,Phadhail>();
    private static final Map<Phadhail,DomProvider> phadhails2DomProviders = new IdentityHashMap<Phadhail,DomProvider>();
    private static final Map<DomProvider,Phadhail> domProviders2Phadhails = new IdentityHashMap<DomProvider,Phadhail>();
    
    PhadhailLook() {
        super("PhadhailLook");
    }
    
    public String getDisplayName() {
        return "Phadhails";
    }
    
    protected void attachTo(Phadhail ph) {
        ph.addPhadhailListener(this);
    }
    
    protected void detachFrom(Phadhail ph) {
        ph.removePhadhailListener(this);
        Lookup.Result<Object> r = phadhails2Results.remove(ph);
        if (r != null) {
            r.removeLookupListener(this);
            assert results2Phadhails.containsKey(r);
            results2Phadhails.remove(r);
        }
        DomProvider p = phadhails2DomProviders.remove(ph);
        if (p != null) {
            p.removeChangeListener(this);
            assert domProviders2Phadhails.containsKey(p);
            domProviders2Phadhails.remove(p);
        }
    }
    
    public boolean isLeaf(Phadhail ph, Lookup e) {
        assert EventQueue.isDispatchThread();
        return !ph.hasChildren() && PhadhailLookups.getLookup(ph).lookup(DomProvider.class) == null;
    }
    
    public List getChildObjects(final Phadhail ph, Lookup e) {
        assert EventQueue.isDispatchThread();
        if (ph.hasChildren()) {
            return ph.getChildren();
        } else {
            DomProvider p = PhadhailLookups.getLookup(ph).lookup(DomProvider.class);
            if (p != null) {
                if (!phadhails2DomProviders.containsKey(ph)) {
                    phadhails2DomProviders.put(ph, p);
                    assert !domProviders2Phadhails.containsKey(p);
                    domProviders2Phadhails.put(p, ph);
                    p.addChangeListener(this);
                    p.start();
                }
                if (p.isReady()) {
                    logger.finer("DOM tree is ready, will ask for its document element");
                    // XXX do this block atomically in a lock?
                    try {
                        return Collections.singletonList(p.getDocument().getDocumentElement());
                    } catch (IOException x) {
                        assert false : x;
                    }
                } else {
                    logger.finer("DOM tree is not ready");
                    p.start();
                    // Cf. PhadhailLookSelector.StringLook:
                    return Collections.singletonList("Please wait...");
                }
            }
            return null;
        }
    }
    
    public String getName(Phadhail ph, Lookup e) {
        assert EventQueue.isDispatchThread();
        return ph.getName();
    }

    public String getDisplayName(Phadhail ph, Lookup e) {
        assert EventQueue.isDispatchThread();
        return ph.getPath();
    }
    
    public boolean canRename(Phadhail ph, Lookup e) {
        return true;
    }
    
    public void rename(Phadhail ph, String newName, Lookup e) throws IOException {
        ph.rename(newName);
    }
    
    public boolean canDestroy(Phadhail ph, Lookup e) {
        return true;
    }
    
    public void destroy(Phadhail ph, Lookup e) throws IOException {
        ph.delete();
        // XXX since this fires no changes of its own...
        fireChange(ph, Look.DESTROY);
    }
    
    public Action[] getActions(Phadhail ph, Lookup e) {
        return new Action[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(SaveAction.class),
            null,
            SystemAction.get(NewAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            SystemAction.get(RenameAction.class),
            //SystemAction.get(ToolsAction.class),
        };
    }
    
    public NewType[] getNewTypes(Phadhail ph, Lookup e) {
        if (ph.hasChildren()) {
            return new NewType[] {
                new PhadhailNewType(ph, false),
                new PhadhailNewType(ph, true),
            };
        } else {
            return new NewType[0];
        }
    }
    
    public Collection getLookupItems(Phadhail ph, Lookup env) {
        assert EventQueue.isDispatchThread();
        Lookup.Result<Object> r = phadhails2Results.get(ph);
        if (r == null) {
            Lookup l = PhadhailLookups.getLookup(ph);
            r = l.lookupResult(Object.class);
            assert r != null : "Null lookup from " + l + " in " + ph;
            phadhails2Results.put(ph, r);
            assert !results2Phadhails.containsKey(r);
            results2Phadhails.put(r, ph);
            r.addLookupListener(this);
        }
        return r.allItems();
    }
    
    public void resultChanged(LookupEvent ev) {
        // XXX #33372: should be able to do ev.getResult()
        Lookup.Result r = (Lookup.Result)ev.getSource();
        final Phadhail ph = results2Phadhails.get(r);
        assert ph != null;
        Locks.event().readLater(new Runnable() {
            public void run() {
                fireChange(ph, Look.GET_LOOKUP_ITEMS);
            }
        });
    }
    
    public void childrenChanged(final PhadhailEvent ev) {
        assert ev.getPhadhail().lock().canRead();
        Locks.event().readLater(new Runnable() {
            public void run() {
                fireChange(ev.getPhadhail(), Look.GET_CHILD_OBJECTS);
            }
        });
    }
    
    public void nameChanged(final PhadhailNameEvent ev) {
        assert ev.getPhadhail().lock().canRead();
        Locks.event().readLater(new Runnable() {
            public void run() {
                fireChange(ev.getPhadhail(), Look.GET_NAME | Look.GET_DISPLAY_NAME);
            }
        });
    }
    
    public void stateChanged(ChangeEvent e) {
        logger.finer("got change");
        DomProvider p = (DomProvider)e.getSource();
        final Phadhail ph = domProviders2Phadhails.get(p);
        assert ph != null;
        Locks.event().readLater(new Runnable() {
            public void run() {
                fireChange(ph, Look.GET_CHILD_OBJECTS);
            }
        });
    }
    
}
