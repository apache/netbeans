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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import threaddemo.locking.RWLock;

/**
 * A convenience skeleton for making a phadhail based on files.
 * Supplies the actual file operations, and handles caching and
 * event firing and so on. Subclasses supply specialized threading
 * behaviors (this class is not thread-safe, except for adding and
 * removing listeners).
 * @author Jesse Glick
 */
public abstract class AbstractPhadhail implements Phadhail {
    
    private static final Map<Factory, Map<File, Reference<AbstractPhadhail>>> instances = new WeakHashMap<Factory,Map<File,Reference<AbstractPhadhail>>>();
    
    protected interface Factory {
        AbstractPhadhail create(File f);
    }
    
    private static Map<File, Reference<AbstractPhadhail>> instancesForFactory(Factory y) {
        assert Thread.holdsLock(AbstractPhadhail.class);
        Map<File,Reference<AbstractPhadhail>> instances2 = instances.get(y);
        if (instances2 == null) {
            instances2 = new WeakHashMap<File,Reference<AbstractPhadhail>>();
            instances.put(y, instances2);
        }
        return instances2;
    }
    
    /** factory */
    protected static synchronized AbstractPhadhail forFile(File f, Factory y) {
        Map<File,Reference<AbstractPhadhail>> instances2 = instancesForFactory(y);
        Reference<AbstractPhadhail> r = instances2.get(f);
        AbstractPhadhail ph = (r != null) ? r.get() : null;
        if (ph == null) {
            // XXX could also yield lock while calling create, but don't bother
            ph = y.create(f);
            instances2.put(f, new WeakReference<AbstractPhadhail>(ph));
        }
        return ph;
    }
    
    private File f;
    private List<PhadhailListener> listeners = null;
    private Reference<List<Phadhail>> kids;
    private static boolean firing = false;
    
    protected AbstractPhadhail(File f) {
        this.f = f;
    }

    /** factory to create new instances of this class; should be a constant */
    protected abstract Factory factory();
    
    public List<Phadhail> getChildren() {
        assert lock().canRead();
        List<Phadhail> phs = null;
        if (kids != null) {
            phs = kids.get();
        }
        if (phs == null) {
            // Need to (re)calculate the children.
            File[] fs = f.listFiles();
            if (fs != null) {
                Arrays.sort(fs);
                phs = new ChildrenList(fs);
            } else {
                phs = Collections.emptyList();
            }
            kids = new WeakReference<List<Phadhail>>(phs);
        }
        return phs;
    }
    
    private final class ChildrenList extends AbstractList<Phadhail> {
        private final File[] files;
        private final Phadhail[] kids;
        public ChildrenList(File[] files) {
            this.files = files;
            kids = new Phadhail[files.length];
        }
        // These methods need not be called with the read lock held
        // (see Phadhail.getChildren Javadoc).
        public Phadhail get(int i) {
            Phadhail ph = kids[i];
            if (ph == null) {
                ph = forFile(files[i], factory());
            }
            return ph;
        }
        public int size() {
            return files.length;
        }
    }
    
    public String getName() {
        assert lock().canRead();
        return f.getName();
    }
    
    public String getPath() {
        assert lock().canRead();
        return f.getAbsolutePath();
    }
    
    public boolean hasChildren() {
        assert lock().canRead();
        return f.isDirectory();
    }
    
    /**
     * add/removePhadhailListener must be called serially
     */
    private static final Object LISTENER_LOCK = new String("LP.LL");
    
    public final void addPhadhailListener(PhadhailListener l) {
        synchronized (LISTENER_LOCK) {
            if (listeners == null) {
                listeners = new ArrayList<PhadhailListener>();
            }
            listeners.add(l);
        }
    }
    
    public final void removePhadhailListener(PhadhailListener l) {
        synchronized (LISTENER_LOCK) {
            if (listeners != null) {
                listeners.remove(l);
                if (listeners.isEmpty()) {
                    listeners = null;
                }
            }
        }
    }
    
    private final PhadhailListener[] listeners() {
        synchronized (LISTENER_LOCK) {
            if (listeners != null) {
                return listeners.toArray(new PhadhailListener[listeners.size()]);
            } else {
                return null;
            }
        }
    }
    
    protected final void fireChildrenChanged() {
        final PhadhailListener[] l = listeners();
        if (l != null) {
            lock().readLater(new Runnable() {
                public void run() {
                    firing = true;
                    try {
                        PhadhailEvent ev = PhadhailEvent.create(AbstractPhadhail.this);
                        for (PhadhailListener listener : l) {
                            listener.childrenChanged(ev);
                        }
                    } finally {
                        firing = false;
                    }
                }
            });
        }
    }
    
    protected final void fireNameChanged(final String oldName, final String newName) {
        final PhadhailListener[] l = listeners();
        if (l != null) {
            lock().read(new Runnable() {
                public void run() {
                    firing = true;
                    try {
                        PhadhailNameEvent ev = PhadhailNameEvent.create(AbstractPhadhail.this, oldName, newName);
                        for (PhadhailListener listener : l) {
                            listener.nameChanged(ev);
                        }
                    } finally {
                        firing = false;
                    }
                }
            });
        }
    }
    
    public void rename(String nue) throws IOException {
        assert lock().canWrite();
        assert !firing : "Mutation within listener callback";
        String oldName = getName();
        if (oldName.equals(nue)) {
            return;
        }
        File newFile = new File(f.getParentFile(), nue);
        if (!f.renameTo(newFile)) {
            throw new IOException("Renaming " + f + " to " + nue);
        }
        File oldFile = f;
        f = newFile;
        synchronized (AbstractPhadhail.class) {
            Map<File,Reference<AbstractPhadhail>> instances2 = instancesForFactory(factory());
            instances2.remove(oldFile);
            instances2.put(newFile, new WeakReference<AbstractPhadhail>(this));
        }
        fireNameChanged(oldName, nue);
        if (hasChildren()) {
            // Fire changes in path of children too.
            List<AbstractPhadhail> recChildren = new ArrayList<AbstractPhadhail>(100);
            String prefix = oldFile.getAbsolutePath() + File.separatorChar;
            synchronized (AbstractPhadhail.class) {
                for (Reference<AbstractPhadhail> r : instancesForFactory(factory()).values()) {
                    AbstractPhadhail ph = r.get();
                    if (ph != null && ph != this && ph.getPath().startsWith(prefix)) {
                        recChildren.add(ph);
                    }
                }
            }
            // Do the notification after traversing the instances map, since
            // we cannot mutate the map while an iterator is active.
            for (AbstractPhadhail ph : recChildren) {
                ph.parentRenamed(oldFile, newFile);
            }
        }
    }
    
    /**
     * Called when some parent dir has been renamed, and our name
     * needs to change as well.
     */
    private void parentRenamed(File oldParent, File newParent) {
        String prefix = newParent.getAbsolutePath();
        String suffix = f.getAbsolutePath().substring(oldParent.getAbsolutePath().length());
        File oldFile = f;
        f = new File(prefix + suffix);
        synchronized (AbstractPhadhail.class) {
            Map<File,Reference<AbstractPhadhail>> instances2 = instancesForFactory(factory());
            instances2.remove(oldFile);
            instances2.put(f, new WeakReference<AbstractPhadhail>(this));
        }
        fireNameChanged(null, null);
    }
    
    public Phadhail createContainerPhadhail(String name) throws IOException {
        assert lock().canWrite();
        assert !firing : "Mutation within listener callback";
        File child = new File(f, name);
        if (!child.mkdir()) {
            throw new IOException("Creating dir " + child);
        }
        fireChildrenChanged();
        return forFile(child, factory());
    }
    
    public Phadhail createLeafPhadhail(String name) throws IOException {
        assert lock().canWrite();
        assert !firing : "Mutation within listener callback";
        File child = new File(f, name);
        if (!child.createNewFile()) {
            throw new IOException("Creating file " + child);
        }
        fireChildrenChanged();
        return forFile(child, factory());
    }
    
    public void delete() throws IOException {
        assert lock().canWrite();
        assert !firing : "Mutation within listener callback";
        if (!f.delete()) {
            throw new IOException("Deleting file " + f);
        }
        forFile(f.getParentFile(), factory()).fireChildrenChanged();
    }
    
    public InputStream getInputStream() throws IOException {
        assert lock().canRead();
        return new FileInputStream(f);
    }
    
    public OutputStream getOutputStream() throws IOException {
        // Yes, read access - for the sake of the demo, currently Phadhail.getOutputStream
        // is not considered a mutator method (fires no changes); this would be different
        // if PhadhailListener included a content change event.
        // That would be trickier because then you would need to acquire the write lock
        // when opening the stream but release it when closing the stream (*not* when
        // returning it to the caller).
        assert lock().canRead();
        return new FileOutputStream(f);
    }
    
    public String toString() {
        String clazz = getClass().getName();
        int i = clazz.lastIndexOf('.');
        return clazz.substring(i + 1) + "<" + f + ">";
    }

    public abstract RWLock lock();
    
}
