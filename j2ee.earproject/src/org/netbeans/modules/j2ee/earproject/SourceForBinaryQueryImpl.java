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

package org.netbeans.modules.j2ee.earproject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 */
public class SourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation {

    private final Map<String, SourceForBinaryQuery.Result> cache = new HashMap<String, SourceForBinaryQuery.Result>();

    private EarProject p;

    public SourceForBinaryQueryImpl(EarProject p) {
        this.p = p;
    }

    @Override
    public Result findSourceRoots(URL binaryRoot) {
        if (FileUtil.getArchiveFile(binaryRoot) != null) {
            binaryRoot = FileUtil.getArchiveFile(binaryRoot);
        }
        SourceForBinaryQuery.Result res = cache.get(binaryRoot.toExternalForm());
        if (res != null) {
            return res;
        }

        FileObject fo = URLMapper.findFileObject(binaryRoot);
        if (fo == null || fo.isFolder()) {
            return null;
        }

        String buildDir = p.evaluator().evaluate("${build.dir}/lib");
        if (buildDir == null) {
            return null;
        }
        FileObject libRoot = p.getAntProjectHelper().resolveFileObject(buildDir);
        if (libRoot == null) {
            return null;
        }
        if (!fo.getParent().equals(libRoot)) {
            return null;
        }
        String libFile = fo.getNameExt();

        for (Project subProject : p.getLookup().lookup(SubprojectProvider.class).getSubprojects()) {
            SourceGroup sgp[] = ProjectUtils.getSources(subProject).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (sgp.length == 0) {
                continue;
            }
            ClassPath cp = ClassPath.getClassPath(sgp[0].getRootFolder(), ClassPath.COMPILE);
            if (cp == null) {
                continue;
            }
            for (ClassPath.Entry entry : cp.entries()) {
                URL u = entry.getURL();
                if (FileUtil.getArchiveFile(u) != null) {
                    u = FileUtil.getArchiveFile(u);
                }
                // #213372:
                if (u.toExternalForm().equals(binaryRoot.toExternalForm())) {
                    continue;
                }
                FileObject cpItem = URLMapper.findFileObject(u);
                if (cpItem == null || cpItem.isFolder()) {
                    continue;
                }
                if (cpItem.getNameExt().equals(libFile)) {
                    res = SourceForBinaryQuery.findSourceRoots(entry.getURL());
                    if (res != null) {
                        cache.put(binaryRoot.toExternalForm(), res);
                        return res;
                    }
                }
            }
        }

        return null;
    }

}
