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
package org.netbeans.modules.css.visual.api;

/**
 * View modes for the rule editor UI component.
 * 
 * @author marekfukala
 */
public enum ViewMode {
    
    /**
     * No categories, properties sorted alphabetically, show only set properties.
     */
    UPDATED_ONLY(false, false),
    
    /**
     * Categories shown, elements sorted alphabetically, show all existing properties.
     */
    CATEGORIZED(true, true),
    
    /**
     * No categories, properties sorted alphabetically, show all existing properties.
     */
    ALL(false, true);
    
    
    private final boolean showCategories;
    private final boolean showAllProperties;

    private ViewMode(boolean showCategories, boolean showAllProperties) {
        this.showCategories = showCategories;
        this.showAllProperties = showAllProperties;
    }

    public boolean isShowCategories() {
        return showCategories;
    }

    public boolean isShowAllProperties() {
        return showAllProperties;
    }
    
}
