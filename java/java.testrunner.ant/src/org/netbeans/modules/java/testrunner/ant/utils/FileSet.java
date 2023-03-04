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

package org.netbeans.modules.java.testrunner.ant.utils;

import java.io.File;
import java.util.Collection;
import org.apache.tools.ant.module.spi.TaskStructure;

/**
 *
 * @author  Marian Petras
 */
final class FileSet {

    /** */
    private final AntProject project;
    
    /** */
    private PatternSet implicitPatternSet;
    /** */
    private File baseDir;
    /** */
    private File file;
    /** */
    private boolean defaultExcludes = true;
    /** */
    private boolean caseSensitive = true;
    /** */
    private boolean followSymlinks;

    /**
     */
    FileSet(AntProject project) {
        this.project = project;
    }

    /**
     *
     */
    void handleChildrenAndAttrs(TaskStructure struct) {
        /*
         * Only FileSet-specific attributes are handled by setAttrs(struct).
         * Attributes of the implicit pattern set are handled by the call of
         * implicitPatternSet.handleChildrenAndAttrs(struct) below.
         */
        setAttrs(struct);
        
        /* Handles PatternSet-like children and attributes: */
        implicitPatternSet = new PatternSet(project);
        implicitPatternSet.handleChildrenAndAttrs(struct);
    }
    
    /**
     * Handles this {@code FileSet}'s attributes.
     *
     * @param  struct  XML element corresponding to this {@code FileSet}
     */
    private void setAttrs(TaskStructure struct) {
        String dirName = struct.getAttribute("dir");                    //NOI18N
        String fileName = struct.getAttribute("file");                  //NOI18N
        String defaultExcludes = struct.getAttribute("defaultexcludes");//NOI18N
        String caseSensitive = struct.getAttribute("casesensitive");    //NOI18N
        String followSymlinks = struct.getAttribute("followsymlinks");  //NOI18N
        
        if (dirName != null) {
            dirName = project.replaceProperties(dirName);
            setBaseDir(project.resolveFile(dirName));
        }
        if (fileName != null) {
            fileName = project.replaceProperties(fileName);
            setFile(project.resolveFile(fileName));
        }
        if (defaultExcludes != null) {
            defaultExcludes = project.replaceProperties(defaultExcludes);
            setDefaultExcludes(AntProject.toBoolean(defaultExcludes));
        }
        if (caseSensitive != null) {
            caseSensitive = project.replaceProperties(caseSensitive);
            setCaseSensitive(AntProject.toBoolean(caseSensitive));
        }
        if (followSymlinks != null) {
            followSymlinks = project.replaceProperties(followSymlinks);
            setFollowSymlinks(AntProject.toBoolean(followSymlinks));
        }
    }
    
    /**
     */
    private void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }
    
    /**
     */
    private void setFile(File file) {
        this.file = file;
    }
    
    /**
     */
    private void setDefaultExcludes(boolean defaultExcludes) {
        this.defaultExcludes = defaultExcludes;
    }
    
    /**
     */
    private void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
    
    /**
     */
    private void setFollowSymlinks(boolean followSymlinks) {
        this.followSymlinks = followSymlinks;
    }
    
    
    /**
     */
    File getBaseDir() {
        return baseDir;
    }
    
    /**
     */
    File getFile() {
        return file;
    }
    
    /**
     */
    boolean isDefaultExcludes() {
        return defaultExcludes;
    }
    
    /**
     */
    boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    /**
     */
    boolean isFollowSymlinks() {
        return followSymlinks;
    }
    
    /**
     */
    Collection<String> getIncludePatterns() {
        return implicitPatternSet.getIncludePatterns();
    }
    
    /**
     */
    Collection<String> getExcludesPatterns() {
        return implicitPatternSet.getExcludePatterns();
    }
    
}
