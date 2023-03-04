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
package org.netbeans.modules.refactoring.java.ui.instant;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
public final class InstantOption {
    
    private final String displayName;
    private final String tooltip;
    private boolean selected;

    public InstantOption(String displayName, String tooltip, boolean selected) {
        this.displayName = displayName;
        this.tooltip = tooltip;
        this.selected = selected;
    }
    
    /**
     * The options' display name. Will be used as the display name of the
     * checkbox in the customizer.
     */
    public String displayName() {
        return displayName;
    }

    /**
     * The tooltip of the checkbox in the customizer.
     */
    public String tooltip() {
        return tooltip;
    }

    /**
     * The default value of the option.
     */
    public boolean selected() {
        return selected;
    }

    void setSelected(boolean newValue) {
        selected = newValue;
    }
}
