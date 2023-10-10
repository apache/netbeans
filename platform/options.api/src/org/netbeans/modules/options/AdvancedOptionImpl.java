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

package org.netbeans.modules.options;

import java.lang.String;
import java.util.Arrays;
import org.netbeans.spi.options.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.openide.util.Exceptions;

/**
 * Advanced Option implementation class. Used by factory method from
 * <code>AdvancedOption</code> as instance created from layer.xml values
 *
 * @author Max Sauer
 */
public class AdvancedOptionImpl extends AdvancedOption {
    
    private String displayName;
    private String tooltip;
    private String keywords;
    private Callable<OptionsPanelController> controller;
    private String keywordsCategory;

    @SuppressWarnings("deprecation")
    public AdvancedOptionImpl(Callable<OptionsPanelController> controller, String displayName, String tooltip, String keywords, String keywordsCategory) {
        this.controller = controller;
        this.displayName = displayName;
        this.tooltip = tooltip;
        this.keywords = keywords;
        this.keywordsCategory = keywordsCategory;
    }

    @Override
    public String getDisplayName () {
        return displayName;
    }

    @Override
    public String getTooltip () {
        return tooltip;
    }

    /**
     * Provides list of options for this category
     * @return set of keywords for each optioncategory sub-panel
     */
    public Set<String> getKeywordsByCategory() {
	if (keywords != null) {
	    return Collections.singleton(keywords);
	} else {
	    return Collections.emptySet();
	}
    }

    @Override
    public OptionsPanelController create() {
        try {
            return controller.call();
        } catch (Exception x) {
            Exceptions.printStackTrace(x);
            return new TabbedController("<error>"); // NOI18N
        }
    }

}
