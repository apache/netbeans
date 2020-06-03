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

package org.netbeans.modules.cnd.refactoring.spi;

import org.openide.util.Lookup;

/**
 * Create your own provider of this class and register it in META-INF services, if you want to
 * create your own implementations of refactorin actions.
 * For instance Java module wants to have refactoring rename action for java files.
 * So Java Refactoring module must implement 2 methods. 
 *
 * <pre>
 * public boolean canChangeParameters(Lookup lookup) {
 *   Node[] nodes = lookup.lookupAll(Node.class);
 *   if (..one node selected and the node belongs to java...)
 *      return true;
 *   else 
 *      return false;
 * }
 *
 * public void doChangeParameters(Lookup lookup) {
 *   Node[] nodes = lookup.lookupAll(Node.class);
 *   final FileObject fo = getFileFromNode(nodes[0]);
 *   UI.openRefactoringUI(new ChangeParametersUI(fo);
 * }
 * </pre>     
 *
 * For help on creating and registering actions
 * See <a href=http://wiki.netbeans.org/wiki/view/RefactoringFAQ>Refactoring FAQ</a>
 * 
 */
public class CsmActionsImplementationProvider {

    /**
     * @param lookup 
     * @return true if provider can handle rename
     */
    public boolean canEncapsulateFields(Lookup lookup) {
        return false;
    }

    /**
     * @param lookup 
     */
    public void doEncapsulateFields(Lookup lookup) {
        
    }

    /**
     * @param lookup 
     * @return true if provider can handle find usages
     */
    public boolean canChangeParameters(Lookup lookup) {
        return false;
    }

    /**
     * implementation of "invoke Change Parameters"
     * @param lookup 
     */
    public void doChangeParameters(Lookup lookup) {
        
    }
    
    /**
     * @param lookup 
     * @return true if provider can handle find usages
     */
    public boolean canPerformInlineRefactoring(Lookup lookup) {
        return false;
    }

    /**
     * implementation of "invoke Inline Refactoring"
     * @param lookup 
     */
    public void doInlineRefactoring(Lookup lookup) {
        
    }
}
