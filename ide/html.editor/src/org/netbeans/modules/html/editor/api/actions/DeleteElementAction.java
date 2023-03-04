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
package org.netbeans.modules.html.editor.api.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "action.name_delete.element=Delete"
})
public class DeleteElementAction extends AbstractSourceElementAction {

    public DeleteElementAction(FileObject file, String elementPath) {
        super(file, elementPath);
        putValue(Action.NAME, Bundle.action_name_delete_element());

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            SourceElementHandle handle = createSourceElementHandle();
            if(!handle.isResolved()) {
                return ;
            }
            OpenTag tag = handle.getOpenTag();

            int from = tag.from();
            int to = tag.semanticEnd();

            final int ast_from = handle.getSnapshot().getOriginalOffset(from);
            final int ast_to = handle.getSnapshot().getOriginalOffset(to);

            final BaseDocument doc = (BaseDocument)DataLoadersBridge.getDefault().getDocument(file);
            if(doc == null) {
                return ;
            }
            
            doc.runAtomicAsUser(new Runnable() {

                @Override
                public void run() {
                    try {
                        doc.remove(ast_from, ast_to - ast_from);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                
            });

            //TODO possibly save if not opened in editor
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

    }
}
