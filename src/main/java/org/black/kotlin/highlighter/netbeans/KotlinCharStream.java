package org.black.kotlin.highlighter.netbeans;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import org.netbeans.spi.lexer.LexerInput;

/**
 *
 * @author Александр
 */

public class KotlinCharStream {

    private LexerInput input;

    static boolean staticFlag;

    public KotlinCharStream(LexerInput input) {
        this.input = input;
    }

    KotlinCharStream(Reader stream, int i, int i0) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    KotlinCharStream(InputStream stream, String encoding, int i, int i0) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    char BeginToken() throws IOException {
        return readChar();
    }

    String GetImage() {
        return input.readText().toString();
    }

    public char[] GetSuffix(int len) {
        if (len > input.readLength()) {
            throw new IllegalArgumentException();
        }
        return input.readText(input.readLength() - len, input.readLength()).toString().toCharArray();
    }

    void ReInit(Reader stream, int i, int i0) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void ReInit(InputStream stream, String encoding, int i, int i0) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void backup(int i) {
        input.backup(i);
    }

    int getBeginColumn() {
        return 0;
    }

    int getBeginLine() {
        return 0;
    }

    int getEndColumn() {
        return 0;
    }

    int getEndLine() {
        return 0;
    }

    char readChar() throws IOException {
        int result = input.read();
        if (result == LexerInput.EOF) {
            throw new IOException("LexerInput EOF");
        }
        return (char) result;
    }

}