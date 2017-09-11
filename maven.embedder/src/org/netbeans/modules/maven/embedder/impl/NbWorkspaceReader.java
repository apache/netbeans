/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.embedder.impl;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.maven.embedder.ArtifactFixer;
import org.openide.util.Lookup;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;

/**
 *
 * @author mkleint
 */
public class NbWorkspaceReader implements WorkspaceReader {
    private final WorkspaceRepository repo;
    private final Collection<? extends ArtifactFixer> fixers;
    boolean silence = false;
    
    public NbWorkspaceReader() {
        repo = new WorkspaceRepository("ide", getClass());
        fixers = Lookup.getDefault().lookupAll(ArtifactFixer.class);
    }

    @Override
    public WorkspaceRepository getRepository() {
        return repo;
    }

    @Override
    public File findArtifact(org.eclipse.aether.artifact.Artifact artifact) {
        if (silence) {
            return null;
        }
        
        for (ArtifactFixer fixer : fixers) {
            File f = fixer.resolve(artifact);
            if (f != null) {
                return f;
            }
        }
        return null;
    }

    @Override
    public List<String> findVersions(org.eclipse.aether.artifact.Artifact artifact) {
        if (silence) {
            return Collections.emptyList();
        }
        //this is important for snapshots, without it the SNAPSHOT will be attempted to be resolved to time-based snapshot version
        for (ArtifactFixer fixer : fixers) {
            File f = fixer.resolve(artifact);
            if (f != null) {
                return Collections.singletonList(artifact.getBaseVersion());
            }
        }
        return Collections.emptyList();
    }

    /**
     * signals the class to start delegating to workspace
     */
    void normal() {
        silence = false;
    }

    /**
     * signals the class to stop delegating to workspace
     */
    void silence() {
        silence = true;
    }

}
