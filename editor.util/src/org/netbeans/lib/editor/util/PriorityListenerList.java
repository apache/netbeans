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
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

/**
 * Listener list that layers the maintained listeners
 * according to the given priority index.
 * <br>
 * Simply said it's an array of listener arrays. The priority index defines
 * the event listeners array holding all the listeners with the given priority index.
 *
 * @author Miloslav Metelka
 * @since 1.4
 */

public class PriorityListenerList<T extends EventListener> implements Serializable {
    
    static final long serialVersionUID = 0L;
    
    private static final EventListener[] EMPTY_LISTENER_ARRAY = new EventListener[0];
    
    private static final EventListener[][] EMPTY_LISTENER_ARRAY_ARRAY = new EventListener[0][];

    private transient T[][] listenersArray;
    
    private int listenerCount;
    
    public PriorityListenerList() {
        listenersArray = emptyTArrayArray();
    }
    
    /**
     * Add listener with the given priority.
     *
     * @param listener listener to be added. If null is passed it is ignored (nothing gets added).
     * @param priority &gt;=0 index defining priority
     *  with which the listener should be fired.
     *  <br>
     *  The higher the priority the sooner the listener will be fired.
     *  <br>
     *  It's guaranteed that all the listeners with higher priority index will be fired
     *  sooner than listeners with lower priority.
     *  <br>
     *  The number of priority levels should be limited to reasonably
     *  low number.
     * @throws IndexOutOfBoundsException when priority &lt; 0
     */
    public synchronized void add(T listener, int priority) {
        if (listener == null)
            return;

        if (priority >= listenersArray.length) {
            T[][] newListenersArray = allocateTArrayArray(priority + 1);
            System.arraycopy(listenersArray, 0, newListenersArray, 0, listenersArray.length);
            for (int i = listenersArray.length; i < priority; i++) {
                newListenersArray[i] = emptyTArray();
            }
            T[] arr = allocateTArray(1);
            arr[0] = listener;
            newListenersArray[priority] = arr;
            listenersArray = newListenersArray;

        } else { // Add into existing listeners
            @SuppressWarnings("unchecked")
            T[][] newListenersArray = listenersArray.clone();
            T[] listeners = listenersArray[priority];
            T[] newListeners = allocateTArray(listeners.length + 1);
            System.arraycopy(listeners, 0, newListeners, 1, listeners.length);
            newListeners[0] = listener;
            newListenersArray[priority] = newListeners;
            listenersArray = newListenersArray;
        }
        listenerCount++;
    }
    
    /**
     * Remove listener with the given priority index.
     *
     * @param listener listener to be removed. If null is passed it is ignored (nothing gets removed).
     * @param priority &gt;=0 index defining priority
     *  with which the listener was originally added.
     *  <br>
     *  If the listener was not added or it was added with different
     *  priority then no action happens.
     * @throws IndexOutOfBoundsException when priority &lt; 0
     */
    public synchronized void remove(T listener, int priority) {
        if (listener == null)
            return;

        if (priority < listenersArray.length) {
            T[] listeners = listenersArray[priority];
            // Search from 0 - suppose that later added will sooner be removed
            int index = 0;
            while (index < listeners.length && listeners[index] != listener) {
                index++;
            }
            if (index < listeners.length) {
                listenerCount--;
                T[] newListeners;
                boolean removeHighestPriorityLevel;
                if (listeners.length == 1) {
                    newListeners = emptyTArray();
                    removeHighestPriorityLevel = (priority == listenersArray.length - 1);
                } else {
                    newListeners = allocateTArray(listeners.length - 1);
                    System.arraycopy(listeners, 0, newListeners, 0, index);
                    System.arraycopy(listeners, index + 1, newListeners, index,
                            newListeners.length - index);
                    removeHighestPriorityLevel = false;
                }
                
                
                if (removeHighestPriorityLevel) {
                    T[][] newListenersArray = allocateTArrayArray(listenersArray.length - 1);
                    System.arraycopy(listenersArray, 0, newListenersArray, 0, newListenersArray.length);
                    listenersArray = newListenersArray;
                } else { // levels count stays the same
                    @SuppressWarnings("unchecked")
                    T[][] newListenersArray = listenersArray.clone();
                    newListenersArray[priority] = newListeners;
                    listenersArray = newListenersArray;
                }
            }
        }
    }

    /**
     * Return the actual array of listeners arrays maintained by this listeners list.
     * <br>
     * <strong>WARNING!</strong>
     * Absolutely NO modification should be done on the contents of the returned
     * data.
     *
     * <p>
     * The higher index means sooner firing. Listeners with the same priority
     * are ordered so that the one added sooner has higher index than the one
     * added later. So the following firing mechanism should be used:
     * </p>
     * <pre>
     *  private void fireMyEvent(MyEvent evt) {
     *      MyListener[][] listenersArray = priorityListenerList.getListenersArray();
     *      for (int priority = listenersArray.length - 1; priority >= 0; priority--) {
     *          MyListener[] listeners = listenersArray[priority];
     *          for (int i = listeners.length - 1; i >= 0; i--) {
     *              listeners[i].notify(evt);
     *          }
     *      } 
     *  }
     * </pre>
     */
    public T[][] getListenersArray() {
        return listenersArray;
    }
    
    
    
    /**
     * Get total count of listeners contained in this list at all priority levels.
     */
    public int getListenerCount() {
        return listenerCount;
    }

    // Serialization support.
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        
        // Save serializable event listeners
        int priority = listenersArray.length - 1; // max priority
        s.writeInt(priority); // write max priority
        for (; priority >= 0; priority--) {
            T[] listeners = listenersArray[priority];
            // Write in opposite order of adding 
            for (int i = 0; i < listeners.length; i++) {
                T listener = listeners[i];
                // Save only the serializable listeners
                if (listener instanceof Serializable) {
                    s.writeObject(listener);
                }
            }
            s.writeObject(null);
        }
    }
    
    private void readObject(ObjectInputStream s)
    throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int priority = s.readInt();
        listenersArray = (priority != -1)
            ? allocateTArrayArray(priority + 1)
            : emptyTArrayArray();

        for (; priority >= 0; priority--) {
            List<T> lList = new ArrayList<T>();
            Object listenerOrNull;
            while (null != (listenerOrNull = s.readObject())) {
                @SuppressWarnings("unchecked")
                T l = (T)listenerOrNull;
                lList.add(l);
            }
            @SuppressWarnings("unchecked")
            T[] lArr = (T[])lList.toArray(new EventListener[lList.size()]);
            listenersArray[priority] = lArr;
        }
    }
    
    @SuppressWarnings("unchecked")
    private T[] emptyTArray() {
        return (T[])EMPTY_LISTENER_ARRAY;
    }

    @SuppressWarnings("unchecked")
    private T[][] emptyTArrayArray() {
        return (T[][])EMPTY_LISTENER_ARRAY_ARRAY;
    }

    @SuppressWarnings("unchecked")
    private T[] allocateTArray(int length) {
        return (T[])new EventListener[length];
    }

    @SuppressWarnings("unchecked")
    private T[][] allocateTArrayArray(int length) {
        return (T[][])new EventListener[length][];
    }

}
