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
package org.netbeans.modules.html.editor.refactoring;

import org.netbeans.modules.web.common.ui.refactoring.RenameRefactoringUI;
import java.util.Arrays;
import java.util.Collection;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * Generic rename refactoring UI for all kinds of file possibly refered from an html code.
 * The main purpose is to allow refactoring of html references to such files.
 *
 * Please look at REFACTORABLE_TYPES field to find out what mimetypes this refactoring
 * plugin registeres an UI for.
 *
 * Anyone who want to provide its own rename refactoring has to register his 
 * ActionsImplementationProvider to a lower position.
 *
 * @author marekfukala
 */
//default position=Integet.MAX_VALUE; all who wants to provide its own refactgoring UI
//for one of the registered mimetypes has to use a lower position
@ServiceProvider(service = ActionsImplementationProvider.class)
public class HtmlActionsImplementationProvider extends ActionsImplementationProvider {

    //all mimetypes which we want to register the rename refactoring ui to
    //basically the list should contain all mimetypes which can be referenced from an html file
    //since this service provider has a very high position, if one of the mimetypes has
    //its own refactoring UI registered that one will be prefered.
    public static Collection<String> REFACTORABLE_TYPES = 
            Arrays.asList(new String[]{"text/html", "text/xhtml", "text/css", "text/javascript", "text/x-json",
            "image/gif", "image/jpeg", "image/png", "image/bmp"}); //NOI18N

    @Override
    //file rename
    public boolean canRename(Lookup lookup) {
	Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
	//we are able to rename only one node selection [at least for now ;-) ]
	if (nodes.size() != 1) {
	    return false;
	}

        //apply only on supported mimetypes and if not invoked in editor context
	Node node = nodes.iterator().next();
        EditorCookie ec = getEditorCookie(node);
        if(ec == null || !isFromEditor(ec)) {
            FileObject fo = getFileObjectFromNode(node);
            return fo != null && REFACTORABLE_TYPES.contains(fo.getMIMEType());
        }

	return false;

    }

    @Override
    //file rename
    public void doRename(Lookup selectedNodes) {
	Collection<? extends Node> nodes = selectedNodes.lookupAll(Node.class);
        assert nodes.size() == 1;
        Node node = nodes.iterator().next();
        FileObject file = getFileObjectFromNode(node);
        UI.openRefactoringUI(new RenameRefactoringUI(file));
    }


    private static FileObject getFileObjectFromNode(Node node) {
	DataObject dobj = node.getLookup().lookup(DataObject.class);
	return dobj != null ? dobj.getPrimaryFile() : null;
    }

    private static boolean isFromEditor(final EditorCookie ec) {
        return Mutex.EVENT.readAccess(new Mutex.Action<Boolean>() {
            @Override
            public Boolean run() {
                if (ec != null && ec.getOpenedPanes() != null) {
                    TopComponent activetc = TopComponent.getRegistry().getActivated();
                    if (activetc instanceof CloneableEditorSupport.Pane) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private static EditorCookie getEditorCookie(Node node) {
	return node.getLookup().lookup(EditorCookie.class);
    }


}
