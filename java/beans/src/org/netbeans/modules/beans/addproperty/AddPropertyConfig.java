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

package org.netbeans.modules.beans.addproperty;

/**
 *
 * @author  Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public class AddPropertyConfig {
    // Default Add Property template path
    public static final String DEFAULT_TEMPLATE_PATH = "org.netbeans.modules.java.codestyle/AddProperty.freemarker"; // NOI18N
    
    private String TEMPLATE_PATH = DEFAULT_TEMPLATE_PATH; // NOI18N

    public enum ACCESS {PRIVATE, PACKAGE, PROTECTED, PUBLIC};
    public enum GENERATE {GETTER_AND_SETTER, GETTER, SETTER, NONE};
    
    private String name;
    private String initializer;
    private String type;
    private String className;

    private ACCESS access = AddPropertyConfig.ACCESS.PRIVATE;
    
    private boolean _static;
    private boolean _final;
    
    private GENERATE generateGetterSetter = AddPropertyConfig.GENERATE.GETTER_AND_SETTER;
    
    private boolean generateJavadoc = true;
    private boolean bound;
    private String  propName;
    private boolean vetoable;

    private boolean indexed;
    
    private String propertyChangeSupportName;
    private String vetoableChangeSupportName;
    
    private boolean generatePropertyChangeSupport;
    private boolean generateVetoableChangeSupport;

    public AddPropertyConfig(
            String name,
            String initializer,
            String type,
            String className,
            ACCESS access,
            boolean _static,
            boolean _final,
            GENERATE generateGetterSetter,
            boolean generateJavadoc,
            boolean bound,
            String popName,
            boolean vetoable,
            boolean indexed,
            String propertyChangeSupportName,
            String vetoableChangeSupportName,
            boolean generatePropertyChangeSupport,
            boolean generateVetoableChangeSupport) {
        this.name = name;
        this.initializer = initializer;
        this.type = type;
        this.className = className;
        this.access = access;
        this._static = _static;
        this._final = _final;
        this.generateGetterSetter = generateGetterSetter;
        this.generateJavadoc = generateJavadoc;
        this.bound = bound;
        this.propName = popName;
        this.vetoable = vetoable;
        this.indexed = indexed;
        this.propertyChangeSupportName = propertyChangeSupportName;
        this.vetoableChangeSupportName = vetoableChangeSupportName;
        this.generatePropertyChangeSupport = generatePropertyChangeSupport;
        this.generateVetoableChangeSupport = generateVetoableChangeSupport;
    }

    public String getTEMPLATE_PATH() {
        return TEMPLATE_PATH;
    }

    public void setTEMPLATE_PATH(String TEMPLATE_PATH) {
        this.TEMPLATE_PATH = TEMPLATE_PATH;
    }        

    public boolean isFinale() {
        return _final;
    }

    public void setFinale(boolean _finale) {
        this._final = _finale;
    }

    public boolean isStatic() {
        return _static;
    }

    public void setStatic(boolean _static) {
        this._static = _static;
    }

    public ACCESS getAccess() {
        return access;
    }

    public void setAccess(ACCESS access) {
        this.access = access;
    }

    public boolean isBound() {
        return bound;
    }

    public void setBound(boolean bound) {
        this.bound = bound;
    }

    public GENERATE getGenerateGetterSetter() {
        return generateGetterSetter;
    }

    public void setGenerateGetterSetter(GENERATE generateGetterSetter) {
        this.generateGetterSetter = generateGetterSetter;
    }

    public boolean isGenerateJavadoc() {
        return generateJavadoc;
    }

    public void setGenerateJavadoc(boolean generateJavadoc) {
        this.generateJavadoc = generateJavadoc;
    }

    public boolean isGeneratePropertyChangeSupport() {
        return generatePropertyChangeSupport;
    }

    public String getPropertyChangeSupportName() {
        return propertyChangeSupportName;
    }

    public void setPropertyChangeSupportName(String propertyChangeSupportName) {
        this.propertyChangeSupportName = propertyChangeSupportName;
    }

    public String getVetoableChangeSupportName() {
        return vetoableChangeSupportName;
    }

    public void setVetoableChangeSupportName(String vetoableChangeSupportName) {
        this.vetoableChangeSupportName = vetoableChangeSupportName;
    }

    public void setGeneratePropertyChangeSupport(boolean generatePropertyChangeSupport) {
        this.generatePropertyChangeSupport = generatePropertyChangeSupport;
    }

    public boolean isGenerateVetoableChangeSupport() {
        return generateVetoableChangeSupport;
    }

    public void setGenerateVetoableChangeSupport(boolean generateVetoableChangeSupport) {
        this.generateVetoableChangeSupport = generateVetoableChangeSupport;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
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

    public String getPopName() {
        return propName;
    }

    public void setPopName(String popName) {
        this.propName = popName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public boolean isVetoable() {
        return vetoable;
    }

    public void setVetoable(boolean vetoable) {
        this.vetoable = vetoable;
    }
}
