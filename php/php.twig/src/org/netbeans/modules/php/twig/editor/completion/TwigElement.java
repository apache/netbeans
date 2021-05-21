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
package org.netbeans.modules.php.twig.editor.completion;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public interface TwigElement extends ElementHandle {

    String getTemplate();

    void formatParameters(HtmlFormatter formatter);

    TwigDocumentation getDocumentation();

    public static class Factory {

        public static TwigElement create(final String name, final TwigDocumentationFactory documentationFactory) {
            return new TwigElementWithoutParams(name, documentationFactory);
        }

        public static TwigElement create(final String name, final TwigDocumentationFactory documentationFactory, final List<Parameter> parameters) {
            return new TwigElementWithParams(name, documentationFactory, parameters);
        }

        public static TwigElement create(final String name, final TwigDocumentationFactory documentationFactory, final String customTemplate) {
            return new TwigElementWithCustomTemplate(name, documentationFactory, customTemplate);
        }
    }

    abstract static class BaseTwigElementItem implements TwigElement {

        private final String name;
        private final TwigDocumentationFactory documentationFactory;

        public BaseTwigElementItem(final String name, final TwigDocumentationFactory documentationFactory) {
            this.name = name;
            this.documentationFactory = documentationFactory;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public FileObject getFileObject() {
            return null;
        }

        @Override
        public String getMimeType() {
            return "";
        }

        @Override
        public String getIn() {
            return "";
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.OTHER;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return false;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }

        @Override
        public TwigDocumentation getDocumentation() {
            return documentationFactory.create(getName());
        }
    }

    static class TwigElementWithoutParams extends BaseTwigElementItem {

        public TwigElementWithoutParams(final String name, final TwigDocumentationFactory documentationFactory) {
            super(name, documentationFactory);
        }

        @Override
        public String getTemplate() {
            return getName();
        }

        @Override
        public void formatParameters(final HtmlFormatter formatter) {
        }
    }

    static class TwigElementWithCustomTemplate extends BaseTwigElementItem {

        private final String customTemplate;

        public TwigElementWithCustomTemplate(final String name, final TwigDocumentationFactory documentationFactory, final String customTemplate) {
            super(name, documentationFactory);
            this.customTemplate = customTemplate;
        }

        @Override
        public String getTemplate() {
            return customTemplate;
        }

        @Override
        public void formatParameters(HtmlFormatter formatter) {
        }
    }

    static class TwigElementWithParams extends BaseTwigElementItem {

        private final List<Parameter> parameters;

        public TwigElementWithParams(final String name, final TwigDocumentationFactory documentationFactory, final List<Parameter> parameters) {
            super(name, documentationFactory);
            this.parameters = parameters;
        }

        @Override
        public void formatParameters(final HtmlFormatter formatter) {
            formatter.appendText("("); //NOI18N
            for (int i = 0; i < parameters.size(); i++) {
                Parameter parameter = parameters.get(i);
                if (i != 0) {
                    formatter.appendText(", "); //NOI18N
                }
                parameter.format(formatter);
            }
            formatter.appendText(")"); //NOI18N
        }

        @Override
        public String getTemplate() {
            StringBuilder template = new StringBuilder();
            template.append(getName());
            template.append("("); //NOI18N
            for (int i = 0; i < parameters.size(); i++) {
                Parameter parameter = parameters.get(i);
                if (i != 0) {
                    template.append(", "); //NOI18N
                }
                parameter.prepareTemplate(template);
            }
            template.append(")"); //NOI18N
            return template.toString();
        }
    }

    public static class Parameter {

        public enum Need {

            OPTIONAL,
            MANDATORY;
        }
        private final String name;
        private final Need need;

        public Parameter(final String name) {
            this(name, Need.MANDATORY);
        }

        public Parameter(final String name, final Need need) {
            this.name = name;
            this.need = need;
        }

        public void format(final HtmlFormatter formatter) {
            if (!isMandatory()) {
                formatter.appendText(name);
            } else {
                formatter.emphasis(true);
                formatter.appendText(name);
                formatter.emphasis(false);
            }
        }

        public void prepareTemplate(StringBuilder template) {
            template.append("${").append(name).append("}"); //NOI18N
        }

        private boolean isMandatory() {
            return Need.MANDATORY.equals(need);
        }
    }
}
