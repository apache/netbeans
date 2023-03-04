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
package org.netbeans.modules.csl.editor;

import javax.swing.SwingUtilities;
import org.netbeans.modules.csl.api.EditRegions;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;


/**
 * Provide access to document synch editing (until this is a core editing API)
 *
 * @author Tor Norbye
 */
public class EditRegionsImpl extends EditRegions {

    public void edit(final FileObject fo, final Set<OffsetRange> regions, final int caretOffset) throws BadLocationException {
        
        // This can only be called on the SwingUtilities thread
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        edit(fo, regions, caretOffset);
                    } catch (BadLocationException ble) {
                        Exceptions.printStackTrace(ble);
                    }
                }
            });
            
            return;
        }

        // Update caret listener
        JEditorPane[] panes = DataLoadersBridge.getDefault().getOpenedPanes(fo);

        if ((panes == null) || (panes.length == 0)) {
            return;
        }

        JEditorPane pane = panes[0];

        Document doc = pane.getDocument();

        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(fo.getMIMEType());
        if (language == null) {
            return;
        }

        if ((regions != null) && (regions.size() > 0)) {
            InstantRenamePerformer.performInstantRename(pane, regions, caretOffset);
        }
    }
}
