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
