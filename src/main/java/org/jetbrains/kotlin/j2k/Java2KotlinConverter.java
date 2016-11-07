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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jetbrains.kotlin.formatting.KotlinFormatterUtils;
import org.jetbrains.kotlin.log.KotlinLogger;
import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtPsiFactory;
import org.jetbrains.kotlin.psi.KtPsiFactoryKt;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

public class Java2KotlinConverter {

    public static void convert(Document doc, Project proj, FileObject fo) {
        String translatedCode = getTranslatedCode(doc, proj);
        if (translatedCode == null) {
            showError("Error while converting to Kotlin");
            return;
        }
        
        String newKotlinFilePath = fo.getParent().getPath() + 
                ProjectUtils.FILE_SEPARATOR + fo.getName() + ".kt";
        
        File kotlinFile = new File(newKotlinFilePath);
        if (kotlinFile.exists()) {
            showError(newKotlinFilePath + " already exists");
            return;
        }
        
        try {
            kotlinFile.createNewFile();
        } catch (IOException ex) {
            showError("Error");
            return;
        }
        
        String formattedCode = getFormattedCode(translatedCode, kotlinFile.getName(), proj);
        
        if (!addContent(kotlinFile, formattedCode)) {
            showError("Couldn't add content to Kotlin file");
            return;
        }
        
        try {
            fo.delete();
        } catch (IOException ex) {
            showError("Couldn't delete Java file");
            return;
        }
        
        FileObject kotlinFO = FileUtil.toFileObject(kotlinFile);
        try {
            DataObject.find(kotlinFO).getLookup().lookup(OpenCookie.class).open();
        } catch (DataObjectNotFoundException ex) {
            KotlinLogger.INSTANCE.logException("Cannot open Kotlin file", ex);
        }
    }
    
    private static boolean addContent(File file, String code) {
        BufferedWriter writer = null;
        try {
            writer = Files.newBufferedWriter(file.toPath());
            writer.write(code);
            writer.flush();
            return true;
        } catch (IOException ex) {
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    private static String getFormattedCode(String code, String fileName, Project project) {
        KtFile ktFile = new KtPsiFactory(
                KotlinEnvironment.getEnvironment(project).getProject()).createFile(code);
        String formattedCode = KotlinFormatterUtils.formatCode(code, fileName, 
                KtPsiFactoryKt.KtPsiFactory(ktFile), "\n");
        
        return formattedCode;
    }
    
    private static void showError(String message) {
        NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(nd);
    }
    
    private static String getTranslatedCode(Document doc, Project proj) {
        try {
            String contents = doc.getText(0, doc.getLength());
            return JavaToKotlinTranslatorKt.translateToKotlin(contents,
                            KotlinEnvironment.getEnvironment(proj).getProject());
        } catch (BadLocationException ex) {
            return null;
        }
    }
    
}
