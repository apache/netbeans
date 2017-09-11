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
