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
package org.netbeans.modules.java.api.common.project.ui.customizer.vmo;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

/**
 * @author Rastislav Komara
 */
public class UnrecognizedOption extends SwitchNode {
    private final TokenStream input;
    private final Token start;
    private final Token stop;
    private final RecognitionException e;

    public UnrecognizedOption(TokenStream input, Token start, Token stop, RecognitionException e) {
        super(start);        
        this.input = input;
        this.start = start;
        this.stop = stop;
        this.e = e;
        if (start != null) {
            setName(start.getText());
        }
        setValue(new OptionValue.SwitchOnly(true));
    }

    public UnrecognizedOption(Token start) {
        this(null, start, null, null);
    }

    public UnrecognizedOption(String name) {
        this(null, null, null, null);
        setName(name);
        setValue(new OptionValue.SwitchOnly(true));
    }

    @Override
    public StringBuilder print(StringBuilder sb) {
        sb = ensureBuilder(sb);
        if (input != null) {
            for (int i = start.getTokenIndex(); i <= stop.getTokenIndex(); i++) {
                sb.append(input.get(i).getText());
            }
        } else {
            sb.append(HYPHEN).append(getName());
        }
        return sb;
    }

    @Override
    public String toString() {
        return "UnrecognizedOption{" +
                "input=" + input +
                ", start=" + start +
                ", stop=" + stop +
                ", e=" + e +
                " as " + super.toString() + 
                '}';
    }
}
