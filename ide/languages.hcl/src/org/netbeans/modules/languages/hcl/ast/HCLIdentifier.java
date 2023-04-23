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

/**
 *
 * @author Laszlo Kishalmi
 */
public abstract class HCLIdentifier extends HCLElement {

    final String id;

    public HCLIdentifier(SourceRef src, String id) {
        super(src);
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    public final static class SimpleId extends HCLIdentifier {

        public SimpleId(SourceRef src, String id) {
            super(src, id);
        }

        @Override
        public String toString() {
            return id;
        }
    }

    public final static class StringId extends HCLIdentifier {

        public StringId(SourceRef src, String id) {
            super(src, id);
        }

        @Override
        public String toString() {
            return "\"" + id + "\"";
        }

    }
}
