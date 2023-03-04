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
/*
 * BaseResourceNode.java
 *
 * Created on August 18, 2005, 9:30 AM
 *
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import org.openide.nodes.Children;
import org.openide.loaders.DataNode;
import org.openide.actions.PropertiesAction;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader.SunResourceDataObject;

import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;

/**
 *
 * @author Nitya Doraisamy
 */
public abstract class BaseResourceNode extends DataNode implements java.beans.PropertyChangeListener {
    
                
    /** Creates a new instance of BaseResourceNode */
    public BaseResourceNode(SunResourceDataObject obj) {
        super(obj, Children.LEAF);
    }
    
    public javax.swing.Action getPreferredAction(){
        return SystemAction.get(PropertiesAction.class);
    }
    
    protected SunResourceDataObject getSunResourceDataObject() {
        return (SunResourceDataObject)getDataObject();
    }
    
    public abstract Resources getBeanGraph();
    
    public abstract void propertyChange(java.beans.PropertyChangeEvent evt);
    
}
