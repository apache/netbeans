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

import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.execution.BinaryExecSupport;

/** A node to represent an Elf executable object */
public class ExeNode extends CndDataNode {

    public ExeNode(ExeObject obj) {
        super(obj, Children.LEAF, obj.getLookup());
        setIconBaseWithExtension("org/netbeans/modules/cnd/loaders/ExeIcon.gif"); // NOI18N
    }

    // Example of adding Executor / Debugger / Arguments to node:
    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();

        Sheet.Set set = sheet.get(BinaryExecSupport.PROP_EXECUTION);
        if (set == null) {
            set = new Sheet.Set();
            set.setName(BinaryExecSupport.PROP_EXECUTION);
            set.setDisplayName(NbBundle.getBundle(ExeNode.class).
                    getString("displayNameForExeElfNodeExecSheet"));  // NOI18N
            set.setShortDescription(NbBundle.getBundle(ExeNode.class).
                    getString("hintForExeElfNodeExecSheet"));   // NOI18N
            BinaryExecSupport es = (getCookie(BinaryExecSupport.class));
            if (es != null) {
                es.addProperties(set);
            }
            sheet.put(set);
        }

        return sheet;
    }
}
