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
package org.netbeans.modules.java.lsp.server.protocol;

import java.util.Map;
import java.util.Objects;
import org.eclipse.xtext.xbase.lib.Pure;

public class HtmlPageParams  {
    private String id;
    private String text;
    private boolean pause;
    private Map<String, String> resources;

    public HtmlPageParams(String id, String text) {
        this.id = id;
        this.text = text;
    }

    @Pure
    public String getId() {
        return id;
    }

    public HtmlPageParams setId(String id) {
        this.id = id;
        return this;
    }

    @Pure
    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    @Pure
    public String getText() {
        return text;
    }

    public HtmlPageParams setText(String text) {
        this.text = text;
        return this;
    }

    @Pure
    public Map<String, String> getResources() {
        return resources;
    }

    public HtmlPageParams setResources(Map<String, String> resources) {
        this.resources = resources;
        return this;
    }

    @Pure
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.id);
        hash = 83 * hash + Objects.hashCode(this.text);
        hash = 83 * hash + Objects.hashCode(this.pause);
        hash = 83 * hash + Objects.hashCode(this.resources);
        return hash;
    }

    @Pure
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HtmlPageParams other = (HtmlPageParams) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (this.pause != other.pause) {
            return false;
        }
        if (!Objects.equals(this.text, other.text)) {
            return false;
        }
        if (!Objects.equals(this.resources, other.resources)) {
            return false;
        }
        return true;
    }

}
