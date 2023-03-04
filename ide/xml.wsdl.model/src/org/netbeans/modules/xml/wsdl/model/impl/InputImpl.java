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

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class InputImpl extends OperationParameterImpl implements Input {
    
    /** Creates a new instance of InputImpl */
    public InputImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public InputImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.INPUT.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getName() {
        if (super.getName() == null && getParent() != null) {
            String suffix = ""; //NOI18N
            if (getParent() instanceof RequestResponseOperation) {
                suffix = NbBundle.getMessage(InputImpl.class, "LBL_Request");
                return ((Operation)getParent()).getName()+suffix;
            } else if (getParent() instanceof SolicitResponseOperation) {
                suffix = NbBundle.getMessage(InputImpl.class, "LBL_Solicit");
                return ((Operation)getParent()).getName()+suffix;
            } else {
                return ((Operation)getParent()).getName();
            }
        }
        return super.getName();
    }
}
