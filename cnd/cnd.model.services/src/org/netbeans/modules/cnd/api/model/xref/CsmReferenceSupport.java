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

package org.netbeans.modules.cnd.api.model.xref;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.spi.utils.FileObjectRedirector;
import org.netbeans.modules.cnd.xref.impl.ReferenceSupportImpl;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * some help methods to support CsmReference objects
 */
public final class CsmReferenceSupport {
    
    private static final ReferenceSupportImpl impl = new ReferenceSupportImpl();
    private CsmReferenceSupport() {
        
    }
    
    public static CsmReference createObjectReference(CsmOffsetable obj) {
        return impl.createObjectReference(obj);
    }

    public static CsmReference createObjectReference(CsmObject target, CsmOffsetable owner) {
        return impl.createObjectReference(target, owner);
    }
    
    public static CharSequence getContextLineHtml(CsmReference ref, boolean refNameInBold) {
        CsmFile csmFile = ref.getContainingFile();
        int stToken = ref.getStartOffset();
        int endToken = ref.getEndOffset();
        CharSequence out = CsmDisplayUtilities.getContextLineHtml(csmFile, stToken, endToken, refNameInBold);
        if (out == null) {
            out = ref.getText();
        }
        return out;
    }

    public static CharSequence getContextLine(CsmReference ref) {
        CsmFile csmFile = ref.getContainingFile();
        int stToken = ref.getStartOffset();
        int endToken = ref.getEndOffset();
        CharSequence out = CsmDisplayUtilities.getContextLine(csmFile, stToken, endToken);
        if (out == null) {
            out = ref.getText();
        }
        return out;
    }

    public static boolean sameFile(CsmFile checkFile, CsmFile targetFile) {
        if (checkFile != null && targetFile != null && checkFile.getName().equals(targetFile.getName())) {
            if (checkFile.equals(targetFile)) {
                return true;
            }
            final CharSequence checkAbsolutePath = checkFile.getAbsolutePath();
            final CharSequence targetAbsolutePath = targetFile.getAbsolutePath();
            if (checkAbsolutePath.equals(targetAbsolutePath)) {
                FileObject checkDeclFO = checkFile.getFileObject();
                FileObject targetDeclFO = targetFile.getFileObject();
                if (checkDeclFO != null && targetDeclFO != null) {
                    return checkDeclFO.equals(targetDeclFO);
                }
                return true;
            } else {
                FileObject checkDeclFO = checkFile.getFileObject();
                FileObject targetDeclFO = targetFile.getFileObject();
                if (checkDeclFO != null && targetDeclFO != null) {
                    Collection<? extends FileObjectRedirector> redirectors = Lookup.getDefault().lookupAll(FileObjectRedirector.class);
                    for (FileObjectRedirector redirector : redirectors) {
                        FileObject fo1 = redirector.redirect(checkDeclFO);
                        FileObject fo2 = redirector.redirect(targetDeclFO);
                        if (fo1 != null && fo2 != null) {
                            return fo1.equals(fo2);
                        } else if (fo1 != null && fo2 == null) {
                            return fo1.equals(targetDeclFO);
                        } else if (fo1 == null && fo2 != null) {
                            return checkDeclFO.equals(fo2);
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean sameDeclaration(CsmObject checkDecl, CsmObject targetDecl) {
        // objects have to be the same or (if from different projects) should 
        // have same file:offset positions
        // special check is needed for functions which are same in case of 
        // same signatures
        if (checkDecl.equals(targetDecl)) {
            return true;
        } else if (isSameOffsetables(checkDecl, targetDecl)) {
            return true;
        } else if (CsmKindUtilities.isConstructor(checkDecl)) {
            return false;
        } else if (CsmKindUtilities.isFunction(checkDecl) && CsmKindUtilities.isFunction(targetDecl)) {
            CharSequence fqnCheck = ((CsmQualifiedNamedElement) checkDecl).getQualifiedName();
            CharSequence fqnTarget = ((CsmQualifiedNamedElement) targetDecl).getQualifiedName();
            if (fqnCheck.equals(fqnTarget)) {
                if (CsmBaseUtilities.sameSignature((CsmFunction)checkDecl, (CsmFunction)targetDecl)) {
                    return true;
                }
            }            
        } else if (CsmKindUtilities.isVariable(checkDecl) && CsmKindUtilities.isVariable(targetDecl)) {
            CharSequence checkName = ((CsmNamedElement)checkDecl).getName();
            CharSequence targetName = ((CsmNamedElement)targetDecl).getName();
            if (checkName.equals(targetName) &&
                (CsmKindUtilities.isGlobalVariable(checkDecl) && CsmKindUtilities.isGlobalVariable(targetDecl))) {
                CharSequence fqnCheck = ((CsmQualifiedNamedElement) checkDecl).getQualifiedName();
                CharSequence fqnTarget = ((CsmQualifiedNamedElement) targetDecl).getQualifiedName();
                if (fqnCheck.equals(fqnTarget)) {
                    // check same project or dependent project relations
                    return belongsToRelatedProjects(((CsmVariable)checkDecl).getContainingFile(), ((CsmVariable)targetDecl).getContainingFile());
                }
            }
        }
        return false;
    }
    
    private static boolean belongsToRelatedProjects(CsmFile checkFile, CsmFile targetFile) {
        if (checkFile == null || targetFile == null) {
            return false;
        }
        CsmProject checkPrj = checkFile.getProject();
        CsmProject targetPrj = targetFile.getProject();
        if (checkPrj == null || targetPrj == null) {
            return false;
        }
        if (checkPrj.equals(targetPrj)) {
            return true;
        }
        if (checkPrj.getLibraries().contains(targetPrj)) {
            return true;
        }
        if (targetPrj.getLibraries().contains(checkPrj)) {
            return true;
        }
        return false;
    }
    
    private static boolean isSameOffsetables(CsmObject checkDecl, CsmObject targetDecl) {
        if (CsmKindUtilities.isOffsetable(checkDecl) && CsmKindUtilities.isOffsetable(targetDecl)) {
            CsmOffsetable offsCheckDecl = (CsmOffsetable) checkDecl;
            CsmOffsetable offsTargetDecl = (CsmOffsetable) targetDecl;
            if (offsCheckDecl.getStartOffset() == offsTargetDecl.getStartOffset()) {
                CsmFile checkDeclFile = offsCheckDecl.getContainingFile();
                CsmFile targetDeclFile = offsTargetDecl.getContainingFile();
                return sameFile(checkDeclFile, targetDeclFile);
            }
        }
        return false;
    }    
}
