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

package org.netbeans.modules.refactoring.spi.ui;

import org.openide.util.Lookup;
/**
 * Create your own provider of this class and register it in META-INF services, if you want to
 * create your own implementations of refactorin actions.
 * For instance Java module wants to have refactoring rename action for java files.
 * So Java Refactoring module must implement 2 methods. 
 *
 * <pre>
 * public boolean canRename(Lookup lookup) {
 *   Node[] nodes = lookup.lookupAll(Node.class);
 *   if (..one node selected and the node belongs to java...)
 *      return true;
 *   else 
 *      return false;
 * }
 *
 * public void doRename(Lookup selectedNodes) {
 *   Node[] nodes = lookup.lookupAll(Node.class);
 *   final FileObject fo = getFileFromNode(nodes[0]);
 *   UI.openRefactoringUI(new RenameRefactoringUI(fo);
 * }
 * </pre>     
 *
 * For help on creating and registering actions
 * See <a href=http://wiki.netbeans.org/wiki/view/RefactoringFAQ>Refactoring FAQ</a>
 * 
 * @author Jan Becicka
 */
public abstract class ActionsImplementationProvider {
    
    /**
     * @param lookup current context
     * @return true if provider can handle rename
     */
    public boolean canRename(Lookup lookup) {
        return false;
    }

    /**
     * @param lookup current context
     */
    public void doRename(Lookup lookup) {
        throw new UnsupportedOperationException("Not implemented!"); // NOI18N
    }

    /**
     * @param lookup current context
     * @return true if provider can handle find usages
     */
    public boolean canFindUsages(Lookup lookup) {
        return false;
    }

    /**
     * @param lookup current context
     */
    public void doFindUsages(Lookup lookup) {
        throw new UnsupportedOperationException("Not implemented!"); // NOI18N
    }

    /**
     * @param lookup current context
     * @return true if provider can handle delete
     */
    public boolean canDelete(Lookup lookup) {
        return false;
    }
    
    /**
     * @param lookup current context
     */
    public void doDelete(Lookup lookup) {
        throw new UnsupportedOperationException("Not implemented!"); // NOI18N
    }

    /**
     * @param lookup current context
     * @return true if provider can handle move
     */
    public boolean canMove(Lookup lookup) {
        return false;
    }

    /**
     * @param lookup current context
     */
    public void doMove(Lookup lookup) {
        throw new UnsupportedOperationException("Not implemented!"); // NOI18N
    }

    /**
     * @param lookup current context
     * @return true if provider can handle copy
     */
    public boolean canCopy(Lookup lookup) {
        return false;
    }

    /**
     * @param lookup current context
     */
    public void doCopy(Lookup lookup) {
        throw new UnsupportedOperationException("Not implemented!"); // NOI18N
    }
}
