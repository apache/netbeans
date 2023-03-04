/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
