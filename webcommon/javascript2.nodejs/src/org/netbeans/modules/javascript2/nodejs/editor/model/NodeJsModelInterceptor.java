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

import java.util.Collection;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.javascript2.model.spi.ModelInterceptor;
import org.netbeans.modules.javascript2.nodejs.editor.NodeJsDataProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
@ModelInterceptor.Registration(priority=106)
public class NodeJsModelInterceptor implements ModelInterceptor {

    private static Collection<JsObject> globals = null;
    
    
    @Override
    public Collection<JsObject> interceptGlobal(ModelElementFactory factory, FileObject fo) {
        return getGlobalObjects(factory, fo);
    }
    
    
    private Collection<JsObject> getGlobalObjects(ModelElementFactory factory, FileObject fo) {
        if (globals == null) {
            globals = NodeJsDataProvider.getDefault(fo).getGlobalObjects(factory);
        }
        return globals;
    }
}
