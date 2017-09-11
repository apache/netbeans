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
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
