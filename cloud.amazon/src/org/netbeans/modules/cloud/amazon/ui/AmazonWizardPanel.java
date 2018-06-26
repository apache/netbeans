/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cloud.amazon.ui;

import com.amazonaws.AmazonClientException;
import java.awt.Component;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cloud.amazon.AmazonInstance;
import org.netbeans.modules.cloud.amazon.serverplugin.AmazonJ2EEInstance;
import org.netbeans.modules.cloud.common.spi.support.ui.CloudResourcesWizardPanel;
import org.netbeans.modules.cloud.common.spi.support.ui.ServerResourceDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 */
public class AmazonWizardPanel implements WizardDescriptor.AsynchronousValidatingPanel<WizardDescriptor> {

    public static final String KEY_ID = "access-key-id"; // String
    public static final String KEY = "secret-access-key"; // String
    public static final String REGION = "region"; // String
    
    private ChangeSupport listeners;
    private AmazonWizardComponent component;
    private List<ServerResourceDescriptor> servers;
    private WizardDescriptor wd = null;
    
    public AmazonWizardPanel() {
        listeners = new ChangeSupport(this);
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new AmazonWizardComponent(this, null);
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, getPanelContentData());            
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(0));
        }
        return component;
    }

    static String[] getPanelContentData() {
        return new String[] {
                NbBundle.getMessage(AmazonWizardPanel.class, "LBL_ACIW_Amazon"),
                NbBundle.getMessage(AmazonWizardPanel.class, "LBL_ACIW_Resources")
            };
    }
    
    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        wd = settings;
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        if (component != null) {
            settings.putProperty(KEY_ID, component.getKeyId());
            settings.putProperty(KEY, component.getKey());
            settings.putProperty(CloudResourcesWizardPanel.PROP_SERVER_RESOURCES, servers);
            settings.putProperty(REGION, component.getRegionUrl());
        }
    }
    
    public void setErrorMessage(String message) {
        wd.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }

    @Override
    public boolean isValid() {
        if (component == null || wd == null) {
            // ignore this case
        } else if (component.getKeyId().trim().length() == 0) {
            setErrorMessage(NbBundle.getMessage(AmazonWizardPanel.class, "AmazonWizardPanel.missingKeyID"));
            return false;
        } else if (component.getKey().trim().length() == 0) {
            setErrorMessage(NbBundle.getMessage(AmazonWizardPanel.class, "AmazonWizardPanel.missingKey"));
            return false;
        }
        setErrorMessage("");
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.removeChangeListener(l);
    }
    
    void fireChange() {
        listeners.fireChange();
    }

    @Override
    public void prepareValidation() {
        getComponent().setCursor(Utilities.createProgressCursor(getComponent()));
    }

    @Override
    public void validate() throws WizardValidationException {
        try {
            servers = new ArrayList<ServerResourceDescriptor>();
            AmazonInstance ai = new AmazonInstance("temporary", component.getKeyId(), component.getKey(), component.getRegionUrl());
            try {
                ai.testConnection();
            } catch (AmazonClientException ex) {
                throw new WizardValidationException((JComponent)getComponent(), 
                        "connection failed", NbBundle.getMessage(AmazonWizardPanel.class, "AmazonWizardPanel.wrong.credentials"));
            }
            List<AmazonJ2EEInstance> list = ai.readJ2EEServerInstances();
            for (AmazonJ2EEInstance inst : list) {
                AmazonJ2EEInstanceNode n = new AmazonJ2EEInstanceNode(inst);
                n.showServerType();
                servers.add(new ServerResourceDescriptor("Server", n.getDisplayName(), "", ImageUtilities.image2Icon(n.getIcon(BeanInfo.ICON_COLOR_16x16))));
            }
        } finally {
            getComponent().setCursor(null);
        }
    }
    
}
