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

package org.netbeans.modules.refactoring.java.spi.ui;

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
 * @author Jan Becicka
 */
public class JavaActionsImplementationProvider {

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
        throw new UnsupportedOperationException("Not implemented"); // NOI18N
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
        throw new UnsupportedOperationException("Not implemented"); // NOI18N
    }
    
    /**
     * @param lookup 
     * @return true if provider can handle find usages
     */
    public boolean canIntroduceParameter(Lookup lookup) {
        return false;
    }

    /**
     * implementation of "invoke Change Parameters"
     * @param lookup 
     */
    public void doIntroduceParameter(Lookup lookup) {
        throw new UnsupportedOperationException("Not implemented"); // NOI18N
    }
    
    /**
     * @param lookup 
     * @return true if provider can handle Pull Up
     */
    public boolean canPullUp(Lookup lookup) {
        return false;
    }
    
    /**
     * implementation of "invoke Pull Up"
     * @param lookup 
     */
    public void doPullUp(Lookup lookup) {
        throw new UnsupportedOperationException("Not implemented"); // NOI18N
    }

    /**
     * @param lookup 
     * @return true if provider can handle push down
     */
    public boolean canPushDown(Lookup lookup) {
        return false;
    }

    /**
     * implementation of "invoke Push Down"
     * @param lookup 
     */
    public void doPushDown(Lookup lookup) {
        throw new UnsupportedOperationException("Not implemented"); // NOI18N
    }
    
    /**
     * @param lookup 
     * @return true if provider can handle Inner to Outer
     */
    public boolean canInnerToOuter(Lookup lookup) {
        return false;
    }

    /**
     * implementation of "invoke Inner To Outer"
     * @param lookup 
     */
    public void doInnerToOuter(Lookup lookup) {
        throw new UnsupportedOperationException("Not implemented"); // NOI18N
    }    
    
    /**
     * @param lookup 
     * @return true if provider can handle Use Super Type
     */
    public boolean canUseSuperType(Lookup lookup) {
        return false;
    }

    /**
     * implementation of "invoke Use Super Type"
     * @param lookup 
     */
    public void doUseSuperType(Lookup lookup) {
        throw new UnsupportedOperationException("Not implemented"); // NOI18N
    }    
    
    /**
     * @param lookup 
     * @return true if provider can handle extract superclass
     */
    public boolean canExtractSuperclass(Lookup lookup) {
        return false;
    }

    /**
     * implementation of "invoke Extract Superclass"
     * @param lookup 
     */
    public void doExtractSuperclass(Lookup lookup) {
        throw new UnsupportedOperationException("Not implemented"); // NOI18N
    }    
    
    /**
     * @param lookup 
     * @return true if provider can handle extract Interface
     */
    public boolean canExtractInterface(Lookup lookup) {
        return false;
    }

    /**
     * implementation of "invoke Extract Interface"
     * @param lookup 
     */
    public void doExtractInterface(Lookup lookup) {
        throw new UnsupportedOperationException("Not implemented"); // NOI18N
    }    
    
    /**
     * @param lookup 
     * @return true if provider can handle inline
     */
    public boolean canInline(Lookup lookup) {
        return false;
    }

    /**
     * implementation of "Inline"
     * @param lookup 
     */
    public void doInline(Lookup lookup) {
        throw new UnsupportedOperationException("Not implemented"); // NOI18N
    }
    
    /**
     * @param lookup 
     * @return true if provider can handle Introduce local extension
     * @since 1.34
     */
    public boolean canIntroduceLocalExtension(Lookup lookup) {
        return false;
    }

    /**
     * implementation of "Introduce local extension"
     * @param lookup 
     * @since 1.34
     */
    public void doIntroduceLocalExtension(Lookup lookup) {
        throw new UnsupportedOperationException("Not implemented"); // NOI18N
    }
}
