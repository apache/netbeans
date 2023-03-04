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
package org.netbeans.modules.php.latte.completion;

import java.util.Arrays;
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
public interface LatteElement extends ElementHandle {

    String getTemplate();

    void formatParameters(HtmlFormatter formatter);

    String getDocumentationText();

    public static class MacroFactory {
        private static final String END_MACRO_MARKER = "/"; //NOI18N
        private static final LatteDocumentationFactory DF = LatteDocumentationFactory.MacroDocumentationFactory.getInstance();

        public static LatteElement create(String name, String macroParameter, String customTemplate) {
            return new LatteElementExtended(name, Arrays.asList(new Parameter[] {new MacroParameter(macroParameter)}), customTemplate, DF);
        }

        public static LatteElement create(String name) {
            return new LatteElementSimple(name, DF);
        }

        public static LatteElement createEnd(String name) {
            return new LatteElementSimple(END_MACRO_MARKER + name, DF);
        }

    }

    public static class HelperFactory {
        private static final LatteDocumentationFactory DF = LatteDocumentationFactory.HelperDocumentationFactory.getInstance();

        public static LatteElement create(String name) {
            return new LatteElementSimple(name, DF);
        }

        public static LatteElement create(String name, List<Parameter> parameters) {
            return new LatteElementExtended(name, parameters, DF);
        }

    }

    public static class KeywordFactory {
        private static final LatteDocumentationFactory DF = LatteDocumentationFactory.KeywordDocumentationFactory.getInstance();

        public static LatteElement create(String name) {
            return new LatteElementSimple(name, DF);
        }

    }

    public static class IteratorItemFactory {
        private static final LatteDocumentationFactory DF = LatteDocumentationFactory.IteratorItemDocumentationFactory.getInstance();

        public static LatteElement create(String name) {
            return new LatteElementSimple(name, DF);
        }

    }

    public static class VariableFactory {
        private static final LatteDocumentationFactory DF = LatteDocumentationFactory.VariableDocumentationFactory.getInstance();

        public static LatteElement create(String name) {
            return new LatteElementSimple(name, DF);
        }

    }

    public static class ControlFactory {
        private static final LatteDocumentationFactory DF = LatteDocumentationFactory.ControlDocumentationFactory.getInstance();

        public static LatteElement create(String name) {
            return new LatteElementSimple(name, DF);
        }

    }

    abstract static class BaseLatteElementItem implements LatteElement {
        private final String name;
        private final LatteDocumentationFactory documentationFactory;

        public BaseLatteElementItem(String name, LatteDocumentationFactory documentationFactory) {
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
            return Collections.<Modifier>emptySet();
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
        public String getDocumentationText() {
            LatteDocumentation documentation = documentationFactory.create(name.startsWith(MacroFactory.END_MACRO_MARKER) ? name.substring(1) : name);
            return documentation.getHeader() + documentation.getContent();
        }

    }

    static class LatteElementSimple extends BaseLatteElementItem {

        public LatteElementSimple(String name, LatteDocumentationFactory documentationFactory) {
            super(name, documentationFactory);
        }

        @Override
        public String getTemplate() {
            return getName();
        }

        @Override
        public void formatParameters(HtmlFormatter formatter) {
        }

    }

    static class LatteElementExtended extends BaseLatteElementItem {
        private final List<Parameter> parameters;
        private final String customTemplate;

        public LatteElementExtended(String name, List<Parameter> parameters, LatteDocumentationFactory documentationFactory) {
            this(name, parameters, null, documentationFactory);
        }

        public LatteElementExtended(String name, List<Parameter> parameters, String customTemplate, LatteDocumentationFactory documentationFactory) {
            super(name, documentationFactory);
            this.parameters = parameters;
            this.customTemplate = customTemplate;
        }

        @Override
        public void formatParameters(final HtmlFormatter formatter) {
            for (int i = 0; i < parameters.size(); i++) {
                Parameter parameter = parameters.get(i);
                formatter.appendText(parameter.getDelimiter());
                parameter.format(formatter);
            }
        }

        @Override
        public String getTemplate() {
            String result = customTemplate;
            if (result == null) {
                StringBuilder template = new StringBuilder();
                template.append(getName());
                for (int i = 0; i < parameters.size(); i++) {
                    Parameter parameter = parameters.get(i);
                    parameter.prepareTemplate(template);
                }
                result = template.toString();
            }
            return result;
        }
    }

    public static class Parameter {
        private final String delimiter;
        private final String name;
        private final String defaultValue;

        public Parameter(String delimiter, String name, String defaultValue) {
            this.delimiter = delimiter;
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public Parameter(String delimiter, String name) {
            this(delimiter, name, null);
        }

        public void format(HtmlFormatter formatter) {
            formatter.parameters(true);
            if (isMandatory()) {
                formatter.appendText(name);
            } else {
                formatter.appendText(name);
                formatter.appendText("="); //NOI18N
                formatter.appendText(defaultValue);
            }
            formatter.parameters(false);
        }

        public void prepareTemplate(StringBuilder template) {
            if (isMandatory()) {
                template.append(getDelimiter()).append("${").append(name).append("}"); //NOI18N
            }
        }

        private boolean isMandatory() {
            return defaultValue == null;
        }

        public String getDelimiter() {
            return delimiter;
        }
    }

    public static final class HelperParameter extends Parameter {

        public HelperParameter(String name) {
            this(name, null);
        }

        public HelperParameter(String name, String defaultValue) {
            super(":", name, defaultValue); //NOI18N
        }

    }

    public static final class MacroParameter extends Parameter {

        public MacroParameter(String name) {
            this(name, null);
        }

        public MacroParameter(String name, String defaultValue) {
            super(" ", name, defaultValue); //NOI18N
        }

    }

}
