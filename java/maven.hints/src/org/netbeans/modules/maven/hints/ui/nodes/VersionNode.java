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
/*
 * Contributor(s): theanuradha@netbeans.org
 */
package org.netbeans.modules.maven.hints.ui.nodes;

import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;

import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.spi.IconResources;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;

/**
 *
 * 
 * @author Anuradha
 */
public class VersionNode extends AbstractNode {
    private static final @StaticResource String SOURCE = "org/netbeans/modules/maven/hints/DependencySrcIncluded.png";
    private static final @StaticResource String JAVADOC = "org/netbeans/modules/maven/hints/DependencyJavadocIncluded.png";

    private final NBVersionInfo nbvi;
    private final boolean hasJavadoc;
    private final boolean hasSources;



    /** Creates a new instance of VersionNode */
    public VersionNode(NBVersionInfo versionInfo, boolean javadoc, boolean source) {
        super(Children.LEAF);
 
        hasJavadoc = javadoc;
        hasSources = source;
        this.nbvi = versionInfo;
        
            setName(versionInfo.getVersion());
            setDisplayName(versionInfo.getVersion() + " [ " + versionInfo.getType() 
                    + (versionInfo.getClassifier() != null ? ("," + versionInfo.getClassifier()) : "") + " ] "
                    + " - "+versionInfo.getRepoId()
                    
                    );
        
        setIconBaseWithExtension(IconResources.ICON_DEPENDENCY_JAR);
    }

    @Override
    public Action[] getActions(boolean context) {
      
        return new Action[0];
    }

    @Override
    public java.awt.Image getIcon(int param) {
        java.awt.Image retValue = super.getIcon(param);
        if (hasJavadoc) {
            retValue = ImageUtilities.mergeImages(retValue,
                    ImageUtilities.loadImage(JAVADOC),//NOI18N
                    12, 12);
        }
        if (hasSources) {
            retValue = ImageUtilities.mergeImages(retValue,
                    ImageUtilities.loadImage(SOURCE),//NOI18N
                    12, 8);
        }
        return retValue;

    }

    public NBVersionInfo getNBVersionInfo() {
        return nbvi;
    }

    @Override
    public String getShortDescription() {
        
        return nbvi.toString();
    }
}
