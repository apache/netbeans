/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
