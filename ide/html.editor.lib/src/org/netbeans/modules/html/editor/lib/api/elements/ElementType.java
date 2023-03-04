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
package org.netbeans.modules.html.editor.lib.api.elements;

/**
 * A representation of html source code elements types.
 * 
 * Used for both plain view and tree view.
 *
 * @author marekfukala
 */
public enum ElementType {

    ATTRIBUTE,
    
    /**
     * Root element of each parse tree. 
     * 
     * Element may contain subelements.
     */
    ROOT,

    /**
     * Html code comment element. 
     * 
     * Represents a leaf element (no subelements).
     */
    COMMENT,
    
    /**
     * Html declaration (<!doctype ...> for example).
     * 
     * Represents a leaf element (no subelements).
     */
    DECLARATION,
    
    /**
     * Html text.
     * 
     * Represents a leaf element (no subelements).
     */
    TEXT,
    
    /**
     * Html entity reference (&nbsp).
     * 
     * Represents a leaf element (no subelements).
     */
    ENTITY_REFERENCE,
    
    /**
     * A syntax error in the html source code.
     * 
     * Represents a leaf element (no subelements).
     */
    ERROR,
    
    /**
     * Html or xml open tag (<div>, <jsp:useBean>).
     * 
     * Element may contain subelements.
     */
    OPEN_TAG, 
    
    /**
     * Html or xml end tag (</div>, </jsp:useBean>).
     * 
     * Represents a leaf element (no subelements).
     */
    CLOSE_TAG

}
