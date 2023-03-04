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

package org.netbeans.modules.xml.schema.model.impl.resolver;

import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.netbeans.modules.xml.xam.NamedReferenceable;

/**
 * The chamelion resolver is intended to be used only when the initial
 * schema doesn't have target namespace and the sought namespace isn't empty.
 *
 * @author Nikita Krjukov
 */
public class ChamelionResolver {

    public static <T extends NamedReferenceable> T resolve(
            SchemaModelImpl sModel, String namespace, String localName,
            Class<T> type) {
        //
        assert namespace != null;
        Schema schema = sModel.getSchema();
        if (schema == null) {
            return null;
        }
        String myTargetNamespace = schema.getTargetNamespace();
        assert myTargetNamespace == null;
        //
        ResolveSession session = new ResolveSession(sModel, namespace);
        T found = null;
        //
        // Find in included recursively at first
        found = IncludeResolver.resolveRecursiveDown(
                sModel, localName, type, session);
        if (found != null) {
            return found;
        }
        //
        // Find in direct imports
        found = ImportResolver.findInImports(
                sModel, namespace, localName, type, session);
        if (found != null) {
            return found;
        }
        //
        ImportResolver.populateRecursivelyIncludedImports(sModel, namespace, session);
        //
        // Find in included imports Down
        found = ImportResolver.findInIncludedImportsDown(
                namespace, localName, type, session, false);
        if (found != null) {
            return found;
        }
        //
        // Try using mega-include approach
        found = IncludeResolver.resolveUpwards(
                sModel, namespace, localName, type, session);
        if (found != null) {
            return found;
        }
        //
        ImportResolver.populateMegaIncludedImports(sModel, namespace, session);
        //
        // Find in included imports Down again but now by mega imports
        found = ImportResolver.findInIncludedImportsDown(
                namespace, localName, type, session, true);
        if (found != null) {
            return found;
        }
        //
        // Find in included imports Up
        found = ImportResolver.findInIncludedImportsUpwards(
                namespace, localName, type, session, false);
        if (found != null) {
            return found;
        }
        //
        // Find in included mega-imports Up
        found = ImportResolver.findInIncludedImportsUpwards(
                namespace, localName, type, session, true);
        if (found != null) {
            return found;
        }
        //
        //
        return null;
    }


}
