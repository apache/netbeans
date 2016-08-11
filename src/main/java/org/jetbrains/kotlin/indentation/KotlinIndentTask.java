/*******************************************************************************
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
 *******************************************************************************/
package org.jetbrains.kotlin.indentation;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jetbrains.kotlin.formatting.KotlinIndentStrategy;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class KotlinIndentTask implements IndentTask {

    private final Context context;
    private final Document doc;

    KotlinIndentTask(Context context) {
        this.context = context;
        this.doc = context.document();
    }

    @Override
    public void reindent() {
        try {
            KotlinIndentStrategy strategy = new KotlinIndentStrategy(context);
            strategy.addIndent();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public ExtraLock indentLock() {
        return null;
    }

}
