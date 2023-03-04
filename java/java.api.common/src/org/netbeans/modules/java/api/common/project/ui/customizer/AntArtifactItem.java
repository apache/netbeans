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
package org.netbeans.modules.java.api.common.project.ui.customizer;

import java.awt.Component;
import java.net.URI;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;

/**
 * Pair of AntArtifact and one of jars it produces.
 * 
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
public final class AntArtifactItem {

    private AntArtifact artifact;
    private URI artifactURI;

    public AntArtifactItem(AntArtifact artifact, URI artifactURI) {
        super();
        this.artifact = artifact;
        this.artifactURI = artifactURI;
    }

    public static AntArtifactItem[] showAntArtifactItemChooser( String[] artifactTypes, Project master, Component parent ) {
        return AntArtifactChooser.showDialog(artifactTypes, master, parent);
    }

    public AntArtifact getArtifact() {
        return artifact;
    }

    public URI getArtifactURI() {
        return artifactURI;
    }

    @Override
    public String toString() {
        return artifactURI.toString();
    }
}
