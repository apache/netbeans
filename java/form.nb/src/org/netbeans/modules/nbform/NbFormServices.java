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
package org.netbeans.modules.nbform;

import java.awt.datatransfer.Transferable;
import java.util.logging.Level;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.editor.DialogBinding;
import org.netbeans.api.java.loaders.JavaDataSupport;
import org.netbeans.modules.form.EditorSupport;
import org.netbeans.modules.form.FormDataObject;
import org.netbeans.modules.form.FormServices;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.nbform.palette.BeanInstaller;
import org.netbeans.modules.nbform.palette.FormPaletteActions;
import org.netbeans.spi.palette.PaletteActions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.lookup.ServiceProvider;

/**
 * NetBeans implementation of {@code FormServices}.
 *
 * @author Jan Stola
 */
@ServiceProvider(service=FormServices.class)
public class NbFormServices implements FormServices {

    @Override
    public JEditorPane createCodeEditorPane() {
        return new JEditorPane();
    }

    @Override
    public void setupCodeEditorPane(JEditorPane editor, FileObject srcFile, int ccPosition) {
        DataObject dob = null;
        try {
            dob = DataObject.find(srcFile);
        } catch (DataObjectNotFoundException dnfex) {
            FormUtils.LOGGER.log(Level.INFO, dnfex.getMessage(), dnfex);
        }
        if (!(dob instanceof FormDataObject)) {
            FormUtils.LOGGER.log(Level.INFO, "Unable to find FormDataObject for {0}", srcFile); // NOI18N
            return;
        }
        FormDataObject formDob = (FormDataObject)dob;
        Document document = formDob.getFormEditorSupport().getDocument();
        DialogBinding.bindComponentToDocument(document, ccPosition, 0, editor);

        // do not highlight current row
        editor.putClientProperty(
            "HighlightsLayerExcludes", //NOI18N
            "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" //NOI18N
        );

        FormUtils.setupTextUndoRedo(editor);
    }

    @Override
    public PaletteActions createPaletteActions() {
        return new FormPaletteActions();
    }

    @Override
    public ClassSource getCopiedBeanClassSource(Transferable transferable) {
        DataObject dobj = NodeTransfer.cookie(transferable, NodeTransfer.COPY, DataObject.class);
        FileObject fo = (dobj != null && dobj.isValid()) ? dobj.getPrimaryFile() : null;
        if (fo == null) {
            return null;
        }

        String clsName = BeanInstaller.findJavaBeanName(fo);
        if (clsName == null) {
            return null;
        }

        return BeanInstaller.getProjectClassSource(fo, clsName);
    }

    @Override
    public Node createFormDataNode(FormDataObject formDataObject) {
        FormDataNode node = new FormDataNode(formDataObject);
        return node;
    }

    @Override
    public Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
        return JavaDataSupport.createJavaFileEntry(obj, primaryFile);
    }

    @Override
    public boolean isLayoutExtensionsLibrarySupported() {
        return true;
    }

    @Override
    public Class<? extends EditorSupport> getEditorSupportClass(FormDataObject formDataObject) {
        return FormEditorSupport.class;
    }

    @Override
    public EditorSupport createEditorSupport(FormDataObject formDataObject) {
        return new FormEditorSupport(formDataObject);
    }
    
}
