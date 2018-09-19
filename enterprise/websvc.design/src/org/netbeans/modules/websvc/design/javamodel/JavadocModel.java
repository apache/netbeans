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
    
    public List<String> getInlineJavadoc() {
        return inlineJavadoc;
    }

    void setInlineJavadoc(List<String> inlineJavadoc) {
        this.inlineJavadoc = inlineJavadoc;
    }

    public List<String> getParamJavadoc() {
        return paramJavadoc;
    }

    void setParamJavadoc(List<String> paramJavadoc) {
        this.paramJavadoc = paramJavadoc;
    }

    public List<String> getThrowsJavadoc() {
        return throwsJavadoc;
    }

    void setThrowsJavadoc(List<String> throwsJavadoc) {
        this.throwsJavadoc = throwsJavadoc;
    }

    public String getReturnJavadoc() {
        return returnJavadoc;
    }

    void setReturnJavadoc(String returnJavadoc) {
        this.returnJavadoc = returnJavadoc;
    }

        
    public boolean isEqualTo(JavadocModel model) {
        if (!Utils.isEqualTo(text,model.text)) return false;
        return true;
    }
}
