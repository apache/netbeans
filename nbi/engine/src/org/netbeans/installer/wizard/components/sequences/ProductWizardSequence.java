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

package org.netbeans.installer.wizard.components.sequences;

import java.util.List;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.WizardSequence;


public class ProductWizardSequence extends WizardSequence {
    private Product product;
    
    public ProductWizardSequence(final Product product) {
        this.product = product;
    }
    
    public void executeForward() {
        childWizard = getWizard().createSubWizard(
                product.getWizardComponents(), 
                -1, 
                product, 
                product.getClassLoader());
        
        childWizard.getContext().put(product);
        childWizard.next();
    }
    
    public void executeBackward() {
        childWizard = getWizard().createSubWizard(
                product.getWizardComponents(), 
                product.getWizardComponents().size(), 
                product, 
                product.getClassLoader());
        
        childWizard.getContext().put(product);
        childWizard.previous();
    }
    
    public boolean canExecuteForward() {
        if (product.isLogicDownloaded()) {
            List<WizardComponent> components = product.getWizardComponents();
            
            for (int i = 0; i < components.size(); i++) {
                WizardComponent component = components.get(i);
                
                // if the component can be executed forward the whole sequence 
                // can be executed as well
                if (component.canExecuteForward()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean canExecuteBackward() {
        if (product.isLogicDownloaded()) {
            List<WizardComponent> components = product.getWizardComponents();
            
            for (int i = components.size() - 1; i > -1; i--) {
                WizardComponent component = components.get(i);
                
                // if the component can be executed backward the whole sequence can 
                // be executed as well
                if (component.canExecuteBackward()) {
                    return true;
                }
                
                // if the currently examined component is a point of no return and 
                // it cannot be executed (since we passed the previous statement) - 
                // we have no previous component
                if (component.isPointOfNoReturn()) {
                    return false;
                }
            }
        }
        
        return false;
    }
    
    public boolean isPointOfNoReturn() {
        if (product.isLogicDownloaded()) {
            List<WizardComponent> components = product.getWizardComponents();
            
            if (childWizard != null) {
                for (int i = 0; i < components.size(); i++) {
                    if (components.get(i).isPointOfNoReturn() && (i < childWizard.getIndex())) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
}
