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
package org.netbeans.test.j2ee.multiview;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.AssertionFailedErrorException;

/**
 *
 * @author blaha
 */
public class TestDocumentListener implements DocumentListener {

    private String findText;

    public TestDocumentListener(String findText) {
        this.findText = findText;
    }

    @Override
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
    }

    @Override
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        fireEvent(e);
    }

    @Override
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        fireEvent(e);
    }

    public void fireEvent(DocumentEvent e) throws RuntimeException {
        try {
            Document document = (Document) e.getDocument();
            String text = document.getText(0, document.getLength());
            int index = text.indexOf(findText);
            if (index < 0) { // don't find the text in document
                throw new AssertionFailedError("Cannot find correct element " + findText + "in XML view (editor document)");
            }
            document.removeDocumentListener(this);
            System.out.println("Found text: " + findText + ", index: " + index);
        } catch (javax.swing.text.BadLocationException ex) {
            throw new AssertionFailedErrorException("Failed to read the document: ", ex);
        }
    }
}
