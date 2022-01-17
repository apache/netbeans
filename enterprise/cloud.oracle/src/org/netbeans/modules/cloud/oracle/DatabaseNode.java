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
package org.netbeans.modules.cloud.oracle;

import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Horvath
 */
public class DatabaseNode extends AbstractNode {
    
    private static final String DB_ICON = "org/netbeans/modules/cloud/oracle/resources/database.svg"; // NOI18N
    
    public DatabaseNode(OCIItem dbSummary) {
        super(Children.LEAF, Lookups.fixed(dbSummary));
        setName(dbSummary.getName()); 
        setDisplayName(dbSummary.getName());
        setIconBaseWithExtension(DB_ICON);
    }
   
    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Cloud/Oracle/Databases/Actions").toArray(new Action[0]); // NOI18N
    }
}
