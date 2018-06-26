/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.platform;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

public final class NodeJsSourceRoots {

    private static final Logger LOGGER = Logger.getLogger(NodeJsSourceRoots.class.getName());

    public static final String LIB_DIRECTORY = "lib"; // NOI18N

    private final Project project;

    // @GuardedBy("this")
    private List<URL> sourceRoots = null;


    public NodeJsSourceRoots(Project project) {
        assert project != null;
        this.project = project;
    }

    public synchronized List<URL> getSourceRoots() {
        if (sourceRoots == null) {
            sourceRoots = findSourceRoots();
        }
        return new ArrayList<>(sourceRoots);
    }

    public synchronized void resetSourceRoots() {
        sourceRoots = null;
    }

    private List<URL> findSourceRoots() {
        File nodeSources = NodeJsUtils.getNodeSources(project);
        if (nodeSources == null) {
            return Collections.emptyList();
        }
        File lib = new File(nodeSources, LIB_DIRECTORY);
        if (!lib.isDirectory()) {
            return Collections.emptyList();
        }
        try {
            URL nodeLib = Utilities.toURI(FileUtil.normalizeFile(lib)).toURL();
            return Collections.singletonList(nodeLib);
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.INFO, null, ex);
            assert false;
        }
        return Collections.emptyList();
    }

}
