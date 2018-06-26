/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;
import java.io.InputStream;
import java.util.Collection;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

/**
 *
 * @author Petr Pisl
 */
public class TemplateClientPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {
    
    private WizardDescriptor wizardDescriptor;
    private TemplateClientPanelVisual component;
    
    /** Creates a new instance of TemplateClientPanel */
    public TemplateClientPanel(WizardDescriptor wizardDescriptor) {
        component = null;
        this.wizardDescriptor = wizardDescriptor;
    }

    @Override
    public Component getComponent() {
        if (component == null){
            component = new TemplateClientPanelVisual(wizardDescriptor);
        }
        
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.jsf.wizards.TemplateClientPanel");
    }

    @Override
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
    }

    @Override
    public void storeSettings(Object settings) {
    }

    @Override
    public boolean isValid() {
        return component.validateTemplate();
    }

    public InputStream getTemplateClient(){
        getComponent();
        return component.getTemplateClient();    
    }
    
    public Collection<String> getTemplateData(){
        getComponent();
        return component.getTemplateData();
    }

    public Collection<String> getTemplateDataToGenerate(){
        getComponent();
        return component.getTemplateDataToGenerate();
    }
    
    public TemplateEntry getTemplate(){
        getComponent();
        return component.getTemplate();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        getComponent();
        component.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        getComponent();
        component.removeChangeListener(l);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    protected static final class TemplateEntry {
        private final boolean resourceLibraryContract;
        private final FileObject template;

        public TemplateEntry(FileObject template) {
            this(template, false);
        }

        public TemplateEntry(FileObject template, boolean resourceLibraryContract) {
            this.template = template;
            this.resourceLibraryContract = resourceLibraryContract;
        }

        public FileObject getTemplate() {
            return template;
        }

        public boolean isResourceLibraryContract() {
            return resourceLibraryContract;
        }
    }
    
}
