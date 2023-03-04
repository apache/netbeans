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

package org.netbeans.modules.refactoring.api.ui;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.netbeans.modules.refactoring.spi.impl.*;
import org.openide.util.ContextAwareAction;

/**
 * Factory class providing instances of refactoring actions.
 * <p><b>Usage:</b></p>
 * <pre>
 * Lookup l = Lookups.singleton(node);
 * Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(l);
 * a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
 * </pre>
 *
 * For help on creating and registering actions
 * See <a href=http://wiki.netbeans.org/wiki/view/RefactoringFAQ>Refactoring FAQ</a>
 *
 * @author Jan Becicka
 */
public final class RefactoringActionsFactory {
    
    /**
     * defualt event for actionPerformed
     */
    public static final ActionEvent DEFAULT_EVENT = new ActionEvent(new Object(), 0, null) {
        @Override
        public void setSource(Object newSource) {        
            throw new UnsupportedOperationException();
        }
    };
    
    private RefactoringActionsFactory(){}
    

    /**
     * Factory method for rename action
     * @return instance of RenameAction
     */
    public static ContextAwareAction renameAction() {
        return RenameAction.findObject(RenameAction.class, true);
    }

    /**
     * Factory method for MoveAction
     * @return an instance of MoveAction
     */
    public static ContextAwareAction moveAction() {
        return MoveAction.findObject(MoveAction.class, true);
    }
    
    /**
     * Factory method for SafeDeleteAction
     * @return an instance of SafeDeleteAction
     */
    public static ContextAwareAction safeDeleteAction() {
        return SafeDeleteAction.findObject(SafeDeleteAction.class, true);
    }
    
    /**
     * Factory method for CopyAction
     * @return an instance of CopyAction
     */
    public static ContextAwareAction copyAction() {
        return SafeDeleteAction.findObject(CopyAction.class, true);
    }
    
    /**
     * Factory method for WhereUsedAction
     * @return an instance of WhereUsedAction
     */
    public static ContextAwareAction whereUsedAction() {
        return WhereUsedAction.findObject(WhereUsedAction.class, true);
    }
    
    /**
     * Factory method for RSMEditorAction
     * @return an instance of RSMEditorAction
     */
    public static Action editorSubmenuAction() {
        return RSMEditorAction.findObject(RSMEditorAction.class, true);
    }
    
    /**
     * Factory method for RSMDataObjectAction
     * @return an instance of RSMDataObjectAction
     */
    public static ContextAwareAction popupSubmenuAction() {
        return RSMDataObjectAction.findObject(RSMDataObjectAction.class, true);
    }
}
