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

package org.netbeans.core.output2;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import org.openide.util.Parameters;

/**
 * A synchronized LIFO map that can contain duplicate keys, and can be
 * toggled between using weak and strong references for values.  Used for
 * mapping instances of NbIO to the names used in NbIOProvider.getIO().
 *
 * @author  Tim Boudreau
 */
class PairMap {
    //XXX weak referencing of objects no longer used - delete?

    private String [] keys = new String[10];
    /** package private for unit tests */
    Object [] vals = new Object[10];
    int last = -1;
    private boolean weak = false;
    
    /** Creates a new instance of PairMap */
    PairMap() {
    }
    
    public synchronized void clear() {
        keys = new String[10];
        vals = new Object[10];
        last = -1;
    }
    
    public synchronized int size() {
        if (weak) {
            return prune();
        } else {
            return last+1;
        }
    }
    
    public synchronized boolean isEmpty() {
        return size() == 0;
    }
    
    public synchronized void setWeak(boolean val) {
        if (weak != val) {
            weak = val;
            for (int i=0; i <= last; i++) {
                if (weak) {
                    vals[i] = new WeakReference<Object>(vals[i]);
                } else {
                    vals[i] = ((WeakReference<?>) vals[i]).get();
                }
            }
            if (!weak) {
                prune();
            }
        }
    }
    
    public synchronized void add (String key, NbIO value) {
        Parameters.notNull("key", key);
        if (last == keys.length -1) {
            growArrays();
        }
        last++;
        keys[last] = key;
        vals[last] = value;
    }
    
    public synchronized NbIO get (String key, boolean mustBeClosed) {
        if (last < 0) {
            return null;
        }
        boolean foundNull = false;
        for (int i=last; i >= 0; i--) {
            if (keys[i].equals(key)) {
                NbIO io = getValue(i);
                foundNull |= io == null;
                if (io != null && (!mustBeClosed || (mustBeClosed && io.isStreamClosed()))) {
                    return io;
                }
            }
        }
        if (foundNull) {
            prune();
        }
        return null;
    }
    
    public boolean containsValue(NbIO io) {
        if (last < 0) {
            return false;
        }
        for (int i=last; i >= 0; i--) {
            if (getValue(i) == io) {
                return true;
            }
        }
        return false;
    }
    
    public synchronized NbIO get (String key) {
        return get(key, false);
    }
    
    public synchronized String remove (NbIO io) {
        int idx = indexOfVal (io);
        if (idx == -1) {
            return null;
        }
        String result = keys[idx];
        removeIndex(idx);
        return result;
    }
    
    public synchronized NbIO remove (String key) {
        int idx = indexOfKey(key);
        if (idx == -1) {
            return null;
        }
        NbIO result = getValue(idx);
        removeIndex(idx);
        return result;
    }
    
    private NbIO getValue(int idx) {
        if (idx > last) {
            throw new ArrayIndexOutOfBoundsException ("Tried to fetch item " +  //NOI18N
                idx + " but map only contains " + (last + 1) + " elements");  //NOI18N
        }
        NbIO result;
        Object o = vals[idx];
        if (weak) {
            result = (NbIO) ((WeakReference) vals[idx]).get();
            if (result == null && !pruning) {
                removeIndex(idx);
            }

        } else {
            result = (NbIO) o;
        }
        return result;
    }
    
    public void setValue (int idx, NbIO value) {
        if (weak) {
            vals[idx] = new WeakReference<Object>(value);
        } else {
            vals[idx] = value;
        }
    }
    
    private void removeIndex (int idx) {
        if (idx < 0 || idx > last) {
            throw new ArrayIndexOutOfBoundsException ("Trying to remove " + //NOI18N
                "element " + idx + " but map only contains " + (last+1) +  //NOI18N
                " elements"); //NOI18N
        }
        if (idx == last) {
            keys[idx] = null;
            vals[idx] = null;
        } else {
            keys[idx] = keys[last];
            vals[idx] = vals[last];
            vals[last] = null;
            keys[last] = null;
        } 
        last--;
    }
    
    private int indexOfKey (String key) {
        for (int i=last; i >= 0; i--) {
            if (keys[i].equals(key)) {
                return i;
            }
        }
        return -1;
    }
    
    private int indexOfVal (NbIO val) {
        for (int i=last; i >= 0; i--) {
            if (vals[i] == val) {
                return i;
            }
        }
        return -1;
    }
    
    private void growArrays() {
        String[] newKeys = new String[keys.length * 2];
        NbIO[] newVals = new NbIO[vals.length * 2];
        System.arraycopy (keys, 0, newKeys, 0, last+1);
        System.arraycopy (vals, 0, newVals, 0, last+1);
        keys = newKeys;
        vals = newVals;
    }
    
    private boolean pruning = false;
    private int prune() {
        pruning = true;
        int oldSize = last + 1;
        int result = oldSize;
        int[] removes = new int[oldSize];
        Arrays.fill (removes, -1);
        for (int i=last; i >= 0; i--) {
            if (getValue(i) == null) {
                removes[i] = i;
                result--;
            }
        }

        if (result != oldSize) {
            for (int i=removes.length-1; i >= 0; i--) {
                if (removes[i] != -1) {
                    removeIndex(removes[i]);
                }
            }
        }
        pruning = false;
        return result;
    }
    
}
