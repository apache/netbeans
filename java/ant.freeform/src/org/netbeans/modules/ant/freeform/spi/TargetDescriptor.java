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

package org.netbeans.modules.ant.freeform.spi;

import java.util.List;

/**
 * Description of the build target to be shown in Target Mappings customizer
 * panel.
 * @see ProjectNature#getExtraTargets
 * @author David Konecny
 */
public final class TargetDescriptor {

    private String actionName;
    private List<String> defaultTargets;
    private String actionLabel;
    private String accessibleLabel;

    /**
     * Constructor.
     * @param actionName IDE action name (see {@link org.netbeans.spi.project.ActionProvider})
     * @param defaultTargets list of regular expressions to match name of the 
     *   Ant target to which this IDE action usually maps
     * @param actionLabel localized label of this action. To be shown in UI customizer
     * @param accessibleLabel accessible label. Used together with actionLabel
     */
    public TargetDescriptor(String actionName, List<String> defaultTargets, String actionLabel, String accessibleLabel) {
        this.actionName = actionName;
        this.defaultTargets = defaultTargets;
        this.actionLabel = actionLabel;
        this.accessibleLabel = accessibleLabel;
    }
    
    /**
     * Name of the IDE action which is mapped to an Ant script.
     */
    public String getIDEActionName() {
        return actionName;
    }
    
    /**
     * List of regular expressions to match name of the target in Ant script 
     * which usually maps to the IDE action. List will be processed in the
     * given order so it is recommended to list the most specific ones first.
     * @return cannot be null; can be empty array
     */
    public List<String> getDefaultTargets() {
        return defaultTargets;
    }

    /**
     * Label name under which this IDE action will be presented in the 
     * Target Mapping customizer panel.
     */
    public String getIDEActionLabel() {
        return actionLabel;
    }
    
    /**
     * Accessibility of the getIDEActionLabel().
     */
    public String getAccessibleLabel() {
        return accessibleLabel;
    }
    
}
