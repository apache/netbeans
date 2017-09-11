/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.maven;

import java.io.IOException;
//import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
//import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jshell.support.SnippetStorage;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(
        service = SnippetStorage.class,
        projectType = "org-netbeans-modules-maven"
)
public class MavenSnippetStorage implements SnippetStorage {
    public static final String SNIPPET_FOLDER = "org.netbeans.jshell.snippetsRoot"; // NOI18N
    private final Project project;
    private final Lookup lookup;

    public MavenSnippetStorage(Project project, Lookup lookup) {
        this.project = project;
        this.lookup = lookup;
    }
    
    private String evaluate(String s) {
        AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
        return props.get(s, true);
    }
    
    private String[] snippetsFolderName() {
        String val = evaluate(SNIPPET_FOLDER);
        if (val == null) {
            return null;
        }
        int lastSlash = val.lastIndexOf('/');
        if (lastSlash == -1 || lastSlash == val.length() - 1) {
            return new String[] {
                null, val, val
            };
        } else {
            return new String[] {
                val.substring(0, lastSlash),
                val.substring(lastSlash + 1),
                val
            };
        }
    }
    
    @Override
    public FileObject getStorageFolder(boolean createIfMissing) {
        FileObject root = project.getProjectDirectory();
        String[] place = snippetsFolderName();
        if (place == null) {
            return null;
        }
        FileObject snippets = place[0] == null ? root : root.getFileObject(place[0]);
        if (snippets == null) {
            if (!createIfMissing) {
                return null;
            }
            try {
                // create the folder:
                FileObject target = FileUtil.createFolder(project.getProjectDirectory(),
                        place[2]);
                return target.getParent();
            } catch (IOException ex) {
                return null;
            }
        }
        return snippets;
    }

    @Override
    public String resourcePrefix() {
        String[] place = snippetsFolderName();
        if (place == null) {
            return null;
        }
        return place[1];
    }

    @Override
    public String startupSnippets(String runAction) {
        return "startup";
    }
}
