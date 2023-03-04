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
