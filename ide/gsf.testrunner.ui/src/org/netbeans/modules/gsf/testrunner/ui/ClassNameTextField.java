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

package org.netbeans.modules.gsf.testrunner.ui;

import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

/**
 * Text-field that validates whether its text is a valid class name (may be
 * package-classified or not) and may notify a registered listener of status
 * changes (empty/valid/invalid).
 * <p>
 * To start listening on validity of the entered class name, register
 * a <code>ChangeListener</code>.
 * <p>
 * Example:
 * <pre><code>     ...
 *     final ClassNameTextField tfClassName = new ClassNameTextField();
 *     tfClassName.setChangeListener(new ChangeListener() {
 *         public void stateChanged(ChangeEvent e) {
 *              int state = tfClassName.getState();
 *              switch (state) {
 *                  case ClassNameTextField.STATUS_EMPTY:
 *                      System.out.println("Empty class name!");
 *                      break;
 *                  case ClassNameTextField.STATUS_INVALID:
 *                      System.out.println("Invalid class name!");
 *                      break;
 *                  case ClassNameTextField.VALID:
 *                      System.out.println("Thank you!");
 *                      break;
 *              }
 *         }
 *     });
 *     panel.add(tfClassName);
 *     ...
 * </code></pre>
 *
 * @author  Marian Petras
 */
public final class ClassNameTextField extends JTextField {
    
    /** status: the class name is valid */
    public static final int STATUS_VALID = 0;
    /** status: the class name is empty */
    public static final int STATUS_EMPTY = 1;
    /** status: the class name is not valid */
    public static final int STATUS_INVALID = 2;
    /** status: the class name is valid but not a default*/
    public static final int STATUS_VALID_NOT_DEFAULT = 3;
    /** status: the class name is valid but not ends with 'Test' nor 'IT'*/
    public static final int STATUS_VALID_END_NOT_TEST = 4;

    /**
     * internal status - when the text is empty or when it ends with a dot
     * (<code>'.'</code>) and appending one legal character to it would make
     * it a legal class name
     */
    public static final int STATUS_BEFORE_PART = 3;
    
    /** */
    private TextListener documentListener;
    /** */
    private int externalStatus = 0;
    /** */
    private boolean externalStatusValid = false;
    /** */
    private ChangeListener changeListener;
    /** */
    private ChangeEvent changeEvent;
    /** */
    private String defaultText;

    /**
     * Creates an empty text-field.
     */
    public ClassNameTextField() {
        this((String) null);
        setupDocumentListener();
    }
    
    /**
     * Creates an empty with initial text.
     *
     * @param  text  initial text of the text-field
     *               (for empty text, use <code>&quot;&quot;</code>
     *               or <code>null</code>)
     */
    public ClassNameTextField(String text) {
        super(text == null ? "" : text);                                //NOI18N
        setupDocumentListener();
    }

    @Override
    protected Document createDefaultModel() {
        PlainDocument doc = (PlainDocument) super.createDefaultModel();
        doc.setDocumentFilter(new SpaceIgnoringDocumentFilter());
        return doc;
    }
    
    /**
     */
    public void setDefaultText(String defaultText) {
        if ((defaultText == null) && (this.defaultText == null)
             || (defaultText != null) && defaultText.equals(this.defaultText)) {
            return;
        }
        
        this.defaultText = defaultText;
        
        if ((defaultText != null)
                || (externalStatusValid
                    && (externalStatus == STATUS_VALID_NOT_DEFAULT))) {
            statusMaybeChanged();
        }
    }
    
    /**
     */
    private void setupDocumentListener() {
        getDocument().addDocumentListener(
                documentListener = new TextListener());
    }

    /**
     * Determines internal status for the current text.
     * The status may be one of
     * <code>STATUS_VALID</code>, <code>STATUS_INVALID</code> and
     * <code>STATUS_BEFORE_PART</code>.
     *
     * @return  status for the current text
     */
    public int determineStatus() {
        String text = getText();
        
        int status = STATUS_BEFORE_PART;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (status) {
                case STATUS_BEFORE_PART:
                    if (!Character.isJavaIdentifierStart(c)) {
                        return STATUS_INVALID;
                    }
                    status = STATUS_VALID;
                    break;
                case STATUS_VALID:
                    if (c == '.') {
                        status = STATUS_BEFORE_PART;
                    } else if (Character.isJavaIdentifierPart(c)) {
                        status = STATUS_VALID;
                    } else {
                        return STATUS_INVALID;
                    }
                    break;
                default:
                    assert false;
            }
        }
        return status;
    }
    
    /**
     * Returns status of the text.
     *
     * @return  one of <code>STATUS_EMPTY</code>,
     *                 <code>STATUS_VALID</code>,
     *                 <code>STATUS_INVALID</code>
     */
    public int getStatus() {
        if (!externalStatusValid) {
            updateExternalStatus();
        }
        return externalStatus;
    }

    /**
     */
    private void updateExternalStatus() {
        assert externalStatusValid == false;

        int internalStatus = documentListener.status;
        switch (internalStatus) {
            case STATUS_VALID:
                if(!getText().endsWith("Test") && !getText().endsWith("IT")){                        //NOI18N
                    externalStatus = STATUS_VALID_END_NOT_TEST;
                }else if((defaultText != null) && !defaultText.equals(getText())){
                    externalStatus = STATUS_VALID_NOT_DEFAULT;
                }else{
                    externalStatus = STATUS_VALID;
                }
                break;
            case STATUS_BEFORE_PART:
                externalStatus = (getText().length() == 0) ? STATUS_EMPTY
                                                           : STATUS_INVALID;
                break;
            case STATUS_INVALID:
                externalStatus = STATUS_INVALID;
                break;
            default:
                assert false;
                externalStatus = STATUS_INVALID;
                break;
        }
        externalStatusValid = true;
    }
    
    /**
     * Registers a change listener.
     * The listener will be notified each time status of this text-field
     * (valid/invalid/empty) changes.
     *
     * <!-- PENDING: The listener cannot be unregistered. -->
     * <!-- PENDING: Only one listener can be registered at a time. -->
     *
     * @param  listener  change listener to be registered
     * @see  #getStatus
     */
    public void setChangeListener(ChangeListener listener) {
        changeEvent = new ChangeEvent(this);
        this.changeListener = listener;
    }
    
    /**
     */
    private void statusMaybeChanged() {
        externalStatusValid = false;

        if (changeListener != null) {
            final int prevExternalStatus = externalStatus;
            externalStatus = getStatus();
            if (externalStatus != prevExternalStatus) {
                changeListener.stateChanged(changeEvent);
            }
        }
    }
    
    /**
     */
    private final class TextListener implements DocumentListener {

        /** internal status of the class name */
        private int status;
        /** */
        private int length;

        /**
         */
        public TextListener() {
            status = determineStatus();
            length = ClassNameTextField.this.getText().length();
        }

        /**
         */
        public void changedUpdate(DocumentEvent documentEvent) {
            length = documentEvent.getDocument().getLength();
            int newStatus = determineStatus();

            if (newStatus != status) {
                status = newStatus;
                statusMaybeChanged();
            } else if ((status == STATUS_VALID) && (defaultText != null)) {
                statusMaybeChanged();     //maybe default <--> not default
            }

            assert length == getDocument().getLength();
        }

        /**
         */
        public void insertUpdate(DocumentEvent documentEvent) {
            int newStatus;
            boolean wasEmpty = (length == 0);

            if (documentEvent.getLength() != 1
                    || (documentEvent.getOffset() != length)) {
                length += documentEvent.getLength();
                newStatus = determineStatus();
            } else {
                char c;

                /* now we know that a single character was appended */
                try {
                    c = documentEvent.getDocument().getText(length++, 1)
                        .charAt(0);
                    switch (status) {
                        case STATUS_VALID:
                            newStatus = (c == '.')
                                        ? newStatus = STATUS_BEFORE_PART
                                        : (Character.isJavaIdentifierPart(c))
                                          ? STATUS_VALID
                                          : STATUS_INVALID;
                            break;
                        case STATUS_BEFORE_PART:
                            newStatus = (Character.isJavaIdentifierStart(c))
                                        ? STATUS_VALID
                                        : STATUS_INVALID;
                            break;
                        case STATUS_INVALID:
                            newStatus = determineStatus();
                            break;
                        default:
                            assert false;
                            newStatus = determineStatus();
                            break;
                    }
                } catch (BadLocationException ex) {
                    assert false;
                    
                    length = documentEvent.getDocument().getLength();
                    newStatus = determineStatus();
                }
            }

            /*
             * We must handle addition of a text to an empty text field
             * specially because it may not change internal state
             * (if it becomes STATUS_BEFORE_PART after the addition).
             */
            if ((newStatus != status) || wasEmpty) {
                status = newStatus;
                statusMaybeChanged();
            } else if ((status == STATUS_VALID) && (defaultText != null)) {
                statusMaybeChanged();     //maybe default <--> not default
            }

            assert length == getDocument().getLength();
        }

        /**
         */
        public void removeUpdate(DocumentEvent documentEvent) {
            int newStatus;

            if (documentEvent.getLength() != 1
                    || (documentEvent.getOffset() != (length - 1))) {
                length -= documentEvent.getLength();
                newStatus = determineStatus();
            } else {

                /*
                 * now we know that a single character was deleted
                 * from the end
                 */
                length--;
                switch (status) {
                    case STATUS_VALID:
                        try {
                            newStatus = ((length == 0)
                                         || (documentEvent.getDocument()
                                             .getText(length - 1, 1))
                                             .charAt(0) == '.')
                                        ? STATUS_BEFORE_PART
                                        : STATUS_VALID;
                        } catch (BadLocationException ex) {
                            assert false;
                            
                            newStatus = determineStatus();
                            length = documentEvent.getDocument().getLength();
                        }
                        break;
                    case STATUS_BEFORE_PART:
                        newStatus = STATUS_VALID;       //trailing dot deleted
                        break;
                    case STATUS_INVALID:
                        newStatus = (length == 0) ? STATUS_VALID
                                                  : determineStatus();
                        break;
                    default:
                        assert false;
                        newStatus = determineStatus();
                        break;
                }
            }

                
            /*
             * We must handle deletion of the whole text specially because
             * it may not change internal state (if it was STATUS_BEFORE_PART
             * before the deletion).
             */
            if ((newStatus != status) || (length == 0)) {
                status = newStatus;
                statusMaybeChanged();
            } else if ((status == STATUS_VALID) && (defaultText != null)) {
                statusMaybeChanged();     //maybe default <--> not default
            }

            assert length == getDocument().getLength();
        }

    }

    /**
     * {@code DocumentFilter} that ignores (does not allow to insert) space
     * characters.
     */
    private static final class SpaceIgnoringDocumentFilter extends DocumentFilter {

        @Override
        public void insertString(DocumentFilter.FilterBypass fb,
                                 int offset,
                                 String string,
                                 AttributeSet attr) throws BadLocationException {
            String strToAdd = removeSpaces(string);
            if (strToAdd != null) {
                super.insertString(fb, offset, strToAdd, null);
            }
        }

        @Override
        public void replace(FilterBypass fb,
                            int offset,
                            int length,
                            String text,
                            AttributeSet attrs) throws BadLocationException {
            String strToAdd = removeSpaces(text);
            if (strToAdd == null) {
                super.remove(fb, offset, length);
            } else {
                super.replace(fb, offset, length, strToAdd, null);
            }
        }

        private String removeSpaces(String str) {
            int length = str.length();
            if (length == 0) {
                return null;
            } else if (length == 1) {
                return (str.charAt(0) == ' ') ? null : str;
            } else {
                StringBuilder buf = null;
                for (int i = 0; i < length; i++) {
                    if (str.charAt(i) != ' ') {
                        if (buf != null) {
                            buf.append(str.charAt(i));
                        }
                    } else if (buf == null) {
                        buf = new StringBuilder(str.length() - 1);
                        buf.append(str.substring(0, i));
                    }
                }
                return (buf == null) ? str
                                     : (buf.length() != 0) ? buf.toString()
                                                           : null;
            }
        }
    }
    
}
