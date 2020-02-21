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
