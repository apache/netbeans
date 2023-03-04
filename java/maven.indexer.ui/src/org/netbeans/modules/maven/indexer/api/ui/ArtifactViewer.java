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

package org.netbeans.modules.maven.indexer.api.ui;

import java.util.List;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerFactory;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * a factory class for creation of artifact view components.
 * @author mkleint
 */
public final class ArtifactViewer {

    public static final String HINT_ARTIFACT = "art"; //NOI18N
    public static final String HINT_PROJECT = "prj"; //NOI18N
    public static final String HINT_DEPENDENCIES = "dep"; //NOI18N
    public static final String HINT_GRAPH = "grf"; //NOI18N



    private ArtifactViewer() {
    }
    /**
     * Shows detailed view component with information about the given artifact.
     */
    public static void showArtifactViewer(NBVersionInfo info) {
        showArtifactViewer(info, null, null, null);
    }

    /**
     * Shows detailed view component with information about the given artifact.
     */
    public static void showArtifactViewer(Artifact artifact, List<ArtifactRepository> repos, String panelHint) {
        showArtifactViewer(null, artifact, repos, panelHint);
    }

    private static void showArtifactViewer(NBVersionInfo info, Artifact artifact, List<ArtifactRepository> repos, String panelHint) {
        ArtifactViewerFactory fact = Lookup.getDefault().lookup(ArtifactViewerFactory.class);
        if (fact == null) {
            Logger.getLogger(ArtifactViewer.class.getName()).info("No implementation of ArtifactViewerFactory available.");
            return;
        }
        Lookup l;
        if (info != null) {
            l = fact.createLookup(info);
        } else {
            l = fact.createLookup(artifact, repos);
        }
        TopComponent tc = fact.createTopComponent(l);
        tc.open();
        tc.requestActive();
        if (panelHint != null) {
            MultiViewHandler hand = MultiViews.findMultiViewHandler(tc);
            if (hand == null) {
                return;
            }
            for (MultiViewPerspective pers : hand.getPerspectives()) {
                if (panelHint.equals(pers.preferredID())) {
                    hand.requestVisible(pers);
                    return;
                }
            }
        }
    }
}

