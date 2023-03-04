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

package org.netbeans.modules.websvc.wsitmodelext.addressing.impl;

import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10WsdlUsingAddressing;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class Addressing10WsdlUsingAddressingImpl extends Addressing10ComponentImpl implements Addressing10WsdlUsingAddressing {

    /**
     * Creates a new instance of Addressing10WsdlUsingAddressingImpl
     */
    public Addressing10WsdlUsingAddressingImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    @Override
    public void setOptional(boolean optional) {
        setAnyAttribute(PolicyQName.OPTIONAL.getQName(ConfigVersion.CONFIG_1_0), optional ? "true" : null); // NOI18N
    }

    @Override
    public boolean isOptional() {
        return Boolean.parseBoolean(getAnyAttribute(PolicyQName.OPTIONAL.getQName(ConfigVersion.CONFIG_1_0)));
    }
    
}
