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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.Collection;
import java.util.HashSet;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.util.WeakList;

/**
 * Coordiates parse finish event for project and it's libs.
 * Singleton.
 *
 */
public class ParseFinishNotificator {

    private static final ParseFinishNotificator instance = new ParseFinishNotificator();
    private final WeakList<CsmProject> waitingProjects = new WeakList<>();

    private ParseFinishNotificator() {
    }

    /**
     * Instance.
     *
     * @return instance
     */
    public static ParseFinishNotificator instance() {
        return instance;
    }

    /**
     * Notificates projects about finish of parsing in libs.
     *
     * @param project - parsed project
     */
    public static void onParseFinish(CsmProject project) {
        synchronized (instance) {
            instance.waitingProjects.remove(project);
            if (areProjectAndLibsParsed(project)) {
                if (project instanceof ProjectBase) {
                    ((ProjectBase) project).onLibParseFinish();
                }
                for (CsmProject lib : instance.waitingProjects) {
                    if (isProjectsLib(lib, project)) {
                        if (areProjectAndLibsParsed(lib)) {
                            if (lib instanceof ProjectBase) {
                                instance.waitingProjects.remove(lib);
                                ((ProjectBase) lib).onLibParseFinish();
                            }
                        }
                    }
                }
            } else {
                if(project.isStable(null)) {
                    instance.waitingProjects.add(project);
                }
            }
        }
    }

    private static boolean isProjectsLib(CsmProject project, CsmProject lib) {
        if (project.equals(lib)) {
            return false;
        }
        HashSet<CsmProject> visited = new HashSet<>();
        visited.add(project);
        return isProjectInLibs(lib, project.getLibraries(), visited);
    }

    private static boolean isProjectInLibs(CsmProject project, Collection<CsmProject> libs, Collection<CsmProject> visited) {
        if (libs != null) {
            for (CsmProject lib : libs) {
                if (visited.contains(lib)) {
                    continue;
                }
                visited.add(lib);
                if (project.equals(lib) || isProjectInLibs(project, lib.getLibraries(), visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean areProjectAndLibsParsed(CsmProject project) {
        if (!project.isStable(null)) {
            return false;
        }
        HashSet<CsmProject> visited = new HashSet<>();
        visited.add(project);
        return areProjectLibsParsed(project.getLibraries(), visited);
    }

    private static boolean areProjectLibsParsed(Collection<CsmProject> libs, Collection<CsmProject> visited) {
        if (libs != null) {
            for (CsmProject lib : libs) {
                if (visited.contains(lib)) {
                    continue;
                }
                visited.add(lib);
                if (!lib.isStable(null) || !areProjectLibsParsed(lib.getLibraries(), visited)) {
                    return false;
                }
            }
        }
        return true;
    }
}
