/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
