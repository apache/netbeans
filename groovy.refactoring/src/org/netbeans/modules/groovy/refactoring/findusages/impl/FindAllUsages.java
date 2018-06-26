/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.refactoring.findusages.impl;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;

/**
 * This strategy is used in refactoring other then Find Usages. 
 *
 * {@link FindTypeUsages} doesn't find all usages in all files (e.g. if we are 
 * finding usages for a specific class type, we don't want to see constructor in
 * the result, but on the other hand these are pretty important for rename refactoring).
 *
 * Because of that we need to have two different implementation. One for FindUsages
 * itself and the second one as a base used by other refactoring types.
 * 
 * @author Martin Janicek
 */
public class FindAllUsages extends AbstractFindUsages {

    public FindAllUsages(RefactoringElement element) {
        super(element);
    }


    @Override
    protected List<AbstractFindUsagesVisitor> getVisitors(ModuleNode moduleNode, String defClass) {
        List<AbstractFindUsagesVisitor> visitors = new ArrayList<AbstractFindUsagesVisitor>();

        visitors.add(new FindTypeUsagesVisitor(moduleNode, defClass));
        visitors.add(new FindConstructorUsagesVisitor(moduleNode, element));
        visitors.add(new FindClassDeclarationVisitor(moduleNode, defClass));

        return visitors;
    }
}
