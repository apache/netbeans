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
package org.netbeans.modules.php.editor.elements;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.PropertyHookElement;
import org.netbeans.modules.php.editor.model.impl.PropertyHookSignatureItem;
import org.netbeans.modules.php.editor.model.nodes.PropertyHookDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.PropertyHookDeclaration;

public class PropertyHookElementImpl extends PhpElementImpl implements PropertyHookElement {

    private final boolean isReference;
    private final boolean isAttributed;
    private final boolean hasBody;
    private final List<ParameterElement> parameters;
    private final OffsetRange offsetRange;

    private PropertyHookElementImpl(Builder builder) {
        super(builder.name, builder.inScope, builder.filenameUrl, builder.offsetRange.getStart(), builder.elementQuery, builder.isDeprecated);
        this.isReference = builder.isReference;
        this.isAttributed = builder.isAttributed;
        this.hasBody = builder.hasBody;
        this.parameters = List.copyOf(builder.parameters);
        this.offsetRange = builder.offsetRange;
    }

    @Override
    public String getSignature() {
        return PropertyHookSignatureItem.getSignatureFromElements(List.of(this));
    }

    @Override
    public PhpElementKind getPhpElementKind() {
        return PhpElementKind.PROPERTY_HOOK;
    }

    @Override
    public boolean isReference() {
        return isReference;
    }

    @Override
    public boolean isAttributed() {
        return isAttributed;
    }

    @Override
    public boolean hasBody() {
        return hasBody;
    }

    @Override
    public List<? extends ParameterElement> getParameters() {
        return parameters;
    }

    @Override
    public OffsetRange getOffsetRange() {
        return offsetRange;
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        return getOffsetRange();
    }

    /**
     * Create property hook elements form PropertyHookSignatureItems.
     *
     * @param signatureItems
     * @param field
     * @param filenameUrl
     * @param elementQuery
     * @return property hook elements
     */
    public static List<PropertyHookElement> fromSignatureItems(final List<PropertyHookSignatureItem> signatureItems, final String field, final String filenameUrl, final ElementQuery elementQuery) {
        List<PropertyHookElement> elements = new ArrayList<>(signatureItems.size());
        for (PropertyHookSignatureItem signatureItem : signatureItems) {
            PropertyHookElement element = new Builder(signatureItem.getName(), field, filenameUrl, signatureItem.getOffsetRange(), elementQuery)
                    .isReference(signatureItem.isReference())
                    .isAttributed(signatureItem.isAttributed())
                    .hasBody(signatureItem.hasBody())
                    .parameters(ParameterElementImpl.parseParameters(signatureItem.getParameterSignature()))
                    .build();
            elements.add(element);
        }
        return elements;
    }

    /**
     * Create property hook elements from the signature.
     *
     * @param signature
     * @param field
     * @param filenameUrl
     * @param elementQuery
     * @return property hook elements
     */
    public static List<PropertyHookElement> fromSignature(String signature, final String field, final String filenameUrl, final ElementQuery elementQuery) {
        List<PropertyHookSignatureItem> signatureItems = PropertyHookSignatureItem.fromSignature(signature);
        return fromSignatureItems(signatureItems, field, filenameUrl, elementQuery);
    }

    /**
     * Create property hook elements from the PropertyHookDeclaration nodes.
     *
     * @param propertyHooks
     * @param field
     * @param filenameUrl
     * @param fileQuery
     * @return property hook elements
     */
    public static List<PropertyHookElement> fromNode(List<PropertyHookDeclaration> propertyHooks, final String field, final String filenameUrl, final ElementQuery.File fileQuery) {
        List<PropertyHookElement> elements = new ArrayList<>(propertyHooks.size());
        for (PropertyHookDeclaration propertyHook : propertyHooks) {
            PropertyHookDeclarationInfo info = PropertyHookDeclarationInfo.create(propertyHook);
            PropertyHookElement element = new Builder(propertyHook.getName().getName(), field, filenameUrl, CodeUtils.getOffsetRagne(propertyHook), fileQuery)
                    .isAttributed(propertyHook.isAttributed())
                    .isReference(propertyHook.isReference())
                    .hasBody(propertyHook.getBody() != null)
                    .parameters(info.getParameters())
                    .build();
            elements.add(element);
        }
        return elements;
    }

    //~ Inner class
    public static class Builder {

        private final String name;
        private final String inScope;
        private final String filenameUrl;
        private final OffsetRange offsetRange;
        private final ElementQuery elementQuery;
        private boolean isReference = false;
        private boolean isAttributed = false;
        private boolean hasBody = false;
        private boolean isDeprecated = false;
        private List<ParameterElement> parameters = List.of();

        public Builder(String name, String inScope, String filenameUrl, OffsetRange offsetRange, ElementQuery elementQuery) {
            this.name = name;
            this.inScope = inScope;
            this.filenameUrl = filenameUrl;
            this.offsetRange = offsetRange;
            this.elementQuery = elementQuery;
        }

        public Builder isReference(boolean isReference) {
            this.isReference = isReference;
            return this;
        }

        public Builder isAttributed(boolean isAttributed) {
            this.isAttributed = isAttributed;
            return this;
        }

        public Builder hasBody(boolean hasBody) {
            this.hasBody = hasBody;
            return this;
        }

        public Builder isDeprecated(boolean isDeprecated) {
            this.isDeprecated = isDeprecated;
            return this;
        }

        public Builder parameters(List<ParameterElement> parameters) {
            this.parameters = List.copyOf(parameters);
            return this;
        }

        public PropertyHookElement build() {
            return new PropertyHookElementImpl(this);
        }
    }
}
