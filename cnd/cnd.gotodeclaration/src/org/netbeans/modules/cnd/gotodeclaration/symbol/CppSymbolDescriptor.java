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

package org.netbeans.modules.cnd.gotodeclaration.symbol;

import javax.swing.Icon;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * SymbolDescriptor implementation for C/C++
 */
public class CppSymbolDescriptor extends SymbolDescriptor implements Runnable {

    private final Icon icon;
    private final CsmProject project;
    private final CharSequence filePath;
    private final int offset;
    private final CharSequence ownerName;
    private final CharSequence name;
    
    public CppSymbolDescriptor(CsmOffsetable csmObj) {
        this(csmObj, null);
    }
    
    public CppSymbolDescriptor(CsmOffsetable csmObj, CsmOffsetable toOpen) {
        Parameters.notNull("csmObj", csmObj);
        CsmFile csmFile;
        if (toOpen != null) {
            csmFile = toOpen.getContainingFile();
            offset = toOpen.getStartOffset();
        } else {
            csmFile = csmObj.getContainingFile();
            offset = csmObj.getStartOffset();
        }
        filePath = csmFile.getAbsolutePath();
        project = csmFile.getProject();
        if (CsmKindUtilities.isClass(csmObj) && CsmKindUtilities.isTemplate(csmObj)) {
            name = ((CsmTemplate)csmObj).getDisplayName();
        } else if (CsmKindUtilities.isFunction(csmObj)) {
            name = ((CsmFunction) csmObj).getSignature();
        } else if (CsmKindUtilities.isNamedElement(csmObj)) {
            name = ((CsmNamedElement) csmObj).getName();
        } else {
            throw new IllegalArgumentException("should be CsmNamedElement, in fact " + csmObj.getClass().getName()); //NOI18N
        }

        CharSequence fileName = csmFile.getName();
        if (CsmKindUtilities.isMacro(csmObj)) {
            //CsmMacro macro = (CsmMacro)  csmObj;
            ownerName = fileName;
        } else if (CsmKindUtilities.isOffsetableDeclaration(csmObj)) {
            CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) csmObj;
            CsmScope scope = decl.getScope();
            if (CsmKindUtilities.isFile(scope)) {
                ownerName = fileName;
            }
            else if (CsmKindUtilities.isQualified(scope)) {
                CharSequence qName = ((CsmQualifiedNamedElement) scope).getQualifiedName();
                if (qName.length() > 0) {
                    ownerName = NbBundle.getMessage(getClass(), "CPP_Descriptor_In_Compound", qName, fileName);
                } else {
                    ownerName = fileName;
                }
            } else {
                throw new IllegalArgumentException("should be either CsmFile or CsmQualifiedNamedElement, in fact " + csmObj.getClass().getName()); //NOI18N
            }
        } else {
            throw new IllegalArgumentException("should be either CsmMacro or CsmDeclaration, in fact " + csmObj.getClass().getName()); //NOI18N
        }
        icon = CsmImageLoader.getIcon(csmObj);
    }
    
    @Override
    public FileObject getFileObject() {
        CndUtils.assertNonUiThread();
        return new FSPath(project.getFileSystem(), filePath.toString()).getFileObject();
    }

    @Override
    public String getFileDisplayPath() {
        return filePath.toString();
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public String getOwnerName() {
        return ownerName.toString();
    }

    @Override
    public Icon getProjectIcon() {
        return CsmImageLoader.getIcon(project);
    }

    @Override
    public String getProjectName() {
        CharSequence prjName = project.getName();
        if (project.isArtificial()) {
            prjName = CsmDisplayUtilities.shrinkPath(prjName, 32, 2, 2);
        }
        return prjName.toString();
    }

    @Override
    public String getSymbolName() {
        return name.toString();
    }

    @Override
    public String getSimpleName() {
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '(') {
                if (i+2 < name.length() && name.charAt(i+1) == ')' && name.charAt(i+2) == '(') {
                    continue;
                }
                return name.subSequence(0, i).toString();
            }
        }
        return name.toString();
    }

    @Override
    public void open() {
        RequestProcessor.getDefault().post(this);
    }

    @Override
    public void run() {
        CsmUtilities.openSource(getFileObject(), offset);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.project != null ? this.project.hashCode() : 0);
        hash = 41 * hash + (this.filePath != null ? this.filePath.hashCode() : 0);
        hash = 41 * hash + this.offset;
        hash = 41 * hash + (this.ownerName != null ? this.ownerName.hashCode() : 0);
        hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CppSymbolDescriptor other = (CppSymbolDescriptor) obj;
        if (this.offset != other.offset) {
            return false;
        }
        if (this.project != other.project && (this.project == null || !this.project.equals(other.project))) {
            return false;
        }
        if (this.filePath != other.filePath && (this.filePath == null || !this.filePath.equals(other.filePath))) {
            return false;
        }
        if (this.ownerName != other.ownerName && (this.ownerName == null || !this.ownerName.equals(other.ownerName))) {
            return false;
        }
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        return true;
    }
    
}
