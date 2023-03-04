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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.impl.resolver.MultivalueMap.BidirectionalGraph;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.netbeans.modules.xml.schema.model.impl.Util;
import org.netbeans.modules.xml.schema.model.impl.resolver.ResolveSession.Checked;
import org.netbeans.modules.xml.xam.NamedReferenceable;

/**
 * The resolver is used in several different conditions:
 *
 * - the initial schema has not empty target namespace and it equals to
 * the sought namespace.
 *
 * - the both namespaces are empty.
 *
 * - the target namespace is empty but the sought namespace isn't.
 * In this case the sought object can be either included or imported and
 * different resolvers are used one by one. This resolver is used at first. 
 *
 * @author Nikita Krjukov
 */
public class IncludeResolver {

    public static <T extends NamedReferenceable> T resolve(
           SchemaModelImpl sModel, String namespace, String localName,
           Class<T> type) {
        //
        ResolveSession session = new ResolveSession(sModel, namespace);
        T found = null;
        //
        // Find in included recursively at first
        found = resolveRecursiveDown(sModel, localName, type, session);
        if (found != null) {
            return found;
        }
        //
        // Try using mega-include approach
        found = resolveUpwards(sModel, namespace, localName, type, session);
        if (found != null) {
            return found;
        }
        //
        return null;
    }

    static <T extends NamedReferenceable> T resolveRecursiveDown(
           SchemaModelImpl sModel, String localName,
           Class<T> type, ResolveSession session) {
        //
        T found = null;
        //
        // Find locally first
        Checked checked = session.getChecked(sModel);
        if (!checked.itself) {
            found = sModel.findByNameAndType(localName, type);
            if (found != null) {
                return found;
            }
            checked.itself = true;
        }
        //
        // Find in sub-includes and sub-redefines
        if (!checked.included) {
            checked.included = true;
            //
            Collection<SchemaModelReference> modelRefs = sModel.getNotImportRefrences();
            for (SchemaModelReference ref : modelRefs) {
                assert !(ref instanceof Import);
                //
                SchemaModelImpl resolvedRef = sModel.resolve(ref);
                if (resolvedRef != null) {
                    // Look inside of imported or redefied schema
                    // Recursion is used here.
                    found = resolveRecursiveDown(resolvedRef, localName, type, session);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        //
        return null;
    }

    static <T extends NamedReferenceable> T resolveUpwards(
           SchemaModelImpl sModel, String namespace, String localName,
           Class<T> type, ResolveSession session) {
        //
        // The sought namespace is used here!
        Collection<SchemaModelImpl> models =
                getMegaIncludedModels(sModel, namespace, session);
        for (SchemaModelImpl sm : models) {
            Checked checked = session.getChecked(sm);
            if (sm != null && !checked.itself) {
                T found = sm.findByNameAndType(localName, type);
                if (found != null) {
                    return found;
                }
                checked.itself = true;
            }
        }
        //
        return null;
    }

    /**
     * It looks for a set of schema models, which can be visible to each others.
     * The matter is, if model A includes B, then model B model's definitions
     * is visible to A. But the oposite assertion is also correct because B
     * logically becomes a part of A after inclusion.
     *
     * The method doesn't analyze models included to current. It looks only
     * models to which current model is included! It is implied that the included
     * models's hierarchy has checked before.
     *
     * Be carefull with the following use-case:
     * If model A includes B and C includes B then it doesn't mean that declarations
     * from model C visible in A.
     *
     * The problem is described in the issue http://www.netbeans.org/issues/show_bug.cgi?id=122836
     *
     * The task of the method is to find a set of schema models, which
     * can be visible to the current model. The parameter is used as a hint
     * to exclude the models from other namespace.
     *
     * @param soughtNs
     * @return
     */
    static Set<SchemaModelImpl> getMegaIncludedModels(
            SchemaModelImpl sModel, String soughtNs, ResolveSession session) {
        //
//        if (true) {
//            // For optimization tests only
              // Uncomment and run tests to check how often the mega-include is called. 
//            throw new RuntimeException("MEGA INCLUDE");
//        }
        //
        Schema mySchema = sModel.getSchema();
        if (mySchema == null) {
            return Collections.EMPTY_SET;
        }
        //
        // If the current model has empty target namespace, then it can be included anywhere
        // If the current model has not empty target namespace, then it can be included only
        // to models with the same target namespace.
        String myTargetNs = mySchema.getTargetNamespace();
        if (myTargetNs != null && !Util.equal(soughtNs, myTargetNs)) {
            return Collections.EMPTY_SET;
        }
        //
        // The graph is lazy initialized in session and can be reused during
        // the resolve session.
        BidirectionalGraph<SchemaModelImpl> graph = 
                session.getInclusionGraph(sModel, soughtNs);
        //
        // Now there is forward and back inclusion graphs.
        if (graph.isEmpty()) {
            return Collections.EMPTY_SET;
        }
        //
        // Look for the roots of inclusion.
        // Root s the top schema model, which includes current schema recursively,
        // but isn't included anywhere itself.
        Set<SchemaModelImpl> inclusionRoots = graph.getRoots(sModel, false);
        //
        HashSet<SchemaModelImpl> result = new HashSet<SchemaModelImpl>();
        for (SchemaModelImpl root : inclusionRoots) {
            // The namespace of the inclusion root has to be exectly the same
            // as required.
            if (Util.equal(root.getSchema().getTargetNamespace(), soughtNs)) {
                MultivalueMap.Utils.populateAllSubItems(graph, root, sModel, result);
            }
        }
        //
        result.remove(sModel);
        //
        return result;
    }

}
