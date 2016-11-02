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

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class Java2KotlinConverter {

    public static void convert(Document doc, Project proj) throws BadLocationException {
        String contents = doc.getText(0, doc.getLength());
        String translatedCode = JavaToKotlinTranslator.INSTANCE.prettify(
            JavaToKotlinTranslatorKt.translateToKotlin(contents, 
                    KotlinEnvironment.getEnvironment(proj).getProject()));
        NotifyDescriptor nd = new NotifyDescriptor.Message(translatedCode, 
                NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(
                nd);
    }
    
}
