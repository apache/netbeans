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

package org.openide.util.lookup;

import org.openide.util.LookupListener;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class LookupListenerList {
    private Object listeners;

    synchronized void add(LookupListener l) {
        if (listeners == null) {
            listeners = l;
        } else if (listeners instanceof LookupListener) {
            if (listeners != l) {
                LookupListener[] arr = new LookupListener[] { (LookupListener)listeners, l };
                listeners = arr;
            }
        } else {
            LookupListener[] arr = (LookupListener[])listeners;
            LookupListener[] newArr = new LookupListener[arr.length + 1];
            for (int i = 0; i < arr.length; i++) {
                if (l == arr[i]) {
                    return;
                }
                newArr[i] = arr[i];
            }
            newArr[arr.length] = l;
            listeners = newArr;
        }
    }

    synchronized void remove(LookupListener l) {
        if (listeners == null) {
            return;
        } else if (listeners instanceof LookupListener) {
            if (listeners == l) {
                listeners = null;
            }
        } else {
            LookupListener[] arr = (LookupListener[]) listeners;
            LookupListener[] newArr = new LookupListener[arr.length - 1];
            int indx = 0;
            for (int i = 0; i < arr.length; i++) {
                if (l == arr[i]) {
                    continue;
                }
                if (indx == newArr.length) {
                    return;
                }
                newArr[indx++] = arr[i];
            }
            if (newArr.length == 0) {
                listeners = null;
            } else {
                listeners = newArr;
            }
        }
    }

    synchronized int getListenerCount() {
        if (listeners == null) {
            return 0;
        } else if (listeners instanceof LookupListener) {
            return 1;
        } else {
            return ((LookupListener[])listeners).length;
        }
    }

    synchronized LookupListener[] getListenerList() {
        if (listeners == null) {
            return EMPTY;
        } else if (listeners instanceof LookupListener) {
            return new LookupListener[] { (LookupListener)listeners };
        } else {
            return ((LookupListener[])listeners);
        }
    }
    private static final LookupListener[] EMPTY = new LookupListener[0];
    
}
