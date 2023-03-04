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
package org.netbeans.modules.xml.text;

import java.awt.Point;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.netbeans.modules.xml.util.Util;
import org.openide.*;
import org.openide.cookies.*;

import org.netbeans.modules.xml.sync.*;

/**
 * Takes care about specifics of XML test representations.
 * It covers update() method.
 *
 * @author  Petr Kuzel
 * @version
 */
public class XMLTextRepresentation extends TextRepresentation {
    
    /** Creates new XMLTextRepresentation */
    public XMLTextRepresentation(TextEditorSupport editor, Synchronizator sync) {
        super(editor, sync);
    }
                    
    /*
     * Retrives editor cookie and perform text replacing holding caret and view at original
     * position if possible.
     *
     */
    public void updateText (Object input) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("XMLTextRepresentation::updateText");//, new RuntimeException ("Updating text...........")); // NOI18N

        final String in = (String) input;
        
        final EditorCookie es = editor;
        if (es == null) {
            return;
        } //es!=null

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    updateTextInAWT(es, in);
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }

            }
        });
    }
    
    private void updateTextInAWT(EditorCookie es, final String in) throws IOException, BadLocationException {
        StyledDocument tmpdoc = es.getDocument();
        if (tmpdoc == null)
            tmpdoc = es.openDocument();

        //sample editor position

        JEditorPane[] eps = es.getOpenedPanes();
        JEditorPane pane = null;
        JViewport port = null;
        int caretPosition = 0;
        Point viewPosition = null;
        if (eps != null) {
            pane = eps[0];
            caretPosition = pane.getCaretPosition();
            port = getParentViewport (pane);
            if (port != null)
                viewPosition = port.getViewPosition();
        }

        // prepare modification task

        final Exception[] taskEx = new Exception[] {null};
        final StyledDocument sdoc = tmpdoc;

        Runnable task = new Runnable() {
            public void run() {
                try {
                    sdoc.remove (0, sdoc.getLength());  // right alternative

                    // we are at Unicode level
                    sdoc.insertString (0, in, null);
                } catch (Exception iex) {
                    taskEx[0] = iex;
                }
            }
        };

        // perform document modification

        org.openide.text.NbDocument.runAtomicAsUser(sdoc, task);

        //??? setModified (true);  

        //restore editor position

        if (eps != null) {
            try {
                pane.setCaretPosition (caretPosition);
            } catch (IllegalArgumentException e) {
            }
            port.setViewPosition (viewPosition);
        }

        if (taskEx[0]!=null) {
            if (taskEx[0] instanceof IOException) {
                throw (IOException)taskEx[0];
            }
            throw new IOException(taskEx[0]);
        }
        
    }
    
    
    /**
     * Update the representation without marking it as modified.
     */
    public void update(Object change) {
        if (change instanceof String) {
            String update = (String) change;
            updateText(update);
        } 
    }

    /**
     * Is this representation modified since last sync?
     */
    public boolean isModified() {
        return false; //!!! es.isModified();
    }


    private JViewport getParentViewport (Component component) {
            if (component == null) {
                return null;
            } else if (component instanceof JViewport) {
                return (JViewport) component;
            } else {
                return getParentViewport(component.getParent());
            }
    }        
    
}
