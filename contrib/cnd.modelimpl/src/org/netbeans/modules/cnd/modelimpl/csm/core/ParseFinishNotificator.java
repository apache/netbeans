/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
