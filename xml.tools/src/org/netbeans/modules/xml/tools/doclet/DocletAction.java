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
package org.netbeans.modules.xml.tools.doclet;

import java.awt.datatransfer.StringSelection;
import java.io.*;


import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.*;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.*;
import org.openide.cookies.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.actions.*;

import org.netbeans.tax.*;
import org.netbeans.modules.xml.*;
import org.netbeans.modules.xml.actions.CollectDTDAction;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.netbeans.modules.xml.tools.generator.*;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;

/**
 * Creates a documentation upon a standalone DTD. Stores it into html.
 * It does work only with standalone DTD (it is feature).
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public final class DocletAction extends CookieAction implements CollectDTDAction.DTDAction {
    /** Stream serialVersionUID. */
    private static final long serialVersionUID = -4037098165368211623L;
    
    
    /** Creates new CSSStyleAction */
    public DocletAction() {
    }

/***********
    public static synchronized DocletAction getInstance() {
        DocletAction actionInstance = null;
        String thisClassName = DocletAction.class.getName();
        try {
            Class actionInstanceClass = Class.forName(thisClassName);
            actionInstance = (DocletAction) actionInstanceClass.newInstance();
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

    public void performAction(Node[] nodes) {
            
        if (nodes == null) return;
        if (nodes.length != 1) return;

        final StringBuffer text = new StringBuffer();                
        final Node dtd = nodes[0];

        final DTDDataObject dtdo = (DTDDataObject) dtd.getCookie(DTDDataObject.class);
        final String encoding = EncodingUtil.getProjectEncoding(dtdo.getPrimaryFile());
        Thread thread = null;
        ErrorManager emgr = ErrorManager.getDefault();        
        try {
            TreeDocumentRoot result;
            TreeEditorCookie cake = (TreeEditorCookie) dtdo.getCookie(TreeEditorCookie.class);
            if (cake != null) {
                result = cake.openDocumentRoot();
            } else {
                throw new TreeException("DTDDataObject:INTERNAL ERROR"); // NOI18N
            }
            final TreeDTD treeDTD = (TreeDTD) result;

            final DTDDoclet doclet = new DTDDoclet(dtdo.getPrimaryFile().getName());

            Runnable task = new Runnable() {
                public void run() {
                    text.append(doclet.createDoclet (treeDTD, encoding));
                }
            };

            //start task in paralel with user input
            
            thread = new Thread(task, "Creating XML doc..."); // NOI18N        
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setDaemon(true);                       
            thread.start();


            try {

                // ask for file object location

                FileObject primFile = dtdo.getPrimaryFile();
                String name = primFile.getName() + NbBundle.getMessage(DocletAction.class, "NAME_SUFFIX_Documentation");
                FileObject folder = primFile.getParent();

                FileObject generFile = (new SelectFileDialog (folder, name, "html")).getFileObject(); // NOI18N
                name = generFile.getName();

                // wait until documentation generated            
                thread.join();

                // fill result file

                FileLock lock = null;
                try {
                     lock = generFile.lock();
                     OutputStream fout = generFile.getOutputStream(lock);
                     try {
                         OutputStream out = new BufferedOutputStream(fout);
                         Writer writer = new OutputStreamWriter(out, "UTF8");  //NOI18N
                         writer.write(text.toString());
                         writer.flush();
                     } finally {
                         if (fout != null) fout.close();
                     }
                     
                } catch (IOException ex) {
                    emgr.annotate(ex, NbBundle.getMessage(DocletAction.class, "MSG_error_leaving_in_clipboard"));
                    emgr.notify(ex);

                    leaveInClipboard(text.toString());
                    return;
                    
                } finally {
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }

                // open results in a browser if exists
                
                try {
                    DataObject html = DataObject.find(generFile);
                
                    ViewCookie vc = (ViewCookie) html.getCookie(ViewCookie.class);
                    if (vc != null) vc.view();
                } catch (DataObjectNotFoundException dex) {
                    // just do not show
                }

                
            } catch (UserCancelException ex) {
                //user cancelled do nothing
                
            } catch (InterruptedException ex) {

                emgr.annotate(ex, NbBundle.getMessage(DocletAction.class, "MSG_generating_interrupted"));
                emgr.notify(ex);            
            }
            
        } catch (IOException ioex) {
            
            emgr.annotate(ioex, NbBundle.getMessage(DocletAction.class, "MSG_IO_ex_docwriting"));
            emgr.notify(ioex);
            
        } catch (TreeException tex) {
            
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DocletAction.class, "MSG_doclet_fatal_error"));
        
        } finally {
            if (thread != null) thread.interrupt();
        }
                                    
    }


    private void leaveInClipboard(String text) {
        StringSelection ss = new StringSelection(text);
        ExClipboard clipboard = (ExClipboard) Lookup.getDefault().lookup(ExClipboard.class);
        clipboard.setContents(ss, null);
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DocletAction.class, "MSG_documentation_in_clipboard"));
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    public String getName() {
        return NbBundle.getMessage(DocletAction.class, "NAME_Generate_Documentation");
    }

    protected boolean asynchronous() {
        return false;
    }

}
