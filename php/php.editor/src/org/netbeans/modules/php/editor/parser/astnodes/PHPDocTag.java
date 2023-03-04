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
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 * Represent a PHPDoc tag in the php documentation
 * @author Petr Pisl
 */
public class PHPDocTag extends ASTNode {

    public enum Type implements AnnotationParsedLine {
        GLOBAL("global"), //NOI18N
        METHOD("method"), //NOI18N
        PROPERTY("property"), PROPERTY_READ("property-read"), PROPERTY_WRITE("property-write"), //NOI18N
        PARAM("param"), //NOI18N
        RETURN("return"), //NOI18N
        VAR("var"), //NOI18N
        MIXIN("mixin"), // NOI18N
        DEPRECATED("deprecated"); //NOI18N

        private final String name;

        private Type(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            //description of these annotations is parsed by an editor parser itself
            return "";
        }

        @Override
        public Map<OffsetRange, String> getTypes() {
            //types of these annotations are parsed by an editor parser itself
            return Collections.<OffsetRange, String>emptyMap();
        }

        @Override
        public boolean startsWithAnnotation() {
            //all default annotations start at the start of the parsed line
            return true;
        }

    }

    private final AnnotationParsedLine type;
    private final String value;

    public PHPDocTag(int start, int end, AnnotationParsedLine kind, String value) {
        super(start, end);
        this.type = kind;
        this.value = value;
    }

    public AnnotationParsedLine getKind() {
        return this.type;
    }

    /**
     *
     * @return this is the whole text of the tag
     */
    public String getValue() {
        return value;
    }

    /**
     *
     * @return the documentation for a tag
     */
    public String getDocumentation() {
        return value;
    }

    @Override
    public void accept(Visitor visitor) {
            visitor.visit(this);
    }
}
