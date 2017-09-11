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
package org.netbeans.modules.timers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;

/**
 *
 * @author Jan Lahoda
 */
public final class TimesCollectorPeer {
    
    private List<Reference<Object>> files;
    private Map<Object, Map<String, Description>> fo2Key2Desc;
    
    private static final TimesCollectorPeer INSTANCE = new TimesCollectorPeer();
    
    private PropertyChangeSupport pcs;
    
    public static TimesCollectorPeer getDefault() {
        return INSTANCE;
    }
    
    /** Creates a new instance of TimesCollectorPeer */
    private TimesCollectorPeer() {
        files = new ArrayList<Reference<Object>>();
        fo2Key2Desc = new WeakHashMap<Object, Map<String, Description>>();
        
        pcs = new PropertyChangeSupport(this);
    }
    
    public void reportTime(Object fo, String key, String message, long time) {
        Map<String, Description> key2Desc = getKey2Desc(fo);
        Description desc = new Description(message, time);
        
        key2Desc.put(key, desc);
        
        pcs.firePropertyChange("PROP", fo, key);
    }
    
    public void reportReference( Object fo, String key, String message, Object object ) {
        Map<String, Description> key2Desc = getKey2Desc(fo);
        
        // Little bit more complicated here
        Description d = key2Desc.get( key );
        assert d == null || d instanceof ObjectCountDescripton : "Illegal state";
        
        ObjectCountDescripton ocd = d == null ? new ObjectCountDescripton( this, fo, key, message ) : (ObjectCountDescripton)d;
        ocd.add( object );
        
        key2Desc.put(key, ocd);        
        pcs.firePropertyChange("PROP", fo, key);
    }
    
    private synchronized Map<String, Description> getKey2Desc(final Object fo) {
        Map<String, Description> result = fo2Key2Desc.get(fo);
        
        if (result == null) {
            files.add(new CleanableWeakReference<Object>(fo));
            fo2Key2Desc.put(fo, result = Collections.synchronizedMap(new LinkedHashMap<String, Description>()));
            pcs.firePropertyChange("fos", null, fo);
            
            if (fo instanceof FileObject) {
                ((FileObject)fo).addFileChangeListener(new FileChangeAdapter() {
                    @Override
                    public void fileDeleted(FileEvent ev) {
                        fileDeletedSync(ev, (FileObject)fo);
                    }
                });
            }
        }
        return result;
    }

    synchronized final void fileDeletedSync(FileEvent ev, FileObject fo) {
        for (Reference<Object> r : files) {
            if (r.get() == fo) {
                files.remove(r);
                break;
            }
        }
        fo2Key2Desc.remove(fo);
        pcs.firePropertyChange("fos", null, null);
    }
    
    public Description getDescription(Object fo, String key) {
        return getKey2Desc(fo).get(key);
    }
    
    public Collection<String> getKeysForFile(Object fo) {
        return getKey2Desc(fo).keySet();
    }
    
    public synchronized Collection<Object> getFiles() {
        List<Object> result = new ArrayList<Object>();
        
        for (Reference<Object> r : files) {
            Object f = r.get();
            
            if (f != null)
                result.add(f);
        }
        return result;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public void select(Object fo) {
        getKey2Desc(fo);
        pcs.firePropertyChange("selected", null, fo);
    }

    public static class Description {
        private String     message;
        private long       time;
        
        public Description(String message, long time) {
            this.message = message;
            this.time    = time;
        }

        public String getMessage() {
            return message;
        }

        public long getTime() {
            return time;
        }
        
    }
    
    public static class ObjectCountDescripton extends Description implements ChangeListener {
        
        private TimesCollectorPeer tcp;
        private Reference<Object> fo;
        private String key;
        private InstanceWatcher iw = new InstanceWatcher();
        
        
        public ObjectCountDescripton( TimesCollectorPeer tcp, Object fo, String key, String message ) {
            super( message, 0 );
            this.tcp = tcp;
            this.fo = new WeakReference<Object>(fo);
            this.key = key;
            iw.addChangeListener( this );
        }

        @Override
        public long getTime( ) {
            return iw.size();
        }
        
        public Collection getInstances() {
            return iw.getInstances();
        }
        
        private void add( Object o ) {
            iw.add( o );
        } 
        
        public void stateChanged(ChangeEvent e) {
            Object file = fo.get();
            
            if (file != null) {
                tcp.pcs.firePropertyChange("PROP", file, key);
            }
        }
        
    }

    final void cleanFiles(CleanableWeakReference cwr) {
        synchronized (this) {
            files.remove(cwr);
        }
        pcs.firePropertyChange("fos", null, null);
    }

    private static final RequestProcessor CLEANER = new RequestProcessor("CleanableWeakReference"); // NOI18N
    private class CleanableWeakReference<T> extends WeakReference<T> implements Runnable {
        public CleanableWeakReference(T o) {
            super(o, Utilities.activeReferenceQueue());
        }

        public void run() {
            if (!CLEANER.isRequestProcessorThread()) {
                CLEANER.post(this);
                return;
            }
            cleanFiles(this);
        }
        
    }
}
