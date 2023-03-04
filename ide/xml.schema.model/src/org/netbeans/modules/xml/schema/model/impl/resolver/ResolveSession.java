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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.impl.resolver.MultivalueMap.BidirectionalGraph;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.netbeans.modules.xml.schema.model.impl.Util;

/**
 *
 * @author Nikita Krjukov
 */
public class ResolveSession {

    private SchemaModelImpl mInitialSModel;
    private String mSoughtNamespace;

    private Map<SchemaModel, Checked> mModelToChecked = new HashMap<SchemaModel, Checked>();

    private Set<SchemaModelImpl> mImported = null;
    private Set<SchemaModelImpl> mMegaImported = null;

    private BidirectionalGraph<SchemaModelImpl> mFirstIncludeGraph = null;
    private BidirectionalGraph<SchemaModelImpl> mSecondIncludeGraph = null;

    /**
     * The initial schema model is required to calculate correct dependency roots.
     */
    public ResolveSession(SchemaModelImpl initialSModel, String soughtNamespace) {
        mInitialSModel = initialSModel;
        mSoughtNamespace = soughtNamespace;
        //
        Schema schema = mInitialSModel.getSchema();
        assert schema != null;
    }

    /**
     * Returns the set of Schema models, which already have checked.
     * @return
     */
    public Checked getChecked(SchemaModel sModel) {
        Checked checked = mModelToChecked.get(sModel);
        if (checked == null) {
            checked = new Checked();
            mModelToChecked.put(sModel, checked);
        }
        return checked;
    }

    /**
     * Collects all importes schema models with the sought target namespace.
     * @return
     */
    public Set<SchemaModelImpl> getImported() {
        if (mImported == null) {
            mImported = new HashSet<SchemaModelImpl>();
        }
        return mImported;
    }

    /**
     * Returns the set of schema models, which are imported by another schema
     * which is accessible only with mega-include approach.
     * @return
     */
    public Set<SchemaModelImpl> getMegaImported() {
        if (mMegaImported == null) {
            mMegaImported = new HashSet<SchemaModelImpl>();
        }
        return mMegaImported;
    }

    /**
     * The graph indicates include (& redifine) dependencies between different models.
     * It is calculated here, but only once per resolution session.
     *
     * Two different graphs can be used in case of 2 mega-include:
     * -- the megainclude claster wher the initial schema is located.
     * -- the megainclude claster of the imported schema.
     * They are based on different namespaces. 
     *
     * @param namespace
     * @return
     */
    public BidirectionalGraph<SchemaModelImpl> getInclusionGraph(
            SchemaModelImpl sModel, String namespace) {
        //
        Set<SchemaModelImpl> sModels = Collections.emptySet();
        BidirectionalGraph<SchemaModelImpl> graph = null;
        if (Util.equal(namespace, mSoughtNamespace)) {
            if (mSecondIncludeGraph == null) {
                mSecondIncludeGraph = new BidirectionalGraph<SchemaModelImpl>();
                sModels = populateInclusionGraph(mSoughtNamespace, mSecondIncludeGraph);
            }
            graph = mSecondIncludeGraph;
        } else {
            String initialTargetNs = mInitialSModel.getSchema().getTargetNamespace();
            if (Util.equal(namespace, initialTargetNs)) {
                if (mFirstIncludeGraph == null) {
                    mFirstIncludeGraph = new BidirectionalGraph<SchemaModelImpl>();
                    sModels = populateInclusionGraph(initialTargetNs, mFirstIncludeGraph);
                }
                graph = mFirstIncludeGraph;
            } else {
                assert false : "Namespace can be either the sought one or equal to " +
                        "target namespace of the initial schema!"; // NOI18N
            }
        }
        //
        // Load additional models
        Set<SchemaModelImpl> inclusionRoots = graph.getRoots(sModel, false);
        Set<SchemaModelImpl> avoidCycling = new HashSet<SchemaModelImpl>();
        for (SchemaModelImpl root : inclusionRoots) {
            checkAllDependingModelsLoaded(root, graph, sModels, avoidCycling);
        }
        //
        return graph;
    }

    /**
     * Populates the graph with claster of related schema models.
     * All models in claster linked with includes refernences.
     * They have either the same targetNamespace or are inclued
     * as chameleon (no targetNamespace) to another schema with the
     * targetNamespace.
     *
     * @param soughtNs
     * @param graph
     */
    private Set<SchemaModelImpl> populateInclusionGraph(String soughtNs,
            BidirectionalGraph<SchemaModelImpl> graph) {
        //
        // Populates inclusion map from all schema models, which have already loaded.
        List<SchemaModel> modelsList = SchemaModelFactory.getDefault().getModels();
        Set<SchemaModelImpl> filteredModels = new HashSet<SchemaModelImpl>();
        for(SchemaModel sModel: modelsList) {
            if(sModel == null || sModel.getSchema() == null) {
                continue;
            }
            //
            Schema otherSchema = sModel.getSchema();
            if (otherSchema == null) {
                continue;
            }
            //
            String otherTargetNs = otherSchema.getTargetNamespace();
            if (!Util.equal(soughtNs, otherTargetNs)) {
                // Skip all other models with different targetNamespace.
                continue;
            }
//            if (soughtNs != null) {
//                String otherTargetNs = otherSchema.getTargetNamespace();
//                if (otherTargetNs != null && !soughtNs.equals(otherTargetNs)) {
//                    // Skip all other models with different targetNamespace.
//                    continue;
//                }
//            }
            //
            assert sModel instanceof SchemaModelImpl;
            SchemaModelImpl otherSModel = SchemaModelImpl.class.cast(sModel);
            filteredModels.add(otherSModel);
            //
            Collection<Include> includes = otherSchema.getIncludes();
            for(Include ref: includes) {
                SchemaModelImpl includedSm = otherSModel.resolve(ref);
                if (includedSm != null) {
                    graph.put(otherSModel, includedSm);
                }
            }
        }
        //
        return filteredModels;
    }

    /**
     * Some models can be not loaded at the time when the inclusion graph is built.
     * It is necessary to iterate by dependencies and load all models.
     * This method does it.
     *
     * @param owner the initial schema model, from which the iteration is started.
     * It's usually is a root of the graph.
     * @param graph
     * @param initiallyLoadedModels.
     * @param avoidCycling
     */
    private void checkAllDependingModelsLoaded(SchemaModelImpl owner,
            BidirectionalGraph<SchemaModelImpl> graph,
            Set<SchemaModelImpl> initiallyLoadedModels,
            Set<SchemaModelImpl> avoidCycling) {
        //
        if (avoidCycling.contains(owner)) {
            return;
        }
        //
        // Put the owner model to the set in order to avoid cyclilng in case of
        // cyclinc references
        avoidCycling.add(owner);
        //
        boolean needRegister = !initiallyLoadedModels.contains(owner);
        //
        for (SchemaModelReference ref : owner.getNotImportRefrences()) {
            SchemaModelImpl referencedSModel = owner.resolve(ref);
            if (referencedSModel != null) {
                if (needRegister) {
                    graph.put(owner, referencedSModel);
                }
                //
                checkAllDependingModelsLoaded(referencedSModel,
                        graph, initiallyLoadedModels, avoidCycling);
            }
        }
    }

    public static class Checked {
        boolean itself = false;
        boolean included = false;
        boolean imports = false;

        @Override
        public String toString() {
            return "inself=" + itself + "; included=" + included + "; imports=" + imports;
        }
    }

}
