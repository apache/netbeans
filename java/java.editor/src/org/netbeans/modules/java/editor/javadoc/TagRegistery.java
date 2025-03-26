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

package org.netbeans.modules.java.editor.javadoc;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ElementKind;

/**
 *
 * @author Jan Pokorsky
 */
final class TagRegistery {
    
    private static final Set<ElementKind> ALL_KINDS = EnumSet.of(
            ElementKind.ANNOTATION_TYPE, ElementKind.CLASS,
            ElementKind.CONSTRUCTOR, ElementKind.ENUM,
            ElementKind.ENUM_CONSTANT, ElementKind.FIELD,
            ElementKind.INTERFACE, ElementKind.RECORD, ElementKind.METHOD,
            // OTHER stands for Overview here
            ElementKind.OTHER, ElementKind.MODULE,
            ElementKind.PACKAGE);
    
    private static final TagRegistery DEFAULT = new TagRegistery();
    
    private final List<TagEntry> tags;
    
    public static TagRegistery getDefault() {
        return DEFAULT;
    }

    public List<TagEntry> getTags(ElementKind kind, boolean inline) {
        List<TagEntry> selection = new ArrayList<TagEntry>();
        for (TagEntry te : tags) {
            if (te.isInline == inline && te.whereUsed.contains(kind)) {
                selection.add(te);
            }
        }
        return selection;
    }
    
    private TagRegistery() {
        this.tags = new ArrayList<TagEntry>(20);
        addTag("@author", false, EnumSet.of(
                ElementKind.ANNOTATION_TYPE, ElementKind.CLASS,
                ElementKind.ENUM, ElementKind.INTERFACE, ElementKind.RECORD,
                ElementKind.OTHER, ElementKind.MODULE,
                ElementKind.PACKAGE));
        addTag("@exception", false, EnumSet.of(ElementKind.METHOD, ElementKind.CONSTRUCTOR));
        // deprecated: not in PACKAGE and OVERVIEW!
        addTag("@deprecated", false, EnumSet.of(
                ElementKind.ANNOTATION_TYPE, ElementKind.CLASS,
                ElementKind.CONSTRUCTOR, ElementKind.ENUM,
                ElementKind.ENUM_CONSTANT, ElementKind.FIELD,
                ElementKind.INTERFACE, ElementKind.METHOD, ElementKind.RECORD));

        addTag("@param", false, EnumSet.of(
                ElementKind.METHOD, ElementKind.CONSTRUCTOR, ElementKind.CLASS,
                ElementKind.INTERFACE, ElementKind.RECORD));
        addTag("@return", false, EnumSet.of(ElementKind.METHOD));
        addTag("@see", false, ALL_KINDS);
        addTag("@serial", false, EnumSet.of(
                ElementKind.ANNOTATION_TYPE, ElementKind.CLASS,
                ElementKind.ENUM,
                ElementKind.ENUM_CONSTANT, ElementKind.FIELD,
                ElementKind.INTERFACE, ElementKind.RECORD,
                ElementKind.PACKAGE));

        // serialData can be used just for writeObject, readObject, writeExternal, readExternal, writeReplace, and readResolve methods
        addTag("@serialData", false, EnumSet.of(ElementKind.METHOD));
        addTag("@serialField", false, EnumSet.of(ElementKind.FIELD));
        addTag("@since", false, ALL_KINDS);
        addTag("@throws", false, EnumSet.of(ElementKind.METHOD, ElementKind.CONSTRUCTOR));
        addTag("@version", false, EnumSet.of(
                ElementKind.ANNOTATION_TYPE, ElementKind.CLASS,
                ElementKind.ENUM,
                ElementKind.INTERFACE, ElementKind.RECORD,
                ElementKind.OTHER, ElementKind.PACKAGE));
        addTag("@hidden", false, ALL_KINDS);
        addTag("@provides", false, EnumSet.of(ElementKind.MODULE));
        addTag("@uses", false, EnumSet.of(ElementKind.MODULE));
        
        addTag("@code", true, ALL_KINDS);
        addTag("@snippet", true, ALL_KINDS);
        addTag("@summary", true, ALL_KINDS);
        addTag("@systemProperty", true, EnumSet.of(
            ElementKind.ANNOTATION_TYPE, ElementKind.CLASS,
            ElementKind.CONSTRUCTOR, ElementKind.ENUM,
            ElementKind.ENUM_CONSTANT, ElementKind.FIELD,
            ElementKind.INTERFACE, ElementKind.METHOD, ElementKind.RECORD,
            ElementKind.MODULE, ElementKind.PACKAGE));
        addTag("@docRoot", true, ALL_KINDS);
        addTag("@index", true, ALL_KINDS);
        // just in empty tag description
        addTag("@inheritDoc", true, EnumSet.of(ElementKind.METHOD));
        addTag("@link", true, ALL_KINDS);
        addTag("@linkplain", true, ALL_KINDS);
        addTag("@literal", true, ALL_KINDS);
        addTag("@value", true, EnumSet.of(ElementKind.FIELD));
    }
    
    void addTag(String name, boolean isInline, Set<ElementKind> where) {
        if (name == null || where == null) {
            throw new NullPointerException();
        }
        TagEntry te = new TagEntry();
        te.name = name;
        te.isInline = isInline;
        te.whereUsed = where;
        this.tags.add(te);
    }
    
    static final class TagEntry {
        String name;
        boolean isInline;
        Set<ElementKind> whereUsed;
        String format;
    }
}
