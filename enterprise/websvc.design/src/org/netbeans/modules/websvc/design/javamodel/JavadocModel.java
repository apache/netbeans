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

package org.netbeans.modules.websvc.design.javamodel;

import java.util.List;

/**
 *
 * @author mkuchtiak
 */
public class JavadocModel {
    
    private String text;
    private List<String> inlineJavadoc;
    private List<String> paramJavadoc;
    private List<String> throwsJavadoc;
    private String returnJavadoc;
    
    /** Creates a new instance of MethodModel */
    JavadocModel() {
    }
    
    /** Creates a new instance of MethodModel */
    JavadocModel(String text) {
        this.text=text;
    }
    
    public String getText() {
        return text;
    }
    
    void setText(String text) {
        this.text=text;
    }
    
    public boolean isEqualTo(JavadocModel model) {
        if (!Utils.isEqualTo(text,model.text)) return false;
        return true;
    }
}
