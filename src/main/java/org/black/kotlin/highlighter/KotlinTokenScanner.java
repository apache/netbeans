/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.black.kotlin.highlighter;

import com.intellij.psi.PsiElement;
import java.util.ArrayList;
import java.util.List;
import org.black.kotlin.builder.KotlinPsiManager;
import org.black.kotlin.highlighter.netbeans.KotlinToken;
import org.black.kotlin.highlighter.netbeans.KotlinTokenId;
import org.jetbrains.kotlin.psi.KtFile;
import org.netbeans.spi.lexer.LexerInput;

/**
 * KotlinTokenScanner parses kotlin code for tokens
 *
 * @author Александр
 *
 */
public final class KotlinTokenScanner {

    private final KotlinTokensFactory kotlinTokensFactory;

    private final KtFile ktFile;
    private List<KotlinToken<KotlinTokenId>> kotlinTokens;
    private int offset = 0;
    private int tokensNumber = 0;
    private final LexerInput input;
    private boolean initializedProperly = false;

    /**
     * Class constructor
     *
     * @param input code that must be parsed
     */
    public KotlinTokenScanner(LexerInput input) {
        kotlinTokensFactory = new KotlinTokensFactory();
        this.input = input;
        ktFile = KotlinPsiManager.INSTANCE.getParsedKtFileForSyntaxHighlighting(getTextToParse());
        if (ktFile == null) {
            return; 
        }
        createListOfKotlinTokens();
    }

    private String getTextToParse() {
        StringBuilder builder = new StringBuilder("");

        int character;

        do {
            character = input.read();
            builder.append((char) character);
        } while (character != LexerInput.EOF);

        input.backup(input.readLengthEOF());

        return builder.toString();
    }

    /**
     * This method creates an ArrayList of tokens from the parsed ktFile.
     */
    private void createListOfKotlinTokens() {
        kotlinTokens = new ArrayList<KotlinToken<KotlinTokenId>>();
        PsiElement lastElement;
        for (;;) {

            lastElement = ktFile.findElementAt(offset);

            if (lastElement != null) {
                offset = lastElement.getTextRange().getEndOffset();
                TokenType tokenType = kotlinTokensFactory.getToken(lastElement);

                kotlinTokens.add(new KotlinToken<KotlinTokenId>(
                        new KotlinTokenId(tokenType.name(), tokenType.name(), tokenType.getId()),
                        lastElement.getText(), tokenType));
                tokensNumber = kotlinTokens.size();
            } else {
                kotlinTokens.add(new KotlinToken<KotlinTokenId>(
                        new KotlinTokenId(TokenType.EOF.name(), TokenType.EOF.name(), 7), "",
                        TokenType.EOF));
                tokensNumber = kotlinTokens.size();
                break;
            }

        }

    }

    /**
     * Returns the next token from the kotlinTokens ArrayList.
     *
     * @return {@link KotlinToken}
     */
    public KotlinToken<KotlinTokenId> getNextToken() {

        KotlinToken<KotlinTokenId> ktToken;

        if (tokensNumber > 0) {
            ktToken = kotlinTokens.get(kotlinTokens.size() - tokensNumber--);
            if (ktToken != null) {
                int tokenLength = ktToken.length();
                while (tokenLength > 0) {
                    input.read();
                    tokenLength--;
                }
            }
            return ktToken;
        } else {
            input.read();
            return new KotlinToken<KotlinTokenId>(
                    new KotlinTokenId(TokenType.EOF.name(), TokenType.EOF.name(), 7), "",
                    TokenType.EOF);
        }

    }

}
