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
package org.netbeans.modules.javafx2.editor.completion.model;

import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;

/**
 * Represents an instance of a FXML bean. 
 *
 * @author sdedic
 */
public final class FxNewInstance extends FxInstance {
    private String          typeName;
    private String          initValue;
    private String          factoryMethod;
    /**
     * true, if the init value is a declared constant (fx:constant)
     */
    private boolean         constantValue;
    
    /**
     * True, if the instance represents a fx:root element - custom root.
     */
    private boolean         customRoot;
    
    public String getTypeName() {
        return typeName;
    }
    
    public FxNewInstance(String sourceName, boolean customRoot) {
        this.typeName = sourceName;
        this.customRoot = customRoot;
        if (customRoot) {
            setSourceName(FxXmlSymbols.FX_ROOT);
        } else {
            setSourceName(sourceName);
        }
    }

    public FxNewInstance(String sourceName) {
        setSourceName(sourceName);
        this.typeName = sourceName;
    }

    @Override
    public Kind getKind() {
        return Kind.Instance;
    }
    
    /**
     * True, if the instance represents a fx:root element
     * @return 
     */
    public boolean isCustomRoot() {
        return customRoot;
    }

    @Override
    public void accept(FxNodeVisitor v) {
        v.visitInstance(this);
    }
    
    FxNewInstance fromValue(CharSequence val) {
        this.initValue = val == null ? null : val.toString();
        return this;
    }
    
    FxNewInstance usingFactory(String factory) {
        this.factoryMethod = factory;
        return this;
    }
    
    void setConstant(boolean constant) {
        this.constantValue = constant;
    }
    
    public boolean isConstant() {
        return constantValue;
    }
    
    void resolveClass(String className, ElementHandle<TypeElement> handle) {
        if (!customRoot) {
            setSourceName(className);
        }
        setJavaType(handle);
    }

    public String getInitValue() {
        return initValue;
    }

    public String getFactoryMethod() {
        return factoryMethod;
    }
    
}

