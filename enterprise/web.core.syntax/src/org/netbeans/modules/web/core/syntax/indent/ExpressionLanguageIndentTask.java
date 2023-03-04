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
package org.netbeans.modules.web.core.syntax.indent;

import javax.swing.text.BadLocationException;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.web.core.syntax.formatting.ExpressionLanguageIndenter;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class ExpressionLanguageIndentTask implements IndentTask, Lookup.Provider {

    private ExpressionLanguageIndenter indenter;
    private Lookup lookup;
    
    ExpressionLanguageIndentTask(Context context) {
        indenter = new ExpressionLanguageIndenter(context);
        lookup = Lookups.singleton(indenter.createFormattingContext());
    }

    public void reindent() throws BadLocationException {
        indenter.reindent();
    }
    
    public ExtraLock indentLock() {
        return null;
    }

    public Lookup getLookup() {
        return lookup;
    }
}
