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
package org.netbeans.modules.languages.hcl;

import java.util.LinkedList;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;

/**
 *
 * @author Laszlo Kishalmi
 */
public abstract class HCLHereDocAdaptor extends Lexer {

    LinkedList<String> hereDocStack = new LinkedList<>();
    String currentHereDocVar;

    public HCLHereDocAdaptor(CharStream input) {
        super(input);
    }

    protected  void pushHereDocVar(String hereDocHead) {
        String hereDocVar = hereDocHead.replaceAll("^<<-?", "");
        hereDocVar = hereDocVar.substring(0, hereDocVar.length() - 1);
        if (currentHereDocVar != null) {
            hereDocStack.addFirst(currentHereDocVar);
        }
        currentHereDocVar = hereDocVar;
    }

    protected void popHereDocVar() {
        currentHereDocVar = hereDocStack.isEmpty() ? null : hereDocStack.removeFirst();
    }

    protected boolean heredocEndAhead(String partialHeredoc) {
        int n = 1;
        int c = _input.LA(1);
        // NewLines are part of heredoc content, but
        // heredoc marker and it's leading space are not
        while (Character.isWhitespace(c) && c != '\n') {
            c = _input.LA(++n);
        }
        if (c == '\n') {
            return false;
        }
        for (int v = 0; v < currentHereDocVar.length(); v++) {
          if (this._input.LA(n + v) != currentHereDocVar.charAt(v)) {
            return false;
          }
        }

        int charAfterDelimiter = this._input.LA(currentHereDocVar.length() + n);

        return charAfterDelimiter == EOF ||  Character.isWhitespace(charAfterDelimiter);
    }

    @Override
    public void reset() {
        super.reset();
        currentHereDocVar = null;
        hereDocStack.clear();
    }
}
