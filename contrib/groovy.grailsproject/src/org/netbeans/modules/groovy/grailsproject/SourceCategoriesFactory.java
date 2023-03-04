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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform.Version;

/**
 * This factory provides source categories for a given version of grails.
 * @author Bruno Flavio
 */
public class SourceCategoriesFactory {
    
    private static final Map<Version, SourceCategoriesInterface> versions = 
            new TreeMap<>(Collections.reverseOrder());
    
    static {
        /*
        Registers the available Source Categories handlers and their minimum version.
        */
        versions.put(SourceCategoriesGrails11.MIN_VERSION, new SourceCategoriesGrails11());
        versions.put(SourceCategoriesGrails301.MIN_VERSION, new SourceCategoriesGrails301());
    }
    
    private SourceCategoriesInterface categories;
    
    /**
     * Creates a source categories factory using the current Grails Platform default version.
     */
    public SourceCategoriesFactory() {
        this(GrailsPlatform.getDefault().getVersion());
    }
    
    /**
     * Creates a source categories factory using the specified Grails version.
     * @param version - Grails version to use.
     */
    public SourceCategoriesFactory(GrailsPlatform.Version version) {
        this.categories =  SourceCategoriesFactory.versions.entrySet().stream()
                .filter(map -> version.compareTo(map.getKey()) >= 0)
                .map( map -> map.getValue() )
                .findFirst()
                .orElse(new SourceCategoriesGrails11());
    }
    
    /**
     * Returns the source category for a given source category type.
     * @param type Source category type to retrieve
     * @return The source category found for this type and factory version.
     */
    public SourceCategory getSourceCategory(SourceCategoryType type) {
        return categories.getSourceCategory(type);
    }

}