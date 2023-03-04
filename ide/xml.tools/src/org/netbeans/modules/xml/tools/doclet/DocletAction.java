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
