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
package org.netbeans.modules.cnd.script.loaders;

import javax.swing.Action;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.actions.SystemAction;
import org.openide.actions.OpenAction;

import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;

/** A node to represent a Shell data object */
public class ShellDataNode extends DataNode {

    /** We need this in several places */
    private ShellExecSupport mes;

    /** Construct the DataNode */
    public ShellDataNode(DataObject obj) {
        super(obj, Children.LEAF, obj.getLookup());
        setIconBaseWithExtension("org/netbeans/modules/cnd/script/resources/ShellDataIcon.gif");
    }

    /** Get the support for methods which need it */
    private final ShellExecSupport getSupport() {
        if (mes == null) {
            mes = getCookie(ShellExecSupport.class);
        }

        return mes;
    }

    /** Create the properties sheet for the node */
    @Override
    protected Sheet createSheet() {
        // Just add properties to default property tab (they used to be in a special 'Building Tab')
        Sheet defaultSheet = super.createSheet();
        Sheet.Set defaultSet = defaultSheet.get(Sheet.PROPERTIES);
        getSupport().addProperties(defaultSet);
        return defaultSheet;
    }

    @Override
    public Action getPreferredAction() {
        Action result = super.getPreferredAction();
        return result == null ? SystemAction.get(OpenAction.class) : result;
    }
}
