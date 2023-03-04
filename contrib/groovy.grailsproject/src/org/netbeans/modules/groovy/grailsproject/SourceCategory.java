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
package org.netbeans.modules.groovy.grailsproject;

/**
 * Represents a grails source category.
 * @author Bruno Flavio
 */
public class SourceCategory {

    private final String relativePath;

    private final String command;

    private final String suffix;

    /**
     * Creates a grails source category.
     * @param relativePath place where files of this category should be stored
     * @param command grails command that should be invoked to create a file of this category
     * @param suffix suffix present in every file name of this category
     */
    SourceCategory(String relativePath, String command, String suffix) {
        this.relativePath = relativePath;
        this.command = command;
        this.suffix = suffix;
    }

    /**
     * Returns this category relative path, i.e the folder where source files
     * of this category should be placed.
     * 
     * @return the relative path.
     */
    public String getRelativePath() {
        return relativePath;
    }

    /**
     * Returns the grails command that creates files of this category.
     * @return the grails command.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns the filename suffix that should be applied by convention to this
     * source category.
     * @return  filename suffix.
     */
    public String getSuffix() {
        return suffix;
    }
}
