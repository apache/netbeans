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
package org.netbeans.modules.cnd.makefile.loaders;

import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;

/** A node to represent a Makefile data object */
public class MakefileDataNode extends DataNode {

    /** We need this in several places */
    private MakeExecSupport mes;

    /** Construct the DataNode */
    public MakefileDataNode(MakefileDataObject obj) {
        super(obj, Children.LEAF, obj.getLookup());
        setIconBaseWithExtension("org/netbeans/modules/cnd/script/resources/MakefileDataIcon.gif"); // NOI18N
    }

    /** Get the support for methods which need it */
    private final MakeExecSupport getSupport() {
        if (mes == null) {
            mes = getCookie(MakeExecSupport.class);
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
}

