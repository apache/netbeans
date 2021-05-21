/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.payara.common.nodes;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.common.CommonServerSupport;
import org.netbeans.modules.payara.spi.AppDesc;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;


/**
 * 
 * @author Ludovic Champenois
 * @author Peter Williams
 */
public class Hk2ApplicationsChildren extends Children.Keys<Object> implements Refreshable {
    
    private Lookup lookup;
    private final static Node WAIT_NODE = Hk2ItemNode.createWaitNode();
    
    Hk2ApplicationsChildren(Lookup lookup) {
        this.lookup = lookup;
    }

    @Override
    public void updateKeys(){
        setKeys(new Object[] { WAIT_NODE });
        
        RequestProcessor t = new RequestProcessor("app-child-updater");
        t.post(new Runnable() {
            Vector<Object> keys = new Vector<Object>();
            
            @Override
            public void run() {
                CommonServerSupport commonSupport = lookup.lookup(
                        CommonServerSupport.class);
                if(commonSupport != null) {
                    try {
                        java.util.Map<String, List<AppDesc>> appMap
                                = commonSupport.getApplications(null);
                        for(Entry<String, List<AppDesc>> entry: appMap.entrySet()) {
                            List<AppDesc> apps = entry.getValue();
                            for(AppDesc app: apps) {
                                keys.add(new Hk2ApplicationNode(lookup, app, DecoratorManager.findDecorator(entry.getKey(), Hk2ItemNode.J2EE_APPLICATION, app.getEnabled())));
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger("payara").log(Level.INFO, ex.getLocalizedMessage(), ex);
                    }
                    
                    setKeys(keys);
                }
            }
        }, 0);
    }
    
    @Override
    protected void addNotify() {
        updateKeys();
    }
    
    @Override
    protected void removeNotify() {
        setKeys((Set<? extends Object>) java.util.Collections.EMPTY_SET);
    }
    
    @Override
    protected org.openide.nodes.Node[] createNodes(Object key) {
        if (key instanceof Hk2ItemNode){
            return new Node [] { (Hk2ItemNode) key };
        }
        
        if (key instanceof String && key.equals(WAIT_NODE)){
            return new Node [] { WAIT_NODE };
        }
        
        return null;
    }
}
