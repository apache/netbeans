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

package org.netbeans.modules.cnd.apt.support;

import java.util.Arrays;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.ListBasedTokenStream;
import org.openide.filesystems.FileSystem;

/**
 * fake include node for "-include file" option of preprocessor
 */
public final class APTIncludeFake implements APTInclude {
    private final FileSystem fs;
    private final String filePath;
    private final boolean system;
    private final APTToken token;
    public APTIncludeFake(FileSystem fs, String filePath, boolean system, int line) {
        this.fs = fs;
        this.filePath = filePath;
        this.system = system;
        this.token = APTUtils.createAPTToken(APTTokenTypes.INCLUDE);
        this.token.setColumn(0);
        this.token.setLine(line);
        this.token.setOffset(line);
        this.token.setEndColumn(0);
        this.token.setEndLine(line);
        this.token.setEndOffset(line);
        this.token.setText("-include"); // NOI18N
    }

    @Override
    public TokenStream getInclude() {
        return new ListBasedTokenStream(Arrays.asList(token, token));
    }

    @Override
    public String getFileName(APTMacroCallback callback) {
        return filePath;
    }

    @Override
    public boolean isSystem(APTMacroCallback callback) {
        return system;
    }

    @Override
    public boolean accept(APTFile curFile, APTToken token) {
        throw new UnsupportedOperationException("Not supposed to be used."); // NOI18N
    }

    @Override
    public APTToken getToken() {
        return this.token;
    }

    @Override
    public APT getFirstChild() {
        return null;
    }

    @Override
    public APT getNextSibling() {
        return null;
    }

    @Override
    public String getText() {
        return filePath;
    }

    @Override
    public int getType() {
        return APT.Type.INCLUDE;
    }

    @Override
    public int getOffset() {
        return this.token.getOffset();
    }

    @Override
    public int getEndOffset() {
        return this.token.getEndOffset();
    }

    @Override
    public void setFirstChild(APT child) {
        throw new UnsupportedOperationException("Not supposed to be used."); // NOI18N
    }

    @Override
    public void setNextSibling(APT next) {
        throw new UnsupportedOperationException("Not supposed to be used."); // NOI18N
    }

    public FileSystem getFileSystem() {
        return fs;
    }
    
    @Override
    public String toString() {
        return "-include " + filePath; // NOI18N
    }

}
