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

package org.netbeans.modules.j2ee.persistence.wizard.unit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class PersistenceUnitWizardDescriptor implements WizardDescriptor.FinishablePanel, ChangeListener {
    
    private PersistenceUnitWizardPanelDS datasourcePanel;
    private PersistenceUnitWizardPanelJdbc jdbcPanel;
    private PersistenceUnitWizardPanel panel;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private WizardDescriptor wizardDescriptor;
    private Project project;
    private boolean isContainerManaged;
    private static String ERROR_MSG_KEY = WizardDescriptor.PROP_ERROR_MESSAGE;
    
    public PersistenceUnitWizardDescriptor(Project project) {
        this.project = project;
        this.isContainerManaged = Util.isContainerManaged(project);
    }
    
    @Override
    public void addChangeListener(javax.swing.event.ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    @Override
    public java.awt.Component getComponent() {
        if (panel == null) {
            if (isContainerManaged) {
                datasourcePanel = new PersistenceUnitWizardPanelDS(project, this, true);
                panel = datasourcePanel;
            } else {
                jdbcPanel= new PersistenceUnitWizardPanelJdbc(project, this, true);
                panel = jdbcPanel;
            }
            panel.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(PersistenceUnitWizardPanel.IS_VALID)) {
                        Object newvalue = evt.getNewValue();
                        if ((newvalue != null) && (newvalue instanceof Boolean)) {
                            stateChanged(null);
                        }
                    }
                }
            });
        }
        return panel;
    }
    
    @Override
    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx(PersistenceUnitWizardDescriptor.class);
    }
    
    @Override
    public boolean isValid() {
        if (wizardDescriptor == null) {
            return true;
        }
        if (!ProviderUtil.isValidServerInstanceOrNone(project)){
            wizardDescriptor.putProperty(ERROR_MSG_KEY,
                    NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_MissingServer")); //NOI18N
            return false;
        }
        if (panel != null && !panel.isValidPanel()) {
            try {
                if (!panel.isNameUnique()){
                    wizardDescriptor.putProperty(ERROR_MSG_KEY,
                            NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_PersistenceUnitNameNotUnique")); //NOI18N
                }
            } catch (InvalidPersistenceXmlException ipx){
                    wizardDescriptor.putProperty(ERROR_MSG_KEY,
                            NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_InvalidPersistenceXml", ipx.getPath())); //NOI18N
                
            }
            return false;
        }
        wizardDescriptor.putProperty(ERROR_MSG_KEY, " "); //NOI18N
        return true;
    }
    
    
    @Override
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        project = Templates.getProject(wizardDescriptor);
    }
    
    @Override
    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    @Override
    public void storeSettings(Object settings) {
    }
    
    @Override
    public boolean isFinishPanel() {
        return isValid();
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }
    
    public String getPersistenceUnitName() {
        return panel.getPersistenceUnitName();
    }
    
    public DatabaseConnection getPersistenceConnection() {
        return jdbcPanel == null ? null : jdbcPanel.getPersistenceConnection();
    }
    
    public String getDatasource() {
        return datasourcePanel == null ? null : datasourcePanel.getDatasource();
    }

    public String getDBResourceSelection() {
        return getPersistenceConnection()!=null ? getPersistenceConnection().getName() : getDatasource();
    }
    
    public boolean isContainerManaged() {
        return isContainerManaged;
    }
    
    public boolean isJTA() {
        return datasourcePanel == null ? false : datasourcePanel.isJTA();
    }
    
    boolean isNonDefaultProviderEnabled() {
        return datasourcePanel == null ? false : datasourcePanel.isNonDefaultProviderEnabled();
    }
    
    public String getNonDefaultProvider() {
        return datasourcePanel == null ? null : datasourcePanel.getNonDefaultProvider();
    }
    
    public String getTableGeneration() {
        return panel.getTableGeneration();
    }
    
    public Provider getSelectedProvider(){
        return panel.getSelectedProvider();
    }
}
