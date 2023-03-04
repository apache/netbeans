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
package org.netbeans.modules.xml.axi;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.axi.visitor.*;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 *
 * @author Ayub Khan
 */
public abstract class SchemaGenerator extends DefaultVisitor {
    
    private SchemaGenerator.Mode mode;
    
    public static SchemaGenerator.Pattern DEFAULT_DESIGN_PATTERN = Pattern.RUSSIAN_DOLL;
    
    public enum Pattern {
        RUSSIAN_DOLL,
        VENITIAN_BLIND,
        GARDEN_OF_EDEN,
        SALAMI_SLICE,
        MIXED;
    }
    
    public enum Mode {
        TRANSFORM,
        UPDATE;
    }
    
    /**
     * Creates a new instance of SchemaGenerator
     */
    public SchemaGenerator(Mode mode) {
        super();
        this.mode = mode;
    }
    
    /*
     * returns mode
     *
     */
    public Mode getMode() {
        return mode;
    }
    
    /*
     * Updates schema using a a particular design pattern
     *
     */
    public abstract void updateSchema(SchemaModel sm) throws BadLocationException, IOException;
    
    /*
     * Transforms schema using a particular design pattern
     *
     */
    public abstract void transformSchema(SchemaModel sm) throws IOException;
    
    public void visit(Element element) {
        visitChildren(element);
    }
    
    public void visit(Attribute attribute) {
        visitChildren(attribute);
    }
    
    public void visit(Compositor compositor) {
        visitChildren(compositor);
    }
    
    protected void visitChildren(AXIComponent component) {
        for(AXIComponent child: component.getChildren()) {
            child.accept(this);
        }
    }
    
    public static interface UniqueId {
        int nextId();
    }
    
    public static interface PrimitiveCart {
        void add(Datatype d, SchemaComponent referer);
        Set<Map.Entry<SchemaComponent, Datatype>> getEntries();
        GlobalSimpleType getDefaultPrimitive();
        public GlobalSimpleType getPrimitiveType(String typeName);
    }
}
