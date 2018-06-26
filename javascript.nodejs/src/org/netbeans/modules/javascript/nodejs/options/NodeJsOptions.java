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
package org.netbeans.modules.javascript.nodejs.options;

import java.util.List;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.javascript.nodejs.exec.ExpressExecutable;
import org.netbeans.modules.javascript.nodejs.util.FileUtils;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.openide.util.NbPreferences;

public final class NodeJsOptions {

    public static final String NODE_PATH = "node.path"; // NOI18N
    public static final String NODE_SOURCES_PATH = "node.sources.path"; // NOI18N
    public static final String NPM_PATH = "npm.path"; // NOI18N
    public static final String NPM_IGNORE_NODE_MODULES = "npm.ignore.node_modules"; // NOI18N
    public static final String EXPRESS_PATH = "express.path"; // NOI18N

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "nodejs"; // NOI18N

    private static final NodeJsOptions INSTANCE = new NodeJsOptions();

    private final Preferences preferences;

    private volatile boolean nodeSearched = false;
    private volatile boolean npmSearched = false;
    private volatile boolean expressSearched = false;


    private NodeJsOptions() {
        preferences = NbPreferences.forModule(NodeJsOptions.class).node(PREFERENCES_PATH);
    }

    public static NodeJsOptions getInstance() {
        return INSTANCE;
    }

    public void addPreferenceChangeListener(PreferenceChangeListener listener) {
        preferences.addPreferenceChangeListener(listener);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener listener) {
        preferences.removePreferenceChangeListener(listener);
    }

    @CheckForNull
    public String getNode() {
        String path = preferences.get(NODE_PATH, null);
        if (path == null
                && !nodeSearched) {
            nodeSearched = true;
            path = NodeJsUtils.getNode();
            if (path != null) {
                setNode(path);
            }
        }
        return path;
    }

    public void setNode(String node) {
        preferences.put(NODE_PATH, node);
    }

    @CheckForNull
    public String getNodeSources() {
        return preferences.get(NODE_SOURCES_PATH, null);
    }

    public void setNodeSources(@NullAllowed String nodeSources) {
        if (nodeSources == null) {
            preferences.remove(NODE_SOURCES_PATH);
        } else {
            preferences.put(NODE_SOURCES_PATH, nodeSources);
        }
    }

    @CheckForNull
    public String getNpm() {
        String path = preferences.get(NPM_PATH, null);
        if (path == null
                && !npmSearched) {
            npmSearched = true;
            path = NodeJsUtils.getNpm();
            if (path != null) {
                setNpm(path);
            }
        }
        return path;
    }

    public void setNpm(String npm) {
        preferences.put(NPM_PATH, npm);
    }

    public boolean isNpmIgnoreNodeModules() {
        return preferences.getBoolean(NPM_IGNORE_NODE_MODULES, true);
    }

    public void setNpmIgnoreNodeModules(boolean npmIgnoreNodeModules) {
        preferences.putBoolean(NPM_IGNORE_NODE_MODULES, npmIgnoreNodeModules);
    }

    @CheckForNull
    public String getExpress() {
        String path = preferences.get(EXPRESS_PATH, null);
        if (path == null
                && !expressSearched) {
            expressSearched = true;
            List<String> files = FileUtils.findFileOnUsersPath(ExpressExecutable.EXPRESS_NAME);
            if (!files.isEmpty()) {
                path = files.get(0);
                setExpress(path);
            }
        }
        return path;
    }

    public void setExpress(String express) {
        preferences.put(EXPRESS_PATH, express);
    }

}
