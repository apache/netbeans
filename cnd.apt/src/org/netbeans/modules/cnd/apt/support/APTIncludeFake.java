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
