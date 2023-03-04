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
package org.netbeans.modules.timers;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Utilities;

/** A class for watching instances.
 *
 * @author Petr Hrebejk
 */
public class InstanceWatcher {
    private final Map<Reference<Object>, Boolean> references;
    private Map<Reference<Object>, Boolean> getReferences() {
        assert Thread.holdsLock(this);
        return references;
    }

    
    private transient List<WeakReference<ChangeListener>> changeListenerList;

    
    /** Creates a new instance of InstanceWatcher */
    public InstanceWatcher() {
        references = new IdentityHashMap<Reference<Object>, Boolean>();
    }
           
    public synchronized void add( Object instance ) {
        if ( ! contains( instance ) ) {
            getReferences().put(new CleanableWeakReference(instance), true);
        }
    }
    
    private synchronized boolean contains( Object o ) {
        for( Reference r : getReferences().keySet() ) {
            if ( r.get() == o ) {
                return true;
            }
        }
        return false;
    }
    
    public synchronized int size() {
        return getReferences().size();
    }
    
    public synchronized Collection<?> getInstances() {
        List<Object> l = new ArrayList<Object>(getReferences().size());
        for (Reference wr : getReferences().keySet()) {
            Object inst = wr.get();
            if (inst != null) l.add(inst);
        }
        return l;
    }
    
    /*
    public Iterator iterator() {
        
    }
    */
    
    /**
     * Registers ChangeListener to receive events. Notice that the listeners are
     * held weakly. Make sure that you create hard reference to yopur listener.
     * @param listener The listener to register.
     */
    public synchronized void addChangeListener(javax.swing.event.ChangeListener listener) {
        if (changeListenerList == null ) {
            changeListenerList = new ArrayList<WeakReference<ChangeListener>>();
        }
        changeListenerList.add(new WeakReference<ChangeListener>( listener ) );
    }

    /**
     * Removes ChangeListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removeChangeListener(ChangeListener listener) {
        
        if ( listener == null ) {
            return;
        }
        
        if (changeListenerList != null ) {
            for( WeakReference<ChangeListener> r : changeListenerList ) {
                if ( listener.equals( r.get() )  ) {
                    changeListenerList.remove( r );
                }
            }
        }
        
    }
    
    // Private methods ---------------------------------------------------------    
    
    private static <T> void cleanAndCopy( List<? extends Reference<T>> src, List<? super T> dest ) {
        for( int i = src.size() - 1; i >= 0; i-- ) {
            T o = src.get(i).get();
            if( o == null ) {
                src.remove(i);
            }
            else if ( dest != null ) {
                dest.add( 0, o );
            }
        }
    }
    
    
    private void fireChangeListenerStateChanged() {
        List<ChangeListener> list = new LinkedList<ChangeListener>();
        synchronized (this) {
            if (changeListenerList == null) {
                return;
            }            
            cleanAndCopy( changeListenerList, list );            
        }
        
        ChangeEvent e = new ChangeEvent( this );
        for (ChangeListener ch : list ) {
            ch.stateChanged (e);
        }
    }

    final void cleanUp(CleanableWeakReference cwr) {
        synchronized (this) {
            getReferences().remove(cwr);
        }
        fireChangeListenerStateChanged();
    }

    // Private innerclasses ----------------------------------------------------
    
    private final class CleanableWeakReference extends WeakReference<Object> implements Runnable {

        public CleanableWeakReference(Object i) {
            super(i, Utilities.activeReferenceQueue());
        }

        public void run() {
            cleanUp(this);
        }

    }
               
}
