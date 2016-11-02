/** *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ****************************************************************************** */
package org.jetbrains.kotlin.j2k;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.text.Document;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.loaders.DataObject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Refactoring",
        id = "org.jetbrains.kotlin.j2k.ConvertToKotlinAction"
)
@ActionRegistration(
        iconBase = "org/jetbrains/kotlin/kotlin.png",
        displayName = "#CTL_ConvertToKotlinAction"
)
@ActionReference(path = "Loaders/text/x-java/Actions", position = 2050)
@Messages("CTL_ConvertToKotlinAction=Convert to Kotlin")
public final class ConvertToKotlinAction implements ActionListener {

    private final DataObject context;

    public ConvertToKotlinAction(DataObject context) {
        this.context = context;
    }

    private void showDialog(FileObject fo, Document doc, Project project) {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
            "Convert to Kotlin. Do you want to continue?", 
                NotifyDescriptor.YES_NO_OPTION);
        Object result = DialogDisplayer.getDefault().notify(nd);
        if (result == NotifyDescriptor.YES_OPTION) {
            Java2KotlinConverter.convert(doc, project, fo);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {
        FileObject fo = context.getPrimaryFile();
        try {
            Document doc = ProjectUtils.getDocumentFromFileObject(fo);
            if (doc == null) {
                return;
            }
            Project project = ProjectUtils.getKotlinProjectForFileObject(fo);
            if (project == null) {
                return;
            }
            
            showDialog(fo, doc, project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } 
    }
}
