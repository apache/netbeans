package org.black.kotlin.highlighter;

import com.intellij.psi.PsiElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.black.kotlin.highlighter.netbeans.KotlinToken;
import org.black.kotlin.highlighter.netbeans.KotlinTokenId;
import org.black.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.psi.KtFile;
import org.netbeans.spi.lexer.LexerInput;
import org.openide.util.Exceptions;

/**
 * KotlinTokenScanner parses kotlin code for tokens
 *
 * @author Александр
 *
 */
public final class KotlinTokenScanner {

    private final KotlinTokensFactory kotlinTokensFactory;

    private KtFile ktFile;
    private File syntaxFile;
    private List<KotlinToken<KotlinTokenId>> kotlinTokens;
    private int offset = 0;
    private int tokensNumber = 0;
    private final LexerInput input;

    /**
     * Class constructor
     *
     * @param input code that must be parsed
     */
    public KotlinTokenScanner(LexerInput input) {
        kotlinTokensFactory = new KotlinTokensFactory();
        this.input = input;
        createSyntaxFile();
        try {
            ktFile = KotlinEnvironment.parseFile(syntaxFile);
            deleteSyntaxFile();
            createListOfKotlinTokens();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Creates temporary file for parsing from the input code. Temporary file
     * name is syntaxFile.
     */
    private void createSyntaxFile() {
        syntaxFile = new File("syntax");
        StringBuilder builder = new StringBuilder("");

        int character;

        do {
            character = input.read();
            builder.append((char) character);
        } while (character != LexerInput.EOF);

        CharSequence readText = builder.toString();
        input.backup(input.readLengthEOF());

        try {
            PrintWriter writer = new PrintWriter(syntaxFile, "UTF-8");
            writer.print(readText);
            writer.close();

        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * This method deletes temporary syntaxFile.
     */
    public void deleteSyntaxFile() {
        if (syntaxFile != null) {
            syntaxFile.deleteOnExit();
        }
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
                        lastElement.getText(), lastElement.getTextOffset(),
                        tokenType));
                tokensNumber = kotlinTokens.size();
            } else {
                kotlinTokens.add(new KotlinToken<KotlinTokenId>(
                        new KotlinTokenId(TokenType.EOF.name(), TokenType.EOF.name(), 7), "",
                        0, TokenType.EOF));
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
                    0, TokenType.EOF);
        }

    }

}
