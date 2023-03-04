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

import org.netbeans.api.db.explorer.node.NodeProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverListener;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.db.explorer.node.NodeProviderFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Rob Englander
 */
public class DriverNodeProvider extends NodeProvider {

    // lazy initialization holder class idiom for static fields is used
    // for retrieving the factory
    public static NodeProviderFactory getFactory() {
        return FactoryHolder.FACTORY;
    }

    private static class FactoryHolder {
        static final NodeProviderFactory FACTORY = new NodeProviderFactory() {
            @Override
            public DriverNodeProvider createInstance(Lookup lookup) {
                return new DriverNodeProvider(lookup);
            }
        };
    }

    private DriverNodeProvider(Lookup lookup) {
        super(lookup, driverComparator);
        
        JDBCDriverManager mgr = JDBCDriverManager.getDefault();
        mgr.addDriverListener(
            new JDBCDriverListener() {
                @Override
                public void driversChanged() {
                    initialize();
                }
            }
        );
    }
    
    @Override
    protected synchronized void initialize() {
        List<Node> newList = new ArrayList<>();
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers();
        for (JDBCDriver driver : drivers) {
            Collection<Node> matches = getNodes(driver);
            if (matches.size() > 0) {
                newList.addAll(matches);
            } else {
                NodeDataLookup lookup = new NodeDataLookup();
                lookup.add(driver);
                newList.add(DriverNode.create(lookup, this));
            }
        }

        setNodes(newList);
    }
    
    private static final Comparator<Node> driverComparator = new Comparator<Node>() {
        @Override
        public int compare(Node model1, Node model2) {
            return model1.getDisplayName().compareToIgnoreCase(model2.getDisplayName());
        }
    };
}
