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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.refactoring.ui.tree;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.support.ElementGrip;
import org.netbeans.modules.cnd.refactoring.support.ElementGripFactory;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * factory of tree elements for C/C++ refactorings
 * 
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation.class, position=50)
public class TreeElementFactoryImpl implements TreeElementFactoryImplementation {

    public Map<Object, TreeElement> map = new WeakHashMap<>();
    
    @Override
    public TreeElement getTreeElement(Object o) {
        TreeElement result = map.get(o);
        if (result!= null) {
            return result;
        }
        if (o instanceof RefactoringElement) {
            Lookup lkp = ((RefactoringElement) o).getLookup();
            CsmOffsetable csmObject = lkp.lookup(CsmOffsetable.class);
            if (csmObject != null) {
                result = new RefactoringTreeElement((RefactoringElement) o);
            } else {
                CsmObject obj = ((RefactoringElement) o).getLookup().lookup(CsmObject.class);
                if (obj != null) {
                    System.err.println("unhandled CsmObject: " + obj);
                }
            }
        } else if (o instanceof ElementGrip) {
            result = new ElementGripTreeElement((ElementGrip)o);
        } else if (CsmKindUtilities.isProject(o)) {
            result = new ProjectTreeElement((CsmProject)o);
        } else if (CsmKindUtilities.isCsmObject(o)) {
            CsmObject csm = (CsmObject)o;
            if (CsmKindUtilities.isFile(csm)) {
                FileObject fo = CsmUtilities.getFileObject((CsmFile)o);
                result = new FileTreeElement(fo, (CsmFile)o);
            } else {
                result = new ParentTreeElement(csm);
            }
        } else if (o instanceof FileObject) {
            FileObject fo = (FileObject)o;
            CsmFile csmFile = CsmUtilities.getCsmFile(fo, false, false);
            // our factory is asked about any FileObject, so check for CND ones only
            if (csmFile != null) {
                result = new FileTreeElement(fo, csmFile);
            }
        }
        if (result != null) {
            map.put(o, result);
        }
        return result;
    }

    @Override
    public void cleanUp() {
        map.clear();
        ElementGripFactory.getDefault().cleanUp();
    }
}
