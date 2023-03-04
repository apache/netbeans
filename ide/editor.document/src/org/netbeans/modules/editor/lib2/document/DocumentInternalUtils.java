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
package org.netbeans.modules.editor.lib2.document;

import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.lib.editor.util.CharSequenceUtilities;

/**
 *
 * @author Miloslav Metelka
 */
public class DocumentInternalUtils {

    private DocumentInternalUtils() {
        // no instances
    }

    public static Element customElement(Document doc, int startOffset, int endOffset) {
        return new CustomRootElement(doc, startOffset, endOffset);
    }

    private static final class CustomElement extends AbstractPositionElement {

        CustomElement(Element parent, int startOffset, int endOffset) {
            super(parent, startOffset, endOffset);
            CharSequenceUtilities.checkIndexesValid(startOffset, endOffset,
                    parent.getDocument().getLength() + 1);
        }

        @Override
        public String getName() {
            return "CustomElement";
        }

    }


    private static final class CustomRootElement extends AbstractRootElement<CustomElement> {

        private final CustomElement customElement;

        public CustomRootElement(Document doc, int startOffset, int endOffset) {
            super(doc);
            customElement = new CustomElement(this, startOffset, endOffset);
        }

        @Override
        public String getName() {
            return "CustomRootElement";
        }

        @Override
        public Element getElement(int index) {
            if (index == 0) {
                return customElement;
            } else {
                return null;
            }
        }

        @Override
        public int getElementCount() {
            return 1;
        }

    }
}
