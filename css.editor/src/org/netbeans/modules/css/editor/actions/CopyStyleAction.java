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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    public final static class XML extends CopyStyleAction {
        
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
    public final static class HTML extends CopyStyleAction {

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
