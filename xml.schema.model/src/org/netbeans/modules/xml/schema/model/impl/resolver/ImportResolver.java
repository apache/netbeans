/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
