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

package org.netbeans.modules.ant.freeform.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.support.AntScriptUtils;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.modules.ant.freeform.spi.TargetDescriptor;
import org.netbeans.modules.ant.freeform.spi.support.NewFreeformProjectSupport;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny
 */
public class TargetMappingWizardPanel implements WizardDescriptor.Panel {

    public static final String PROP_TARGET_MAPPINGS = "targetMappings"; // <List> NOI18N
    
    private TargetMappingPanel component;
    private WizardDescriptor wizardDescriptor;
    private List<TargetDescriptor> targets;
    private List<String> targetNames;
    
    public TargetMappingWizardPanel(List<TargetDescriptor> targets) {
        this.targets = targets;
        getComponent().setName(NbBundle.getMessage(TargetMappingWizardPanel.class, "WizardPanel_BuildAndRunActions"));
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new TargetMappingPanel(targets, false);
            component.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TargetMappingWizardPanel.class, "ACSD_TargetMappingWizardPanel"));
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx( TargetMappingWizardPanel.class );
    }
    
    public boolean isValid() {
        getComponent();
        return true;
    }
    
    private final ChangeSupport cs = new ChangeSupport(this);
    public final void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    public final void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    protected final void fireChangeEvent() {
        cs.fireChange();
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        wizardDescriptor.putProperty("NewProjectWizard_Title", component.getClientProperty("NewProjectWizard_Title")); // NOI18N
        File f = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_ANT_SCRIPT);
        FileObject fo = FileUtil.toFileObject(f);
        // Util.getAntScriptTargetNames can return null when script is 
        // invalid but first panel checks script validity so it is OK here.
        List<String> l = null;
        try {
            l = AntScriptUtils.getCallableTargetNames(fo);
        } catch (IOException x) {/* ignore */}
        // #47784 - update panel only once or when Ant script has changed
        if (targetNames == null || !targetNames.equals(l)) {
            targetNames = new ArrayList<String>(l);
            component.setTargetNames(l, true);
        }
        File projDir = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_PROJECT_FOLDER);
        File antScript = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_ANT_SCRIPT);
        if (!(antScript.getParentFile().equals(projDir) && antScript.getName().equals("build.xml"))) { // NOI18N
            // NON-DEFAULT location of build file
            component.setScript("${"+ProjectConstants.PROP_ANT_SCRIPT+"}"); // NOI18N
        } else {
            component.setScript(null);
        }
    }
    
    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        wizardDescriptor.putProperty(PROP_TARGET_MAPPINGS, component.getMapping());
        wizardDescriptor.putProperty("NewProjectWizard_Title", null); // NOI18N
    }
    
}
