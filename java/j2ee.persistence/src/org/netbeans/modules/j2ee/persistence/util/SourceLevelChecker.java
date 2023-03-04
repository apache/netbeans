/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.persistence.util;

import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;

/**
 * A helper class for checking the source level of projects.
 * 
 * @author Erno Mononen
 */
public class SourceLevelChecker {
    
    private SourceLevelChecker() {
    }

    /**
     * Checks whether the source level of the given <code>project</code>
     * is <code>1.4</code> or lower.
     * 
     * @return true if the source level of the given project was 1.4 or lower, false 
     * otherwise.
     */
    public static boolean isSourceLevel14orLower(Project project) {
        String srcLevel = getSourceLevel(project);
        return srcLevel != null ? srcLevel.matches("1\\.[0-4]([^0-9].*)?"): false;//only 1.0-1.4 should return true, also 1.1.3, 1.1_3 should return true, 1.10>1.9 and return false.
    }
    
    /**
     * Return source level 
     *
     * @return source level for the project
     */
    public static String getSourceLevel(Project project) {
        String srcLevel = null;
        SourceLevelQueryImplementation2 sl2 = project.getLookup().lookup(SourceLevelQueryImplementation2.class);
        if(sl2 != null){
            srcLevel = sl2.getSourceLevel(project.getProjectDirectory()).getSourceLevel();
        } else {
            //backward compartibility
            SourceLevelQueryImplementation sl = project.getLookup().lookup(SourceLevelQueryImplementation.class);
            if(sl != null){
                srcLevel = sl.getSourceLevel(project.getProjectDirectory());
            }
        }
        return srcLevel;
    }
}
