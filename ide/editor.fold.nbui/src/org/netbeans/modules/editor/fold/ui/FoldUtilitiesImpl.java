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
package org.netbeans.modules.editor.fold.ui;

/**
 *
 * @author
 * sdedic
 */
public class FoldUtilitiesImpl {
    /**
     * Prefix used for initial-collapse folding preferences.
     */
    public static final String PREF_COLLAPSE_PREFIX = "code-folding-collapse-";
    
    /**
     * Preference key name for "use defaults" (default: true)
     */
    public static final String PREF_OVERRIDE_DEFAULTS = "code-folding-use-defaults"; // NOI18N
    
    /**
     * Preference key name for enable code folding (default: true)
     */
    public static final String PREF_CODE_FOLDING_ENABLED = "code-folding-enable"; // NOI18N
    
    /**
     * Preference key for "Content preview" display option (default: true).
     */
    public static final String PREF_CONTENT_PREVIEW = "code-folding-content.preview"; // NOI18N

    /**
     * Preference key for "Show summary" display option (default: true).
     */
    public static final String PREF_CONTENT_SUMMARY = "code-folding-content.summary"; // NOI18N
    
}
