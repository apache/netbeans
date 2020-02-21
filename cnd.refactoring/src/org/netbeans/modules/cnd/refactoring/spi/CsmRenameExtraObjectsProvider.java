/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.refactoring.spi;

import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;

/**
 * provider which can contribute extra objects for refactoring.
 * For instance, original object is class, but provider can add other classes and files as well.
 * Adding class is enough to force rename of constructors/destructors of that class as well by
 * infrastructure itself, so no need to return them in collection, class is enough.
 * The same for files, it's enough to return file and all corresponding includes will be handled by
 * infrastructure.
 */
public interface CsmRenameExtraObjectsProvider {
    /**
     * allow to force refactoring when rename file
     * @param file
     * @return 
     */
    boolean needsRefactorRename(CsmFile file);
    
    /**
     * return extra objects which will be refactored as well
     * @param orig original object
     * @return collection of extra objects
     */
    Collection<CsmObject> getExtraObjects(CsmObject orig);
    
    /**
     * return extra references if target object is referenced in file.
     * For instance object reference can be embedded in comment or string
     * @param targetObjectSearchedInFile target objects collection
     * @param csmFile current file
     * @param kinds kinds of references which are collected
     * @return extra references to target object in input file
     */
    Collection<CsmReference> getExtraFileReferences(Collection<? extends CsmObject> targetObjectSearchedInFile, CsmFile csmFile, Set<CsmReferenceKind> kinds);
}
