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
package org.netbeans.modules.languages.neon.completion;

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public interface NeonElement extends ElementHandle {

    String getTemplate();

    String getType();

    public static class Factory {
        private static final String NAMESPACE_SEPARATOR = "\\"; //NOI18N

        public static NeonElement create(String name) {
            return new NeonSimpleElement(name);
        }

        public static NeonElement createType(String typeName) {
            String[] nameParts = typeName.split("\\" + NAMESPACE_SEPARATOR); //NOI18N
            String unqualifiedName = nameParts[nameParts.length - 1];
            return new NeonExtendedElement(unqualifiedName, typeName.startsWith(NAMESPACE_SEPARATOR) ? typeName.substring(1) : typeName);
        }

        public static NeonElement createMethod(String methodName, String typeName) {
            return new NeonTypedElement(methodName, typeName, typeName + "::" + methodName); //NOI18N
        }

        public static NeonElement create(String name, String template) {
            return new NeonExtendedElement(name, template);
        }

    }

    abstract static class BaseNeonElementItem implements NeonElement {
        private final String name;

        public BaseNeonElementItem(String name) {
            this.name = name;
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
        public String getType() {
            return getTemplate();
        }

    }

    static final class NeonSimpleElement extends BaseNeonElementItem {

        private NeonSimpleElement(String name) {
            super(name);
        }

        @Override
        public String getTemplate() {
            return getName();
        }
    }

    static final class NeonExtendedElement extends BaseNeonElementItem {
        private final String template;

        private NeonExtendedElement(String name, String template) {
            super(name);
            this.template = template;
        }

        @Override
        public String getTemplate() {
            return template;
        }
    }

    static final class NeonTypedElement extends BaseNeonElementItem {
        private final String template;
        private final String type;

        private NeonTypedElement(String name, String type, String template) {
            super(name);
            this.template = template;
            this.type = type;
        }

        @Override
        public String getTemplate() {
            return template;
        }

        @Override
        public String getType() {
            return type;
        }

    }

}
