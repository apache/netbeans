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

import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.netbeans.modules.xml.schema.model.impl.Util;
import org.netbeans.modules.xml.schema.model.impl.resolver.ResolveSession.Checked;
import org.netbeans.modules.xml.xam.NamedReferenceable;

/**
 * The resolver mainly used when the initial schema has not empty target
 * namespace and it isn't equal to the sought namespace.
 *
 * In case the target namespace is empty but the sought namespace isn't,
 * the sought object can be either included or imported and this resolver
 * is used after IncludeResolver.
 * 
 * @author Nikita Krjukov
 */
public class ImportResolver {

    public static <T extends NamedReferenceable> T resole(
            SchemaModelImpl sModel, String namespace, String localName, 
            Class<T> type) {
        //
        ResolveSession session = new ResolveSession(sModel, namespace);
        T found = null;
        //
        // Find in direct imports
        found = findInImports(sModel, namespace, localName, type, session);
        if (found != null) {
            return found;
        }
        //
        populateRecursivelyIncludedImports(sModel, namespace, session);
        //
        // Find in included imports Down
        found = findInIncludedImportsDown(namespace, localName, type, session, false);
        if (found != null) {
            return found;
        }
        //
        populateMegaIncludedImports(sModel, namespace, session);
        //
        // Find in included imports Down again but now by mega imports
        found = findInIncludedImportsDown(namespace, localName, type, session, true);
        if (found != null) {
            return found;
        }
        //
        // Find in included imports Up
        found = findInIncludedImportsUpwards(namespace, localName, type, session, false);
        if (found != null) {
            return found;
        }
        //
        // Find in included mega-imports Up
        found = findInIncludedImportsUpwards(namespace, localName, type, session, true);
        if (found != null) {
            return found;
        }
        //
        return null;
    }

    /**
     * Look for directly imported schema. 
     * @param <T>
     * @param sModel
     * @param namespace
     * @param localName
     * @param type
     * @param session
     * @return
     */
    static <T extends NamedReferenceable> T findInImports(
            SchemaModelImpl sModel, String namespace, String localName,
            Class<T> type, ResolveSession session) {
        //
        Schema mySchema = sModel.getSchema();
        if (mySchema == null) {
            return null;
        }
        //
        Collection<Import> imports = mySchema.getImports();
        for (Import imp : imports) {
            //
            String importedNs = imp.getNamespace();
            if (Util.equal(importedNs, namespace)) { // Both can be null
                SchemaModelImpl importedSchema = sModel.resolve(imp);
                if (importedSchema != null) {
                    session.getImported().add(importedSchema);
                    //
                    Checked checked = session.getChecked(importedSchema);
                    if (!checked.itself) {
                        //
                        // Look inside of imported schema
                        T found = importedSchema.findByNameAndType(localName, type);
                        checked.itself = true;
                        //
                        if (found != null) {
                            return found;
                        }
                    }
                }
            }
        }
        //
        return null;
    }

    /**
     * Looks for an object inside of all imported.
     * The imported objects has to be prepopulated before by the
     * populateRecursivelyIncludedImports(...) method.
     * Search inside of imported models is performed upwards way.
     * See description of the method IncludeResolver.resolveUpwards(...)
     *
     * @param <T>
     * @param sModel
     * @param namespace
     * @param localName
     * @param type
     * @param session
     * @return
     */
    static <T extends NamedReferenceable> T findInIncludedImportsDown(
            String namespace, String localName, Class<T> type,
            ResolveSession session, boolean checkMegaImported) {
        //
        Set<SchemaModelImpl> imported = checkMegaImported ? 
            session.getMegaImported() : session.getImported();
        //
        for (SchemaModelImpl imp : imported) {
            assert imp.getSchema() != null;
            assert Util.equal(imp.getSchema().getTargetNamespace(), namespace);
            //
            T found = IncludeResolver.resolveRecursiveDown(
                    imp, localName, type, session);
            if (found != null) {
                return found;
            }
        }
        //
        return null;
    }

    /**
     * Looks for an object inside of all imported.
     * The imported objects has to be prepopulated before by the
     * populateRecursivelyIncludedImports(...) method.
     * Search inside of imported models is performed upwards way.
     * See description of the method IncludeResolver.resolveUpwards(...)
     *
     * @param <T>
     * @param sModel
     * @param namespace
     * @param localName
     * @param type
     * @param session
     * @return
     */
    static <T extends NamedReferenceable> T findInIncludedImportsUpwards(
            String namespace, String localName, Class<T> type,
            ResolveSession session, boolean checkMegaImported) {
        //
        Set<SchemaModelImpl> imported = checkMegaImported ?
            session.getMegaImported() : session.getImported();
        //
        for (SchemaModelImpl imp : imported) {
            assert imp.getSchema() != null;
            assert Util.equal(imp.getSchema().getTargetNamespace(), namespace);
            //
            T found = IncludeResolver.resolveUpwards(
                    imp, namespace, localName, type, session);
            if (found != null) {
                return found;
            }
        }
        //
        return null;
    }

    /**
     * Iterates recursively over inclusion claster starting from the specified
     * schema model and look for all imports with the specified target namespace.
     * The found imported schema models are populated to session.getImported().
     * The iterated includes are added to session.getDirectlyIncluded();
     * 
     * @param sModel
     * @param namespace
     * @param session
     */
    static void populateRecursivelyIncludedImports(
            SchemaModelImpl sModel, String namespace, ResolveSession session) {
        //
        Schema mySchema = sModel.getSchema();
        if (mySchema == null) {
            return;
        }
        //
        Set<SchemaModelImpl> imported = session.getImported();
        //
        Collection<SchemaModelReference> includes = sModel.getNotImportRefrences();
        for (SchemaModelReference smRef : includes) {
            //
            SchemaModelImpl includedSModel = sModel.resolve(smRef);
            if (includedSModel == null) {
                continue;
            }
            //
            Checked checked = session.getChecked(includedSModel);
            if (checked.imports) {
                // it already has processed.
                continue;
            }
            checked.imports = true;
            //
            Schema includedSchema = includedSModel.getSchema();
            if (includedSchema == null) {
                continue;
            }
            Collection<Import> imports = includedSchema.getImports();
            for (Import imp : imports) {
                String importedModelNamespace = imp.getNamespace();
                if (Util.equal(namespace, importedModelNamespace)) {
                    SchemaModelImpl importedSModel = includedSModel.resolve(imp);
                    if (importedSModel != null) {
                        imported.add(importedSModel);
                    }
                }
            }
            //
            //
            populateRecursivelyIncludedImports(includedSModel, namespace, session);
        }
    }

    /**
     * Builds mega-include claster based on the namespace and find for all imports. 
     * The found imported schema models are populated to session.getImported().
     * The iterated includes are added to session.getDirectlyIncluded();
     *
     * @param sModel
     * @param namespace
     * @param session
     */
    static void populateMegaIncludedImports(
            SchemaModelImpl sModel, String namespace, ResolveSession session) {
        //
        Set<SchemaModelImpl> imported = session.getImported();
        Set<SchemaModelImpl> megaImported = session.getMegaImported();
        //
        Schema mySchema = sModel.getSchema();
        if (mySchema == null) {
            return;
        }
        Collection<SchemaModelImpl> models = IncludeResolver.getMegaIncludedModels(
                sModel, mySchema.getTargetNamespace(), session);
        for (SchemaModelImpl includedSModel : models) {
            Checked checked = session.getChecked(includedSModel);
            //
            // skip included models, which already has processed before.
            if (includedSModel != null && !checked.imports) {
                checked.imports = true;
                //
                Schema schema = includedSModel.getSchema();
                if (schema == null) {
                    continue;
                }
                Collection<Import> imports = schema.getImports();
                for (Import imp : imports) {
                    String importedModelNamespace = imp.getNamespace();
                    if (Util.equal(namespace, importedModelNamespace)) {
                        SchemaModelImpl importedSModel = includedSModel.resolve(imp);
                        if (importedSModel != null && !imported.contains(importedSModel)) {
                            megaImported.add(importedSModel);
                        }
                    }
                }
            }
        }
    }

}
