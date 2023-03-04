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

package org.netbeans.modules.languages.lexer;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.languages.CharInput;
import org.netbeans.modules.languages.lexer.SLexer.TokenProperties;
import org.netbeans.modules.languages.lexer.SLexer.Vojta;
import org.netbeans.modules.languages.parser.Pattern;


class DelegatingInputBridge extends CharInput {

    private InputBridge     input;
    private Pattern         start;
    private Pattern         end;
    private int             tokenType;
    private List<Vojta>     embeddings = new ArrayList<Vojta> ();

    DelegatingInputBridge (
        InputBridge         input, 
        Pattern             start,
        Pattern             end,
        int                 tokenType
    ) {
        this.input =        input;
        this.start =        start;
        this.end   =        end;
        this.tokenType =    tokenType;
    }

    public char read () {
        readEmbeddings ();
        return input.read ();
    }

    public void setIndex (int index) {
        input.setIndex (index);
    }

    public int getIndex () {
        return input.getIndex ();
    }

    public char next () {
        readEmbeddings ();
        return input.next ();
    }

    public boolean eof () {
        readEmbeddings ();
        return input.eof ();
    }

    public String getString (int from, int to) {
        return input.getString (from, to);
    }

    public String toString () {
        return input.toString ();
    }
    
    public List<Vojta> getEmbeddings () {
        List<Vojta> e = embeddings;
        embeddings = new ArrayList<Vojta> ();
        return e;
    }
    
    private void readEmbeddings () {
        int startIndex = input.getIndex ();
        if (!input.eof () && start.next (input) != null) {
            int startSkipLength = input.getIndex () - startIndex;
            int endSkipLength = input.getIndex ();
            while (!input.eof () && end.next (input) == null) {
                input.read ();
                endSkipLength = input.getIndex ();
            }
            endSkipLength = input.getIndex () - endSkipLength;
            embeddings.add (
                new Vojta (
                    tokenType,
                    startIndex,
                    input.getIndex (),
                    new TokenProperties (
                        SLexer.INJECTED_CODE,
                        startSkipLength,
                        endSkipLength
                    )
                )
            );
        }
    }
}


