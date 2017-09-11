/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
