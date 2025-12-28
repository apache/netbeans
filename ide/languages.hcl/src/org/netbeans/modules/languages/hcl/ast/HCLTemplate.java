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
package org.netbeans.modules.languages.hcl.ast;

import java.util.List;

/**
 *
 * @author lkishalmi
 */
public sealed interface HCLTemplate extends HCLExpression {
    
    List<? extends Part> parts();

    public sealed interface Part {
        public static final StringPart NL = new StringPart("\n");

        String value();
        default String asString() {
            if (this instanceof StringPart) return value();
            if (this instanceof InterpolationPart) return "${" + value() + "}";
            if (this instanceof TemplatePart) return "%{" + value() + "}";
            return null;
        }

        public record StringPart(String value) implements Part {}
        /**
         * This is just a temporal implementation as the template expression
         * should really form a tree.
         */
        public record InterpolationPart(String value) implements Part {}
        public record TemplatePart(String value) implements Part {}
    }
    
    public record HereDoc(String marker, int indent, List<Part> parts) implements HCLTemplate {

        public HereDoc {
            parts = List.copyOf(parts);
        }

        public boolean isIndented() {
            return indent != -1;
        }
        
        @Override
        public String asString() {
            StringBuilder sb = new StringBuilder();
            sb.append(indent > -1 ? "<<-" : "<<").append(marker).append('\n');
            for (Part part : parts) {
                sb.append(part);
            }
            sb.append(marker);
            return sb.toString();
        }
    }
    
    public record StringTemplate(List<Part> parts) implements HCLTemplate {

        public StringTemplate {
            parts = List.copyOf(parts);
        }

        @Override
        public String asString() {
            StringBuilder sb = new StringBuilder();
            sb.append('"');
            for (Part part : parts) {
                sb.append(part);
            }
            sb.append('"');
            return sb.toString();
        }        
    }
    
}
