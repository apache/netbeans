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


package org.netbeans.core.windows;


import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;

import java.util.Iterator;
import java.util.Set;


/**
 * Class representing group of TopComponents. Those TopComponents belong together
 * in the sense they can be opened or closed at one step.
 *
 * @author  Peter Zavadsky
 */
public class TopComponentGroupImpl implements TopComponentGroup {


    /** Creates a new instance of TopComponentGroup */
    public TopComponentGroupImpl(String name) {
        this(name, false);
    }
    
    public TopComponentGroupImpl(String name, boolean opened) {
        getCentral().createGroupModel(this, name, opened);
    }
    

    public void open() {
        WindowManagerImpl.assertEventDispatchThread();
        
        getCentral().openGroup(this);
    }
    
    public void close() {
        WindowManagerImpl.assertEventDispatchThread();
        
        getCentral().closeGroup(this);
    }
    
    public Set<TopComponent> getTopComponents() {
        return getCentral().getGroupTopComponents(this);
    }

    
    public String getName() {
        return getCentral().getGroupName(this);
    }
    
    public boolean isOpened() {
        return getCentral().isGroupOpened(this);
    }
    
    public Set<TopComponent> getOpeningSet() {
        return getCentral().getGroupOpeningTopComponents(this);
    }
    
    public Set getClosingSet() {
        return getCentral().getGroupClosingTopComponents(this);
    }

    public boolean addUnloadedTopComponent(String tcID) {
        return getCentral().addGroupUnloadedTopComponent(this, tcID);
    }
    
    public boolean removeUnloadedTopComponent(String tcID) {
        return getCentral().removeGroupUnloadedTopComponent(this, tcID);
    }
    
    public boolean addUnloadedOpeningTopComponent(String tcID) {
        return getCentral().addGroupUnloadedOpeningTopComponent(this, tcID);
    }
    
    public boolean removeUnloadedOpeningTopComponent(String tcID) {
        return getCentral().removeGroupUnloadedOpeningTopComponent(this, tcID);
    }
    
    public boolean addUnloadedClosingTopComponent(String tcID) {
        return getCentral().addGroupUnloadedClosingTopComponent(this, tcID);
    }
    
    public boolean removeUnloadedClosingTopComponent(String tcID) {
        return getCentral().removeGroupUnloadedClosingTopComponent(this, tcID);
    }
    
    // XXX
    /** Just for persistence management. */
    public boolean addGroupUnloadedOpenedTopComponent(String tcID) {
        return getCentral().addGroupUnloadedOpenedTopComponent(this, tcID);
    }
    
    public Set getGroupOpenedTopComponents() {
        return getCentral().getGroupOpenedTopComponents(this);
    }
    
    // XXX>>
    public Set<String> getTopComponentsIDs() {
        return getCentral().getGroupTopComponentsIDs(this);
    }
    
    public Set<String> getOpeningSetIDs() {
        return getCentral().getGroupOpeningSetIDs(this);
    }
    
    public Set<String> getClosingSetIDs() {
        return getCentral().getGroupClosingSetIDs(this);
    }
    
    public Set<String> getGroupOpenedTopComponentsIDs() {
        return getCentral().getGroupOpenedTopComponentsIDs(this);
    }
    // XXX<<
    
    private Central getCentral() {
        return WindowManagerImpl.getInstance().getCentral();
    }
    
    public String toString() {
        StringBuffer buff = new StringBuffer();
        for(Iterator it = getTopComponents().iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            buff.append("\n\t" + tc.getClass().getName() + "@" + Integer.toHexString(tc.hashCode()) // NOI18N
                + "[name=" + tc.getName() // NOI18N
                + ", openFlag=" + getOpeningSet().contains(tc) // NOI18N
                + ", closeFlag=" + getClosingSet().contains(tc) + "]"); // NOI18N
        }
        
        return super.toString() + "[topComponents=[" + buff.toString() + "\n]]"; // NOI18N
    }

}
