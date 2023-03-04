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

package org.netbeans.modules.editor.lib;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.document.implspi.DocumentServiceFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = DocumentServiceFactory.class, path = "Editors/Documents/org.netbeans.editor.BaseDocument")
public class DocumentServices implements DocumentServiceFactory<BaseDocument> {

    @Override
    public Lookup forDocument(BaseDocument doc) {
        return Lookups.fixed(EditorPackageAccessor.get().BaseDocument_newServices(doc));
    }
}
