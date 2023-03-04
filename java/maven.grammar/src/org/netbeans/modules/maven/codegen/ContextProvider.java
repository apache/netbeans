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

import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
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
@MimeRegistration(mimeType=Constants.POM_MIME_TYPE, service=CodeGeneratorContextProvider.class)
public class ContextProvider implements CodeGeneratorContextProvider {

    @Override
    public void runTaskWithinContext(Lookup context, Task task) {
        JTextComponent component = context.lookup(JTextComponent.class);
        if (component != null) {
            DataObject dobj = NbEditorUtilities.getDataObject(component.getDocument());
            if (dobj != null) {
                FileObject fo = dobj.getPrimaryFile();
                ModelSource ms = Utilities.createModelSource(fo);
                if (ms.isEditable()) {
                    POMModel model = POMModelFactory.getDefault().getModel(ms);
                    if (model != null) {
                        Lookup newContext = new ProxyLookup(context, Lookups.fixed(model));
                        task.run(newContext);
                        return;
                    }
                }
            }
        }
        task.run(context);
    }

}
