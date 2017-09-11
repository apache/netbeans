/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.mercurial.util;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.config.HgConfigFiles;

/**
 * A class to encapsulate a Repository and allow us to cache some values
 *
 * @author John Rice
 */
public class HgRepositoryContextCache {
    private Map<File, Map<String, String>> rootToDefaultPaths;

    private static HgRepositoryContextCache instance;

    private HgRepositoryContextCache() {
    }

    public static HgRepositoryContextCache getInstance() {
        if(instance == null) {
            instance = new HgRepositoryContextCache();
        }
        return instance;
    }

    public synchronized void reset() {
        getRootToDefaultPaths().clear();
    }

    public synchronized String getPullDefault(File file) {
        File repoRoot = Mercurial.getInstance().getRepositoryRoot(file);
        if(repoRoot == null) return null;
        Map<String, String> paths = getPaths(repoRoot);
        return paths.get(HgConfigFiles.HG_DEFAULT_PULL_VALUE);
    }

    public synchronized String getPushDefault(File file) {
        File repoRoot = Mercurial.getInstance().getRepositoryRoot(file);
        if(repoRoot == null) return null;
        Map<String, String> paths = getPaths(repoRoot);
        return paths.get(HgConfigFiles.HG_DEFAULT_PUSH);
    }

    public String getPathValue (File root, String path) {
        File repoRoot = Mercurial.getInstance().getRepositoryRoot(root);
        if(repoRoot == null) return null;
        Map<String, String> paths = getPaths(repoRoot);
        return paths.get(path);
    }

    public Map<String, String> getPathValues (File root) {
        File repoRoot = Mercurial.getInstance().getRepositoryRoot(root);
        if (repoRoot == null) return Collections.<String, String>emptyMap();
        Map<String, String> paths = getPaths(repoRoot);
        return new HashMap<String, String>(paths);
    }

    private Map<String, String> getPaths(File repoRoot) {
        Map<File, Map<String, String>> map = getRootToDefaultPaths();
        Map<String, String> paths = map.get(repoRoot);
        if (paths == null) {
            HgConfigFiles config = new HgConfigFiles(repoRoot);
            String pull = config.getDefaultPull(true);
            String push = config.getDefaultPush(true);
            paths = new HashMap<String, String>();
            paths.put(HgConfigFiles.HG_DEFAULT_PULL_VALUE, pull);
            paths.put(HgConfigFiles.HG_DEFAULT_PUSH, push);
            for (Map.Entry<Object, Object> e : config.getProperties(HgConfigFiles.HG_PATHS_SECTION).entrySet()) {
                String key = (String) e.getKey();
                String value = (String) e.getValue();
                if (key != null && value != null && !key.isEmpty() && !value.isEmpty()) {
                    paths.put(key.trim(), value.trim());
                }
            }
            map.put(repoRoot, paths);
        }
        return paths;
    }

    private Map<File, Map<String, String>> getRootToDefaultPaths() {
        if(rootToDefaultPaths == null) {
            rootToDefaultPaths = new HashMap<File, Map<String, String>>();
        }
        return rootToDefaultPaths;
    }
}

