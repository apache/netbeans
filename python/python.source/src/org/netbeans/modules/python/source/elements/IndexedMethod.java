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
package org.netbeans.modules.python.source.elements;

import org.netbeans.modules.csl.api.ElementKind;

public class IndexedMethod extends IndexedElement {
    private String[] params;

    public IndexedMethod(String name, ElementKind kind, String url, String module, String clz, String signature) {
        super(name, kind, url, module, clz, signature);
    }

    public String[] getParams() {
        if (params == null) {
            //int argsBegin = name.length()+3;
            int argsBegin = getAttributeSection(IndexedElement.ARG_INDEX);
            int argsEnd = attributes.indexOf(';', argsBegin);
            assert argsEnd != -1 : attributes;
            if (argsEnd > argsBegin) {
                String paramString = attributes.substring(argsBegin, argsEnd);
                params = paramString.split(",");
            } else {
                params = new String[0];
            }
        }

        return params;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexedMethod other = (IndexedMethod)obj;
        if (this.attributes != other.attributes && (this.attributes == null || !this.attributes.equals(other.attributes))) {
            return false;
        }
        if (this.clz != other.clz && (this.clz == null || !this.clz.equals(other.clz))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.attributes != null ? this.attributes.hashCode() : 0);
        hash = 53 * hash + (this.clz != null ? this.clz.hashCode() : 0);
        return hash;
    }
}
