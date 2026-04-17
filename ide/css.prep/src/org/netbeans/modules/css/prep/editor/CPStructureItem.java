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
package org.netbeans.modules.css.prep.editor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.css.prep.editor.model.CPElement;
import org.netbeans.modules.css.prep.editor.model.CPElementHandle;

/**
 *
 * @author mfukala@netbeans.org
 */
public abstract class CPStructureItem implements StructureItem {

    private CPElementHandle handle;
    private CPCslElementHandle cslHandle;
    private OffsetRange range;

    public CPStructureItem(CPElement element) {
        this.handle = element.getHandle();
        this.range = element.getRange();

        this.cslHandle = new CPCslElementHandle(handle.getFile(), handle.getName(), element.getRange(), element.getType());
    }

    @Override
    public long getPosition() {
        return range.getStart();
    }

    @Override
    public long getEndPosition() {
        return range.getEnd();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public ElementHandle getElementHandle() {
        return cslHandle;
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getHtml(HtmlFormatter formatter) {
        switch (handle.getType()) {
            case VARIABLE_GLOBAL_DECLARATION:
                formatter.emphasis(true);
                break;
        }

        formatter.appendText(getName());

        switch (handle.getType()) {
            case VARIABLE_GLOBAL_DECLARATION:
                formatter.emphasis(false);
                break;
        }

        return formatter.getText();
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public ImageIcon getCustomIcon() {
        return null;
    }

    @Override
    public List<? extends StructureItem> getNestedItems() {
        return Collections.emptyList();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(handle.getName());
        hash = 37 * hash + Objects.hashCode(handle.getType());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CPStructureItem other = (CPStructureItem) obj;
        if (!Objects.equals(this.handle.getName(), other.handle.getName())) {
            return false;
        }
        if (!Objects.equals(this.handle.getType(), other.handle.getType())) {
            return false;
        }
        return true;
    }

    
    
    public static class Mixin extends CPStructureItem {

        public Mixin(CPElement element) {
            super(element);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }
    }

    public static class Variable extends CPStructureItem {

        public Variable(CPElement element) {
            super(element);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }
    }
}
