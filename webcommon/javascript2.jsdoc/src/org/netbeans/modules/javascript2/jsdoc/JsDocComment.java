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
package org.netbeans.modules.javascript2.jsdoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.doc.spi.DocParameter;
import org.netbeans.modules.javascript2.doc.spi.JsComment;
import org.netbeans.modules.javascript2.doc.spi.JsModifier;
import org.netbeans.modules.javascript2.jsdoc.model.DeclarationElement;
import org.netbeans.modules.javascript2.jsdoc.model.DescriptionElement;
import org.netbeans.modules.javascript2.jsdoc.model.JsDocElement;
import org.netbeans.modules.javascript2.jsdoc.model.JsDocElementType;
import org.netbeans.modules.javascript2.jsdoc.model.JsDocElementUtils;
import org.netbeans.modules.javascript2.jsdoc.model.NamedParameterElement;
import org.netbeans.modules.javascript2.jsdoc.model.UnnamedParameterElement;
import org.netbeans.modules.javascript2.types.api.Type;

/**
 * Represents block of jsDoc comment which contains particular {@link JsDocTag}s.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocComment extends JsComment {

    private final Map<JsDocElementType, List<JsDocElement>> tags = new EnumMap<>(JsDocElementType.class);
    private final JsDocCommentType type;

    /**
     * Creates new {@code JsComment} with given parameters.
     *
     * @param offsetRange offset range of the comment
     * @param type comment {@code JsDocCommentType}
     * @param elements list of tags contained in this block, never {@code null}
     */
    public JsDocComment(OffsetRange offsetRange, JsDocCommentType type, List<JsDocElement> elements) {
        super(offsetRange);
        this.type = type;
        initTags(elements);
    }

    /**
     * Gets type of the jsDoc block comment.
     * @return type of the jsDoc block comment
     */
    public JsDocCommentType getType() {
        return type;
    }

    @Override
    public List<String> getSummary() {
        List<? extends JsDocElement> descriptionElements = getTagsForTypes(new JsDocElementType[]{
            JsDocElementType.DESCRIPTION,
            JsDocElementType.CONTEXT_SENSITIVE,
            JsDocElementType.CLASS
        });

        List<String> summaries = new ArrayList<>(descriptionElements.size());
        for (JsDocElement jsDocElement : descriptionElements) {
            summaries.add(((DescriptionElement) jsDocElement).getDescription());
        }

        return summaries;
    }

    @Override
    public List<String> getSyntax() {
        List<? extends JsDocElement> syntaxElements = getTagsForType(JsDocElementType.SYNTAX);
        List<String> syntaxes = new ArrayList<>(syntaxElements.size());
        for (JsDocElement jsDocElement : syntaxElements) {
            syntaxes.add(((DescriptionElement) jsDocElement).getDescription());
        }
        return syntaxes;
    }

    @Override
    public DocParameter getReturnType() {
        for (JsDocElement jsDocElement : getTagsForTypes(
                new JsDocElementType[]{JsDocElementType.RETURN, JsDocElementType.RETURNS})) {
            return  (DocParameter) jsDocElement;
        }
        for (JsDocElement jsDocElement : getTagsForType(JsDocElementType.TYPE)) {
            return UnnamedParameterElement.create(
                    jsDocElement.getType(),
                    JsDocElementUtils.parseTypes(((DeclarationElement) jsDocElement).getDeclaredType().getType(), ((DeclarationElement) jsDocElement).getDeclaredType().getOffset()),
                    "");
        }
        return null;
    }

    @Override
    public List<DocParameter> getParameters() {
        List<? extends JsDocElement> parameterElements = getTagsForTypes(new JsDocElementType[]{
            JsDocElementType.PARAM,
            JsDocElementType.ARGUMENT
        });
        List<DocParameter> params = new ArrayList<>(parameterElements.size());
        for (JsDocElement jsDocElement : parameterElements) {
            params.add((NamedParameterElement) jsDocElement);
        }
        return params;
    }

    @Override
    public String getDeprecated() {
        List<? extends JsDocElement> deprecatedTags = getTagsForType(JsDocElementType.DEPRECATED);
        if (deprecatedTags.isEmpty()) {
            return null;
        } else {
            return ((DescriptionElement) deprecatedTags.get(0)).getDescription();
        }
    }

    @Override
    public Set<JsModifier> getModifiers() {
        Set<JsModifier> modifiers = EnumSet.noneOf(JsModifier.class);
        for (JsDocElement jsDocElement : getTagsForTypes(new JsDocElementType[]{
                JsDocElementType.PRIVATE, JsDocElementType.PUBLIC, JsDocElementType.STATIC})) {
            modifiers.add(JsModifier.fromString(jsDocElement.getType().toString().substring(1)));
        }
        return modifiers;
    }

    @Override
    public List<DocParameter> getThrows() {
        List<? extends JsDocElement> throwsElements = getTagsForType(JsDocElementType.THROWS);
        List<DocParameter> throwsEntries = new ArrayList<>(throwsElements.size());
        for (JsDocElement exceptionTag : throwsElements) {
            throwsEntries.add((UnnamedParameterElement) exceptionTag);
        }
        return throwsEntries;
    }

    @Override
    public List<Type> getExtends() {
        List<? extends JsDocElement> extendsElements = getTagsForTypes(new JsDocElementType[]{
            JsDocElementType.EXTENDS,
            JsDocElementType.AUGMENTS
        });
        List<Type> extendsEntries = new ArrayList<>(extendsElements.size());
        for (JsDocElement extend : extendsElements) {
            DeclarationElement ident = (DeclarationElement) extend;
            extendsEntries.add(ident.getDeclaredType());
        }
        return extendsEntries;
    }

    @Override
    public List<String> getSee() {
        List<? extends JsDocElement> seeElements = getTagsForType(JsDocElementType.SEE);
        List<String> sees = new ArrayList<>(seeElements.size());
        for (JsDocElement extend : seeElements) {
            DescriptionElement element = (DescriptionElement) extend;
            sees.add(element.getDescription());
        }
        return sees;
    }

    @Override
    public String getSince() {
        List<? extends JsDocElement> since = getTagsForType(JsDocElementType.SINCE);
        if (since.isEmpty()) {
            return null;
        } else {
            return ((DescriptionElement) since.get(0)).getDescription();
        }
    }

    @Override
    public boolean isClass() {
        return !getTagsForTypes(new JsDocElementType[]{JsDocElementType.CLASS, JsDocElementType.CONSTRUCTOR,
            JsDocElementType.CONSTRUCTS}).isEmpty();
    }

    @Override
    public boolean isConstant() {
        return !getTagsForTypes(new JsDocElementType[]{JsDocElementType.CONSTANT}).isEmpty();
    }

    @Override
    public List<String> getExamples() {
        List<? extends JsDocElement> exampleElements = getTagsForType(JsDocElementType.EXAMPLE);
        List<String> examples = new ArrayList<>(exampleElements.size());
        for (JsDocElement extend : getTagsForType(JsDocElementType.EXAMPLE)) {
            DescriptionElement element = (DescriptionElement) extend;
            examples.add(element.getDescription());
        }
        return examples;
    }

    private void initTags(List<JsDocElement> elements) {
        for (JsDocElement jsDocElement : elements) {
            List<JsDocElement> list = tags.get(jsDocElement.getType());
            if (list == null) {
                list = new ArrayList<>();
                tags.put(jsDocElement.getType(), list);
            }
            tags.get(jsDocElement.getType()).add(jsDocElement);
        }
    }

    /**
     * Gets list of all {@code JsDocTag}s.
     * <p>
     * Should be used just in testing use cases.
     * @return list of {@code JsDocTag}s
     */
    protected List<? extends JsDocElement> getTags() {
        List<JsDocElement> allTags = new ArrayList<>();
        for (List<JsDocElement> list : tags.values()) {
            allTags.addAll(list);
        }
        return allTags;
    }

    /**
     * Gets list of {@code JsDocTag}s of given type.
     * @return list of {@code JsDocTag}s
     */
    public List<? extends JsDocElement> getTagsForType(JsDocElementType type) {
        List<JsDocElement> tagsForType = tags.get(type);
        return tagsForType == null ? Collections.<JsDocElement>emptyList() : tagsForType;
    }

    /**
     * Gets list of {@code JsDocTag}s of given types.
     * @return list of {@code JsDocTag}s
     */
    public List<? extends JsDocElement> getTagsForTypes(JsDocElementType[] types) {
        List<JsDocElement> list = new ArrayList<>();
        for (JsDocElementType elementType : types) {
            list.addAll(getTagsForType(elementType));
        }
        return list;
    }

    @Override
    public List<DocParameter> getProperties() {
        List<? extends JsDocElement> propertyElements = getTagsForType(JsDocElementType.PROPERTY);
        List<DocParameter> properties = new ArrayList<>(propertyElements.size());
        for (JsDocElement jsDocElement : propertyElements) {
            properties.add(((NamedParameterElement) jsDocElement));
        }
        return properties;
    }

    @Override
    public DocParameter getDefinedType() {
        List<? extends JsDocElement> typedefs = getTagsForType(JsDocElementType.TYPEDEF);
        return typedefs.isEmpty() ? null : ((NamedParameterElement) typedefs.get(0));
    }

    @Override
    public List<Type> getTypes() {
        List<? extends JsDocElement> propertyElements = getTagsForType(JsDocElementType.CALLBACK);
        List<Type> properties = new ArrayList<>(propertyElements.size());
        for (JsDocElement jsDocElement : propertyElements) {
            properties.add(((DeclarationElement) jsDocElement).getDeclaredType());
        }
        return properties;
    }

    @Override
    public Type getCallBack() {
        List<? extends JsDocElement> callbacks = getTagsForType(JsDocElementType.CALLBACK);
        return callbacks.isEmpty() ? null : ((DeclarationElement) callbacks.get(0)).getDeclaredType();
    }


}