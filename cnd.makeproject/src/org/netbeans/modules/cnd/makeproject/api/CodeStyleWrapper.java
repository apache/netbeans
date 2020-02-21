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
package org.netbeans.modules.cnd.makeproject.api;

/**
 *
 */
public final class CodeStyleWrapper {
    public static final String CLANG_FORMAT_FILE = ".clang-format"; //NOI18N
    
    private final String styleId;
    private final String displayName;
    private MakeProject.FormattingStyle type;
    
    public static CodeStyleWrapper createProjectStyle(String styleId, String displayName) {
        return new CodeStyleWrapper(MakeProject.FormattingStyle.Project, styleId, displayName);
    }

    public static CodeStyleWrapper createClangFormatStyle(String fileOrStyle, boolean isFile) {
        if (isFile) { //NOI18N
            return new CodeStyleWrapper(MakeProject.FormattingStyle.ClangFormat, fileOrStyle, "file"); //NOI18N
        } else {
            return new CodeStyleWrapper(MakeProject.FormattingStyle.ClangFormat, fileOrStyle, "style"); //NOI18N
        }
    }

    public static CodeStyleWrapper decodeProjectStyle(MakeProject.FormattingStyle type, String styleIdAndDisplayName) {
        return new CodeStyleWrapper(type, styleIdAndDisplayName);
    }

    private CodeStyleWrapper(MakeProject.FormattingStyle type, String styleId, String displayName) {
        this.styleId = styleId;
        this.displayName = displayName;
        this.type = type;
    }

    private CodeStyleWrapper(MakeProject.FormattingStyle type, String styleIdAndDisplayName) {
        int i = styleIdAndDisplayName.indexOf('|');
        if (i > 0) {
            this.styleId = styleIdAndDisplayName.substring(0, i);
            this.displayName = styleIdAndDisplayName.substring(i + 1);
        } else {
            this.styleId = styleIdAndDisplayName;
            this.displayName = styleIdAndDisplayName;
        }
        this.type = type;
    }

    public String getStyleId() {
        return styleId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String toExternal() {
        return styleId + '|' + displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
    
}
