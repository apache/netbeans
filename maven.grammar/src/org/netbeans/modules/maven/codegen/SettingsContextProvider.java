/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.codegen;

import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.grammar.POMDataObject;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.netbeans.modules.maven.model.settings.SettingsModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author mkleint
 */
@MimeRegistration(mimeType=POMDataObject.SETTINGS_MIME_TYPE, service=CodeGeneratorContextProvider.class)
public class SettingsContextProvider implements CodeGeneratorContextProvider {

    @Override
    public void runTaskWithinContext(Lookup context, Task task) {
        JTextComponent component = context.lookup(JTextComponent.class);
        if (component != null) {
            DataObject dobj = NbEditorUtilities.getDataObject(component.getDocument());
            if (dobj != null) {
                FileObject fo = dobj.getPrimaryFile();
                ModelSource ms = Utilities.createModelSource(fo);
                SettingsModel model = SettingsModelFactory.getDefault().getModel(ms);
                if (model != null) {
                    Lookup newContext = new ProxyLookup(context, Lookups.fixed(model));
                    task.run(newContext);
                    return;
                }
            }
        }
        task.run(context);
    }

}
