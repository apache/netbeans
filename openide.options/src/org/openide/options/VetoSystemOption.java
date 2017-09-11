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
package org.openide.options;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import java.util.*;


/** Extends the functionality of <CODE>SystemOption</CODE>
* by providing support for veto listeners.
*
* @author Jaroslav Tulach
* @version 0.11 Dec 6, 1997
*/
public abstract class VetoSystemOption extends SystemOption {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -614731095908156413L;

    /** vetoable listener property */
    private static final String PROP_VETO_SUPPORT = "vetoSupport"; // NOI18N

    /** Default constructor. */
    public VetoSystemOption() {
    }

    /** Lazy getter for veto hashtable.
    * @return the hashtable
    */
    private HashSet getVeto() {
        HashSet set = (HashSet) getProperty(PROP_VETO_SUPPORT);

        if (set == null) {
            set = new HashSet();
            putProperty(PROP_VETO_SUPPORT, set);
        }

        return set;
    }

    /** Add a new veto listener to all instances of this exact class.
    * @param list the listener to add
    */
    public final void addVetoableChangeListener(VetoableChangeListener list) {
        synchronized (getLock()) {
            getVeto().add(list);
        }
    }

    /** Remove a veto listener from all instances of this exact class.
    * @param list the listener to remove
    */
    public final void removeVetoableChangeListener(VetoableChangeListener list) {
        synchronized (getLock()) {
            getVeto().remove(list);
        }
    }

    /** Fire a property change event.
    * @param name the name of the property
    * @param oldValue the old value
    * @param newValue the new value
    * @exception PropertyVetoException if the change is vetoed
    */
    public final void fireVetoableChange(String name, Object oldValue, Object newValue)
    throws PropertyVetoException {
        PropertyChangeEvent ev = new PropertyChangeEvent(this, name, oldValue, newValue);

        Iterator en;

        synchronized (getLock()) {
            en = ((HashSet) getVeto().clone()).iterator();
        }

        while (en.hasNext()) {
            ((VetoableChangeListener) en.next()).vetoableChange(ev);
        }
    }
}
