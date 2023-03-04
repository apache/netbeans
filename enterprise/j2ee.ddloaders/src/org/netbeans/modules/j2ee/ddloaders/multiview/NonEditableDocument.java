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
package org.netbeans.modules.j2ee.ddloaders.multiview;

import javax.swing.text.PlainDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;

/**
 * @author pfiala
 */
public abstract class NonEditableDocument extends PlainDocument {

    String text = null;

    protected abstract String retrieveText();

    protected NonEditableDocument() {
        init();
    }

    public void init() {
        String s = retrieveText();
        if (s == null) {
            s = "";
        }
        if (!s.equals(text)) {
            text = s;
            try {
                super.remove(0, super.getLength());
                super.insertString(0, s, null);
            } catch (BadLocationException e) {

            }
        }
    }

    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {

    }

    public void remove(int offs, int len) throws BadLocationException {

    }
}
