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

package org.netbeans.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
* Similair functionality as PropertyChangeSupport but holds only
* weak references to listener classes
*
* @author Miloslav Metelka
* @version 1.00
*/

public class WeakPropertyChangeSupport {

    private transient ArrayList listeners = new ArrayList();

    private transient ArrayList interestNames = new ArrayList();


    /** Add weak listener to listen to change of any property. The caller must
    * hold the listener object in some instance variable to prevent it
    * from being garbage collected.
    */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        addLImpl(null, l);
    }

    /** Add weak listener to listen to change of the specified property.
    * The caller must hold the listener object in some instance variable
    * to prevent it from being garbage collected.
    */
    public synchronized void addPropertyChangeListener(String propertyName,
            PropertyChangeListener l) {
        addLImpl(propertyName, l);
    }

    /** Remove listener for changes in properties */
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        int cnt = listeners.size();
        for (int i = 0; i < cnt; i++) {
            Object o = ((WeakReference)listeners.get(i)).get();
            if (o == null || o == l) { // remove null references and the required one
                listeners.remove(i);
                interestNames.remove(i);
                i--;
                cnt--;
            }
        }
    }

    public void firePropertyChange(Object source, String propertyName,
                                   Object oldValue, Object newValue) {
        if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
            return;
        }
        PropertyChangeListener la[];
        String isa[];
        int cnt;
        synchronized (this) {
            cnt = listeners.size();
            la = new PropertyChangeListener[cnt];
            for (int i = 0; i < cnt; i++) {
                PropertyChangeListener l = (PropertyChangeListener)
                                           ((WeakReference)listeners.get(i)).get();
                if (l == null) { // remove null references
                    listeners.remove(i);
                    interestNames.remove(i);
                    i--;
                    cnt--;
                } else {
                    la[i] = l;
                }
            }
            isa = (String [])interestNames.toArray(new String[cnt]);
        }

        // now create and fire the event
        PropertyChangeEvent evt = new PropertyChangeEvent(source, propertyName,
                                  oldValue, newValue);
        for (int i = 0; i < cnt; i++) {
            if (isa[i] == null || propertyName == null || isa[i].equals(propertyName)) {
                la[i].propertyChange(evt);
            }
        }
    }

    private void addLImpl(String sn, PropertyChangeListener l) {
        listeners.add(new WeakReference(l));
        interestNames.add(sn);
    }

}
