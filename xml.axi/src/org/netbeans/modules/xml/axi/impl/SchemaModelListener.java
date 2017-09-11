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
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SchemaModelListener implements PropertyChangeListener {
        
    /**
     * Creates a new instance of SchemaModelListener
     */
    public SchemaModelListener(AXIModelImpl model) {
        this.model = model;
    }
    
    /**
     * Returns true if the event pool is not empty,
     * false otherwise.
     */
    boolean needsSync() {
        return !events.isEmpty();
    }
    
    void syncCompleted() {
        events.clear();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        assert(model != null);        
        if(model.isIntransaction() || !isValidEvent(evt))
            return;

        events.add(evt);
        ((ModelAccessImpl)model.getAccess()).setDirty();
    }
    
    private boolean isValidEvent(PropertyChangeEvent evt) {
        if(evt.getSource() instanceof SchemaModel) {
            return true;
        }
        
        if(!(evt.getSource() instanceof SchemaComponent))
            return false;
        
        SchemaComponent component = (SchemaComponent)evt.getSource();
        if( (evt.getOldValue() == null) &&
            (evt.getNewValue() != null) &&
            (evt.getNewValue() instanceof SchemaComponent) ) {
            component = (SchemaComponent)evt.getNewValue();
        }
        
        if( (evt.getNewValue() == null) &&
            (evt.getOldValue() != null) &&
            (evt.getOldValue() instanceof SchemaComponent) ) {
            component = (SchemaComponent)evt.getOldValue();
        }
        
        //query to check if this component affects the model
        AXIModelBuilderQuery query = new AXIModelBuilderQuery(model);
        return query.affectsModel(component);
    }
    
    private List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
    private AXIModelImpl model;
}
