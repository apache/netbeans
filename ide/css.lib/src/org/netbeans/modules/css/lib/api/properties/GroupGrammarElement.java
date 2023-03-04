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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mfukala@netbeans.org
 */
public class GroupGrammarElement extends GrammarElement {

    int index;

    public enum Type {

        /**
         * One of the group members needs to be resolved.
         * 
         * Represented by the | operator in the grammar definition: a | b
         */
        SET,
        
        /** 
         * Any of the elements can be present in the value (at least one of them????).
         * 
         * Represented by the || operator in the grammar definition: a || b
         */
        COLLECTION,
        
        /**
         * All of the group members needs to be resolved in the defined order.
         * 
         * Represented by empty operator (WS) in the grammar definition: a b
         */
        LIST,
        
        /**
         * All of the group members needs to be present in arbitrary order
         * 
         * Represented by the && operator in the grammar definition: a && b
         */
        ALL;
    }

    public GroupGrammarElement(GroupGrammarElement parent, int index, String referenceName) {
        super(parent, referenceName);
        this.index = index;
        this.type = Type.LIST; //default type
    }

    public GroupGrammarElement(GroupGrammarElement parent, int index) {
        this(parent, index, null);
    }
    
    private List<GrammarElement> elements = new ArrayList<>(5);
    private Type type;

    
    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public void accept(GrammarElementVisitor visitor) {
        boolean recurse = visitor.visit(this);
        if (recurse) {
            for (GrammarElement child : elements()) {
                child.accept(visitor);
            }
        }
    }

    /**
     * 
     * @return List of children elements
     */
    public List<GrammarElement> elements() {
        return elements;
    }

    public void addElement(GrammarElement element) {
        elements.add(element);
    }

    public List<GrammarElement> getAllPossibleValues() {
        List<GrammarElement> all = new ArrayList<>(10);
        if (getType() == Type.LIST) {
            //sequence
            GrammarElement e = elements.get(0); //first element
            if (e instanceof GroupGrammarElement) {
                all.addAll(((GroupGrammarElement) e).getAllPossibleValues());
            } else {
                all.add(e);
            }
        } else {
            //list or set
            for (GrammarElement e : elements()) {
                if (e instanceof GroupGrammarElement) {
                    all.addAll(((GroupGrammarElement) e).getAllPossibleValues());
                } else {
                    all.add(e);
                }
            }
        }
        return all;
    }
    
    /**
     * Checks if the property is a visible property (not '@' prefixed)
     */
    public boolean isVisible() {
        return getName() != null && getName().charAt(0) != GrammarElement.INVISIBLE_PROPERTY_PREFIX;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(getType().name().charAt(0));
        sb.append(index);
        if (getName() != null) {
            sb.append("|").append(getName()); //NOI18N
        }
        sb.append(']');
        return sb.toString(); //NOI18N
    }
}
