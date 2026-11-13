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
package org.netbeans.modules.xml.text.typinghooks;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.Acceptor;
import org.netbeans.modules.xml.text.ComplexValueSettingsFactory;
import org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor;
import org.openide.util.NbPreferences;

/**
 * Camel-case navigation for XML text, tag names, and attribute values.
 * A boundary is reported only when it falls inside an identifier run; punctuation
 * and markup stay on the default word scan.
 */
public class XmlCamelCaseInterceptor implements CamelCaseInterceptor {

    private static final String CAMEL_CASE_NAVIGATION_PROPERTY = "useCamelCaseStyleNavigation"; //NOI18N

    private static boolean isUsingCamelCase() {
        return NbPreferences.root().getBoolean(CAMEL_CASE_NAVIGATION_PROPERTY, true);
    }

    @Override
    public boolean beforeChange(MutableContext context) throws BadLocationException {
        return false;
    }

    @Override
    public void change(MutableContext context) throws BadLocationException {
        if (!isUsingCamelCase()) {
            return;
        }
        Document doc = context.getDocument();
        int offset = context.getOffset();
        boolean backward = context.isBackward();
        doc.render(() -> {
            int nextOffset = getWordOffset(doc, offset, backward);
            if (nextOffset != -1) {
                context.setNextWordOffset(nextOffset);
            }
        });
    }

    @Override
    public void afterChange(MutableContext context) throws BadLocationException {
    }

    @Override
    public void cancelled(MutableContext context) {
    }

    /**
     * Offset of the next camel-case boundary, or {@code -1} when the default word scan should run.
     * Caller must hold the document read lock.
     */
    static int getWordOffset(Document doc, int offset, boolean backward) {
        TokenHierarchy<Document> hi = TokenHierarchy.get(doc);
        if (hi == null) {
            return -1;
        }
        TokenSequence<XMLTokenId> ts = hi.tokenSequence(XMLTokenId.language());
        if (ts == null) {
            return -1;
        }
        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return -1;
        }
        if (backward && ts.offset() == offset && !ts.movePrevious()) {
            return -1;
        }

        Token<XMLTokenId> token = ts.token();
        if (token == null || !isTextLike(token.id())) {
            return -1;
        }
        CharSequence image = token.text();
        if (image == null || image.length() == 0) {
            return -1;
        }
        if (backward) {
            return previousCamelCaseBoundary(image, ts.offset(), offset);
        }
        return nextCamelCaseBoundary(image, ts.offset(), offset - ts.offset());
    }

    private static boolean isTextLike(XMLTokenId id) {
        switch (id) {
            case TAG:
            case ARGUMENT:
            case TEXT:
            case VALUE:
            case PI_TARGET:
            case PI_CONTENT:
            case CDATA_SECTION:
                return true;
            default:
                return false;
        }
    }

    /**
     * Same uppercase-run rules as {@code CamelCaseOperations}, except a rejected
     * identifier character or the end of the run leaves the offset unset.
     */
    private static int nextCamelCaseBoundary(CharSequence image, int tokenStart, int offsetInImage) {
        int length = image.length();
        if (offsetInImage < 0 || offsetInImage >= length) {
            return -1;
        }
        Acceptor acceptor = identifierAcceptor();
        if (!acceptor.accept(image.charAt(offsetInImage))) {
            return -1;
        }
        int start = offsetInImage + 1;
        if (Character.isUpperCase(image.charAt(offsetInImage))) {
            for (int i = start; i < length; i++) {
                char ch = image.charAt(i);
                if (!acceptor.accept(ch) || !Character.isUpperCase(ch)) {
                    break;
                }
                start++;
            }
        }
        for (int i = start; i < length; i++) {
            char ch = image.charAt(i);
            if (!acceptor.accept(ch)) {
                return -1;
            }
            if (Character.isUpperCase(ch)) {
                return tokenStart + i;
            }
        }
        return -1;
    }

    private static int previousCamelCaseBoundary(CharSequence image, int tokenStart, int offset) {
        int offsetInImage = offset - 1 - tokenStart;
        int length = image.length();
        if (offsetInImage < 0 || offsetInImage >= length) {
            return -1;
        }
        Acceptor acceptor = identifierAcceptor();
        if (!acceptor.accept(image.charAt(offsetInImage))) {
            return -1;
        }
        if (Character.isUpperCase(image.charAt(offsetInImage))) {
            for (int i = offsetInImage - 1; i >= 0; i--) {
                char ch = image.charAt(i);
                if (!acceptor.accept(ch) || !Character.isUpperCase(ch)) {
                    return tokenStart + i + 1;
                }
            }
            return -1;
        }
        for (int i = offsetInImage - 1; i >= 0; i--) {
            char ch = image.charAt(i);
            if (!acceptor.accept(ch)) {
                return -1;
            }
            if (Character.isUpperCase(ch)) {
                for (int j = i; j >= 0; j--) {
                    char earlier = image.charAt(j);
                    if (!acceptor.accept(earlier) || !Character.isUpperCase(earlier)) {
                        return tokenStart + j + 1;
                    }
                }
                return -1;
            }
        }
        return -1;
    }

    private static Acceptor identifierAcceptor() {
        return ComplexValueSettingsFactory.getXMLIdentifierAcceptor();
    }

    @MimeRegistration(mimeType = "text/xml", service = CamelCaseInterceptor.Factory.class)
    public static final class Factory implements CamelCaseInterceptor.Factory {

        @Override
        public CamelCaseInterceptor createCamelCaseInterceptor(MimePath mimePath) {
            return new XmlCamelCaseInterceptor();
        }
    }
}
