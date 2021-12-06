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

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.ui.wizard;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.versionvault.RepositoryFile;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 *
 *
 * @author Tomas Stupka
 */
public class RepositoryStep extends AbstractStep implements WizardDescriptor.AsynchronousValidatingPanel, PropertyChangeListener {

    public static final String IMPORT_HELP_ID = "org.netbeans.modules.clearcase.ui.wizard.RepositoryStep.import";
    public static final String CHECKOUT_HELP_ID = "org.netbeans.modules.clearcase.ui.wizard.RepositoryStep.checkout";
    public static final String URL_PATTERN_HELP_ID = "org.netbeans.modules.clearcase.ui.wizard.RepositoryStep.urlPattern";
    
    private Repository repository;        
    private RepositoryStepPanel panel;  
    private RepositoryPanel repositoryPanel;
    private RepositoryFile repositoryFile;    
    private int repositoryModeMask;

    private final String helpID;
    
    public RepositoryStep(String helpID) {
        this.repositoryModeMask = 0;
        this.helpID = helpID;
    }
    
    public RepositoryStep(int repositoryModeMask, String helpID) {
        this.repositoryModeMask = repositoryModeMask;
        this.helpID = helpID;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(helpID);
    }        

    protected JComponent createComponent() {
        if (repository == null) {         
            repositoryModeMask = repositoryModeMask | Repository.FLAG_URL_EDITABLE | Repository.FLAG_URL_ENABLED | Repository.FLAG_SHOW_HINTS | Repository.FLAG_SHOW_PROXY;
            String title = org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Location");       // NOI18N
            repository = new Repository(repositoryModeMask, title); 
            repository.addPropertyChangeListener(this);
            panel = new RepositoryStepPanel();           
            panel.repositoryPanel.setLayout(new BorderLayout());
            panel.repositoryPanel.add(repository.getPanel());
            //valid();
        }                       
        return panel;
    }
    
    protected void validateBeforeNext() {            
    }
    
    public void prepareValidation() {                
    }
    
    private void storeHistory() {         
    }
    
    public RepositoryFile getRepositoryFile() {
        return repository.getRepositoryFile();
    }                            

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Repository.PROP_VALID)) {
            if(repository.isValid()) {
                valid(repository.getMessage());
            } else {
                invalid(repository.getMessage());
            }
        }
    }
     public void stop() {
         //to be added 
    }
   
}

