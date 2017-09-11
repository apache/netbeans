
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
package org.netbeans.modules.html;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.html.api.HtmlDataNode;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node.Cookie;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.openide.util.UserQuestionException;
import org.openide.windows.CloneableOpenSupport;

/**
 * Editor support for HTML data objects.
 *
 * @author Radim Kubacki
 * @author Marek Fukala
 *
 * @see org.openide.text.DataEditorSupport
 */
public final class HtmlEditorSupport extends DataEditorSupport implements OpenCookie,
        EditCookie, EditorCookie.Observable, PrintCookie {

    private static final String DOCUMENT_SAVE_ENCODING = "Document_Save_Encoding";
    private static final String UTF_8_ENCODING = "UTF-8";
    /**
     * SaveCookie for this support instance. The cookie is adding/removing data
     * object's cookie set depending on if modification flag was set/unset. It
     * also invokes beforeSave() method on the HtmlDataObject to give it a
     * chance to eg. reflect changes in 'charset' attribute
     *
     */
    private final SaveCookie saveCookie = new SaveCookie() {
        /**
         * Implements
         * <code>SaveCookie</code> interface.
         */
        @Override
        public void save() throws IOException {
            try {
                saveDocument();
            } catch (UserCancelException uce) {
                //just ignore
            }
        }

        @Override
        public String toString() {
            return getDataObject().getPrimaryFile().getNameExt();
        }
        
        
    };

    HtmlEditorSupport(HtmlDataObject obj) {
        super(obj, null, new Environment(obj));
        setMIMEType(getDataObject().getPrimaryFile().getMIMEType());
    }

    @Override
    protected boolean close(boolean ask) {
        boolean closed = super.close(ask);
        DataObject dobj = getDataObject();
        if(closed && dobj.isValid()) {
            //set the original property sets
            HtmlDataNode nodeDelegate = (HtmlDataNode)dobj.getNodeDelegate();
            nodeDelegate.setPropertySets(null);
        }
        return closed;
    }

    @Override
    protected boolean asynchronousOpen() {
        return true;
    }

    @Override
    protected Pane createPane() {
        return (CloneableEditorSupport.Pane) MultiViews.createCloneableMultiView(HtmlLoader.HTML_MIMETYPE, getDataObject());
    }

    @Override
    public void saveDocument() throws IOException {
        updateEncoding();
        super.saveDocument();
    }

    void updateEncoding() throws UserQuestionException {
        //try to find encoding specification in the editor content
        String documentContent = getDocumentText();
        String encoding = HtmlDataObject.findEncoding(documentContent);
        String feqEncoding = FileEncodingQuery.getEncoding(getDataObject().getPrimaryFile()).name();
        if (encoding != null) {
            //found encoding specified in the file content by meta tag
            if (!isSupportedEncoding(encoding) || !canEncode(documentContent, encoding)) {
                //test if the file can be saved by the original encoding or if it needs to be saved using utf-8
                final String defaultEncoding = canEncode(documentContent, feqEncoding) ? feqEncoding : UTF_8_ENCODING;
                String msg = NbBundle.getMessage(
                        HtmlEditorSupport.class, 
                        "MSG_unsupportedEncodingSave", 
                        new Object[]{
                            getDataObject().getPrimaryFile().getNameExt(), 
                            encoding, 
                            defaultEncoding, 
                            defaultEncoding.equals(UTF_8_ENCODING) ? "" : " the original"}
                        );
                throw new UserQuestionException(msg) {
                    @Override
                    public void confirmed() throws IOException {
                        setEncodingProperty(defaultEncoding);
                        HtmlEditorSupport.super.saveDocument();
                    }
                };
                
            } else {
                setEncodingProperty(encoding);
            }
        } else {
            //no encoding specified in the file, use FEQ value
            if (!canEncode(documentContent, feqEncoding)) {
                String msg = NbBundle.getMessage(
                        HtmlEditorSupport.class, 
                        "MSG_badCharConversionSave", 
                        new Object[]{getDataObject().getPrimaryFile().getNameExt(), feqEncoding});
                throw new UserQuestionException(msg) {
                    @Override
                    public void confirmed() throws IOException {
                        setEncodingProperty(UTF_8_ENCODING);
                        HtmlEditorSupport.super.saveDocument();
                    }
                };
            } else {
                setEncodingProperty(feqEncoding);
            }
        }

    }
    
    private void setEncodingProperty(String encoding) {
        //FEQ cannot be run in saveFromKitToStream since document is locked for writing,
        //so setting the FEQ result to document property
        Document doc = getDocument();
        //the document is already loaded so getDocument() should normally return 
        //no null value, but if a CES redirector returns null from the redirected
        //CES.getDocument() then we are not able to set the found encoding
        if (doc != null) {
            doc.putProperty(DOCUMENT_SAVE_ENCODING, encoding);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        String foundEncoding = (String) doc.getProperty(DOCUMENT_SAVE_ENCODING);
        String usedEncoding = foundEncoding != null ? foundEncoding : UTF_8_ENCODING;
        final Charset c = Charset.forName(usedEncoding);
        final Writer w = new OutputStreamWriter(stream, c);
        try {
            kit.write(w, doc, 0, doc.getLength());
        } finally {
            w.close();
        }
    }

    /**
     * Overrides superclass method. Adds adding of save cookie if the document
     * has been marked modified.
     *
     * @return true if the environment accepted being marked as modified or
     * false if it has refused and the document should remain unmodified
     */
    @Override
    protected boolean notifyModified() {
        if (!super.notifyModified()) {
            return false;
        }

        addSaveCookie();

        return true;
    }

    /**
     * Overrides superclass method. Adds removing of save cookie.
     */
    @Override
    protected void notifyUnmodified() {
        super.notifyUnmodified();

        removeSaveCookie();
    }

    /**
     * Helper method. Adds save cookie to the data object.
     */
    private void addSaveCookie() {
        HtmlDataObject obj = (HtmlDataObject) getDataObject();

        // Adds save cookie to the data object.
        if (obj.getCookie(SaveCookie.class) == null) {
            obj.getCookieSet0().add(saveCookie);
            obj.setModified(true);
        }
    }

    /**
     * Helper method. Removes save cookie from the data object.
     */
    void removeSaveCookie() {
        HtmlDataObject obj = (HtmlDataObject) getDataObject();

        // Remove save cookie from the data object.
        Cookie cookie = obj.getCookie(SaveCookie.class);

        if (cookie != null && cookie.equals(saveCookie)) {
            obj.getCookieSet0().remove(saveCookie);
            obj.setModified(false);
        }
    }

    private String getDocumentText() {
        String text = "";
        try {
            StyledDocument doc = getDocument();
            if (doc != null) {
                text = doc.getText(doc.getStartPosition().getOffset(), doc.getLength());
            }
        } catch (BadLocationException e) {
            Logger.getLogger("global").log(Level.WARNING, null, e);
        }
        return text;
    }

    private boolean canDecodeFile(FileObject fo, String encoding) {
        CharsetDecoder decoder = Charset.forName(encoding).newDecoder().onUnmappableCharacter(CodingErrorAction.REPORT).onMalformedInput(CodingErrorAction.REPORT);
        try {
            BufferedInputStream bis = new BufferedInputStream(fo.getInputStream());
            //I probably have to create such big buffer since I am not sure
            //how to cut the file to smaller byte arrays so it cannot happen
            //that an encoded character is divided by the arrays border.
            //In such case it might happen that the method woult return
            //incorrect value.
            byte[] buffer = new byte[(int) fo.getSize()];
            bis.read(buffer);
            bis.close();
            decoder.decode(ByteBuffer.wrap(buffer));
            return true;
        } catch (CharacterCodingException ex) {
            //return false
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.WARNING, "Error during charset verification", ioe);
        }
        return false;
    }

    private boolean canEncode(String docText, String encoding) {
        CharsetEncoder encoder = Charset.forName(encoding).newEncoder();
        return encoder.canEncode(docText);
    }

    private boolean isSupportedEncoding(String encoding) {
        boolean supported;
        try {
            supported = java.nio.charset.Charset.isSupported(encoding);
        } catch (java.nio.charset.IllegalCharsetNameException e) {
            supported = false;
        }
        return supported;
    }

    /**
     * Nested class. Environment for this support. Extends
     * <code>DataEditorSupport.Env</code> abstract class.
     */
    private static class Environment extends DataEditorSupport.Env {

        private static final long serialVersionUID = 3035543168452715818L;

        /**
         * Constructor.
         */
        public Environment(HtmlDataObject obj) {
            super(obj);
        }

        /**
         * Implements abstract superclass method.
         */
        @Override
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        /**
         * Implements abstract superclass method.
         */
        @Override
        protected FileLock takeLock() throws IOException {
            return ((HtmlDataObject) getDataObject()).getPrimaryEntry().takeLock();
        }

        /**
         * Overrides superclass method.
         *
         * @return text editor support (instance of enclosing class)
         */
        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (HtmlEditorSupport) getDataObject().getCookie(HtmlEditorSupport.class);
        }
    } // End of nested Environment class.


    
}
