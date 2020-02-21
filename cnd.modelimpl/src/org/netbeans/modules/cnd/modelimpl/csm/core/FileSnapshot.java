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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
