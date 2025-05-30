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
package org.netbeans.modules.xml.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.net.*;

import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.*;
import org.openide.nodes.*;
import org.openide.cookies.*;
import org.openide.windows.*;


import org.netbeans.api.xml.cookies.*;
import org.netbeans.modules.xml.util.Util;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;

/**
 * Provides InputOutput UI for CheckXMLCookie.
 * <p>
 * Implementation: <code>display</code> method samples <code>Line</code> where error occured so
 * further modifications (fixes) do not affect it and installs a InputOutput 
 * line handler for SAXParseErrors.
 *
 * @author  Petr Kuzel
 * @see     InputOutput
 * @deprecated XML tools actions API candidate
 */
@Deprecated
public final class InputOutputReporter implements CookieObserver {

    //0 extends message, 1 line number, 2 url of external entity
    private final String FORMAT = "{0} [{1}] {2}";                              // NOI18N

    private String ioName;

    private DataObject dataObject;

    // remember all attached annotations
    private static final Set hyperlinks = 
        Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap())); // Set<Hyperlink>
    
    /** 
     * Creates new InputOutputReporter regirecting ProcessorListener
     * to InputOutput. To finish per call initialization setNode()
     * must be called.
     */
    public InputOutputReporter() {        
        this(Util.THIS.getString(InputOutputReporter.class, "TITLE_XML_check_window"));
    }

    public InputOutputReporter(String name) {
        initInputOutput(name);
    }

    /**
     * Somehow helps to properly link to external entities.
     * XXX But actual test case is not know.
     */
    public void setNode(Node node) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("InputOutputReporter.setNode: " + node, new RuntimeException ("Who calls InputOutputReporter.setNode"));

        dataObject = node.getCookie(DataObject.class);
    }
    
    /**
     * Associated data object accessor
     */
    private DataObject dataObject() {
        return dataObject;
    }

    public void receive(CookieMessage msg) {
        Object detail = msg.getDetail(XMLProcessorDetail.class);

        if ( Util.THIS.isLoggable() ) /* then */ {
            Util.THIS.debug ("InputOutputReporter.receive:");
            Util.THIS.debug ("    dataObject = " + dataObject);
            Util.THIS.debug ("    Message = " + msg);
            Util.THIS.debug ("    detail  = " + detail);
            if ( detail == null ) {
                Util.THIS.debug (new RuntimeException ("Message's Detail is _null_!!!"));
            }
        }

        if (detail instanceof XMLProcessorDetail) {
            display(dataObject(), msg.getMessage(), (XMLProcessorDetail) detail);
        } else {
            message(msg.getMessage());
        }
    }
    
    
    /**
     * Display plain message in output window.
     */
    public void message(String message) {
        out().println(message);
    }
    
    /**
     * Try to move InputOutput to front. Suitable for first message.
     */
    public final void moveToFront() {
        moveToFront(false);
    }

    /**
     * Try to move InputOUtput to front. Suitable for first and last messages.
     * @param lastMessage if true close OutputWriter relation
     */
    public final void moveToFront(boolean lastMessage) {
        boolean wasFocusTaken = tab().isFocusTaken();
        tab().select();
        tab().setFocusTaken(true);
        out().write("\r");
        tab().setFocusTaken(wasFocusTaken);
        if (lastMessage) {
            out().close();
        }
    }

    /** Show using SAX parser error format */
    private void display(DataObject dobj, String message, XMLProcessorDetail detail) {
        // resolve actual data object that caused exception
        // it may differ from XML document for external entities
        
        DataObject actualDataObject = null;  
        try {
            String systemId = detail.getSystemId();
            URL url = new URL (systemId);
            FileObject fos = URLMapper.findFileObject(url);
            if (fos != null) {
                actualDataObject = DataObject.find(fos);
            }

            if ( Util.THIS.isLoggable() ) /* then */ {
                Util.THIS.debug ("InputOutputReporter.display: " + message);
                Util.THIS.debug ("    systemId = " + detail.getSystemId());
                Util.THIS.debug ("    url = " + url);
                Util.THIS.debug ("    fos = " + fos);
            }
        } catch (MalformedURLException ex) {
            // we test for null
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (ex);
        } catch (DataObjectNotFoundException ex) {
            // we test for null            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (ex);
        }

        // external should contain systemID for unresolned external entities
        
        String external = "";                                                   // NOI18N
        
        if (actualDataObject == null) {
            external = detail.getSystemId();
        }
        
        
        display (
            actualDataObject, message, external,
            detail.getLineNumber(),
            detail.getColumnNumber()
        );
    }


    /** Show it in output tab formatted and with attached  controller. */
    private void display(DataObject dobj, String message, String ext, int line, int col) {
        
        String text = null;
        if (line >= 0) { 
            Object[] args = new Object[] {
                                message,
                                Integer.valueOf(line),
                                ext
                            };

            text = MessageFormat.format(FORMAT, args);
        } else {  
            // unknown line so attach controller to file only
            text = message;
        }

        if (dobj == null) {
            out().println(text);     // print without controller
        } else {
            try {
                Hyperlink ec = new Hyperlink (
                    text,
                    dobj,
                    Math.max(line - 1, 0),
                    Math.max(col - 1, 0)
                );
                out().println(text, ec);
            } catch (IOException catchIt) {
                out().println(text);     // print without controller
            }
        }
    }

    /** Set output writer used by this displayer.
    * Share existing, clear content on reuse.
    */
    private void initInputOutput (String name) {
        ioName = name;
        tab().setFocusTaken (false);

        // clear previous output
        try {
            out().reset();
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    private OutputWriter out() {
        return tab().getOut();
    }

    private InputOutput tab() {
        return IOProvider.getDefault().getIO(ioName, false);
    }

    /**
     * Release all annotations attached by this class
     */
    public static void releaseAllAnnotations() {
        synchronized (hyperlinks) {
            Iterator it = hyperlinks.iterator();
            while (it.hasNext()) {
                ((Hyperlink)it.next()).detach();
            }
            hyperlinks.clear();
        }
    }
            
    private static class Hyperlink extends Annotation implements OutputListener, PropertyChangeListener {
        
        /** sampled line containing the error */
        private Line xline;

        /** original column with the err or -1 */
        private int column;

        private final String message;
        
        public Hyperlink (String message, DataObject data, int line, int column) throws IOException {
            this.column = column;
            this.message = message;
            LineCookie cookie = data.getCookie(LineCookie.class);
            if (cookie == null) {
                throw new java.io.FileNotFoundException ();
            } else {
                xline = cookie.getLineSet ().getCurrent(line);
            }
        }

        public void outputLineSelected (OutputEvent ev) {
            try {
                markError();
                show (Line.SHOW_TRY_SHOW);
            } catch (IndexOutOfBoundsException ex) {
            } catch (ClassCastException ex) {
                // This is hack because of CloneableEditorSupport error -- see CloneableEditorSupport:1193
            }
        }

        public void outputLineAction (OutputEvent ev) {            
            try {  
                markError();
                show(Line.SHOW_GOTO);
            } catch (IndexOutOfBoundsException ex) {
            } catch (ClassCastException ex) {
                // This is hack because of CloneableEditorSupport error -- see CloneableEditorSupport:1193
            }
        }

        public void outputLineCleared (OutputEvent ev) {
            hyperlinks.remove(this);
            detach();
        }
        
        protected void notifyDetached(Annotatable ann) {
            ann.removePropertyChangeListener(this);
        }

        protected void notifyAttached(Annotatable ann) {
            ann.addPropertyChangeListener(this);
        }
        
        /**
         * Prepare annotation target
         */
        private Annotatable createAnnotatable() {
//            if (column < 1 ) {
                return xline;
//            } else {
                  // I have never got proper property changes on Line.Part                
//                return xline.createPart(0, column - 1);
//            }            
        }
        
        // open document in editor
        private void show(int mode) {            
            if (column == -1) {
                xline.show(mode);
            } else {
                xline.show(mode, column);
            }                
        }
        
        // we need to have one error at time
        private void markError() {            
            releaseAllAnnotations();
            hyperlinks.add(this);
            attach(createAnnotatable());
        }
        
        /** 
         * Returns name of the file which describes the annotation type.
         * The file must be defined in module installation layer in the
         * directory "Editors/AnnotationTypes"
         */
        public String getAnnotationType() {
            return "org-netbeans-modules-xml-error";                       // NOI18N
        }
        
        /** 
         * Returns the tooltip text for this annotation.
         */
        public String getShortDescription() {
            return message;
        }
        
        // Affected line has changed.
        public void propertyChange(PropertyChangeEvent ev) {
            String prop = ev.getPropertyName();
            if (prop == null ||
                    prop.equals(Annotatable.PROP_TEXT) ||
                    prop.equals(Annotatable.PROP_DELETED)) {                
                // Assume user has edited & corrected the error (or at least we do
                // nok know error column position anymore).
                column = -1;
                hyperlinks.remove(this);
                detach();
            }
        }        
    }

}
