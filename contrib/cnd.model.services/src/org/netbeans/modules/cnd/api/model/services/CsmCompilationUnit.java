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

package org.netbeans.modules.cnd.api.model.services;

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;

/**
 * compilation unit description
 */
public final class CsmCompilationUnit {
    private final CharSequence startPath;
    private final CsmFile startFile;
    private final CsmProject startProject;
    private CsmCompilationUnit(CsmProject startProject, CharSequence path, CsmFile startFile) {
        assert startProject != null;
        assert path != null;
        this.startPath = FilePathCache.getManager().getString(path);
        this.startProject = startProject;
        this.startFile = startFile;
    }

    public static CsmCompilationUnit createCompilationUnit(CsmFile file) {
        return createCompilationUnit(file.getProject(), file.getAbsolutePath(), file);
    }

    public static CsmCompilationUnit createCompilationUnit(CsmProject startProject, CharSequence path, CsmFile startFile) {
        return new CsmCompilationUnit(startProject, path, startFile);
    }

    /**
     * returns start project
     * @return start project (never null)
     */
    public CsmProject getStartProject() {
        return startProject;
    }

    /**
     * start file if project still contains it
     * @return start file (could be null)
     */
    public CsmFile getStartFile() {
        return startFile;
    }

    /**
     * path of start file
     * @return file path (never null)
     */
    public CharSequence getStartFilePath() {
        return startPath;
    }
}
