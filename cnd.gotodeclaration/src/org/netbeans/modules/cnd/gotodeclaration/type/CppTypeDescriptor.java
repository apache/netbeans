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
package org.netbeans.modules.cnd.gotodeclaration.type;

import javax.swing.Icon;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.gotodeclaration.util.ContextUtil;

import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;

import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * 
 * Implementation of type descriptor for "Jump to Type" for C/C++
 */
/* package-local */
final class CppTypeDescriptor extends TypeDescriptor {

    private final CsmUID<CsmClassifier> uid;

    private final String simpleName;
    private final String typeName;
    private final CsmDeclaration.Kind kind;
    private final int modifiers;
    private final CsmProject project; 
    private final String outerName; 
    private final String contextName; 
    private final String filePath; // we need this to eliminate duplication
    @SuppressWarnings("unchecked")
    public CppTypeDescriptor(CsmClassifier classifier) {
	uid = UIDs.get(classifier);
	kind = classifier.getKind();
	modifiers = CsmUtilities.getModifiers(classifier);
	if( CsmKindUtilities.isOffsetable(classifier) ) {
            CsmFile file = ((CsmOffsetable) classifier).getContainingFile();
	    if( file != null ) {
                project = file.getProject();
                filePath = file.getAbsolutePath().toString();
	    } else {
                project = null;
                filePath = "";
            }
	} else {
            project = null;
            filePath = "";
        }
        if (CsmKindUtilities.isClass(classifier) && CsmKindUtilities.isTemplate(classifier)) {
            simpleName = ((CsmTemplate)classifier).getDisplayName().toString();
        } else {
            simpleName = classifier.getName().toString();
        }
	String aTypeName = simpleName;
	
	String anOuterName = ContextUtil.getContextName(classifier);
	CsmScope scope = classifier.getScope();
	if( CsmKindUtilities.isClass(scope) ) {
	    CsmClass cls = ((CsmClass) scope);
	    aTypeName = NbBundle.getMessage(CppTypeDescriptor.class, "TYPE_NAME_FORMAT", simpleName, ContextUtil.getClassFullName(cls));
	}	
        this.outerName = anOuterName;
        this.typeName = aTypeName;
        if (outerName != null && !outerName.isEmpty()) {
            contextName = NbBundle.getMessage(CppTypeDescriptor.class, "CONTEXT_NAME_FORMAT", outerName);
        } else {
            contextName = "";
        }
    }
    
    @Override
    public String getSimpleName() {
	return simpleName;
    }

    @Override
    public String getOuterName() {
	return outerName;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public String getContextName() {
        return contextName;
    }

    @Override
    public Icon getIcon() {
        return CsmImageLoader.getIcon(kind, modifiers);
    }

    @Override
    public String getProjectName() {
        return project == null ? "" : project.getName().toString();
    }

    @Override
    public Icon getProjectIcon() {
	return project == null ? null : CsmImageLoader.getIcon(project);
    }

    @Override
    public FileObject getFileObject() {
	CsmClassifier cls = uid.getObject();
	if( CsmKindUtilities.isOffsetable(cls) ) {
	    CsmFile file = ((CsmOffsetable) cls).getContainingFile();
	    return CsmUtilities.getFileObject(file);
	}
	return null;
    }

    @Override
    public String getFileDisplayPath() {
        return filePath;
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public void open() {
        CsmUtilities.openSource(uid.getObject());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CppTypeDescriptor other = (CppTypeDescriptor) obj;
        if (this.simpleName != other.simpleName && (this.simpleName == null || !this.simpleName.equals(other.simpleName))) {
            return false;
        }
        if (this.kind != other.kind) {
            return false;
        }
        if (this.outerName != other.outerName && (this.outerName == null || !this.outerName.equals(other.outerName))) {
            return false;
        }
        if (this.filePath != other.filePath && (this.filePath == null || !this.filePath.equals(other.filePath))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + (this.simpleName != null ? this.simpleName.hashCode() : 0);
        hash = 19 * hash + (this.kind != null ? this.kind.hashCode() : 0);
        hash = 19 * hash + (this.outerName != null ? this.outerName.hashCode() : 0);
        hash = 19 * hash + (this.filePath != null ? this.filePath.hashCode() : 0);
        return hash;
    }  
}
