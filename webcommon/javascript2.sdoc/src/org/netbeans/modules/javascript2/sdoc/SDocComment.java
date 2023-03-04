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
package org.netbeans.modules.javascript2.sdoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.doc.spi.JsModifier;
import org.netbeans.modules.javascript2.doc.spi.DocParameter;
import org.netbeans.modules.javascript2.doc.spi.JsComment;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.sdoc.elements.SDocDescriptionElement;
import org.netbeans.modules.javascript2.sdoc.elements.SDocElement;
import org.netbeans.modules.javascript2.sdoc.elements.SDocElementType;
import org.netbeans.modules.javascript2.sdoc.elements.SDocIdentifierElement;
import org.netbeans.modules.javascript2.sdoc.elements.SDocTypeDescribedElement;
import org.netbeans.modules.javascript2.sdoc.elements.SDocTypeNamedElement;
import org.netbeans.modules.javascript2.sdoc.elements.SDocTypeSimpleElement;
import org.netbeans.modules.javascript2.types.api.TypeUsage;

/**
 * Represents documentation comment block of ScriptDoc.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SDocComment extends JsComment {

    private final Map<SDocElementType, List<SDocElement>> tags = new EnumMap<SDocElementType, List<SDocElement>>(SDocElementType.class);

    public SDocComment(OffsetRange offsetRange, List<SDocElement> elements) {
        super(offsetRange);
        initComment(elements);
    }

    @Override
    public List<String> getSummary() {
        List<String> summaries = new LinkedList<String>();
        for (SDocElement sDocElement : getTagsForTypes(
                new SDocElementType[]{SDocElementType.DESCRIPTION, SDocElementType.CLASS_DESCRIPTION, SDocElementType.PROJECT_DESCRIPTION})) {
            summaries.add(((SDocDescriptionElement) sDocElement).getDescription());
        }
        return summaries;
    }

    @Override
    public List<String> getSyntax() {
        return Collections.<String>emptyList();
    }

    @Override
    public DocParameter getReturnType() {
        for (SDocElement sDocElement : getTagsForTypes(
                new SDocElementType[]{SDocElementType.RETURN, SDocElementType.TYPE, SDocElementType.PROPERTY})) {
            return (SDocTypeSimpleElement) sDocElement;
        }
        return null;
    }

    @Override
    public List<DocParameter> getParameters() {
        List<DocParameter> params = new LinkedList<DocParameter>();
        for (SDocElement jsDocElement : getTagsForType(SDocElementType.PARAM)) {
            params.add((SDocTypeNamedElement) jsDocElement);
        }
        return params;
    }

    @Override
    public String getDeprecated() {
        if (getTagsForType(SDocElementType.DEPRECATED).isEmpty()) {
            return null;
        } else {
            return "";
        }
    }

    @Override
    public Set<JsModifier> getModifiers() {
        Set<JsModifier> modifiers = EnumSet.noneOf(JsModifier.class);
        for (SDocElement jsDocElement : getTagsForType(SDocElementType.PRIVATE)) {
            modifiers.add(JsModifier.fromString(jsDocElement.getType().toString().substring(1)));
        }
        return modifiers;
    }

    @Override
    public List<DocParameter> getThrows() {
        List<DocParameter> throwsEntries = new LinkedList<DocParameter>();
        for (SDocElement exceptionTag : getTagsForType(SDocElementType.EXCEPTION)) {
            throwsEntries.add((SDocTypeDescribedElement) exceptionTag);
        }
        return throwsEntries;
    }

    @Override
    public List<Type> getExtends() {
        List<Type> extendsEntries = new LinkedList<Type>();
        for (SDocElement extend : getTagsForType(SDocElementType.INHERITS)) {
            SDocIdentifierElement ident = (SDocIdentifierElement) extend;
            extendsEntries.add(new TypeUsage(ident.getIdentifier(), -1));
        }
        return extendsEntries;
    }

    @Override
    public List<String> getSee() {
        List<String> sees = new LinkedList<String>();
        for (SDocElement extend : getTagsForType(SDocElementType.SEE)) {
            SDocDescriptionElement element = (SDocDescriptionElement) extend;
            sees.add(element.getDescription());
        }
        return sees;
    }

    @Override
    public String getSince() {
        List<? extends SDocElement> since = getTagsForType(SDocElementType.SINCE);
        if (since.isEmpty()) {
            return null;
        } else {
            return ((SDocDescriptionElement) since.get(0)).getDescription();
        }
    }

    @Override
    public boolean isClass() {
        return !getTagsForTypes(new SDocElementType[]{SDocElementType.CONSTRUCTOR}).isEmpty();
    }
    
    @Override
    public boolean isConstant() {
        return !getTagsForTypes(new SDocElementType[]{SDocElementType.CONSTANT}).isEmpty();
    }

//    @Override
//    public List<String> getAuthor() {
//        List<String> authors = new LinkedList<String>();
//        for (SDocElement extend : getTagsForType(SDocElementType.AUTHOR)) {
//            SDocDescriptionElement element = (SDocDescriptionElement) extend;
//            authors.add(element.getDescription());
//        }
//        return authors;
//    }
//
//    @Override
//    public String getVersion() {
//        List<? extends SDocElement> version = getTagsForType(SDocElementType.VERSION);
//        if (version.isEmpty()) {
//            return null;
//        } else {
//            return ((SDocDescriptionElement) version.get(0)).getDescription();
//        }
//    }

    @Override
    public List<String> getExamples() {
        List<String> examples = new LinkedList<String>();
        for (SDocElement extend : getTagsForType(SDocElementType.EXAMPLE)) {
            SDocDescriptionElement element = (SDocDescriptionElement) extend;
            examples.add(element.getDescription());
        }
        return examples;
    }

    private void initComment(List<SDocElement> elements) {
        for (SDocElement element : elements) {
            List<SDocElement> list = tags.get(element.getType());
            if (list == null) {
                list = new LinkedList<SDocElement>();
                tags.put(element.getType(), list);
            }
            tags.get(element.getType()).add(element);
        }
    }

    /**
     * Gets list of all {@code SDocTag}s inside this comment. <p> Used just in testing use cases.
     *
     * @return list of {@code SDocTag}s
     */
    protected List<? extends SDocElement> getTags() {
        List<SDocElement> allTags = new ArrayList<SDocElement>();
        for (List<SDocElement> list : tags.values()) {
            allTags.addAll(list);
        }
        return allTags;
    }

    /**
     * Gets list of {@code SDocElement}s of given type.
     *
     * @return list of {@code SDocElement}s
     */
    public List<? extends SDocElement> getTagsForType(SDocElementType type) {
        List<SDocElement> tagsForType = tags.get(type);
        return tagsForType == null ? Collections.<SDocElement>emptyList() : tagsForType;
    }

    /**
     * Gets list of {@code JsDocTag}s of given types.
     *
     * @return list of {@code JsDocTag}s
     */
    public List<? extends SDocElement> getTagsForTypes(SDocElementType[] types) {
        List<SDocElement> list = new LinkedList<SDocElement>();
        for (SDocElementType type : types) {
            list.addAll(getTagsForType(type));
        }
        return list;
    }

    @Override
    public List<DocParameter> getProperties() {
        List<DocParameter> properties = new LinkedList<DocParameter>();
        for (SDocElement jsDocElement : getTagsForType(SDocElementType.PROPERTY)) {
            if (jsDocElement instanceof SDocTypeNamedElement) {
                properties.add((SDocTypeNamedElement) jsDocElement);
            }
        }
        return properties;
    }

    @Override
    public DocParameter getDefinedType() {
        return null;
    }

    @Override
    public List<Type> getTypes() {
        return Collections.emptyList();
    }

    @Override
    public Type getCallBack() {
        return null;
    }
    
}
