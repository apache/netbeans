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

package org.netbeans.installer.wizard.components;

import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 * This class is a specialization of the {@link WizardComponent} which defines 
 * behavior specific to sequences.
 * 
 * <p>
 * A sequence is merely a list of {@link WizardComponent}s which executes when the 
 * sequence itself is executed. This makes wizard sequences the instrument to add
 * "depth" to the wizard, enable conditional execution of a set of components, etc.
 * It is also the only "standard" component which actually makes use of its child 
 * components.
 * 
 * <p>
 * {@link WizardSequence} uses child {@link Wizard} instances to execute its 
 * child components. The {@link Wizard} instances are created via the 
 * {@link Wizard#createSubWizard(List,int)} method.
 * 
 * <p>
 * A wizard sequence does not have a UI of its own - it completely relies on the UI
 * of its child components.
 * 
 * @author Kirill Sorokin
 * @since 1.0
 */
public class WizardSequence extends WizardComponent {
    /**
     * Current child {@link WIzard} instance which is used to iterate of over the 
     * list of child {@link WizardComponent}s.
     */
    protected Wizard childWizard;
    
    /**
     * Executes the sequence when it is reached via a call to {@link Wizard#next()}. 
     * This method simply creates a new child instance of {@link Wizard} over the 
     * list of child components, sets the index of the active component to the 
     * pre-first one and calls {@link Wizard#next()}.
     * 
     * @see WizardComponent#executeForward()
     */
    public void executeForward() {
        childWizard = getWizard().createSubWizard(
                getChildren(), -1);
        
        childWizard.next();
    }
    
    /**
     * Executes the sequence when it is reached via a call to 
     * {@link Wizard#previous()}. This method simply creates a new child instance 
     * of {@link Wizard} over the of child components, sets the index of the active 
     * component to the after-last one and calls {@link Wizard#previous()}.
     * 
     * @see WizardComponent#executeBackward()
     */
    public void executeBackward() {
        childWizard = getWizard().createSubWizard(
                getChildren(), getChildren().size());
        
        childWizard.previous();
    }
    
    /**
     * The default implementation of this method for {@link WizardSequence} has an 
     * empty body. Concrete implementations are expected to override this method
     * if they require any custom initialization.
     * 
     * @see WizardComponent#initialize()
     */
    public void initialize() {
        // does nothing
    }
    
    /**
     * Whether the sequence can be executed when reached via a call to 
     * {@link Wizard#next()}. Since the sequence does not contain any logic of its 
     * own, it searches through the list of child components checking whether there
     * is one that can be executed. If such a component is found, the sequence 
     * considers itself to be able to execute.
     * 
     * @return <code>true</code> is the sequence can be executed, <code>false</code>
     *      otherwise.
     */
    @Override
    public boolean canExecuteForward() {
        // whether a sequence can be executed completely depends on whether its 
        // children can be executed, thus we should run through them and check
        for (int i = 0; i < getChildren().size(); i++) {
            final WizardComponent component = getChildren().get(i);
            
            // if the component can be executed forward the whole sequence can be 
            // executed as well
            if (component.canExecuteForward()) {
                return true;
            }
        }
        
        // if none of the components can be executed, it does not make sense to
        // execute the sequence as well
        return false;
    }
    
    /**
     * Whether the sequence can be executed when reached via a call to 
     * {@link Wizard#previous()}. Since the sequence does not contain any logic of 
     * its own, it searches through the list of child components checking whether 
     * there is one that can be executed. If such a component is found, the sequence 
     * considers itself to be able to execute.
     * 
     * @return <code>true</code> is the sequence can be executed, <code>false</code>
     *      otherwise.
     */
    @Override
    public boolean canExecuteBackward() {
        // whether a sequence can be executed completely depends on whether its 
        // children can be executed, thus we should run through them and check
        for (int i = getChildren().size() - 1; i > -1; i--) {
            final WizardComponent component = getChildren().get(i);
            
            // if the component can be executed backward the whole sequence can be 
            // executed as well
            if (component.canExecuteBackward()) {
                return true;
            }
            
            // if the currently examined component is a point of no return and it
            // cannot be executed (since we passed the previous statement) - we have
            // no previous component
            if (component.isPointOfNoReturn()) {
                return false;
            }
        }
        
        // if none of the components can be executed it does not make sense to
        // execute the sequence as well
        return false;
    }
    
    /**
     * Whether the sequence is a point of no return. Since the sequence does not 
     * contain any logic of its own, it searches through the list of child 
     * components checking whether there is one that is a point of no return. If 
     * such a component is found and the index of the active component is greater 
     * than that of the found component, the sequence considers itself to be the 
     * point of no return.
     *
     * @return <code>true</code> is the sequence is the point of no return,
     *      <code>false</code> otherwise.
     */
    @Override
    public boolean isPointOfNoReturn() {
        // if there is a point-of-no-return child and it has already been passed,
        // then the sequence of a point of no return, otherwise it's not
        if (childWizard != null) {
            for (int i = 0; i < getChildren().size(); i++) {
                if (getChildren().get(i).isPointOfNoReturn() && 
                        (i < childWizard.getIndex())) {
                    return true;
                }
            }
        }
        
        // otherwise, it's not
        return false;
    }
    
    /**
     * Returns the {@link WizardUi} object for this component. Since the sequence 
     * does not have an UI of its own, this method always returns <code>null</code>.
     *
     * @return <code>null</code>.
     */
    public WizardUi getWizardUi() {
        // a sequence does not have a UI of its own, thus returning null
        return null;
    }
}
