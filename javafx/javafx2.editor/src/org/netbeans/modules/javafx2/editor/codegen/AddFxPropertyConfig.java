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

package org.netbeans.modules.javafx2.editor.codegen;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
public class AddFxPropertyConfig {
    public static enum ACCESS {PRIVATE, PACKAGE, PROTECTED, PUBLIC};
    public static enum GENERATE {WRITABLE, READ_ONLY};
    
    private String name;
    private String initializer;
    private String propertyType;
    private String implementationType;
    private ACCESS access = AddFxPropertyConfig.ACCESS.PRIVATE;
    private GENERATE generate = AddFxPropertyConfig.GENERATE.WRITABLE;
    private boolean javadoc = true;

    public AddFxPropertyConfig(
            String name,
            String initializer,
            String propertyType,
            String implementationType,
            ACCESS access,
            GENERATE generate,
            boolean javadoc) {
        this.name = name;
        this.initializer = initializer;
        this.propertyType = propertyType;
        this.implementationType = implementationType;
        this.access = access;
        this.generate = generate;
        this.javadoc = javadoc;
    }

    public ACCESS getAccess() {
        return access;
    }

    public void setAccess(ACCESS access) {
        this.access = access;
    }

    public GENERATE getGenerate() {
        return generate;
    }

    public void setGenerate(GENERATE generate) {
        this.generate = generate;
    }

    public boolean isGenerateJavadoc() {
        return javadoc;
    }

    public void setGenerateJavadoc(boolean generateJavadoc) {
        this.javadoc = generateJavadoc;
    }

    public String getInitializer() {
        return initializer;
    }

    public void setInitializer(String initializer) {
        this.initializer = initializer;
    }       

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }
    
    public String getImplementationType() {
        return implementationType;
    }
    
    public void setImplementationType(String implementationType) {
        this.implementationType = implementationType;
    }
}
