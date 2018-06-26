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
package org.netbeans.modules.web.clientproject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.web.clientproject.env.CommonProjectHelper;
import org.openide.util.EditableProperties;

public class ProjectUpgrader {

    private final ClientSideProject project;


    public ProjectUpgrader(ClientSideProject project) {
        assert project != null;
        this.project = project;
    }

    public void upgrade() {
        ProjectManager.mutex().writeAccess(new Runnable() {
            @Override
            public void run() {
                upgradeProjectProperties();
            }
        });
    }

    void upgradeProjectProperties() {
        EditableProperties properties = project.getProjectHelper().getProperties(CommonProjectHelper.PROJECT_PROPERTIES_PATH);
        // specific upgrades
        upgradeGrunt(properties);
        project.getProjectHelper().putProperties(CommonProjectHelper.PROJECT_PROPERTIES_PATH, properties);
    }

    //~ Grunt

    private static final String LEGACY_GRUNT_ACTION_PREFIX = "grunt.action."; // NOI18N
    private static final String GRUNT_ACTION_PREFIX = "auxiliary.org-netbeans-modules-javascript-grunt.action_2e_"; // NOI18N


    private void upgradeGrunt(EditableProperties properties) {
        Set<String> toRemove = null;
        Map<String, String> toAdd = null;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(LEGACY_GRUNT_ACTION_PREFIX)) {
                if (toRemove == null) {
                    toRemove = new HashSet<>();
                }
                toRemove.add(key);
                if (toAdd == null) {
                    toAdd = new HashMap<>();
                }
                String newKey = GRUNT_ACTION_PREFIX + key.replace(LEGACY_GRUNT_ACTION_PREFIX, ""); // NOI18N
                toAdd.put(newKey, entry.getValue());
            }
        }
        if (toRemove != null) {
            for (String key : toRemove) {
                properties.remove(key);
            }
        }
        if (toAdd != null) {
            properties.putAll(toAdd);
        }
    }

}
