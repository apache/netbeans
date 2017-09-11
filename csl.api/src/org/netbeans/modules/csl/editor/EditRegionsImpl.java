/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
