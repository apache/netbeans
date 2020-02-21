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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.PrintWriter;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class FileSnapshot implements CsmFile {
    private final CharSequence absPath;
    private final CsmProject project;
    private final CharSequence name;
    private final Collection<CsmInclude> includes;
    private final Collection<CsmErrorDirective> errors;
    private final Collection<CsmOffsetableDeclaration> declarations;
    private final Collection<CsmMacro> macros;
    private final Collection<CsmScopeElement> scoped;
    private final FileType fileType;
    private final boolean isSource;
    private final boolean isHeader;
    private final FileImpl delegate;

    FileSnapshot(FileImpl impl) {
        absPath = impl.getAbsolutePath();
        project = impl.getProject();
        name = impl.getName();
        includes = impl.getIncludes();
        errors = impl.getErrors();
        declarations = impl.getDeclarations();
        macros = impl.getMacros();
        scoped = impl.getScopeElements();
        isSource = impl.isSourceFile();
        isHeader = impl.isHeaderFile();
        fileType = impl.getFileType();
        delegate = impl;
    }

    @Override
    public CharSequence getAbsolutePath() {
        return absPath;
    }

    @Override
    public FileObject getFileObject() {
        return delegate.getFileObject();
    }

    @Override
    public CsmProject getProject() {
        return project;
    }

    @Override
    public CharSequence getText() {
        assert false;
        return delegate.getText();
    }

    @Override
    public CharSequence getText(int start, int end) {
        assert false;
        return delegate.getText(start, end);
    }

    @Override
    public Collection<CsmInclude> getIncludes() {
        return includes;
    }

    @Override
    public Collection<CsmErrorDirective> getErrors() {
        return errors;
    }

    @Override
    public Collection<CsmOffsetableDeclaration> getDeclarations() {
        return declarations;
    }

    @Override
    public Collection<CsmMacro> getMacros() {
        return macros;
    }

    @Override
    public boolean isParsed() {
        return true;
    }

    @Override
    public void scheduleParsing(boolean wait) throws InterruptedException {
    }

    @Override
    public boolean isSourceFile() {
        return isSource;
    }

    @Override
    public boolean isHeaderFile() {
        return isHeader;
    }

    @Override
    public FileType getFileType() {
        return fileType;
    }

    @Override
    public CharSequence getName() {
        return name;
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        return scoped;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public void dumpInfo(PrintWriter printOut) {
        printOut.printf("Snapshot %s%n", this.absPath);// NOI18N
    }

    public void dumpIndex(PrintWriter printOut) {
        printOut.printf("Snapshot %s%n", this.absPath);// NOI18N
    }
}
