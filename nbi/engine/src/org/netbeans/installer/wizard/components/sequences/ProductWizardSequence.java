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
