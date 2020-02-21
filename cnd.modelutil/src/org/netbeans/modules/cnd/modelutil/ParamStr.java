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

package org.netbeans.modules.cnd.modelutil;

import java.awt.Color;


public class ParamStr {
    private final String type, simpleType, prm, text;
    private final Color typeColor;
    private final boolean isVarArg;
    
    public ParamStr(String type, String simpleType, String prm, String text, boolean isVarArg, Color typeColor) {
        this.type = type;
        this.simpleType = simpleType;
        this.prm = prm;
        this.typeColor = typeColor;
        this.isVarArg = isVarArg;
        this.text = text;
    }

    public String getTypeName() {
        return type;
    }

    public String getSimpleTypeName() {
        return simpleType;
    }

    public String getName() {
        return prm;
    }

    public String getText() {
        return text;
    }
    
    public Color getTypeColor() {
        return typeColor;
    }
    
    public boolean isVarArg() {
        return isVarArg;
    }
}
