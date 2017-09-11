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

import java.util.Arrays;

/**
 * A collections-like lineStartList of primitive integers.
 */
final class IntListSimple {
    private int[] array;
    private int used = 0;

    /** Creates a new instance of IntMap */
    IntListSimple(int capacity) {
        array = new int[capacity];
    }
    
    public synchronized void add (int value) {
        if (used >= array.length) {
            growArray();
        }
        array[used++] = value;
    }
    
    public synchronized int get(int index) {
        if (index >= used) {
            throw new ArrayIndexOutOfBoundsException("List contains " + used 
                + " items, but tried to fetch item " + index);
        }
        return array[index];
    }
    
    public synchronized int size() {
        return used;
    }
    
    public void set(int index, int value) {
        if (index >= used) {
            throw new IndexOutOfBoundsException();
        } else {
            array[index] = value;
        }
    }

    public void shorten(int newSize) {
        if (newSize > used) {
            throw new IllegalArgumentException();
        } else {
            used = newSize;
        }
    }

    private void growArray() {
        int[] old = array;
        array = new int[Math.round(array.length * 2)];
        System.arraycopy(old, 0, array, 0, old.length);
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder ("IntListSimple [");
        for (int i=0; i < used; i++) {
            result.append (i);
            result.append (':');
            result.append (array[i]);
            if (i != used-1) {
                result.append(',');
            }
        }
        result.append (']');
        return result.toString();
    }
    
    /**
     * Shift the list (to left). First {@code shift} items will be forgotten.
     * Each item can be decremented by {@code decrement}.
     *
     * @param shift How many items should be removed. Item at index
     * {@code shift} will be at index 0 after this operation.
     * @param increment The value each item should be decremented by.
     */
    public synchronized void compact(int shift, int decrement) {
        if (shift < 0 || shift > used) {
            throw new IllegalArgumentException();
        }
        for (int i = shift; i < used; i++) {
            array[i - shift] = array[i] - decrement;
        }
        Arrays.fill(array, used - shift, used, Integer.MAX_VALUE);
        used -= shift;
    }
}
