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
package org.netbeans.modules.maven.codegen;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.maven.api.Constants;
import static org.netbeans.modules.maven.codegen.Bundle.*;
import org.netbeans.modules.maven.model.pom.License;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Milos Kleint
 */
@NbBundle.Messages("NAME_License=License...")
public class LicenseGenerator extends AbstractGenerator<POMModel> {

    @MimeRegistration(mimeType=Constants.POM_MIME_TYPE, service=CodeGenerator.Factory.class, position=400)
    public static class Factory implements CodeGenerator.Factory {
        
        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> toRet = new ArrayList<CodeGenerator>();
            POMModel model = context.lookup(POMModel.class);
            JTextComponent component = context.lookup(JTextComponent.class);
            if (model != null) {
                toRet.add(new LicenseGenerator(model, component));
            }
            return toRet;
        }
    }

    private LicenseGenerator(POMModel model, JTextComponent component) {
        super(model, component);
    }

    @Override
    public String getDisplayName() {
        return NAME_License();
    }

    @Override
    @NbBundle.Messages("TIT_Add_License=Add License")    
    protected void doInvoke() {
        final NewLicensePanel panel = new NewLicensePanel(model);
        DialogDescriptor dd = new DialogDescriptor(panel, TIT_Add_License());
        panel.attachDialogDisplayer(dd);
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (ret == DialogDescriptor.OK_OPTION) {
            final String name = panel.getLicenseName();
            final String url = panel.getLicenseUrl();
            writeModel(new ModelWriter() {
                @Override
                public int write() {
                    License lic = model.getFactory().createLicense();
                    if (!name.isEmpty()) {
                        lic.setName(name);
                    }
                    if (!url.isEmpty()) {
                        lic.setUrl(url);
                    }
                    
                    if(!addAtPosition(model.getPOMQNames().LICENSES.getName(), model.getProject()::getLicenses, lic)) {                        
                        model.getProject().addLicense(lic);
                    } 
                    
                   return lic.getModel().getAccess().findPosition(lic.getPeer());
                }                                
            });
        }
    }
}
