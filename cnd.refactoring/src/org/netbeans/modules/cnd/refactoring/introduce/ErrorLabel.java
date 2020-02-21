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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.refactoring.introduce;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.openide.util.ImageUtilities;

/**
 * A special label that displays an error message if the text in the document it is
 * tracking is not valid.
 * 
 */
class ErrorLabel extends JLabel {
    
    /**
     * Property that is fired when the valid/invalid status of the tracked text document changes.
     */
    public static final String PROP_IS_VALID = "isValid"; // NOI18N
    
    private Document document;
    private Validator validator;
    private boolean isValid = true;
    
    /** Creates a new instance of InputErrorDisplayer 
     * @param doc Document to track for editing changes, e.g. from a JTextField
     * @param validator The logic that decides whether the text is valid or not.
     */
    public ErrorLabel( Document doc, Validator validator ) {
        setText( null );
        setIcon( null );
        
        assert null != doc;
        assert null != validator;
        
        this.document = doc;
        this.validator = validator;
        revalidateText();
        
        doc.addDocumentListener( new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent arg0) {
                revalidateText();
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                revalidateText();
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                revalidateText();
            }
        });
    }
    
    /**
     * @return True if the text in the tracked document is valid.
     */
    public boolean isInputTextValid() {
        return isValid;
    }
    
    protected void revalidateText() {
        boolean oldStatus = isValid;
        String errMessage = null;
        try     {
            errMessage = validator.validate( document.getText( 0, document.getLength() ) );
        } catch (BadLocationException ex) {
            //ignore
            return;
        }
        isValid = errMessage == null;
        setText( errMessage );
        setIcon( null == errMessage || "".equals(errMessage)? null : getErrorIcon() );
            
        firePropertyChange( PROP_IS_VALID, oldStatus, isValid);
    }
    
    protected Icon getErrorIcon() {
        return ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/refactoring/resources/error-glyph.gif", false); // NOI18N
    }
    
    /**
     * Validates the given text.
     */
    public static interface Validator {
        /**
         * Check the given text and return error message if the text is not valid or null if there are no errors.
         * @param text Text to be checked for errors.
         * @return Error message to be displayed to the user or null if there no errors in the document.
         */
        public String validate( String text );
    }
}
