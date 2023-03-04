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

package org.netbeans.modules.spring.java;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class Property {
    private String name;
    private ExecutableElement getter;
    private ExecutableElement setter;

    public Property(String name) {
        this.name = name;
    }

    public ExecutableElement getGetter() {
        return getter;
    }

    public void setGetter(ExecutableElement getter) {
        this.getter = getter;
    }

    public ExecutableElement getSetter() {
        return setter;
    }

    public void setSetter(ExecutableElement setter) {
        this.setter = setter;
    }

    public String getName() {
        return name;
    }
    
    public PropertyType getType() {
        if(this.getter != null && this.setter != null) {
            return PropertyType.READ_WRITE;
        } else if(this.getter != null) {
            return PropertyType.READ_ONLY;
        } else if(this.setter != null) {
            return PropertyType.WRITE_ONLY;
        }
        
        return null; // Should never occur
    }
    
    public TypeMirror getImplementationType() {
        if(getter != null) {
            return getter.getReturnType();
        }

        return setter.getParameters().get(0).asType();
    }
}
