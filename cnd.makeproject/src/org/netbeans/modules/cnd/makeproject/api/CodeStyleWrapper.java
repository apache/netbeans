/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
