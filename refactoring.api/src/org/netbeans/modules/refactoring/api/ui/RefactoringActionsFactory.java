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
