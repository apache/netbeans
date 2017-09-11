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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.axi.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;

/**
 * Listener to listen to this model changes. Used by code generator.
 * 
 * @author Ayub
 */
public class AXIModelListener implements PropertyChangeListener {
    List<PropertyChangeEvent> events  = new ArrayList<PropertyChangeEvent>();
    
    public void propertyChange(PropertyChangeEvent evt) {
        
        //filter events if not intended for code generator.
        if(!validatePropertyChangeEvent(evt))
            return;
        
        //add event to the event queue.
        events.add(evt);
    }
    
    public List<PropertyChangeEvent> getEvents() {
        return events;
    }
    
    public void clearEvents() { events.clear();}
    
    /**
     * Checks the validity of this event. Certain events shouldn't go to
     * the code generator. For example events coming from proxy components.
     * There are certain other cases as well.
     */
    private boolean validatePropertyChangeEvent(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        //events coming from model are valid
        if( !(source instanceof AXIComponent) )
            return true;
                
        //ignore proxy related events
        if(proxyRelated(evt))
            return false;
                        
        
        return true;
    }
    
    private boolean proxyRelated(PropertyChangeEvent evt) {
        if(evt.getSource() instanceof AXIComponentProxy)
            return true;
        
        Object oldValue = evt.getOldValue();
        Object newValue = evt.getNewValue();
        //proxy child added
        if( (newValue != null) && (oldValue == null) &&
            (newValue instanceof AXIComponentProxy) )
            return true;
        //proxy child removed
        if( (oldValue != null) && (newValue == null) &&
            (oldValue instanceof AXIComponentProxy) )
            return true;
        
        return false;
    }
    
}
