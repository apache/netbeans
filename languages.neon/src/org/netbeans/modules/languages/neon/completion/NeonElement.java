/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
