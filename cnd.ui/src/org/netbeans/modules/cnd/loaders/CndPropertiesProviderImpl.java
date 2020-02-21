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

import org.netbeans.modules.cnd.execution.CompileExecSupport;
import org.netbeans.modules.cnd.source.spi.CndPropertiesProvider;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=CndPropertiesProvider.class)
public class CndPropertiesProviderImpl extends CndPropertiesProvider {

    @Override
    public void addExtraProperties(DataNode node, Sheet sheet) {
        DataObject dao = node.getDataObject();
        CompileExecSupport ces = dao.getLookup().lookup(CompileExecSupport.class);
        if (ces != null) {
            Sheet.Set set = sheet.get(Sheet.PROPERTIES);
            ces.addProperties(set);
        }
    }
}
