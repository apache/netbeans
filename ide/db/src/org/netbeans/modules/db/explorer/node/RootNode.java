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

package org.netbeans.modules.db.explorer.node;

import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.ChildNodeFactory;
import org.netbeans.lib.ddl.impl.SpecificationFactory;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * This is the root node for the database explorer.  This is a singleton
 * instance since the database explorer only uses 1 root node.
 * 
 * @author Rob Englander
 */ 
public class RootNode extends BaseNode {
    private static final String NAME = "Databases"; //NOI18N
    private static final String ICONBASE = "org/netbeans/modules/db/resources/database.gif"; //NOI18N
    private static final String FOLDER = "Root"; //NOI18N

    /** the singleton instance */
    private static RootNode instance = null;
    
    private SpecificationFactory factory;

    /**
     * Gets the singleton instance.
     *            
     * @return the singleton instance
     */
    @ServicesTabNodeRegistration(
        name="Databases",
        displayName="org.netbeans.modules.db.explorer.node.Bundle#RootNode_DISPLAYNAME",
        iconResource="org/netbeans/modules/db/resources/database.gif",
        position=101
    )
    public static RootNode instance() {
        if (instance == null) { 
            NodeDataLookup lookup = new NodeDataLookup();
            lookup.add(ConnectionList.getDefault());
            instance = new RootNode(lookup);
            instance.setup();
        }
        
        return instance;
    }

    public static boolean isCreated() {
        return instance != null;
    }

    /**
     * Constructor.  This is private to prevent multiple instances from
     * being created.
     * 
     * @param lookup the associated lookup
     */
    private RootNode(NodeDataLookup lookup) {
        super(new ChildNodeFactory(lookup), lookup, FOLDER, null);
    }
    
    protected void initialize() {
        try {
            factory = new SpecificationFactory();
            if (factory == null) {
                throw new Exception(
                        NbBundle.getMessage (RootNode.class, "EXC_NoSpecificationFactory"));
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    public SpecificationFactory getSpecificationFactory() {
        return factory;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage (RootNode.class, "RootNode_DISPLAYNAME"); // NOI18N
    }

    @Override
    public String getIconBase() {
        return ICONBASE;
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (RootNode.class, "ND_Root"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(RootNode.class);
    }
}
