/*
Licensed to the Apache Software Foundation (ASF)
 */
package org.netbeans.modules.php.blade.syntax.antlr4.php;

import org.netbeans.modules.php.blade.syntax.antlr4.v10.*;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import static org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrParser.*;
import org.netbeans.spi.lexer.antlr4.AntlrTokenSequence;

/**
 *
 * @author bogdan
 */
public class BladePhpAntlrUtils {

    public static AntlrTokenSequence lexerStringScan(String text) {
        CharStream cs = CharStreams.fromString(text);
        BladePhpAntlrLexer lexer = new BladePhpAntlrLexer(cs);
        AntlrTokenSequence tokens = new AntlrTokenSequence(lexer);
        return tokens;
    }

    public static Token getToken(String text, int offset) {
        AntlrTokenSequence tokens = lexerStringScan(text);
        if (offset > text.length()){
            return null;
        }
        
        if (tokens.isEmpty()){
            return null;
        }
        
        boolean hasPrev = tokens.hasPrevious();
        
        while(tokens.hasNext()){
            Token test = tokens.next().get();
            String testText = test.getText();
            int x = 1;
        }
        
        tokens.seekTo(offset);

        if (!tokens.hasNext()){
            return null;
        }
        Token token = tokens.next().get();
        return token;
    }
}
