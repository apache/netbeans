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

package org.netbeans.modules.xml.multiview;

import org.netbeans.core.spi.multiview.*;

/**
 * DesignMultiViewDesc.java
 *
 * Created on October 9, 2004, 11:37 AM
 * @author mkuchtiak
 */
public abstract class DesignMultiViewDesc implements MultiViewDescription, java.io.Serializable {

    static final long serialVersionUID = -3640713597058983397L;

    private String name;
    private XmlMultiViewDataObject dObj;

    public DesignMultiViewDesc() {
    }

    public DesignMultiViewDesc(XmlMultiViewDataObject dObj, String name) {
        this.name=name;
        this.dObj=dObj;
    }

    public abstract MultiViewElement createElement();
    
    protected XmlMultiViewDataObject getDataObject() {
        return dObj;
    }

    public String getDisplayName() {
        return name;
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return null;
    }
    
    public abstract java.awt.Image getIcon();

    public int getPersistenceType() {
        return org.openide.windows.TopComponent.PERSISTENCE_NEVER;
    }
    
    public abstract String preferredID();
        
}
