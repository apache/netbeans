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


package org.netbeans.modules.j2ee.jpa.refactoring.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import javax.swing.text.Position.Bias;
import org.openide.nodes.Node;
import org.openide.text.PositionRef;
import org.openide.util.Parameters;

/**
 * Resolves position bounds of an element that is being refactored. If the
 * <code>elementName</code> is null or no matching string can be found in the data
 * object, returns <code>PositionBounds</code> that represents the beginning of the file.
 *
 * @author Erno Mononen
 */
public class PositionBoundsResolver {
    
    /**
     * The data object that is being refactored.
     */
    private final DataObject dataObject;
    /**
     * The editor support associated with the data object.
     */
    private final CloneableEditorSupport editorSupport;
    
    /**
     * Name of the element that is being refactored. If the element represents class,
     * it should be its fully qualified name.
     */
    private final String elementName;
    
    /**
     * Creates a new resolver for the given <code>dataObject</code>.
     * @param dataObject the data object that is being refactored. Must have an
     *  associated <code>CloneableEditorSupport</code> and must not be null.
     * @param elementName the element that is being refactored. If the element represents class,
     * it should be its fully qualified name. If a <code>null</code> is given, {@link #getPositionBounds}
     * returns PositionBounds that represents the beginning of the file.
     * @throws IllegalArgumentException if the given <code>dataObject</code> was null
     *  or if didn't have CloneableEditorSupport associated.
     */
    public PositionBoundsResolver(DataObject dataObject, String elementName) {
        Parameters.notNull("dataObject", dataObject);
        this.dataObject = dataObject;
        this.editorSupport = findCloneableEditorSupport();

        if (this.editorSupport == null){
            throw new IllegalArgumentException("Couldn't get CloneableEditorSupport for " + dataObject); //NO18N
        }
        this.elementName = elementName;
    }
    
    /**
     *@return PositionBounds representing the position of the name of the entity that is being
     * refactored or PostionBounds representing the start of the file if the position
     * of the entity could not be resolved.
     */
    public PositionBounds getPositionBounds(){
        if (elementName != null){
            try {
                BaseDocument doc = getDocument();
                String text = doc.getText(0, doc.getLength());
                int offset = text.indexOf(elementName);
                if (offset > -1){
                    PositionRef start = editorSupport.createPositionRef(offset, Bias.Forward);
                    PositionRef end = editorSupport.createPositionRef(offset + elementName.length(), Bias.Backward);
                    return new PositionBounds(start, end);
                }
            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        return getDefaultPositionBounds();
    }
    
    /**
     * @return PositionBounds representing the beginning of the file associated
     * with the <code>dataObject</code>.
     */
    private PositionBounds getDefaultPositionBounds(){
        PositionRef start = editorSupport.createPositionRef(0, Bias.Forward);
        PositionRef end = editorSupport.createPositionRef(0, Bias.Backward);
        return new PositionBounds(start, end);
    }
    
    
    // adapted from JSFEditorUtilities
    private CloneableEditorSupport findCloneableEditorSupport() {
        Node.Cookie obj = dataObject.getCookie(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        obj = dataObject.getCookie(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        return null;
    }
    
    /**
     * @return the BaseDocument representing contents of our <code>dataObject</code>.
     */
    private BaseDocument getDocument(){
        BaseDocument result = (BaseDocument) editorSupport.getDocument();
        // editor was not opened
        if (result == null) {
            final CreateXMLPane runnable = new CreateXMLPane();
            try {
                if (SwingUtilities.isEventDispatchThread()){
                    runnable.run();
                } else {
                    SwingUtilities.invokeAndWait(runnable);
                }
                result = new BaseDocument(runnable.getPane().getEditorKit().getClass(), false);
                String text= readResource(dataObject.getPrimaryFile().getInputStream());
                result.remove(0, result.getLength());
                result.insertString(0, text, null);
            } catch (InterruptedException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (InvocationTargetException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (FileNotFoundException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        return result;
        
    }
    
    /**
     * Reads the given stream.
     */
    private String readResource(InputStream stream){
        StringBuffer result = new StringBuffer();
        String lineSep = System.getProperty("line.separator");//NO18N
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));//NO18N
            String line = reader.readLine();
            while (line != null) {
                result.append(line);
                result.append(lineSep);
                line = reader.readLine();
            }
        } catch (UnsupportedEncodingException ex) {
            ErrorManager.getDefault().notify(ex);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        } finally {
            try {
                if (reader != null){
                    reader.close();
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        return result.toString();
    }
    
    // copied from JSFFrameworkProvider
    private static class CreateXMLPane implements Runnable{
        JEditorPane pane;
        
        public void run(){
            pane = new JEditorPane("text/xml", "");
        }
        
        public JEditorPane getPane(){
            return pane;
        }
    }
    
}
