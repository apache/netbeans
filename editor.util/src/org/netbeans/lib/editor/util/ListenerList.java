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

package org.netbeans.lib.editor.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

/**
 * Listener list storing listeners of a single type.
 *
 * @author Miloslav Metelka
 * @since 1.11
 */

public final class ListenerList<T extends EventListener> implements Serializable {

    static final long serialVersionUID = 0L;
    
    /** A null array to be shared by all empty listener lists */
    private static final EventListener[] EMPTY_LISTENER_ARRAY = new EventListener[0];
    
    /* The array of listeners. */
    private transient ImmutableList<T> listenersList;
    
    public ListenerList() {
        listenersList = new ImmutableList<T>(EMPTY_LISTENER_ARRAY);
    }
    
    /**
     * Returns a list of listeners.
     * 
     * <p>The listeners are returned in exactly the same order
     * as they were added to this list. Use the following code
     * for firing events to all the listeners you get from this
     * method.
     * 
     * <pre>
     *      List<MyListener> listeners = listenerList.getListeners();
     *      for (MyListener l : listeners) {
     *          l.notify(evt);
     *      }
     * </pre>
     * 
     * @return An immutable list of listeners contained in this listener list.
     */
    public synchronized List<T> getListeners() {
        return listenersList;
    }
    
    /**
     * Returns the total number of listeners for this listener list.
     */
    public synchronized int getListenerCount() {
        return listenersList.size();
    }
    
    /**
     * Adds the given listener to this listener list.
     * 
     * @param listener the listener to be added. If null is passed it is ignored (nothing gets added).
     */
    public synchronized void add(T listener) {
        if (listener == null)
            return;

        EventListener [] arr = new EventListener[listenersList.getArray().length + 1];
        if (arr.length > 1) {
            System.arraycopy(listenersList.getArray(), 0, arr, 0, arr.length - 1);
        }
        arr[arr.length - 1] = listener;
        
        listenersList = new ImmutableList<T>(arr);
    }
    
    /**
     * Removes the given listener from this listener list.
     * 
     * @param listener the listener to be removed. If null is passed it is ignored (nothing gets removed).
     */
    public synchronized void remove(T listener) {
        if (listener == null)
            return;

        int idx = listenersList.indexOf(listener);
        if (idx == -1) {
            return;
        }
        
        EventListener [] arr = new EventListener[listenersList.getArray().length - 1];
        if (arr.length > 0) {
            System.arraycopy(listenersList.getArray(), 0, arr, 0, idx);
        }
        if (arr.length > idx) {
            System.arraycopy(listenersList.getArray(), idx + 1, arr, idx, listenersList.getArray().length - idx - 1);
        }
        
        listenersList = new ImmutableList<T>(arr);
    }
    
    // Serialization support.
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        
        // Write in opposite order of adding 
        for (Iterator<T> i = listenersList.iterator(); i.hasNext(); ) {
            T l = i.next();
            // Save only the serializable listeners
            if (l instanceof Serializable) {
                s.writeObject(l);
            }
        }
        
        s.writeObject(null);
    }
    
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        List<T> lList = new ArrayList<T>();
        Object listenerOrNull;
        while (null != (listenerOrNull = s.readObject())) {
            @SuppressWarnings("unchecked")
            T l = (T)listenerOrNull;
            lList.add(l);
        }
        this.listenersList = new ImmutableList<T>((EventListener [])lList.toArray(new EventListener[lList.size()]));
    }
    
    public String toString() {
        return listenersList.toString();
    }

    private static final class ImmutableList<E extends EventListener> extends AbstractList<E> {

        private EventListener[] array;
        
        public ImmutableList(EventListener[] array) {
            super();
            
            assert array != null : "The array can't be null"; //NOI18N
            this.array = array;
        }
        
        public E get(int index) {
            if (index >= 0 && index < array.length) {
                @SuppressWarnings("unchecked") 
                E element = (E) array[index];
                return element;
            } else {
                throw new IndexOutOfBoundsException("index = " + index + ", size = " + array.length); //NOI18N
            }
        }
        
        public int size() {
            return array.length;
        }
        
        public EventListener[] getArray() {
            return array;
        }
    } // End of ImmutableList class
}
