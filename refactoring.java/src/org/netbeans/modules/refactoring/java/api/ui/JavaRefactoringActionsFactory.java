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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.refactoring.java.api.ui;

import org.netbeans.modules.refactoring.java.ui.*;
import org.openide.util.ContextAwareAction;

/**
 * Factory class providing instances of refactoring actions.
 * <p><b>Usage:</b></p>
 * <pre>
 * InstanceContent ic = new InstanceContent();
 * ic.add(node);
 * Lookup l = new AbstractLookup(ic);
 * Action a = RefactoringActionsFactory.encapsulateFieldsAction().createContextAwareInstance(l);
 * a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
 * </pre>
 *
 * For help on creating and registering actions
 * See <a href=http://wiki.netbeans.org/wiki/view/RefactoringFAQ>Refactoring FAQ</a>
 * 
 * @author Jan Becicka
 */
public final class JavaRefactoringActionsFactory {
    
    private JavaRefactoringActionsFactory(){}
    
   /**
    * Factory method for InlineAction
    * @return an instance of InlineAction
    */
   public static ContextAwareAction inlineAction() {
       return InlineAction.findObject(InlineAction.class, true);
   }
    
   /**
     * Factory method for EncapsulateFieldsAction
     * @return an instance of EncapsulateFieldsAction
     */
    public static ContextAwareAction encapsulateFieldsAction() {
        return EncapsulateFieldAction.findObject(EncapsulateFieldAction.class, true);
    }
    
    /**
     * Factory method for ChangeParametersAction
     * @return an instance of ChangeParametersAction
     */
    public static ContextAwareAction changeParametersAction() {
        return ChangeParametersAction.findObject(ChangeParametersAction.class, true);
    }
    
    
    /**
     * Factory method for PullUpAction
     * @return an instance of PullUpAction
     */
    public static ContextAwareAction pullUpAction() {
        return PullUpAction.findObject(PullUpAction.class, true);
    }
    
    /**
     * Factory method for PushDownAction
     * @return an instance of PushDownAction
     */
    public static ContextAwareAction pushDownAction() {
        return PushDownAction.findObject(PushDownAction.class, true);
    }
    
    /**
     * Factory method for InnerToOuterAction
     * @return an instance of InnerToOuterAction
     */
    public static ContextAwareAction innerToOuterAction() {
        return InnerToOuterAction.findObject(InnerToOuterAction.class, true);
    }

    /**
     * Factory method for UseSuperTypeAction
     * @return an instance of UseSuperTypeAction
     */
    public static ContextAwareAction useSuperTypeAction() {
        return UseSuperTypeAction.findObject(UseSuperTypeAction.class, true);
    }
    
    /**
     * Factory method for ExtractSuperclassAction
     * @return an instance of ExtractSuperclassAction
     */
    public static ContextAwareAction extractSuperclassAction() {
        return ExtractSuperclassAction.findObject(ExtractSuperclassAction.class, true);
    }
    
    /**
     * Factory method for ExtractInterfaceAction
     * @return an instance of ExtractInterfaceAction
     */
    public static ContextAwareAction extractInterfaceAction() {
        return ExtractInterfaceAction.findObject(ExtractInterfaceAction.class, true);
    }
    
    /**
     * Factory method for IntroduceParameterAction
     * @return an instance of IntroduceParameterAction
     */
    public static ContextAwareAction introduceParameterAction() {
        return IntroduceParameterAction.findObject(IntroduceParameterAction.class, true);
    }
    
    public static ContextAwareAction introduceLocalExtensionAction() {
        return IntroduceLocalExtensionAction.findObject(IntroduceLocalExtensionAction.class, true);
    }
}
