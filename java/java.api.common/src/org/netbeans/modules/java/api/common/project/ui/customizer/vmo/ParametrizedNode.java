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
package org.netbeans.modules.java.api.common.project.ui.customizer.vmo;

/**
 * A VM option with a parameter, e.g. {@code -Xmx512m},
 * {@code -splash:img.png} or {@code -cp a.jar:b.jar}.
 *
 * @author Rastislav Komara
 */
public class ParametrizedNode extends JavaVMOption<OptionValue.SimpleString> {
    private String delimiter;

    public String getDelimiter() {
        return delimiter;
    }

    public ParametrizedNode(String name, String delimiter) {
        super();
        setName(name);
        this.delimiter = delimiter;
        setValue(new OptionValue.SimpleString());
    }

    public ParametrizedNode(String name, String delimiter, String parameter) {
        this(name, delimiter, parameter, true);
    }

    public ParametrizedNode(String name, String delimiter, String parameter, boolean isValid) {
        super();
        setName(name);
        this.delimiter = delimiter;
        setValue(parameter == null ? new OptionValue.SimpleString() : new OptionValue.SimpleString(parameter));
        setValid(isValid);
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
