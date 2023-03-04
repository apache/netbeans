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
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.xml.axi.impl.SchemaGeneratorFactoryImpl;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 *
 * @author Ayub Khan
 */
public abstract class SchemaGeneratorFactory {
    
    public enum TransformHint{
        OK,
        SAME_DESIGN_PATTERN,
        INVALID_SCHEMA,
        NO_GLOBAL_ELEMENTS,
        GLOBAL_ELEMENTS_HAVE_NO_CHILD_ELEMENTS,
        GLOBAL_ELEMENTS_HAVE_NO_CHILD_ATTRIBUTES,
        GLOBAL_ELEMENTS_HAVE_NO_CHILD_ELEMENTS_AND_ATTRIBUTES,
        GLOBAL_ELEMENTS_HAVE_NO_GRAND_CHILDREN,
        NO_ATTRIBUTES,
        WILL_REMOVE_TYPES,
        WILL_REMOVE_GLOBAL_ELEMENTS,
        WILL_REMOVE_GLOBAL_ELEMENTS_AND_TYPES,
        CANNOT_REMOVE_TYPES,
        CANNOT_REMOVE_GLOBAL_ELEMENTS,
        CANNOT_REMOVE_GLOBAL_ELEMENTS_AND_TYPES;
    }
    
    private static SchemaGeneratorFactory instance;
    
    /** Creates a new instance of SchemaGeneratorFactory */
    public static SchemaGeneratorFactory getDefault() {
        if(instance == null)
            instance = new SchemaGeneratorFactoryImpl();
        return instance;
    }
    
    /*
     * infers design pattern
     *
     */
    public abstract SchemaGenerator.Pattern inferDesignPattern(AXIModel am);
    
    /*
     * Updates schema using a a particular design pattern
     *
     */
    public abstract void updateSchema(SchemaModel sm,
            SchemaGenerator.Pattern pattern) throws BadLocationException, IOException;
    
    /*
     * returns list of all master axi global elements
     *
     * @param am - AXIModel
     * @return ges - list of all master axi global elements
     */    
    public abstract List<Element> findMasterGlobalElements(AXIModel am);
    
    /*
     * can transforms schema using a a particular design pattern
     *
     * @param sm - SchemaModel
     * @param currentPattern - if null then checks only if the schema is valid and well-formed for transform
     * @param targetPattern - if null then checks only if the schema is valid and well-formed for transform
     */
    public abstract TransformHint canTransformSchema(SchemaModel sm,
            SchemaGenerator.Pattern currentPattern, SchemaGenerator.Pattern targetPattern);
    
    /*
     * can transforms schema using a a particular design pattern
     *
     * @param sm - SchemaModel
     * @param currentPattern - if null then checks only if the schema is valid and well-formed for transform
     * @param targetPattern - if null then checks only if the schema is valid and well-formed for transform
     * @param ges - list of all master axi global elements
     */
    public abstract TransformHint canTransformSchema(SchemaModel sm,
            SchemaGenerator.Pattern currentPattern, SchemaGenerator.Pattern targetPattern,
            List<Element> ges);
    
    /*
     * transforms schema using a a particular design pattern
     *
     */
    public abstract void transformSchema(SchemaModel sm,
            SchemaGenerator.Pattern targetPattern) throws IOException;
}
