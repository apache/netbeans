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

package org.netbeans.installer.wizard.ui;

import javax.swing.JComponent;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiPanel;

/**
 * This class represents the UI of a {@link WizardComponent} for the 
 * {@link UiMode#SWING} UI mode.
 * 
 * @author Kirill Sorokin
 * @since 1.0
 */
public abstract class SwingUi extends NbiPanel {
    /**
     * Returns the title of the component. The way the title is displayed is
     * dependent on the container. A frame could expose the title in the windows 
     * heading, for example.
     * 
     * @return Title of the component, or <code>null</code> if the component does 
     *      not have a title.
     */
    public abstract String getTitle();
    
    /**
     * Returns the description of the component. The way the description is 
     * displayed is dependent on the container.
     * 
     * @return Description of the component, or <code>null</code> if the component 
     *      does not have a description.
     */
    public abstract String getDescription();
    
    /**
     * Hook, allowing the component's UI to execute some custom logic when the user 
     * activates the standard <code>Help</code> button. The expected behavior would 
     * be to display a help dialog which describes the required user input for the 
     * current component.
     */
    public abstract void evaluateHelpButtonClick();
    
    /**
     * Hook, allowing the component's UI to execute some custom logic when the user 
     * activates the standard <code>Back</code> button. The expected behavior would 
     * be to call the {@link Wizard#previous()} method.
     */
    public abstract void evaluateBackButtonClick();
    
    /**
     * Hook, allowing the component's UI to execute some custom logic when the user 
     * activates the standard <code>Next</code> button. The expected behavior would 
     * be to call the {@link Wizard#next()} method.
     */
    public abstract void evaluateNextButtonClick();
    
    /**
     * Hook, allowing the component's UI to execute some custom logic when the user 
     * activates the standard <code>Cancel</code> button. The expected behavior 
     * would be to cancel the wizard execution.
     */
    public abstract void evaluateCancelButtonClick();
    
    /**
     * Returns the button which should be activated when the user presses the 
     * <code>Enter</code> key.
     * 
     * @return Default handler for the <code>Enter</code> key.
     */
    public abstract NbiButton getDefaultEnterButton();
    
    /**
     * Returns the button which should be activated when the user presses the 
     * <code>Escape</code> key.
     * 
     * @return Default handler for the <code>Escape</code> key.
     */
    public abstract NbiButton getDefaultEscapeButton();
    
    /**
     * Returns the Swing component which should have focus when this UI is shown.
     * 
     * @return Default focus owner for this UI.
     */
    public abstract JComponent getDefaultFocusOwner();
}
