/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.nblexer;

import java.util.Stack;
import org.antlr.runtime.CharStream;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author marekfukala
 */
public class NbLexerCharStream implements CharStream {

    private static final String STREAM_NAME = "NbLexerCharStream"; //NOI18N
    private LexerInput li;
    private Stack<Integer> markers = new Stack<>();
//    private int index;

    public NbLexerCharStream(LexerRestartInfo lri) {
        this.li = lri.input();

    }

    @Override
    public String substring(int i, int i1) {
        return li.readText(i, i1).toString();
    }

    @Override
    public int LT(int i) {
        return LA(i);
    }

    @Override
    public int getLine() {
        return -1;
    }

    @Override
    public void setLine(int i) {
        //no-op
    }

    @Override
    public void setCharPositionInLine(int i) {
        //no-op
    }

    @Override
    public int getCharPositionInLine() {
        return -1;
    }

    @Override
    public void consume() {
        read();
//        index++;
    }

    @Override
    public int LA(int lookahead) {
        if (lookahead == 0) {
            return 0; //the behaviour is not defined
        }

        int c = 0;
        for (int i = 0; i < lookahead; i++) {
            c = read();
        }
        li.backup(lookahead);
        return c;
    }

    @Override
    public int mark() {
        markers.push(index());
        return markers.size() - 1;
    }

    @Override
    public int index() {
        return li.readLengthEOF();
    }

    @Override
    public void rewind(int marker) {
        if(markers.size() < marker) {
            return ; //report?
        }
        int markedIndex = markers.get(marker);
        
        //remove all markers after the given one, keep the requested one
        for(int i = marker + 1; i < markers.size(); i++) {
            markers.remove(i);
        }
        
        //seek to the marked index
        seek(markedIndex);
    }

    @Override
    public void rewind() {
        if(markers.isEmpty()) { 
            return ;
        }
        
        rewind(markers.size() - 1);
    }

    @Override
    public void release(int marker) {
        if(markers.size() < marker) {
            return ; //report?
        }
        
        //remove all markers from the given one, including the requested one
        for(int i = marker; i < markers.size(); i++) {
            markers.remove(i);
        }
    }

    @Override
    public void seek(int i) {
        if (i < index()) {
            //go backward
            li.backup(index() - i);
//            index = i;
        } else {
            // go forward
            while (index() < i) {
                consume();
            }
        }
    }

    @Override
    public int size() {
        return -1; //not supported
    }

    @Override
    public String getSourceName() {
        return STREAM_NAME;
    }
    
     private int read() {
        int result = li.read();
        if (result == LexerInput.EOF) {
            result = CharStream.EOF;
        }

        return result;
    }
}
