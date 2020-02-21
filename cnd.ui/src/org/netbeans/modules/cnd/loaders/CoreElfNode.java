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
package org.netbeans.modules.cnd.loaders;

import org.netbeans.modules.cnd.execution.BinaryExecSupport;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.execution.ExecutionSupport;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;

/** A node to represent an Elf core object */
public class CoreElfNode extends CndDataNode {

    public CoreElfNode(CoreElfObject obj) {
        super(obj, Children.LEAF, obj.getLookup());
        setIconBaseWithExtension("org/netbeans/modules/cnd/loaders/CoreElfIcon.gif"); // NOI18N
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();

        Sheet.Set set = sheet.get(BinaryExecSupport.PROP_EXECUTION);
        if (set == null) {
            set = new Sheet.Set();
            set.setName(BinaryExecSupport.PROP_EXECUTION);
            set.setDisplayName(NbBundle.getBundle(CoreElfNode.class).
                    getString("displayNameForExeElfNodeExecSheet"));  // NOI18N
            set.setShortDescription(NbBundle.getBundle(CoreElfNode.class).
                    getString("hintForExeElfNodeExecSheet"));   // NOI18N
            BinaryExecSupport es = (getCookie(BinaryExecSupport.class));
            if (es != null) {
                es.addProperties(set);
            }

            // Trick from org/apache/tools/ant/module/nodes/AntProjectNode.java
            // Get rid of Arguments property and the Execution property;
            // corefiles can only be debugged.
            set.remove(ExecutionSupport.PROP_FILE_PARAMS);
            set.remove(ExecutionSupport.PROP_EXECUTION);

            sheet.put(set);
        }
        return sheet;
    }
}
