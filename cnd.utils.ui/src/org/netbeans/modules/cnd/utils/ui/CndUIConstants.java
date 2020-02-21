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

package org.netbeans.modules.cnd.utils.ui;

/**
 * Utility class with some UI constants
 */
public final class CndUIConstants {

    private CndUIConstants() {
    }

    // CND options IDs
    public static final String TOOLS_OPTIONS_CND_CATEGORY_ID="CPlusPlus"; // NOI18N
    public static final String TOOLS_OPTIONS_CND_HIGHLIGHTING_ID="HighlightingTab"; // NOI18N
    public static final String TOOLS_OPTIONS_CND_DEBUGGER_ID="DebuggerAdvancedOption"; // NOI18N
    public static final String TOOLS_OPTIONS_CND_TOOLS_ID="ToolsTab"; // NOI18N
    public static final String TOOLS_OPTIONS_CND_OTHER_ID="OtherOptionsTab"; // NOI18N
    public static final String TOOLS_OPTIONS_CND_CODE_ASSISTANCE_ID="CodeAssistanceTab"; // NOI18N
    public static final String TOOLS_OPTIONS_CND_PROJECTS_ID="ProjectsTab"; // NOI18N

    private static final String DELIM = "/"; // NOI18N

    // CND options paths. Can be used with OptionsDisplayer.getDefault().open(path)
    public static final String TOOLS_OPTIONS_CND_HIGHLIGHTING_PATH = TOOLS_OPTIONS_CND_CATEGORY_ID + DELIM + TOOLS_OPTIONS_CND_HIGHLIGHTING_ID;
    public static final String TOOLS_OPTIONS_CND_DEBUGGER_PATH = TOOLS_OPTIONS_CND_CATEGORY_ID + DELIM + TOOLS_OPTIONS_CND_DEBUGGER_ID;
    public static final String TOOLS_OPTIONS_CND_TOOLS_PATH = TOOLS_OPTIONS_CND_CATEGORY_ID + DELIM + TOOLS_OPTIONS_CND_TOOLS_ID;
    public static final String TOOLS_OPTIONS_CND_OTHER_PATH = TOOLS_OPTIONS_CND_CATEGORY_ID + DELIM + TOOLS_OPTIONS_CND_OTHER_ID;
    public static final String TOOLS_OPTIONS_CND_CODE_ASSISTANCE_PATH = TOOLS_OPTIONS_CND_CATEGORY_ID + DELIM + TOOLS_OPTIONS_CND_CODE_ASSISTANCE_ID;
    public static final String TOOLS_OPTIONS_CND_PROJECTS_PATH = TOOLS_OPTIONS_CND_CATEGORY_ID + DELIM + TOOLS_OPTIONS_CND_PROJECTS_ID;

}
