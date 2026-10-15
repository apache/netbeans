/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import static org.netbeans.modules.maven.codegen.Bundle.*;
import org.netbeans.modules.maven.grammar.POMDataObject;
import org.netbeans.modules.maven.model.settings.Mirror;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
/**
 *
 * @author Milos Kleint
 */
@Messages({"NAME_Mirror=Mirror...",
           "TIT_Add_mirror=Add new mirror"
})
public class MirrorGenerator extends AbstractGenerator<SettingsModel> {

    @MimeRegistration(mimeType=POMDataObject.SETTINGS_MIME_TYPE, service=CodeGenerator.Factory.class, position=100)
    public static class Factory implements CodeGenerator.Factory {
        
        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> toRet = new ArrayList<>();
            SettingsModel model = context.lookup(SettingsModel.class);
            JTextComponent component = context.lookup(JTextComponent.class);
            if (model != null) {
                toRet.add(new MirrorGenerator(model, component));
            }
            return toRet;
        }
    }
    
    /** Creates a new instance of ProfileGenerator */
    private MirrorGenerator(SettingsModel model, JTextComponent component) {
        super(model, component);
    }

    @Override
    public String getDisplayName() {
        return NAME_Mirror();
    }
    
    @Override
    protected void doInvoke() {
        final NewMirrorPanel panel = new NewMirrorPanel(model);
        DialogDescriptor dd = new DialogDescriptor(panel, TIT_Add_mirror());
        panel.attachDialogDisplayer(dd);
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (ret == DialogDescriptor.OK_OPTION) {
            final String id = panel.getMirrorId();
            writeModel(new ModelWriter() {
                @Override
                public int write() {
                    Mirror mirror = model.getSettings().findMirrorById(id);
                    if (mirror == null) {
                        mirror = model.getFactory().createMirror();
                        mirror.setId(id);
                        mirror.setUrl(panel.getMirrorUrl());
                        mirror.setMirrorOf(panel.getMirrorOf());
                        model.getSettings().addMirror(mirror);
                        return mirror.getModel().getAccess().findPosition(mirror.getPeer());
                    }
                    return -1;
                }
            });
        }
    }

}
