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
package org.netbeans.modules.maven.repository;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author mkleint
 * @author Anuradha G
 */
public class ArtifactChildren extends ChildFactory<NBVersionInfo> {

    private String artifactId;
    private String groupId;
    private RepositoryInfo info;
    /**
     * creates a new instance of ArtifactChildren from browsing interface
     */
    public ArtifactChildren( RepositoryInfo info,String groupId, String artifactId) {
        this.info = info;
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    protected @Override Node createNodeForKey(NBVersionInfo record) {
        boolean hasSources = record.isSourcesExists();
        boolean hasJavadoc = record.isJavadocExists();
        return new VersionNode(info, record, hasJavadoc, hasSources, groupId != null);
    }
    
    protected @Override boolean createKeys(List<NBVersionInfo> toPopulate) {
        Result<NBVersionInfo> result = RepositoryQueries.getVersionsResult(groupId, artifactId, Collections.singletonList(info));
        toPopulate.addAll(result.getResults());
        return true;
    }

}
