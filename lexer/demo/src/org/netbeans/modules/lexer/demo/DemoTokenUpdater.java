/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
