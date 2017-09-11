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
