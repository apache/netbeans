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
package org.netbeans.modules.javascript2.nodejs.editor.model;

import org.netbeans.modules.javascript2.model.spi.TypeNameConvertor;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.nodejs.editor.NodeJsUtils;

/**
 *
 * @author Petr Pisl
 */
@TypeNameConvertor.Registration(priority=200)
public class NodeJsTypeDisplayNameConvertor implements TypeNameConvertor {
    private static final String REQUIRE_MODULE_NAME = NodeJsUtils.REQUIRE_METHOD_NAME + "." + NodeJsUtils.FAKE_OBJECT_NAME_PREFIX;
    
    @Override
    public String getDisplayName(Type type) {
        String typeString = type.getType();
        String displayName = null;
        if (typeString != null 
                && (typeString.startsWith(NodeJsUtils.FAKE_OBJECT_NAME_PREFIX)
                || typeString.startsWith(REQUIRE_MODULE_NAME))) {
            displayName = typeString.substring(typeString.indexOf(NodeJsUtils.FAKE_OBJECT_NAME_PREFIX) + NodeJsUtils.FAKE_OBJECT_NAME_PREFIX.length());
            if (displayName.endsWith(NodeJsUtils.EXPORTS)) {
                int index = displayName.indexOf('.');
                if (index > 0) {
                    displayName = "Module " + displayName.substring(0, index); //NOI18N
                }
            } else {
                int index = displayName.lastIndexOf('.');
                if (index > 0) {
                    displayName = displayName.substring(index + 1);  
                } else {
                    displayName = "Module " + displayName; //NOI18N
                }
            }
        }
        return displayName;
    }
    
}
