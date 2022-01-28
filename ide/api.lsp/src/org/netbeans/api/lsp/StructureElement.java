/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.lsp;

import java.util.List;
import java.util.Set;

/**
 * StructureElement is a tree item that shows the structure of the source code.
 * 
 * @author Petr Pisl
 * @since 1.8
 */
public interface StructureElement {
   
    /**
     * Kind of the structure element. 
     */
    public static enum Kind {
        File,
	Module,
	Namespace,
	Package,
	Class,
	Method,
	Property,
	Field,
	Constructor,
	Enum,
	Interface,
	Function,
	Variable,
	Constant,
	String,
	Number,
	Boolean,
	Array,
	Object,
	Key,
	Null,
	EnumMember,
	Struct,
	Event,
	Operator,
	TypeParameter
    }
    
    /**
     * Tags are extra annotations that tweak the rendering of a symbol.
     */
    public static enum Tag {
        Deprecated;
    }
    
    /**
     * The name is displayed in the structure tree and other ui.
     * @return The name of this element.
     */
    public String getName();
    
    /**
     * The start of offset range where this element should be selected and revealed 
     * when selected.
     * @return start offset of the selection
     */
    public int getSelectionStartOffset();
    
    /**
     * The end of offset range where this element should be selected and revealed 
     * when selected.
     * @return end offset of the selection
     */
    public int getSelectionEndOffset();
    
    
    /**
     * The expanded range is offset range that is typically used to determine if the cursors
     * inside the element to reveal in the element in the UI. 
     * @return start of the enclosed range
     */
    public int getExpandedStartOffset();
    
    /**
     * The expanded range is offset range that is typically used to determine if the cursors
     * inside the element to reveal in the element in the UI. 
     * @return end of the enclosed range
     */
    public int getExpandedEndOffset();
    
    /**
     * Kind of this symbol
     * @return Kind of this symbol
     */
    public Kind getKind();
    
    /**
     * Tags for this element.
     * @return list of tags
     */
    public Set<Tag> getTags();
    
   /**
   * More detail for this symbol, e.g the signature of a function.If not provided the name is used.
   * @return the detail text for this element
   */
    public String getDetail();
    
    /**
     * Children of this element, e.g. method and fields of a class. 
     * @return 
     */
    public List<? extends StructureElement> getChildren();
    
}
