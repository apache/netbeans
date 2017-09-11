/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
