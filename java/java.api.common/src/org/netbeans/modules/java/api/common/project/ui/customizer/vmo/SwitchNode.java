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
public class SwitchNode extends JavaVMOption<OptionValue.SwitchOnly> {

    public SwitchNode(Token t) {
        super(t);
        if (t != null) {
            setName(t.getText());
            setValue((OptionValue.SwitchOnly) OptionValue.createSwitch());
        }
    }
        
    public SwitchNode(int ttype, Token t) {
        this(t);
    }

    public SwitchNode(String name) {
        super(name);
        setValue(new OptionValue.SwitchOnly(false));
    }

    @Override
    public StringBuilder print(StringBuilder builder) {
        StringBuilder sb = super.print(builder);
        return getValue().isPresent() ? sb.append(SPACE).append(HYPHEN).append(getName()) : sb;
    }
    
}
