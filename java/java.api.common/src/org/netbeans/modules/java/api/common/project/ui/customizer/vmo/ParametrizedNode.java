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

import org.antlr.runtime.Token;

/**
 * @author Rastislav Komara
 */
public class ParametrizedNode extends JavaVMOption<OptionValue.SimpleString> {
    private String delimiter;

    public ParametrizedNode(Token token, int splitIndex) {
        super(token);
        final String string = token.getText();
        if (string != null) {
            setName(string.substring(0, splitIndex));
            setValue(new OptionValue.SimpleString(string.substring(splitIndex)));
            delimiter = "";
            setValid(true);
        } else {
            setName("");
            setValid(false);
        }
    }

    public ParametrizedNode(Token name, String delimiter, String parameter) {
        this(name,delimiter, parameter, true);
    }
    public ParametrizedNode(Token name, String delimiter, String parameter, boolean isValid) {
        super(name);
        setName(name.getText());
        this.delimiter = delimiter;
        if (parameter != null) {
            setValue(new OptionValue.SimpleString(parameter));
        }
        setValid(isValid);
    }

    public ParametrizedNode(String name, String delimiter) {
        super(name);
        this.delimiter = delimiter;
        setValue(new OptionValue.SimpleString());
    }

    public ParametrizedNode(Token token, String name, String delimiter, String value) {
        super(token);
        setName(name);
        this.delimiter = delimiter;
        setValue(new OptionValue.SimpleString(value));
    }

    @Override
    public StringBuilder print(StringBuilder builder) {
        StringBuilder sb = super.print(builder);
        if (getValue().isPresent()) {
                sb.append(SPACE).append(HYPHEN).append(getName()).append(delimiter).append(getValue().getValue());
        }
        return sb;
    }
}
