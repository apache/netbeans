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
package org.netbeans.modules.php.blade.editor.typinghooks;

import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.php.blade.editor.BladeLanguage;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.blade.editor.lexer.BladeTokenId;
import static org.netbeans.modules.php.blade.editor.lexer.BladeTokenId.HTML;
import org.netbeans.modules.php.blade.editor.preferences.ModulePreferences;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 * auto complete for '[', '(', '\'', '"' and blade tags
 *
 * @author bhaidu
 */
public class BladeTypedTextInterceptor implements TypedTextInterceptor {

    private static final Map<Character, Character> CHAR_PAIR = new WeakHashMap<>();

    public static enum TagType {
        CONTENT,
        RAW,
        COMMENT
    }

    /**
     * auto complete char pair
     */
    static {
        CHAR_PAIR.put('(', ')');
        CHAR_PAIR.put('[', ']');
        CHAR_PAIR.put('\'', '\'');
        CHAR_PAIR.put('"', '"');
    }

    @Override
    public boolean beforeInsert(Context cntxt) throws BadLocationException {
        return false;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        if (context.getReplacedText().length() != 0) {
            return;
        }

        char ch = context.getText().charAt(0);

        if (CHAR_PAIR.containsKey(ch)) {
            completePairChar(context, ch, CHAR_PAIR.get(ch));
            return;
        }

        if (!isAutoTagCompletionEnabled()) {
            return;
        }

        String typedText = context.getText();

        TagType tagType = getTagType(typedText);

        if (tagType == null) {
            return;
        }

        int offset = context.getOffset();

        if (offset < 1) {
            return;
        }

        Document document = context.getDocument();
        TokenHierarchy th = TokenHierarchy.get(document);
        TokenSequence<?> ts = th.tokenSequence();
        ts.move(context.getOffset() - 1);
        ts.moveNext();

        Token<?> token = ts.token();

        if (token == null || !(token.id() instanceof BladeTokenId)) {
            return;
        }

        BladeTokenId bladeToken = (BladeTokenId) token.id();

        String tokenText = token.text().toString();

        if (bladeToken.equals(HTML)
                || tokenText.equals("{{") && ch == '-') { // NOI18N 
            completeFromHtmlFragments(tokenText, context, tagType);
        }
    }

    /**
     * simple char context completion
     *
     * @param context
     * @param chopen
     * @param chclose
     */
    private void completePairChar(MutableContext context, char chopen, char chclose) {
        StringBuilder sb = new StringBuilder();
        sb.append(chopen);
        sb.append(chclose);
        String text = sb.toString();
        context.setText(text, 1);
    }

    @Override
    public void afterInsert(Context cntxt) throws BadLocationException {

    }

    @Override
    public void cancelled(Context cntxt) {

    }

    public boolean isAutoTagCompletionEnabled() {
        return ModulePreferences.isAutoTagCompletionEnabled();
    }

    private TagType getTagType(String typedText) {

        return switch (typedText) {
            case "{" -> // NOI18N
                TagType.CONTENT;
            case "!" -> // NOI18N  
                TagType.RAW;
            case "-" -> // NOI18N 
                TagType.COMMENT;
            default ->
                null;
        };
    }

    private void completeFromHtmlFragments(String tokenText, MutableContext context, TagType tagType) {
        switch (tokenText) {
            case "{" -> // NOI18N 
                completeContentTag(context, tagType);
            case "{!" -> // NOI18N 
                completeRawContentTag(context, tagType);
            case "{{" -> // NOI18N 
                completeCommenTag(context, tagType, "-- --}}");
            case "{{-" -> // NOI18N 
                completeCommenTag(context, tagType, "- --}}");
        }
    }

    private void completeContentTag(MutableContext context, TagType tagType) {
        if (tagType != TagType.CONTENT) {
            return;
        }
        context.setText("{ }}", 1);// NOI18N
    }

    private void completeRawContentTag(MutableContext context, TagType tagType) {
        if (tagType != TagType.RAW) {
            return;
        }
        context.setText("! !!}", 1);// NOI18N
    }

    private void completeCommenTag(MutableContext context, TagType tagType, String completeText) {
        if (tagType != TagType.COMMENT) {
            return;
        }
        context.setText(completeText, 1);// NOI18N
    }

    @MimeRegistrations({
        @MimeRegistration(mimeType = BladeLanguage.MIME_TYPE, service = TypedTextInterceptor.Factory.class),
        @MimeRegistration(mimeType = "text/html", service = TypedTextInterceptor.Factory.class)
    })
    public static class Factory implements TypedTextInterceptor.Factory {

        @Override
        public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
            return new BladeTypedTextInterceptor();
        }

    }

}
