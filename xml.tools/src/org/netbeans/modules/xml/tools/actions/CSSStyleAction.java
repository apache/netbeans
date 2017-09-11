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
package org.netbeans.modules.xml.tools.actions;

import java.util.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.actions.*;
import org.openide.cookies.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;

import org.netbeans.tax.*;
import org.netbeans.modules.xml.DTDDataObject;

import org.netbeans.modules.xml.lib.GuiUtil;
import org.netbeans.modules.xml.tools.generator.SelectFileDialog;
import org.netbeans.modules.xml.actions.CollectDTDAction;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
/**
 * Creates a CSS draft upon a standalone DTD. Stores it into css file.
 * It does work only with standalone DTD (it is feature).
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public final class CSSStyleAction extends CookieAction implements CollectDTDAction.DTDAction {
    /** Serial Version UID */
    private static final long serialVersionUID = 7867855746468L;
    
    /** Creates new CSSStyleAction */
    public CSSStyleAction() {}

/***********
    public static synchronized CSSStyleAction getInstance() {
        CSSStyleAction actionInstance = null;
        String thisClassName = CSSStyleAction.class.getName();
        try {
            Class actionInstanceClass = Class.forName(thisClassName);
            actionInstance = (CSSStyleAction) actionInstanceClass.newInstance();
        } catch(Exception e) {
            Logger.getLogger(thisClassName).log(Level.SEVERE, "", e);
        }
        return actionInstance;
    }
***********/
    
    public Class[] cookieClasses() {
        return new Class[] { DTDDataObject.class };
    }

    public int mode() {
        return MODE_ONE;
    }

    private String css;

    public void performAction(Node[] nodes) {
        if (nodes == null) return;
        if (nodes.length != 1) return;

        Node dtd = nodes[0];

        css = ""; //"@charset \"UTF-8\";\n"; // NOI18N
        css += "/* Cascade style sheet based on " + dtd.getDisplayName() + " DTD */\n"; // NOI18N

        DTDDataObject dtdo = (DTDDataObject) dtd.getCookie(DTDDataObject.class);
        
        ErrorManager emgr = ErrorManager.getDefault();
            
        try {

            TreeDocumentRoot result;

            TreeEditorCookie cake = (TreeEditorCookie) dtdo.getCookie(TreeEditorCookie.class);
            if (cake != null) {
                result = cake.openDocumentRoot();
            } else {
                throw new TreeException("DTDDataObject:INTERNAL ERROR"); // NOI18N
            }
            TreeDTD treeDTD = (TreeDTD) result;

            Iterator it = treeDTD.getElementDeclarations().iterator();

            while (it.hasNext()) {
                TreeElementDecl decl = (TreeElementDecl) it.next();
                String name = decl.getName();
                add(name);
            }
            
            // ask for data object location
            
            FileObject primFile = dtdo.getPrimaryFile();
            String name = primFile.getName() + NbBundle.getMessage(CSSStyleAction.class, "NAME_SUFFIX_Stylesheet");
            FileObject folder = primFile.getParent();

            FileObject generFile = (new SelectFileDialog (folder, name, "css")).getFileObject(); // NOI18N
            name = generFile.getName();
            
            // create and open data object
            
            DataObject targeto;

            
            try {
                targeto = DataObject.find(generFile);
            } catch (DataObjectNotFoundException eex) {
                return;
            }

            EditorCookie ec = (EditorCookie) targeto.getCookie(EditorCookie.class);
            if (ec != null) {
                Document doc = ec.openDocument();
                
                try {
                    doc.remove(0, doc.getLength());
                    doc.insertString(0, css, null);
                    ec.saveDocument();
                } catch (BadLocationException locex) {
                    emgr.annotate(locex, NbBundle.getMessage(CSSStyleAction.class, "MSG_Leaving_CSS_in_clipboard"));
                    emgr.notify(locex);                    
                    
                    StringSelection ss = new StringSelection(css);
                    ExClipboard clipboard = (ExClipboard) Lookup.getDefault().lookup(ExClipboard.class);
                    clipboard.setContents(ss, null);
                    GuiUtil.setStatusText(NbBundle.getMessage(CSSStyleAction.class, "MSG_CSS_placed_in_clipboard"));
                    
                }
                
                
                OpenCookie oc = (OpenCookie) targeto.getCookie(OpenCookie.class);
                if (oc != null) oc.open();
                
            }
            
        } catch (UserCancelException ex) {
            //user cancelled do nothing
            
        } catch (IOException ex) {

            emgr.annotate(ex, NbBundle.getMessage(CSSStyleAction.class, "MSG_IO_ex_CSS_writing."));
            emgr.notify(ex);
            
        } catch (TreeException ex) {
            Logger.getLogger(CSSStyleAction.class.getName()).log(Level.INFO, null, ex);
            GuiUtil.setStatusText(NbBundle.getMessage(CSSStyleAction.class, "MSG_CSS_fatal_error"));
            
        }

    }

    /** adds a new name to just created CSS. */
    private void add(String name) {
        css += name + " { display: block }\n"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    public String getName() {
        return NbBundle.getMessage(CSSStyleAction.class, "NAME_Generate_CSS");
    }
    
    protected boolean asynchronous() {
        return false;
    }

}
