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

package org.netbeans.modules.lexer.demo;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Segment;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.LexerInput;
import org.netbeans.api.lexer.SampleTextMatcher;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Lexer;
import org.netbeans.spi.lexer.util.Compatibility;
import org.netbeans.spi.lexer.inc.TextTokenUpdater;
import org.netbeans.spi.lexer.util.LexerUtilities;

/**
 * Token updater working over a swing document.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class DemoTokenUpdater extends TextTokenUpdater {
    
    private Segment seg = new Segment(); // shared segment instance

    private final Document doc;
    
    private final Language language;

    private final boolean maintainLookbacks;
    
    private boolean debugTokenChanges;
    
    private Lexer sharedLexer; // shared lexer instance
    
    public DemoTokenUpdater(Document doc, Language language) {
        this(doc, language, true);
    }

    public DemoTokenUpdater(Document doc, Language language, boolean maintainLookbacks) {
        this.doc = doc;
        this.language = language;
        this.maintainLookbacks = maintainLookbacks;

        doc.addDocumentListener(
            new DocumentListener() {
                public void insertUpdate(DocumentEvent evt) {
                    if (debugTokenChanges) {
                        try {
                            System.out.println("\nDocument-insert \""
                                + LexerUtilities.toSource(
                                    (evt.getDocument()).getText(evt.getOffset(), 
                                        evt.getLength()))
                                + "\""
                            );
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                    }
                    
                    update(evt.getOffset(), evt.getLength());
                }

                public void removeUpdate(DocumentEvent evt) {
                    if (debugTokenChanges) {
                        System.out.println("\nDocument-remove at offset="
                            + evt.getOffset()
                            + ", length="
                            + evt.getLength()
                        );
                    }
                    
                    update(evt.getOffset(), -evt.getLength());
                }

                public void changedUpdate(DocumentEvent evt) {
                }
            }
        );
    }
    
    public boolean getDebugTokenChanges() {
        return debugTokenChanges;
    }
    
    public void setDebugTokenChanges(boolean debugTokenChanges) {
        this.debugTokenChanges = debugTokenChanges;
    }
    
    protected final Document getDocument() {
        return doc;
    }

    protected final Language getLanguage() {
        return language;
    }
    
    public char textCharAt(int index) {
        synchronized (seg) {
            try {
                doc.getText(index, 1, seg);
                return seg.array[seg.offset];

            } catch (BadLocationException e) {
                throw new IllegalStateException(e.toString());
            }
        }
    }

    public int textLength() {
        return doc.getLength();
    }
    
    private String getDocumentText(int offset, int length) {
        try {
            return doc.getText(offset, length);
        } catch (BadLocationException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
    
    protected Token createToken(TokenId id, int index, int length) {
        String sampleText = null;
        if (Compatibility.charSequenceExists()) {
            SampleTextMatcher matcher = id.getSampleTextMatcher();
            if (matcher != null) {
                /* The recognizedText would not be a string
                 * in the normal applications. It would be
                 * a custom char sequence reused for every
                 * recognized token. Here it's string
                 * to simplify the code.
                 */
                CharSequence recognizedText
                    = (CharSequence)(Object)getDocumentText(index, length); // 1.3 compilability 
                sampleText = matcher.match(recognizedText);
            }
        }

        Token token;
        if (sampleText != null) {
            token = new DemoSampleToken(id, sampleText);
        } else {
            token = new DemoToken(this, id, index, length);
        }

        return token;
    }
    
    protected Lexer createLexer() {
        if (sharedLexer == null) {
            sharedLexer = language.createLexer();
        }
        
        return sharedLexer;
    }
    
    protected void add(Token token, int lookahead, Object state) {
        add(token);

        if (token instanceof DemoSampleToken) {
            DemoSampleToken dft = (DemoSampleToken)token;
            dft.setLookahead(lookahead);
            dft.setState(state);
        } else {
            DemoToken dt = (DemoToken)token;
            dt.setLookahead(lookahead);
            dt.setState(state);
        }
        
        if (debugTokenChanges) {
            System.out.println("Added token: ["
                + (getNextIndex() - 1) + "]="
                + tokenToString(token, false)
            );
        }
    }
    
    protected void remove() {
        if (debugTokenChanges) {
            System.out.println("Removed token at index="
                + (getNextIndex() - 1)
            );
        }

        super.remove();
    }
    
    public int getLookahead() {
        Token token = getToken(getValidPreviousIndex());
        return (token instanceof DemoSampleToken)
            ? ((DemoSampleToken)token).getLookahead()
            : ((DemoToken)token).getLookahead();
    }

    public Object getState() {
        Token token = getToken(getValidPreviousIndex());
        return (token instanceof DemoSampleToken)
            ? ((DemoSampleToken)token).getState()
            : ((DemoToken)token).getState();
    }

    public int getLookback() {
        if (maintainLookbacks) {
            Token token = getToken(getValidPreviousIndex());
            return (token instanceof DemoSampleToken)
                ? ((DemoSampleToken)token).getLookback()
                : ((DemoToken)token).getLookback();
                
        } else { // do not maintain the lookbacks
            return -1;
        }
    }

    protected void setLookback(int lookback) {
        if (maintainLookbacks) {
            Token token = getToken(getValidPreviousIndex());
            if (token instanceof DemoSampleToken) {
                ((DemoSampleToken)token).setLookback(lookback);
            } else {
                ((DemoToken)token).setLookback(lookback);
            }
        }
    }
    
    public boolean hasNext() {
        return super.hasNext();
    }
    
    public Token next() {
        return super.next();
    }
    
    public int relocate(int index) {
        return super.relocate(index);
    }
    
    public String tokenToString(Token token, boolean extraInfo) {
        StringBuffer sb = new StringBuffer();
        
        int length = org.netbeans.spi.lexer.util.Compatibility.getLength(token);
        String text = org.netbeans.spi.lexer.util.Compatibility.toString(token);
        sb.append("\""
            + org.netbeans.spi.lexer.util.LexerUtilities.toSource(text)
            + "\", " + token.getId()
        );

        if (token instanceof DemoToken) {
            DemoToken dt = (DemoToken)token;

            sb.append(", off=");
            sb.append(getOffset(dt.getRawOffset()));

            sb.append(", type=regular");
            if (extraInfo) {
                sb.append(", la=" + dt.getLookahead()
                    + ", lb=" + dt.getLookback()
                    + ", st=" + dt.getState()
                );
            }

        } else {
            DemoSampleToken dft = (DemoSampleToken)token;
            sb.append(", type=sample");
            if (extraInfo) {
                sb.append(", la=" + dft.getLookahead()
                    + ", lb=" + dft.getLookback()
                    + ", st=" + dft.getState()
                );
            }
        }
        
        return sb.toString();
    }        

    public String allTokensToString() {
        StringBuffer sb = new StringBuffer();
        int cnt = getTokenCount();
        sb.append("Dump of tokens (tokenCount=" + cnt + ") in token updater:\n");
        int offset = 0;
        try {
            for (int i = 0; i < cnt; i++) {
                Token t = getToken(i);
                int length = org.netbeans.spi.lexer.util.Compatibility.getLength(t);
                String text = org.netbeans.spi.lexer.util.Compatibility.toString(t);
                sb.append("[" + i + "] \""
                    + org.netbeans.spi.lexer.util.LexerUtilities.toSource(text)
                    + "\", " + t.getId()
                    + ", off=" + offset
                );
                
                if (t instanceof DemoToken) {
                    DemoToken dt = (DemoToken)t;

                    if (getOffset(dt.getRawOffset()) != offset) {
                        throw new IllegalStateException("offsets differ");
                    }
                    
                    sb.append(", type=regular"
                        + ", la=" + dt.getLookahead()
                        + ", lb=" + dt.getLookback()
                        + ", st=" + dt.getState()
                        + "\n"
                    );
                    
                } else {
                    DemoSampleToken dft = (DemoSampleToken)t;
                    sb.append(", type=sample"
                        + ", la=" + dft.getLookahead()
                        + ", lb=" + dft.getLookback()
                        + ", st=" + dft.getState()
                        + "\n"
                    );
                }
                

                offset += length;
            }
        } catch (RuntimeException e) {
            System.err.println(sb.toString());
            throw e;
        }
        
        return sb.toString();
    }

}
