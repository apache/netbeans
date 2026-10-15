/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.ant.debugger;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.apache.tools.ant.module.spi.AntEvent;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotatable;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
        
        
/*
 * AntTest.java
 *
 * Created on 19. leden 2004, 20:03
 */

/**
 *
 * @author  Honza
 */
public class Utils {
            
    private static final Logger logger = Logger.getLogger(Utils.class.getName());
    
    private static Object currentLine;
    
    static synchronized void markCurrent (final Object line) {
        unmarkCurrent ();
        
        Annotatable[] annotatables = (Annotatable[]) line;
        int i = 0, k = annotatables.length;
        
        // first line with icon in gutter
        DebuggerAnnotation[] annotations = new DebuggerAnnotation [k];
        if (annotatables [i] instanceof Line.Part)
            annotations [i] = new DebuggerAnnotation (
                DebuggerAnnotation.CURRENT_LINE_PART_ANNOTATION_TYPE,
                annotatables [i]
            );
        else
            annotations [i] = new DebuggerAnnotation (
                DebuggerAnnotation.CURRENT_LINE_ANNOTATION_TYPE,
                annotatables [i]
            );
        
        // other lines
        for (i = 1; i < k; i++)
            if (annotatables [i] instanceof Line.Part)
                annotations [i] = new DebuggerAnnotation (
                    DebuggerAnnotation.CURRENT_LINE_PART_ANNOTATION_TYPE2,
                    annotatables [i]
                );
            else
                annotations [i] = new DebuggerAnnotation (
                    DebuggerAnnotation.CURRENT_LINE_ANNOTATION_TYPE2,
                    annotatables [i]
                );
        currentLine = annotations;
        
        showLine (line);
    }
    
    static synchronized void unmarkCurrent () {
        if (currentLine != null) {
            
//            ((DebuggerAnnotation) currentLine).detach ();
            int i, k = ((DebuggerAnnotation[]) currentLine).length;
            for (i = 0; i < k; i++)
                ((DebuggerAnnotation[]) currentLine) [i].detach ();
            
            currentLine = null;
        }
    }
    
    static void showLine (final Object line) {
//        SwingUtilities.invokeLater (new Runnable () {
//            public void run () {
//                ((Line) line).show (Line.SHOW_GOTO);
//            }
//        });
        
        final Annotatable[] a = (Annotatable[]) line;
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                if (a [0] instanceof Line)
                    ((Line) a [0]).show (ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
                else
                if (a [0] instanceof Line.Part)
                    ((Line.Part) a [0]).getLine ().show (ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
                else
                    throw new InternalError ();
            }
        });
    }
    
    static int getLineNumber (Object line) {
//        return ((Line) line).getLineNumber ();
        
        final Annotatable[] a = (Annotatable[]) line;
        if (a [0] instanceof Line)
            return ((Line) a [0]).getLineNumber ();
        else
        if (a [0] instanceof Line.Part)
            return ((Line.Part) a [0]).getLine ().getLineNumber ();
        else
            throw new InternalError ();
    }
    
    public static boolean contains (Object currentLine, Line line) {
        if (currentLine == null) return false;
        final Annotatable[] a = (Annotatable[]) currentLine;
        int i, k = a.length;
        for (i = 0; i < k; i++) {
            if (a [i].equals (line)) return true;
            if ( a [i] instanceof Line.Part &&
                 ((Line.Part) a [i]).getLine ().equals (line)
            ) return true;
        }
        return false;
    }
    
    
    static Object getLine (
        final AntEvent event
    ) {
        File file = event.getScriptLocation ();
        final int lineNumber = event.getLine ();
        if (file == null) return null;
        if (lineNumber < 0) return null;

        FileObject fileObject = FileUtil.toFileObject (file);
        EditorCookie editor;
        LineCookie lineCookie;
        DataObject d;
        try {
            d = DataObject.find (fileObject);
        } catch (DataObjectNotFoundException donfex) {
            logger.log(Level.CONFIG, "No DataObject for "+fileObject, donfex);
            return null;
        }
        editor = d.getLookup().lookup (EditorCookie.class);
        lineCookie = d.getLookup().lookup (LineCookie.class);
        assert editor != null;
        assert lineCookie != null;

        InputSource in = null;
        try {
            StyledDocument doc = editor.openDocument ();
            in = createInputSource (fileObject, editor, doc);
        } catch (IOException ioex) {
            logger.log(Level.CONFIG, "A problem while opening "+fileObject, ioex);
        } catch (BadLocationException blex) {
            logger.log(Level.CONFIG, "A problem while opening "+fileObject, blex);
        }
        if (in == null) {
            return null;
        }
        final int[] line = new int [4];
        SAXParserFactory factory = SAXParserFactory.newInstance ();
        try {
            SAXParser parser = factory.newSAXParser ();
            class Handler extends DefaultHandler {
                private Locator locator;
                @Override
                public void setDocumentLocator (Locator l) {
                    locator = l;
                }
                @Override
                public void startElement (
                    String uri, 
                    String localname, 
                    String qname, 
                    Attributes attr
                ) throws SAXException {
                    if (line [0] == 0) {
                        if ( qname.equals (event.getTaskName ()) &&
                             locator.getLineNumber () == lineNumber
                        ) {
                            line[0] = locator.getLineNumber ();
                            line[1] = locator.getColumnNumber () - 1;
                        }
                    }
                }
                @Override
                public void endElement (
                    String uri, 
                    String localname, 
                    String qname
                ) throws SAXException {
                    if ( line [0] != 0 &&
                         line [2] == 0 &&
                         qname.equals (event.getTaskName ())
                    ) {
                        line[2] = locator.getLineNumber ();
                        line[3] = locator.getColumnNumber () - 1;
                    }
                }
            }
            parser.parse (in, new Handler ());
        } catch (IOException ioex) {
            logger.log(Level.CONFIG, "A problem while reading "+fileObject, ioex);
            return null;
        } catch (ParserConfigurationException pcex) {
            logger.log(Level.CONFIG, "A problem while parsing "+fileObject, pcex);
            return null;
        } catch (SAXException saxex) {
            logger.log(Level.CONFIG, "A problem while parsing "+fileObject, saxex);
            return null;
        }
        if (line [0] == 0) return null;
        Annotatable[] annotatables = new Annotatable [
            line [2] - line [0] + 1
        ];
        int i = 0;
        for (int ln = line [0]; ln <= line [2]; ln ++) {
            Line l = lineCookie.getLineSet ().getCurrent (ln - 1);
            annotatables [i++] = l;
        }
        return annotatables;
    }
    
    static Object getLine (
        final TargetLister.Target target, 
        String nextTargetName
    ) {
        FileObject fileObject = target.getScript ().getFileObject ();
        assert fileObject != null : "No build script for " + target.getName ();
        EditorCookie editor;
        LineCookie lineCookie;
        DataObject d;
        try {
            d = DataObject.find (fileObject);
        } catch (DataObjectNotFoundException donfex) {
            logger.log(Level.CONFIG, "No DataObject for "+fileObject, donfex);
            return null;
        }
        editor = d.getLookup().lookup (EditorCookie.class);
        lineCookie = d.getLookup().lookup (LineCookie.class);
        assert editor != null;
        assert lineCookie != null;
        InputSource in = null;
        try {
            StyledDocument doc = editor.openDocument ();
            in = createInputSource (fileObject, editor, doc);
        } catch (IOException ioex) {
            logger.log(Level.CONFIG, "A problem while opening "+fileObject, ioex);
        } catch (BadLocationException blex) {
            logger.log(Level.CONFIG, "A problem while opening "+fileObject, blex);
        }
        if (in == null) {
            return null;
        }
        final int[] line = new int [4];
        SAXParserFactory factory = SAXParserFactory.newInstance ();
        try {
            SAXParser parser = factory.newSAXParser ();
            class Handler extends DefaultHandler {
                private Locator locator;
                @Override
                public void setDocumentLocator (Locator l) {
                    locator = l;
                }
                @Override
                public void startElement (
                    String uri, 
                    String localname, 
                    String qname, 
                    Attributes attr
                ) throws SAXException {
                    if (line [0] == 0) {
                        if (qname.equals ("target") &&  // NOI18N
                            target.getName ().equals (attr.getValue ("name")) // NOI18N
                        ) {
                            line[0] = locator.getLineNumber ();
                            line[1] = locator.getColumnNumber ();
                        }
                    }
                }
                @Override
                public void endElement (
                    String uri, 
                    String localname, 
                    String qname
                ) throws SAXException {
                    if ( line [0] != 0 &&
                         line [2] == 0 &&
                         qname.equals ("target")
                    ) {
                        line[2] = locator.getLineNumber ();
                        line[3] = locator.getColumnNumber ();
                    }
                }
            }
            parser.parse (in, new Handler ());
        } catch (IOException ioex) {
            logger.log(Level.CONFIG, "A problem while reading "+fileObject, ioex);
            return null;
        } catch (ParserConfigurationException pcex) {
            logger.log(Level.CONFIG, "A problem while parsing "+fileObject, pcex);
            return null;
        } catch (SAXException saxex) {
            logger.log(Level.CONFIG, "A problem while parsing "+fileObject, saxex);
            return null;
        }
        if (line [0] == 0) return null;

        int ln = line [0] - 1;
        List<Annotatable> annotatables = new ArrayList<>();
        if (nextTargetName != null) {
            Line fLine = lineCookie.getLineSet ().getCurrent (ln);
            int inx = findIndexOf(fLine.getText (), nextTargetName);
            if (inx >= 0) {
                annotatables.add (fLine.createPart (
                    inx, nextTargetName.length ()
                ));
                ln ++;
            }
        }
        if (annotatables.size () < 1)
            for (; ln < line [2]; ln ++) {
                Line l = lineCookie.getLineSet ().getCurrent (ln);
                annotatables.add (l);
            }
        return annotatables.toArray (new Annotatable [0]);
    }
    
    private static int findIndexOf(String text, String target) {
        int index = 0;
        while ((index = text.indexOf(target, index)) > 0) {
            char c = text.charAt(index - 1);
            if (!Character.isWhitespace(c) && c != ',' && c != '\"') {
                // begins with some text => is not the target
                index++;
                continue;
            }
            if (text.length() > index + target.length()) {
                c = text.charAt(index + target.length());
                if (!Character.isWhitespace(c) && c != ',' && c != '\"') {
                    // ends with some text => is not the target
                    index++;
                    continue;
                }
            }
            break;
        }
        return index;
    }
    
    /**
     * Utility method to get a properly configured XML input source for a script.
     */
    private static InputSource createInputSource (
        FileObject fo, 
        EditorCookie editor, 
        final StyledDocument document
    ) throws IOException, BadLocationException {
        final StringWriter w = new StringWriter (document.getLength ());
        final EditorKit kit = findKit (editor);
        if (kit == null) return null;
        final IOException[] ioe = new IOException [1];
        final BadLocationException[] ble = new BadLocationException [1];
        document.render(new Runnable () {
            @Override
            public void run() {
                try {
                    kit.write (w, document, 0, document.getLength ());
                } catch (IOException e) {
                    ioe [0] = e;
                } catch (BadLocationException e) {
                    ble [0] = e;
                }
            }
        });
        if (ioe[0] != null) {
            throw ioe [0];
        } else if (ble [0] != null) {
            throw ble [0];
        }
        InputSource in = new InputSource (new StringReader (w.toString ()));
        if (fo != null) { // #10348
                in.setSystemId(fo.toURL().toExternalForm());
            // [PENDING] Ant's ProjectHelper has an elaborate set of work-
            // arounds for inconsistent parser behavior, e.g. file:foo.xml
            // works in Ant but not with Xerces parser. You must use just foo.xml
            // as the system ID. If necessary, Ant's algorithm could be copied
            // here to make the behavior match perfectly, but it ought not be necessary.
        }
        return in;
    }
    
    private static EditorKit findKit(final EditorCookie editor) {
        if (SwingUtilities.isEventDispatchThread()) {
            return findKit_(editor);
        } else {
            final EditorKit[] ek = new EditorKit[1];
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        ek[0] = findKit_(editor);
                    }
                });
            } catch (InvocationTargetException ex) {
                ErrorManager.getDefault().notify(ex.getTargetException());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            return ek[0];
        }
    }
    
    private static EditorKit findKit_(EditorCookie editor) {
        JEditorPane[] panes = editor.getOpenedPanes();
        EditorKit kit;
        if (panes != null) {
            kit = panes[0].getEditorKit ();
        } else {
            kit = JEditorPane.createEditorKitForContentType ("text/xml"); // NOI18N
            if (kit == null) {
                // #39301: fallback; can happen if xml/text-edit is disabled
                kit = new DefaultEditorKit ();
            }
        }
        assert kit != null;
        return kit;
    }
}
