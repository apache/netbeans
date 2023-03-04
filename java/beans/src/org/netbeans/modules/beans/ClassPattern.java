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
package org.netbeans.modules.beans;

import java.awt.Image;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.TypeMirrorHandle;

/** Class representing a JavaBeans Property
 * @author Petr Hrebejk
 */
public class ClassPattern extends Pattern {

    private Image icon;
    
    // Special constructorfo root
    public ClassPattern( PatternAnalyser patternAnalyser ) {
        super( patternAnalyser, Pattern.Kind.CLASS, "root", null );
        this.icon = PATTERNS; // NOI18N
    }
    
    public ClassPattern( PatternAnalyser patternAnalyser,                         
                         TypeMirror type,
                         String name ) {

        super( patternAnalyser, Pattern.Kind.CLASS, name, TypeMirrorHandle.create(type) );
        this.icon = ((DeclaredType)type).asElement().getKind() == ElementKind.INTERFACE ? INTERFACE : CLASS;
    }
    
    @Override
    public Image getIcon() {
        return icon;
    }

    @Override
    public String getHtmlDisplayName() {
        return null;
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    private Object Utilities;
    
}
