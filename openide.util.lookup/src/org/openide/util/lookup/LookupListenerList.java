/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
