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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.refactoring;

import java.util.Collection;
import org.netbeans.modules.groovy.refactoring.RefactoringTaskFactory.RefactoringType;
import org.netbeans.modules.groovy.refactoring.utils.GroovyProjectUtil;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Martin Janicek
 */
@ServiceProvider(service = ActionsImplementationProvider.class, position=100)
public class RefactoringActionsProvider extends ActionsImplementationProvider {

    @Override
    public boolean canFindUsages(Lookup lookup) {
        return canBeDone(lookup, RefactoringType.FIND_USAGES);
    }

    @Override
    public boolean canRename(Lookup lookup) {
        return canBeDone(lookup, RefactoringType.RENAME);
    }

    @Override
    public boolean canMove(Lookup lookup) {
        return false;
        //return canBeDone(lookup, RefactoringType.MOVE);
    }

    @Override
    public void doFindUsages(Lookup lookup) {
        createTask(lookup, RefactoringType.FIND_USAGES).run();
    }

    @Override
    public void doRename(Lookup lookup) {
        createTask(lookup, RefactoringType.RENAME).run();
    }

    @Override
    public void doMove(Lookup lookup) {
        createTask(lookup, RefactoringType.MOVE).run();
    }

    private boolean canBeDone(Lookup lookup, RefactoringType type) {
        RefactoringTask task = createTask(lookup, type);
        return isValid(lookup) && task != null && task.isValid();
    }

    private RefactoringTask createTask(Lookup lookup, RefactoringType type) {
        return RefactoringTaskFactory.createRefactoringTask(lookup, type);
    }

    private boolean isValid(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if (nodes.size() != 1) {
            return false;
        }

        Node node = nodes.iterator().next();
        DataObject dataObject = node.getLookup().lookup(DataObject.class);
        if (dataObject == null) {
            return false;
        }

        if (GroovyProjectUtil.isGroovyFile(dataObject.getPrimaryFile())) {
            return true;
        }
        return false;
    }
}
