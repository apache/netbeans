/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.editor;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Array;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.EventListener;
import javax.swing.event.EventListenerList;

/**
* Class that can hold the list of event listeners
* in a "weak" manner. The advantage is that the listener
* doesn't need to be explicitly removed. Because of the use
* of the <tt>WeakReference</tt> it gets forgotten
* automatically if it was garbage collected. There must
* be at least one non-weak reference to the listener
* (otherwise it would become garbage-collected).
* One of the ways is to store the listener in some instance
* variable in the class that adds the listener.
* Please note that the methods <tt>getListenerCount()</tt>
* and <tt>getListenerCount(Class t)</tt> give the count
* that doesn't reflect whether some listeners were garbage
* collected.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class WeakEventListenerList extends EventListenerList {

    private int listenerSize;

    /**
    * 
    */
    public synchronized Object[] getListenerList() {
        int tgtInd = 0;
        Object[] ret = new Object[listenerSize];
        for (int i = 1; i < listenerSize; i += 2) {
            Object l = ((WeakReference)listenerList[i]).get();
            if (l != null) {
                ret[tgtInd++] = listenerList[i - 1];
                ret[tgtInd++] = l;
            } else { // listener was garbage collected
                /* Remove the listener and its class. This could be done
                * in a more efficient but much less readable way batching
                * the successive removes into one.
                */
                System.arraycopy(listenerList, i + 1, listenerList, i - 1,
                                 listenerSize - i - 1);
                listenerSize -= 2;
                i -= 2;
            }
        }

        if (ret.length != tgtInd) {
            Object[] tmp = new Object[tgtInd];
            System.arraycopy(ret, 0, tmp, 0, tgtInd);
            ret = tmp;
        }

        return ret;
    }

    public synchronized EventListener[] getListeners(Class t) {
        int tgtInd = 0;
        EventListener[] ret = (EventListener[])Array.newInstance(t, listenerSize);
        for (int i = 0; i < listenerSize; i++) {
            if (listenerList[i++] == t) {
                EventListener l = (EventListener)((WeakReference)listenerList[i]).get();
                if (l != null) {
                    ret[tgtInd++] = l;
                } else { // listener was garbage collected
                    /* Remove the listener and its class. This could be done
                    * in a more efficient but much less readable way batching
                    * the successive removes into one.
                    */
                    System.arraycopy(listenerList, i + 1, listenerList, i - 1,
                                     listenerSize - i - 1);
                    listenerSize -= 2;
                    i -= 2;
                }
            }
        }

        if (ret.length != tgtInd) {
            EventListener[] tmp = (EventListener[])Array.newInstance(t, tgtInd);
            System.arraycopy(ret, 0, tmp, 0, tgtInd);
            ret = tmp;
        }

        return ret;
    }


    /**
     * Adds the listener as a listener of the specified type.
     * @param t the type of the listener to be added
     * @param l the listener to be added
     */
    public synchronized void add(Class t, EventListener l) {
        if (l==null) {
            // In an ideal world, we would do an assertion here
            // to help developers know they are probably doing
            // something wrong
            return;
        }

        if (!t.isInstance(l)) {
            throw new IllegalArgumentException("Listener " + l + // NOI18N
                                               " is not of type " + t); // NOI18N
        }

        if (listenerSize == 0) {
            listenerList = new Object[] { t, new WeakReference(l) };
            listenerSize = 2;
        } else {
            if (listenerSize == listenerList.length) { // reallocate
                Object[] tmp = new Object[listenerSize * 2];
                System.arraycopy(listenerList, 0, tmp, 0, listenerSize);
                listenerList = tmp;
            }

            listenerList[listenerSize++] = t;
            listenerList[listenerSize++] = new WeakReference(l);
        }
    }

    /**
     * Removes the listener as a listener of the specified type.
     * @param t the type of the listener to be removed
     * @param l the listener to be removed
     */
    public synchronized void remove(Class t, EventListener l) {
        if (l ==null) {
            // In an ideal world, we would do an assertion here
            // to help developers know they are probably doing
            // something wrong
            return;
        }

        if (!t.isInstance(l)) {
            throw new IllegalArgumentException("Listener " + l + // NOI18N
                                               " is not of type " + t); // NOI18N
        }

        // Is l on the list?
        int index = -1;
        for (int i = listenerSize - 2; i >= 0; i -= 2) {
            if ((listenerList[i] == t)
                    && l == ((WeakReference)listenerList[ i + 1]).get()
               ) {
                index = i;
                break;
            }
        }

        // If so,  remove it
        if (index >= 0) {
            System.arraycopy(listenerList, index + 2, listenerList, index,
                             listenerSize - index - 2);
            listenerSize -= 2;
        }
    }

    private synchronized void writeObject(ObjectOutputStream os) throws IOException {
        os.defaultWriteObject();

        // Save the non-null event listeners:
        for (int i = 0; i < listenerSize; i += 2) {
            Class t = (Class)listenerList[i];
            EventListener l = (EventListener)((WeakReference)listenerList[i + 1]).get();
            if (l instanceof Serializable) {
                os.writeObject(t.getName());
                os.writeObject(l);
            }
        }

        os.writeObject(null);
    }

    private void readObject(ObjectInputStream is)
    throws IOException, ClassNotFoundException {
        is.defaultReadObject();
        Object listenerTypeOrNull;

        while (null != (listenerTypeOrNull = is.readObject())) {
            EventListener l = (EventListener)is.readObject();
            add(Class.forName((String)listenerTypeOrNull), l);
        }
    }

    public synchronized String toString() {
        StringBuffer sb = new StringBuffer("WeakEventListenerList: "); // NOI18N
        sb.append(listenerSize / 2);
        sb.append(" listeners:\n"); // NOI18N
        for (int i = 0; i < listenerSize; i += 2) {
            sb.append(" type " + ((Class)listenerList[i]).getName()); // NOI18N
            sb.append(" listener " + ((WeakReference)listenerList[i + 1]).get()); // NOI18N
        }
        return sb.toString();
    }

}
