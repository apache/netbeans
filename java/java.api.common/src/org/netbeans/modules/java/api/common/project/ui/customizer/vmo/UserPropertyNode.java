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

import java.util.Map;

/**
 * @author Rastislav Komara
 */
public class UserPropertyNode extends JavaVMOption<OptionValue<Map.Entry<String, String>>> {
    
    public static final String NAME = "D";

    public UserPropertyNode(Token name, String value, int start) {
        super(name);
        setName(name.getText());
        setValue(new OptionValue.StringPair(name.getText(), value));
    }

    public UserPropertyNode() {
        super(NAME);
        setValue(new OptionValue.StringPair());        
    }

    @Override
    public StringBuilder print(StringBuilder builder) {
        StringBuilder sb = super.print(builder);
        OptionValue<Map.Entry<String, String>> val = getValue();
        if (val.isPresent()) {
            sb.append(SPACE).append(HYPHEN);
            Map.Entry<String, String> entry = val.getValue();
            sb.append(entry.getKey());
            if (entry.getValue() != null) {
                sb.append("=").append(entry.getValue());
            }
        }
        return sb;
    }    
}
