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

package org.netbeans.modules.j2ee.ddloaders.common.xmlutils;

import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;

import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.CloneableOpenSupport;

/** Support for editing a XMLJ2eeDataObject as text.
 *
 * @author mkuchtiak
 */
public class XMLJ2eeEditorSupport extends DataEditorSupport
        implements EditCookie, EditorCookie.Observable,/* OpenCookie, */LineCookie, CloseCookie, PrintCookie {
    
    /** Delay for automatic parsing - in milliseconds */
    private static final int AUTO_PARSING_DELAY = 2000;
    private DialogDescriptor dialog;    
    private RequestProcessor.Task parsingDocumentTask;
    XMLJ2eeDataObject dataObject;
    SaveCookie saveCookie = new Save();

    /** Create a new editor support.
     * @param obj the data object whose primary file will be edited as text
     */
    public XMLJ2eeEditorSupport(XMLJ2eeDataObject obj) {
        super (obj, null, new XmlEnv (obj));
        dataObject=obj;
        
        // Set a MIME type as needed, e.g.:
        setMIMEType ("text/xml");   // NOI18N

    }

    @Override
    protected Pane createPane() {
        if (dataObject.getEditorMimeType() != null) {
            return (CloneableEditorSupport.Pane) MultiViews.createCloneableMultiView(dataObject.getEditorMimeType(), getDataObject());
        }
        return (CloneableEditorSupport.Pane) MultiViews.createCloneableMultiView("text/xml", getDataObject());
    }

    /**
     * Overridden method from CloneableEditorSupport.
     */
    protected void saveFromKitToStream (StyledDocument doc, EditorKit kit,
                                            OutputStream stream)
        			throws IOException, BadLocationException {
        // kit and kit() are not accessible so we pretend
        // to create the kit; actually this should just return kit.
        EditorKit k = this.createEditorKit();
        OutputStreamWriter osw = new OutputStreamWriter(stream, "UTF8"); // NOI18N
        Writer writer = new BufferedWriter(osw);
        k.write(writer, doc, 0, doc.getLength());
        writer.close();
    }
 
    /**
     * Overridden method from CloneableEditorSupport.
     */
    protected void loadFromStreamToKit (StyledDocument doc, InputStream stream,
                                            EditorKit kit)
        			throws IOException, BadLocationException {
        // kit and kit() are not accessible so we pretend
        // to create the kit; actually this should just return kit.
        EditorKit k = this.createEditorKit();
        InputStreamReader isr = new InputStreamReader(stream, "UTF8"); // NOI18N
        Reader reader = new BufferedReader(isr);
        k.read(reader, doc, 0);
        reader.close();
    }

    /** Restart the timer which starts the parser after the specified delay.
    * @param onlyIfRunning Restarts the timer only if it is already running
    */
    public void restartTimer() {
        //System.out.println("XMLJ2eeEditorSupport:restartTimer "+this.hashCode());
        dataObject.setDocumentDirty(true);
        Runnable r = new Runnable() {
            public void run() {
                dataObject.parsingDocument();
            }
	};
        if (parsingDocumentTask==null || parsingDocumentTask.isFinished() || 
            parsingDocumentTask.cancel()) {
            parsingDocumentTask = RequestProcessor.getDefault().post(r,100);                 
        } else {
            parsingDocumentTask = RequestProcessor.getDefault().post(r,AUTO_PARSING_DELAY);             
        }
    }    

    /** Called when the document is modified.
     * Here, adding a save cookie to the object and marking it modified.
     * @return true if the modification is acceptable
     */
    protected boolean notifyModified () {
        boolean notif = super.notifyModified();
        if (!notif){
            return false;
        }
        XMLJ2eeDataObject obj = (XMLJ2eeDataObject) getDataObject ();
        //System.out.println("notifyModified(), nodeDirty="+obj.isNodeDirty());
        if (obj.getCookie (SaveCookie.class) == null) {
            obj.addSaveCookie (saveCookie);
        }
        if (!obj.isNodeDirty()) restartTimer();
        return true;
    }

    /** Called when the document becomes unmodified.
     * Here, removing the save cookie from the object and marking it unmodified.
     */
    protected void notifyUnmodified () {
        super.notifyUnmodified ();
        XMLJ2eeDataObject obj = (XMLJ2eeDataObject) getDataObject ();
        obj.removeSaveCookie();
    }

    /** A save cookie to use for the editor support.
     * When saved, saves the document to disk and marks the object unmodified.
     */
    private class Save implements SaveCookie {
        
        public void save () throws IOException {
            XMLJ2eeDataObject obj = (XMLJ2eeDataObject) getDataObject ();
            if (obj.isDocumentValid()) {
                obj.setSavingDocument(true);
                saveDocument();
            }else {
                obj.displayErrorMessage();
                dialog = new DialogDescriptor(
                    NbBundle.getMessage (XMLJ2eeEditorSupport.class, "MSG_invalidXmlWarning"),
                    NbBundle.getMessage (XMLJ2eeEditorSupport.class, "TTL_invalidXmlWarning"));
                Dialog d = DialogDisplayer.getDefault().createDialog(dialog);
                d.setVisible(true);
                if (dialog.getValue() == DialogDescriptor.OK_OPTION) {
                    obj.setSavingDocument(true);
                    saveDocument();
                }
            }
        }
    }
    /*
     * Save document using encoding declared in XML prolog if possible otherwise
     * at UTF-8 (in such case it updates the prolog).
     */
    public void saveDocument () throws IOException {
        final StyledDocument doc = getDocument();
        // dependency on xml/core
        String enc = EncodingUtil.detectEncoding(doc);
        if (enc == null) enc = "UTF8"; //!!! // NOI18N
        
        try {
            //test encoding on dummy stream
            new OutputStreamWriter(new ByteArrayOutputStream(1), enc);
            if (!checkCharsetConversion(enc)) {
                return;
            }
            super.saveDocument();
            //moved from Env.save()
// DataObject.setModified() already called as part of super.saveDocument(). The save action is now asynchronous
// in the IDE and super.saveDocument() checks for possible extra document modifications performed during save
// and sets the DO.modified flag accordingly.
//            getDataObject().setModified (false);
        } catch (UnsupportedEncodingException ex) {
            // ask user what next?
            String message = NbBundle.getMessage(XMLJ2eeEditorSupport.class,"TEXT_SAVE_AS_UTF", enc);
            NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(message);
            Object res = DialogDisplayer.getDefault().notify(descriptor);

            if (res.equals(NotifyDescriptor.YES_OPTION)) {

                // update prolog to new valid encoding                

                try {
                    final int MAX_PROLOG = 1000;                
                    int maxPrologLen = Math.min(MAX_PROLOG, doc.getLength());                
                    final char prolog[] = doc.getText(0, maxPrologLen).toCharArray();
                    int prologLen = 0;  // actual prolog length

                    //parse prolog and get prolog end                
                    if (prolog[0] == '<' && prolog[1] == '?' && prolog[2] == 'x') {

                        // look for delimitting ?>
                        for (int i = 3; i<maxPrologLen; i++) {
                            if (prolog[i] == '?' && prolog[i+1] == '>') {
                                prologLen = i + 1;
                                break;
                            }
                        }                                        
                    }

                    final int passPrologLen = prologLen;

                    Runnable edit = new Runnable() {
                         public void run() {
                             try {

                                doc.remove(0, passPrologLen + 1); // +1 it removes exclusive
                                doc.insertString(0, "<?xml version='1.0' encoding='UTF-8' ?> \n<!-- was: " + new String(prolog, 0, passPrologLen + 1) + " -->", null); // NOI18N

                             } catch (BadLocationException e) {
                                 if (System.getProperty("netbeans.debug.exceptions") != null) // NOI18N
                                     e.printStackTrace();
                             }
                         }
                    };

                    NbDocument.runAtomic(doc, edit);

                    super.saveDocument();
                    //moved from Env.save()
// DataObject.setModified() already called as part of super.saveDocument(). The save action is now asynchronous
// and super.saveDocument() checks for possible extra document modifications performed during save
// and sets the DO.modified flag accordingly.
//                    getDataObject().setModified (false);

                } catch (BadLocationException lex) {
                    Exceptions.printStackTrace(lex);
                }

            } else { // NotifyDescriptor != YES_OPTION
                return;
            }
        }
    }
    
    private boolean checkCharsetConversion(final String encoding) {
        boolean value = true;
        try {
            CharsetEncoder coder = Charset.forName(encoding).newEncoder();
            if (!coder.canEncode(getDocument().getText(0, getDocument().getLength()))){
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(XMLJ2eeEditorSupport.class, "MSG_BadCharConversion",
                        new Object [] { getDataObject().getPrimaryFile().getNameExt(),
                        encoding}),
                        NotifyDescriptor.YES_NO_OPTION,
                        NotifyDescriptor.WARNING_MESSAGE);
                nd.setValue(NotifyDescriptor.NO_OPTION);
                DialogDisplayer.getDefault().notify(nd);
                if(nd.getValue() != NotifyDescriptor.YES_OPTION) {
                    value = false;
                }
            }
        } catch (BadLocationException e){
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        return value;
    }
    
    public UndoRedo.Manager getUndo(){
        return getUndoRedo();
    }
    
    /** Constructs message that should be used to name the editor component.
    *
    * @return name of the editor
    */
    protected String messageName () {
        String name = super.messageName();
        int index1 = name.indexOf('[');
        
        if (index1>=0) {
            String prefix = name.substring(0,index1);
            int index2 = name.lastIndexOf(']');
            String postfix="";
            if (index2>=0) postfix = name.substring(index2+1);
            return prefix+postfix;
        }
        return name;
    }    

    /** A description of the binding between the editor support and the object.
     * Note this may be serialized as part of the window system and so
     * should be static, and use the transient modifier where needed.
     */
    private static class XmlEnv extends DataEditorSupport.Env {

        private static final long serialVersionUID = -800036748848958489L;

        //private static final long serialVersionUID = ...L;

        /** Create a new environment based on the data object.
         * @param obj the data object to edit
         */
        public XmlEnv (XMLJ2eeDataObject obj) {
            super (obj);
        }

        /** Get the file to edit.
         * @return the primary file normally
         */
        protected FileObject getFile () {
            return getDataObject ().getPrimaryFile ();
        }

        /** Lock the file to edit.
         * Should be taken from the file entry if possible, helpful during
         * e.g. deletion of the file.
         * @return a lock on the primary file normally
         * @throws IOException if the lock could not be taken
         */
        protected FileLock takeLock () throws IOException {
            return ((XMLJ2eeDataObject) getDataObject ()).getPrimaryEntry ().takeLock ();
        }

        /** Find the editor support this environment represents.
         * Note that we have to look it up, as keeping a direct
         * reference would not permit this environment to be serialized.
         * @return the editor support
         */
        public CloneableOpenSupport findCloneableOpenSupport () {
            return (XMLJ2eeEditorSupport) getDataObject ().getCookie (XMLJ2eeEditorSupport.class);
        }
    }
}
