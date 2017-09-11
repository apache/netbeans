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

package threaddemo.model;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import threaddemo.locking.RWLock;
import threaddemo.locking.LockAction;
import threaddemo.locking.LockExceptionAction;
import threaddemo.locking.Locks;
import threaddemo.locking.Worker;

/**
 * Phadhail model impl using a technique like SwingWorker.
 * Fairly complicated. Methods are broken down into several categories:
 * 1. Simple methods like delete() will just block on the work thread.
 * 2. Ditto hasChildren(), but the result is cached.
 * 3. name + path return a dummy initial value and later fire a change.
 * 4. Ditto children, but then the results must be wrapped too.
 * 5. create* also wraps results.
 * 6. Listeners are added asynch and their callbacks must be posted back to AWT too.
 * For a more complex model, you could use Proxy to do this stuff, if there some kind
 * of map giving the desired thread behavior of each method.
 * @author Jesse Glick
 */
final class SwungPhadhail implements Phadhail, PhadhailListener {
    
    private static final Logger logger = Logger.getLogger(SwungPhadhail.class.getName());
    
    private static final Map<Phadhail, Reference<Phadhail>> instances = new WeakHashMap<Phadhail,Reference<Phadhail>>();
    
    /** factory */
    public static Phadhail forPhadhail(Phadhail _ph) {
        assert EventQueue.isDispatchThread();
        Reference<Phadhail> r = instances.get(_ph);
        Phadhail ph = (r != null) ? r.get() : null;
        if (ph == null) {
            ph = new SwungPhadhail(_ph);
            instances.put(_ph, new WeakReference<Phadhail>(ph));
        }
        return ph;
    }
    
    private final Phadhail ph;
    private String name = null;
    private String path = null;
    private boolean computingName = false;
    private List<Phadhail> children = null;
    private boolean computingChildren = false;
    private Boolean leaf = null;
    private List<PhadhailListener> listeners = null;
    
    private SwungPhadhail(Phadhail ph) {
        this.ph = ph;
    }
    
    private void fireNameChanged() {
        assert EventQueue.isDispatchThread();
        // XXX synch on listeners to get them, then release
        if (listeners != null) {
            PhadhailNameEvent ev = PhadhailNameEvent.create(this, null, null);
            for (PhadhailListener l : listeners) {
                logger.log(Level.FINER, "fireNameChanged for {0} to {1}", new Object[] {this, l});
                l.nameChanged(ev);
            }
        }
    }
    
    private String getNameOrPath(boolean p) {
        assert EventQueue.isDispatchThread();
        if ((p ? path : name) != null) {
            logger.log(Level.FINER, "cached name for {0}", this);
            return (p ? path : name);
        } else {
            if (!computingName) {
                computingName = true;
                logger.log(Level.FINER, "calculating name for {0}", this);
                Worker.start(new Runnable() {
                    public void run() {
                        final String n = ph.getName();
                        final String p = ph.getPath();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                name = n;
                                path = p;
                                computingName = false;
                                logger.log(Level.FINER, "fireNameChanged for {0}", SwungPhadhail.this);
                                fireNameChanged();
                            }
                        });
                    }
                });
            }
            logger.log(Level.FINER, "dummy name for {0}", this);
            return (p ? "Please wait..." : "computingName");
        }
    }
    
    public String getName() {
        return getNameOrPath(false);
    }
    
    public String getPath() {
        return getNameOrPath(true);
    }
    
    private Phadhail createPhadhail(final String name, final boolean container) throws IOException {
        assert EventQueue.isDispatchThread();
        return forPhadhail(Worker.block(new LockExceptionAction<Phadhail,IOException>() {
            public Phadhail run() throws IOException {
                if (container) {
                    return ph.createContainerPhadhail(name);
                } else {
                    return ph.createLeafPhadhail(name);
                }
            }
        }));
    }
    
    public Phadhail createContainerPhadhail(String name) throws IOException {
        return createPhadhail(name, true);
    }
    
    public Phadhail createLeafPhadhail(String name) throws IOException {
        return createPhadhail(name, false);
    }
    
    public void rename(final String nue) throws IOException {
        assert EventQueue.isDispatchThread();
        Worker.block(new LockExceptionAction<Void,IOException>() {
            public Void run() throws IOException {
                ph.rename(nue);
                return null;
            }
        });
    }
    
    public void delete() throws IOException {
        assert EventQueue.isDispatchThread();
        Worker.block(new LockExceptionAction<Void,IOException>() {
            public Void run() throws IOException {
                ph.delete();
                return null;
            }
        });
    }
    
    private void fireChildrenChanged() {
        assert EventQueue.isDispatchThread();
        // XXX synch on listeners to get them, then release
        if (listeners != null) {
            logger.finer("fireChildrenChanged");
            PhadhailEvent ev = PhadhailEvent.create(this);
            for (PhadhailListener l : listeners) {
                l.childrenChanged(ev);
            }
        }
    }
    
    public List<Phadhail> getChildren() {
        assert EventQueue.isDispatchThread();
        if (children != null) {
            return children;
        } else {
            if (!computingChildren) {
                computingChildren = true;
                Worker.start(new Runnable() {
                    public void run() {
                        final List<Phadhail> ch = ph.getChildren();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                children = new SwungChildrenList(ch);
                                computingChildren = false;
                                fireChildrenChanged();
                            }
                        });
                    }
                });
            }
            return Collections.emptyList();
        }
    }
    
    private static final class SwungChildrenList extends AbstractList<Phadhail> {
        private final List<Phadhail> orig;
        private final Phadhail[] kids;
        public SwungChildrenList(List<Phadhail> orig) {
            this.orig = orig;
            kids = new Phadhail[orig.size()];
        }
        public Phadhail get(int i) {
            assert EventQueue.isDispatchThread();
            if (kids[i] == null) {
                kids[i] = forPhadhail(orig.get(i));
            }
             return kids[i];
        }
        public int size() {
            assert EventQueue.isDispatchThread();
            return kids.length;
        }
    }
    
    public InputStream getInputStream() throws IOException {
        assert EventQueue.isDispatchThread();
        return Worker.block(new LockExceptionAction<InputStream,IOException>() {
            public InputStream run() throws IOException {
                return ph.getInputStream();
            }
        });
    }
    
    public OutputStream getOutputStream() throws IOException {
        assert EventQueue.isDispatchThread();
        return Worker.block(new LockExceptionAction<OutputStream,IOException>() {
            public OutputStream run() throws IOException {
                return ph.getOutputStream();
            }
        });
    }
    
    public boolean hasChildren() {
        assert EventQueue.isDispatchThread();
        logger.log(Level.FINER, "hasChildren on {0}", this);
        if (leaf == null) {
            logger.finer("not cached");
            leaf = Worker.block(new LockAction<Boolean>() {
                public Boolean run() {
                    logger.finer("hasChildren: working...");
                    return ph.hasChildren();
                }
            });
            logger.log(Level.FINER, "leaf={0}", leaf);
        }
        return !leaf.booleanValue();
    }
    
    public synchronized void addPhadhailListener(PhadhailListener l) {
        if (listeners == null) {
            listeners = new ArrayList<PhadhailListener>();
            ph.addPhadhailListener(SwungPhadhail.this);
        }
        listeners.add(l);
    }
    
    public synchronized void removePhadhailListener(PhadhailListener l) {
        if (listeners != null && listeners.remove(l) && listeners.isEmpty()) {
            listeners = null;
            ph.removePhadhailListener(SwungPhadhail.this);
        }
    }
    
    public String toString() {
        return "SwungPhadhail<" + ph + ">";
    }
    
    public void childrenChanged(PhadhailEvent ev) {
        // XXX should this go ahead and compute them now?
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                children = null;
                computingChildren = false; // XXX right?
                fireChildrenChanged();
            }
        });
    }
    
    public void nameChanged(PhadhailNameEvent ev) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                name = null;
                path = null;
                computingName = false;
                fireNameChanged();
            }
        });
    }
    
    public RWLock lock() {
        return Locks.event();
    }
    
}
