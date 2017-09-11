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
package org.openide.util.enum;

import java.util.Enumeration;
import java.util.NoSuchElementException;


/**
 * Enumeration that represents a queue. It allows by redefining
 * method <CODE>process</CODE> each outputed object to add other to the end of
 * queue of waiting objects by a call to <CODE>put</CODE>.
 * @deprecated JDK 1.5 treats enum as a keyword so this class was
 *             replaced by {@link org.openide.util.Enumerations#queue}.
 * @author Jaroslav Tulach, Petr Hamernik
 */
public class QueueEnumeration extends Object implements Enumeration {
    /** next object to be returned */
    private ListItem next = null;

    /** last object in the queue */
    private ListItem last = null;

    /** Processes object before it is returned from nextElement method.
    * This method allows to add other object to the end of the queue
    * by a call to <CODE>put</CODE> method. This implementation does
    * nothing.
    *
    * @see #put
    * @param o the object to be processed
    */
    protected void process(Object o) {
    }

    /** Put adds new object to the end of queue.
    * @param o the object to add
    */
    public synchronized void put(Object o) {
        if (last != null) {
            ListItem li = new ListItem(o);
            last.next = li;
            last = li;
        } else {
            next = last = new ListItem(o);
        }
    }

    /** Adds array of objects into the queue.
    * @param arr array of objects to put into the queue
    */
    public synchronized void put(Object[] arr) {
        for (int i = 0; i < arr.length; i++) {
            put(arr[i]);
        }
    }

    /** Is there any next object?
    * @return true if there is next object, false otherwise
    */
    public boolean hasMoreElements() {
        return next != null;
    }

    /** @return next object in enumeration
    * @exception NoSuchElementException if there is no next object
    */
    public synchronized Object nextElement() {
        if (next == null) {
            throw new NoSuchElementException();
        }

        Object res = next.object;

        if ((next = next.next) == null) {
            last = null;
        }

        ;
        process(res);

        return res;
    }

    /** item in linked list of Objects */
    private static final class ListItem {
        Object object;
        ListItem next;

        /** @param o the object for this item */
        ListItem(Object o) {
            object = o;
        }
    }
}
