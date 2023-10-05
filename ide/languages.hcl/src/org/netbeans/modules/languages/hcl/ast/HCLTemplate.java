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

import java.util.Collections;
import java.util.List;

/**
 *
 * @author lkishalmi
 */
public abstract class HCLTemplate extends HCLExpression {
    
    public final List<Part> parts;

    public HCLTemplate(List<Part> parts) {
        this.parts = Collections.unmodifiableList(parts);
    }
    
    @Override
    public List<? extends HCLExpression> getChildren() {
        return Collections.emptyList();
    }
    
    public abstract static class Part {
        public final String value;

        public Part(String value) {
            this.value = value;
        }        
    }
    
    public final static class StringPart extends Part {
        
        public static final StringPart NL = new StringPart("\n");

        public StringPart(String value) {
            super(value);
        }

        @Override
        public String toString() {
            return value;
        }
    }
    
    public final static class InterpolationPart extends Part {

        public InterpolationPart(String value) {
            super(value);
        }

        @Override
        public String toString() {
            return "${" + value + "}";
        }
    }

    /**
     * This is just a temporal implementation as the template expression
     * should really form a tree.
     */
    public final static class TemplatePart extends Part {
        
        public TemplatePart(String value) {
            super(value);
        }

        @Override
        public String toString() {
            return "%{" + value + "}";
        }
        
    }
    
    public final static class HereDoc extends HCLTemplate {
        public final String marker;
        public final int indent;

        public HereDoc(String marker, int indent, List<Part> parts) {
            super(parts);
            this.marker = marker;
            this.indent = indent;
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
    
    public final static class StringTemplate extends HCLTemplate {

        public StringTemplate(List<Part> parts) {
            super(parts);
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
