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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.module.spi.TaskStructure;

/**
 *
 * @author  Marian Petras
 */
final class PatternSet {
    
    /** */
    private final AntProject project;

    /** */
    private Collection<String> includePatterns;
    /** */
    private Collection<String> excludePatterns;
    
    /**
     */
    PatternSet(AntProject project) {
        this.project = project;
    }

    /**
     *
     */
    void handleChildrenAndAttrs(TaskStructure struct) {
        setAttrs(struct);
        for (TaskStructure child : struct.getChildren()) {
            String childName = child.getName();
            if (childName.equals("include")) {                          //NOI18N
                Entry entry = new Entry();
                entry.handleChildrenAndAttrs(child);
                if (entry.isApplicable()) {
                    addIncludePatterns(entry.getPattern());
                }
                continue;
            }
            if (childName.equals("exclude")) {                          //NOI18N
                Entry entry = new Entry();
                entry.handleChildrenAndAttrs(child);
                if (entry.isApplicable()) {
                    addExcludePatterns(entry.getPattern());
                }
                continue;
            }
            if (childName.equals("includesfile")) {                     //NOI18N
                Entry entry = new Entry();
                entry.handleChildrenAndAttrs(child);
                if (entry.isApplicable()) {
                    addIncludePatterns(project.resolveFile(entry.getPattern()));
                }
                continue;
            }
            if (childName.equals("excludesfile")) {                     //NOI18N
                Entry entry = new Entry();
                entry.handleChildrenAndAttrs(child);
                if (entry.isApplicable()) {
                    addExcludePatterns(project.resolveFile(entry.getPattern()));
                }
                continue;
            }
            if (childName.equals("patternset")) {                       //NOI18N
                PatternSet patternSet = new PatternSet(project);
                patternSet.handleChildrenAndAttrs(child);
                addPatternSet(patternSet);
                continue;
            }
        }
    }
    
    /**
     */
    Collection<String> getIncludePatterns() {
        if (includePatterns != null) {
            return includePatterns;
        } else {
            return Collections.emptyList();
        }
    }
    
    /**
     */
    Collection<String> getExcludePatterns() {
        if (excludePatterns != null) {
            return excludePatterns;
        } else {
            return Collections.emptyList();
        }
    }
    
    /**
     * Handles this {@code PatternSet}'s attributes.
     *
     * @param  struct  XML element corresponding to this {@code PatternSet}
     */
    private void setAttrs(TaskStructure struct) {
        String includes = struct.getAttribute("includes");              //NOI18N
        String includesFile = struct.getAttribute("includesFile");      //NOI18N
        String excludes = struct.getAttribute("excludes");              //NOI18N
        String excludesFile = struct.getAttribute("excludesFile");      //NOI18N
        
        if (includes != null) {
            addIncludePatterns(project.replaceProperties(includes));
        }
        if (excludes != null) {
            addExcludePatterns(project.replaceProperties(excludes));
        }
        if (includesFile != null) {
            addIncludePatterns(project.resolveFile(
                    project.replaceProperties(includesFile)));
        }
        if (excludesFile != null) {
            addExcludePatterns(project.resolveFile(
                    project.replaceProperties(excludesFile)));
        }
    }
    
    /**
     */
    private void addIncludePatterns(String patternsString) {
        if (includePatterns == null) {
            includePatterns = new ArrayList<String>();
        }
        addPatterns(patternsString, includePatterns);
    }
    
    /**
     */
    private void addExcludePatterns(String patternsString) {
        if (excludePatterns == null) {
            excludePatterns = new ArrayList<String>();
        }
        addPatterns(patternsString, excludePatterns);
    }
    
    /**
     */
    private void addIncludePatterns(File includesFile) {
        if (includePatterns == null) {
            includePatterns = new ArrayList<String>();
        }
        readPatterns(includesFile, includePatterns);
    }
    
    /**
     */
    private void addExcludePatterns(File excludesFile) {
        if (excludePatterns == null) {
            excludePatterns = new ArrayList<String>();
        }
        readPatterns(excludesFile, excludePatterns);
    }
    
    /**
     */
    private void addPatterns(String patternsString,
                             Collection<String> patterns) {
        StringTokenizer tokenizer = new StringTokenizer(patternsString,
                                                        ", ");          //NOI18N
        while (tokenizer.hasMoreTokens()) {
            patterns.add(tokenizer.nextToken());
        }
    }
    
    /**
     */
    private void readPatterns(File patternsFile,
                              Collection<String> patterns) {
        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new FileReader(patternsFile));
            for (String line = fileReader.readLine(); line != null; ) {
                if (line.length() != 0) {
                    addPatterns(project.replaceProperties(line), patterns);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName())
                  .log(Level.INFO,
                       "failed to read Ant patterns file "              //NOI18N
                                + patternsFile.getAbsolutePath(),
                       ex);
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException ex2) {
                    //ignore the exception
                }
            }
        }
    }
    
    /**
     */
    private void addPatternSet(PatternSet p) {
        if (p.includePatterns != null) {
            if (includePatterns != null) {
                includePatterns.addAll(p.includePatterns);
            } else {
                includePatterns = p.includePatterns;
            }
        }
        
        if (p.excludePatterns != null) {
            if (excludePatterns != null) {
                excludePatterns.addAll(p.excludePatterns);
            } else {
                excludePatterns = p.excludePatterns;
            }
        }
    }

    
    /**
     *
     */
    class Entry {
        
        /** */
        private String pattern;
        /** */
        private String ifCondition;
        /** */
        private String unlessCondition;
        
        /**
         */
        void handleChildrenAndAttrs(TaskStructure struct) {
            setAttrs(struct);
        }
        
        /**
         * Handles attributes of this {@code Entry}.
         *
         * @param  struct  XML element corresponding to this {@code Entry}
         */
        private void setAttrs(TaskStructure struct) {
            pattern = struct.getAttribute("name");                      //NOI18N
            ifCondition = struct.getAttribute("if");                    //NOI18N
            unlessCondition = struct.getAttribute("unless");            //NOI18N
        }
        
        /**
         * Checks whether this entry is valid according to the
         * <em>if</em> and <em>unless</em> conditions.
         * 
         * @return  {@code true} if call conditions imposed by the optional
         *          <em>if</em> and <em>unless</em> attributes are met;
         *          {@code false} otherwise
         */
        boolean isApplicable() {
            return ((ifCondition == null)
                        || project.toBoolean(
                                project.replaceProperties(ifCondition)))
                   &&
                   ((unlessCondition == null)
                        || !project.toBoolean(
                                project.replaceProperties(unlessCondition)));
        }
        
        /**
         */
        String getPattern() {
            return project.replaceProperties(pattern);
        }

    }
    
}
