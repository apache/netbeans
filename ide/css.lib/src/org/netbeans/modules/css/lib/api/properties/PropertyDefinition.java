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
package org.netbeans.modules.css.lib.api.properties;

import org.netbeans.modules.css.lib.api.CssModule;
import org.netbeans.modules.css.lib.properties.GrammarParser;
import org.openide.filesystems.FileObject;

/**
 * Describes a CSS property.
 *
 * Description of the grammar defining the property values: (the grammar is
 * *almost* the same as used for the property values definitions in the w3c.org
 * specifications)
 *
 * [ ] denotes a group of elements <ref> represents a reference to another
 * element named ref or -ref *,+,?,{min,max} multiplicity of elements or groups
 * e1 | e2 e1 or e2 e1 || e2 e1 or e2 or both e1 e2 e1 followed by e2 !unit
 * represents a unit recognized by a CssPropertyValueAcceptor
 *
 * Example: <uri> [ , <uri>]* list of URIs separated by comma [ right | left ]
 * || center [ !length | !percentage ] !identifier !identifier{1,4}
 *
 * element name starting with at-sign (
 *
 * @) denotes an artificial property which can be referred by other elements but
 * will not be exposed to the editor (completion, error checks etc..)
 *
 * One may use Utilities.parsePropertyDefinitionFile(pathToTheProperiesFile) to
 * obtain the list of PropertyDescriptor-s from a properties file.
 *
 * @author mfukala@netbeans.org
 */
public class PropertyDefinition {

    private String name, grammar;
    private CssModule cssModule;
    private PropertyCategory propertyCategory;
    private GroupGrammarElement resolved;

    /**
     * Creates an instance of PropertyDefinition with the PropertyCategory.OTHER
     * property category.
     *
     * @param name name of the property
     * @param valueGrammar grammar of the property value
     */
    public PropertyDefinition(String name, String valueGrammar) {
        this(name, valueGrammar, null);
    }

    /**
     * Creates an instance of PropertyDefinition with the PropertyCategory.OTHER
     * property category.
     *
     * @param name name of the property
     * @param valueGrammar grammar of the property value
     * @param module CssModule serving this property definition
     */
    public PropertyDefinition(String name, String valueGrammar, CssModule module) {
        this(name, valueGrammar, PropertyCategory.DEFAULT, module);
    }

    /**
     * Creates an instance of PropertyDefinition.
     *
     * @param name name of the property
     * @param valueGrammar grammar of the property value
     * @param module CssModule serving this property definition
     * @param propertyCategory category of the property
     */
    public PropertyDefinition(String name, String valueGrammar, PropertyCategory propertyCategory, CssModule module) {
        this.name = name;
        this.grammar = valueGrammar;
        this.propertyCategory = propertyCategory;
        this.cssModule = module;
    }

    /**
     * Gets the property value grammar.
     */
    public String getGrammar() {
        return grammar;
    }

    /**
     * Returns the root element of the property grammar.
     *
     * @param file context file
     * @return a non null value.
     */
    public synchronized GroupGrammarElement getGrammarElement(FileObject context) {
        if(resolved == null) {
            resolved = GrammarParser.parse(getGrammar(), getName());
        }
        return resolved;
    }

    /**
     * Gets the {@link CssModule} serving this property definition.
     *
     * @return instance of {@link CssModule}. May be null.
     */
    public CssModule getCssModule() {
        return cssModule;
    }

    /**
     * Gets the property category this property definition belongs to
     *
     */
    public PropertyCategory getPropertyCategory() {
        return propertyCategory;
    }

    /**
     * @return The property name.
     */
    public String getName() {
        return name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PropertyDefinition other = (PropertyDefinition) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
