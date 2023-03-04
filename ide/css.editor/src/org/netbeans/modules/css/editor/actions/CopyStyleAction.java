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
package org.netbeans.modules.css.editor.actions;

import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExClipboard;


/**
 * Action that put XML style processing instruction in clipboard.
 * TODO add PI flavor.
 *
 * @author Petr Kuzel
 */
public abstract class CopyStyleAction extends BaseAction {
    
    protected static final String comment = NbBundle.getMessage(CopyStyleAction.class, "Style-Comment") + "\n"; // NOI18N
    
    public CopyStyleAction(String name) {
        super(name);
    }
    
    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        BaseDocument bdoc = Utilities.getDocument(target);
        if(bdoc == null) {
            return ; //no document?!?!
        }
        DataObject csso = NbEditorUtilities.getDataObject(bdoc);
        if(csso == null) {
            return ; //document not backuped by DataObject
        }
        
        String pi = createText(csso);
        StringSelection ss = new StringSelection(pi);
        ExClipboard clipboard = Lookup.getDefault().lookup(ExClipboard.class);
        clipboard.setContents(ss, null);
        StatusDisplayer.getDefault().setStatusText( NbBundle.getMessage(CopyStyleAction.class, "MSG_Style_tag_in_clipboard"));  // NOI18N
        
    }
    
    /** A method that creates particular clipboard text.
     * @return text to be placed to clip board.
     */
    protected abstract String createText(DataObject dobj);
    
    /** Get help context for the action.
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }
    
    /** Converts CSS fileobject to its href that is valid during IDE runtime. */
    protected String getHref(FileObject fo) {
        URL u = URLMapper.findURL(fo, URLMapper.NETWORK);
        if (u != null) {
            return u.toExternalForm();
        } else {
            return fo.getPath();
        }
    }
    
    /** Produces XML PI text. */
    public static final class XML extends CopyStyleAction {
        
        public static final String copyStyleAction =  NbBundle.getMessage(CopyStyleAction.class, "Copy-XML-Style");//NOI18N
        
        public XML() {
            super(copyStyleAction);
            putValue(SHORT_DESCRIPTION, copyStyleAction);
        }
        
        @Override
        protected String createText(DataObject csso) {
            return comment + "<?xml-stylesheet type=\"text/css\" href=\"" + this.getHref(csso.getPrimaryFile()) + "\" ?>"; // NOI18N
        }
        
    }
    
    /** Produces HTML style text. */
    public static final class HTML extends CopyStyleAction {

        public static final String copyStyleAction = NbBundle.getMessage(CopyStyleAction.class, "Copy-HTML-Style");//NOI18N
        
        public HTML() {
            super(copyStyleAction);
            putValue(SHORT_DESCRIPTION, copyStyleAction);
        }
        
        @Override
        protected String createText(DataObject csso) {
            return comment + "<link rel=\"StyleSheet\" type=\"text/css\" href=\"" + this.getHref(csso.getPrimaryFile()) + "\" media=\"screen\" >";  // NOI18N
        }

    }
}
