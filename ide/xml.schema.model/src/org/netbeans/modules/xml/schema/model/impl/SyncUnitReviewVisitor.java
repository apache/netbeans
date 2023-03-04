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

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;
import org.netbeans.modules.xml.xam.dom.SyncUnit;

/**
 *
 * @author Nam Nguyen
 */
public class SyncUnitReviewVisitor extends DefaultSchemaVisitor {
    private SyncUnit unit;

    /** Creates a new instance of SyncUnitFillVisitor */
    public SyncUnitReviewVisitor() {
    }
    
    public SyncUnit review(SyncUnit unit) {
        this.unit = unit;
        ((SchemaComponent)unit.getTarget()).accept(this);
        return unit;
    }
    
    private static SchemaModelImpl getSchemaModel(SchemaComponent c) {
        return (SchemaModelImpl) c.getModel();
    }
    
    private void fixSyncUnit(SchemaComponentImpl atarget) {
        unit = new SyncUnit(atarget.getParent());
        unit.addToRemoveList(atarget);
        unit.addToAddList(getSchemaModel(atarget).createComponent(atarget.getParent(), atarget.getPeer()));
    }
    
    public void visit(LocalElement target) {
        SchemaComponentImpl atarget = (SchemaComponentImpl) target;
        if (atarget.getAttributeValue(SchemaAttributes.REF) != null) {
            fixSyncUnit(atarget);
        }
    }
    
    public void visit(ElementReference target) {
        SchemaComponentImpl atarget = (SchemaComponentImpl) target;
        if (atarget.getAttributeValue(SchemaAttributes.TYPE) != null) {
            fixSyncUnit(atarget);
        }
    }

}
