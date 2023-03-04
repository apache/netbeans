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
package org.netbeans.modules.web.jsf.editor.facelets;

import java.util.Collection;
import java.util.HashSet;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.Tag;

/**
 *
 * @author marekfukala
 */
public  class ProxyTag implements Tag {

        //s has priority
        private Tag s,t;

        public ProxyTag(Tag s, Tag t) {
            assert s != null || t != null;

            this.s = s;
            this.t = t;
        }

        @Override
        public String getName() {
            return s != null ? s.getName() : t.getName();
        }

        @Override
        public String getDescription() {
            String sd = s != null ? s.getDescription() : null;
            String td = t != null ? t.getDescription() : null;
            return sd != null ? sd : td;
        }

        @Override
        public boolean hasNonGenenericAttributes() {
            return s != null ? s.hasNonGenenericAttributes() : t.hasNonGenenericAttributes();
        }

        @Override
        public Collection<Attribute> getAttributes() {
            if(s == null) {
                return t.getAttributes();
            } else if(t == null) {
                return s.getAttributes();
            } else {
                //merge
                Collection<Attribute> merged = new HashSet<>();
                merged.addAll(s.getAttributes());
                merged.addAll(t.getAttributes());
                return merged;
            }
        }

        @Override
        public Attribute getAttribute(String name) {
            for(Attribute a : getAttributes()) {
                if(a.getName().equals(name)) {
                    return a;
                }
            }
            return null;
        }

    }