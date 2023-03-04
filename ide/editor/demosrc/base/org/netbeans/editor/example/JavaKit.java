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

package org.netbeans.editor.example;

import java.io.*;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.text.MessageFormat;

import java.util.Map;
import java.util.List;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import javax.swing.KeyStroke;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;
import org.netbeans.editor.ext.java.*;

/**
* Java editor kit with appropriate document
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaKit extends ExtKit {

    public static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N

    private static final String[] getSetIsPrefixes = new String[] {
                "get", "set", "is" // NOI18N
            };

    /** Switch first letter of word to capital and insert 'get'
    * at word begining.
    */
    public static final String makeGetterAction = "make-getter"; // NOI18N

    /** Switch first letter of word to capital and insert 'set'
    * at word begining.
    */
    public static final String makeSetterAction = "make-setter"; // NOI18N

    /** Switch first letter of word to capital and insert 'is'
    * at word begining.
    */
    public static final String makeIsAction = "make-is"; // NOI18N

    /** Debug source and line number */
    public static final String abbrevDebugLineAction = "abbrev-debug-line"; // NOI18N

    static final long serialVersionUID =-5445829962533684922L;

    static {
        Settings.addInitializer( new JavaSettingsInitializer( JavaKit.class ) );
        Settings.addInitializer( new SaJavaSettingsInitializer() );
        Settings.reset();

        ResourceBundle settings = ResourceBundle.getBundle( "settings" ); // NOI18N
        String jcPath = null;
        try {
            jcPath = settings.getString( "Java_Completion" );
        } catch( MissingResourceException exc ) {}

        if( jcPath != null ) {
	    URL skeleton = JavaKit.class.getResource("/" + jcPath + ".jcs");
	    URL body     = JavaKit.class.getResource("/" + jcPath + ".jcb");
	    
	    if (skeleton == null || body == null) {
	       System.err.println("Warning: Java parser databases not found. Ignoring.");
	    } else {
	        DAFileProvider provider = new DAFileProvider(
		    new URLAccessor(skeleton),
		    new URLAccessor(body)
	        );
	    
                JCBaseFinder finder = new JCBaseFinder();
	    
                finder.append( provider );
                JavaCompletion.setFinder( finder );
	    }
        }
    }

    public String getContentType() {
        return JAVA_MIME_TYPE;
    }

    /** Create new instance of syntax coloring scanner
    * @param doc document to operate on. It can be null in the cases the syntax
    *   creation is not related to the particular document
    */
    public Syntax createSyntax(Document doc) {
        return new JavaSyntax();
    }

    /** Create syntax support */
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new JavaSyntaxSupport(doc);
    }

    public Completion createCompletion(ExtEditorUI extEditorUI) {
        return new JavaCompletion(extEditorUI);
    }

    /** Create the formatter appropriate for this kit */
    public Formatter createFormatter() {
        return new JavaFormatter(this.getClass());
    }
    
    protected EditorUI createEditorUI() {
        return new ExtEditorUI();
    }

    protected void initDocument(BaseDocument doc) {
        doc.addLayer(new JavaDrawLayerFactory.JavaLayer(),
                JavaDrawLayerFactory.JAVA_LAYER_VISIBILITY);
        doc.addDocumentListener(new JavaDrawLayerFactory.LParenWatcher());
    }

    protected Action[] createActions() {
        Action[] javaActions = new Action[] {
                                   new JavaDefaultKeyTypedAction(),
                                   new PrefixMakerAction(makeGetterAction, "get", getSetIsPrefixes), // NOI18N
                                   new PrefixMakerAction(makeSetterAction, "set", getSetIsPrefixes), // NOI18N
                                   new PrefixMakerAction(makeIsAction, "is", getSetIsPrefixes), // NOI18N
                                   new AbbrevDebugLineAction(),
                               };
        return TextAction.augmentList(super.createActions(), javaActions);
    }


    public static class JavaDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {

        protected void checkIndentHotChars(JTextComponent target, String typedText) {
            boolean reindent = false;

            BaseDocument doc = Utilities.getDocument(target);
            int dotPos = target.getCaret().getDot();
            if (doc != null) {
                /* Check whether the user has written the ending 'e'
                 * of the first 'else' on the line.
                 */
                if ("e".equals(typedText)) { // NOI18N
                    try {
                        int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                        if (fnw >= 0 && fnw + 4 == dotPos
                            && "else".equals(doc.getText(fnw, 4)) // NOI18N
                        ) {
                            reindent = true;
                        }
                    } catch (BadLocationException e) {
                    }

                } else if (":".equals(typedText)) { // NOI18N
                    try {
                        int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                        if (fnw >= 0 && fnw + 4 <= doc.getLength()
                            && "case".equals(doc.getText(fnw, 4)) // NOI18N
                        ) {
                            reindent = true;
                        }
                    } catch (BadLocationException e) {
                    }
                }

                // Reindent the line if necessary
                if (reindent) {
                    try {
                        Utilities.reformatLine(doc, dotPos);
                    } catch (BadLocationException e) {
                    }
                }
            }

            super.checkIndentHotChars(target, typedText);
        }

    }



    public static class AbbrevDebugLineAction extends BaseAction {

        public AbbrevDebugLineAction() {
            super(abbrevDebugLineAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                BaseDocument doc = (BaseDocument)target.getDocument();
                StringBuffer sb = new StringBuffer("System.err.println(\""); // NOI18N
                File file = (File)doc.getProperty( "file" );
                if (file != null) {
                    sb.append( file.getAbsolutePath() );
                    sb.append(':');
                }
                try {
                    sb.append(Utilities.getLineOffset(doc, target.getCaret().getDot()) + 1);
                } catch (BadLocationException e) {
                }
                sb.append(' ');

                BaseKit kit = Utilities.getKit(target);
                Action a = kit.getActionByName(BaseKit.insertContentAction);
                if (a != null) {
                    Utilities.performAction(
                        a,
                        new ActionEvent(target, ActionEvent.ACTION_PERFORMED, sb.toString()),
                        target
                    );
                }
            }
        }
    }
    
    
    private static class SaJavaSettingsInitializer extends Settings.AbstractInitializer {
        public SaJavaSettingsInitializer() {
            super( "sa-java-settings-initializer" ); // NOI18N
        }
        
        
        
        public void updateSettingsMap(Class kitClass, Map settingsMap) {
            if (kitClass == JavaKit.class) {
                SettingsUtil.updateListSetting(settingsMap, SettingsNames.KEY_BINDING_LIST, getJavaKeyBindings());
            }

        }

        public MultiKeyBinding[] getJavaKeyBindings() {
            return new MultiKeyBinding[] {
               new MultiKeyBinding(
                   new KeyStroke[] {
                       KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
                       KeyStroke.getKeyStroke(KeyEvent.VK_G, 0)
                   },
                   JavaKit.makeGetterAction
               ),
               new MultiKeyBinding(
                   new KeyStroke[] {
                       KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
                       KeyStroke.getKeyStroke(KeyEvent.VK_S, 0)
                   },
                   JavaKit.makeSetterAction
               ),
               new MultiKeyBinding(
                   new KeyStroke[] {
                       KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
                       KeyStroke.getKeyStroke(KeyEvent.VK_I, 0)
                   },
                   JavaKit.makeIsAction
               ),
               new MultiKeyBinding(
                   KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_MASK),
                   JavaKit.abbrevDebugLineAction
               )
            };
        }
    }


    /**
     *   DataAccessor for parser DB files via URL streams
     *
     *   @author  Petr Nejedly
     */
    public static class URLAccessor implements DataAccessor {
    
        URL url;
        InputStream stream;
	int streamOff;
        int actOff;

        public URLAccessor(URL url) {
            this.url = url;
        }
    
        /** Not implemented
         */
        public void append(byte[] buffer, int off, int len) throws IOException {
	    throw new IllegalArgumentException("read only!"); // NOI18N
        }
    
        /**
         * Reads exactly <code>len</code> bytes from this file resource into the byte
         * array, starting at the current file pointer. This method reads
         * repeatedly from the file until the requested number of bytes are
         * read. This method blocks until the requested number of bytes are
         * read, the end of the inputStream is detected, or an exception is thrown.
         *
         * @param      buffer     the buffer into which the data is read.
         * @param      off        the start offset of the data.
         * @param      len        the number of bytes to read.
         */
        public void read(byte[] buffer, int off, int len) throws IOException {
	    InputStream str = getStream(actOff);
	    while (len > 0) {
		int count = str.read(buffer, off, len);
		streamOff += count;
		off += count;
		len -= count;
	    }
        }
    
        /** Opens DataAccessor file resource 
         *  @param requestWrite if true, file is opened for read/write operation.
         */
        public void open(boolean requestWrite) throws IOException {
	    if(requestWrite) throw new IllegalArgumentException("read only!"); // NOI18N
        }
    
        /** Closes DataAccessor file resource  */
        public void close() throws IOException {
            if (stream!=null) {
                stream.close();
        	stream = null;
	    }
        }
    
        /**
         * Returns the current offset in this file. 
         *
         * @return     the offset from the beginning of the file, in bytes,
         *             at which the next read or write occurs.
         */
        public long getFilePointer() throws IOException {
           return actOff;
        }
    
        /** Clears the file and sets the offset to 0 */
        public void resetFile() throws IOException {
            throw new IllegalArgumentException("read only!"); // NOI18N
        }
    
        /**
         * Sets the file-pointer offset, measured from the beginning of this
         * file, at which the next read or write occurs.
         */    
        public void seek(long pos) throws IOException {
            actOff = (int)pos;
        }

        /** Gets InputStream prepared for reading from <code>off</code> offset position*/
        private InputStream getStream(int off) throws IOException {
	    if (streamOff > off && stream != null) {
		stream.close();
		stream = null;
	    }
	    
            if(stream == null) {
		stream = url.openStream();
		streamOff = 0;
	    }
	    
	    while (streamOff < off) {
		long len = stream.skip(off - streamOff);
		streamOff += (int)len;
		if (len == 0) throw new IOException("EOF"); // NOI18N
	    }

	    return stream;
        }    
    
        public int getFileLength() {
	    try {
		int l =  url.openConnection().getContentLength();
		return l;
	    } catch (IOException e) {
		return 0;
	    }
        }
    
        public String toString() {
            return url.toString();
        }
    }
}
