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
package org.netbeans.modules.languages.yaml;

import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.StructureItem;
import org.openide.xml.XMLUtil;

/**
 *
 * @author lkishalmi
 */
public abstract class YamlStructureItem implements StructureItem {

    public static enum NodeType { MAP, SEQUENCE, MAPPING, SCALAR, ALIAS };
    
    private static final Logger LOGGER = Logger.getLogger(YamlStructureItem.class.getName());
    private final NodeType type;
    private final long startMark;
    
    public YamlStructureItem(NodeType type, long startMark) {
        this.type = type;
        this.startMark = startMark;
    }

    public NodeType getType() {
        return type;
    }
    
    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getHtml(HtmlFormatter formatter) {
        try {
            return XMLUtil.toElementContent(getName());
        } catch (CharConversionException cce) {
            // fine to just log...probably some UTF8 name (e.g. russian cyrillic, etc.)
            LOGGER.log(Level.FINE, "NAME:" + getName(), cce);
            return getName();
        }
    }

    @Override
    public ElementHandle getElementHandle() {
        return null;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.ATTRIBUTE;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public boolean isLeaf() {
        return getNestedItems().isEmpty();
    }

    @Override
    public long getPosition() {
        return startMark;
    }

    @Override
    public ImageIcon getCustomIcon() {
        return null;
    }

    @Override
    public String toString() {
        return "" + getPosition() + ": " + getName();
    }

    public final static class Simple extends YamlStructureItem {

        final String name;
        final long endMark;
        
        public  Simple(NodeType type, String name, long startMark, long endMark) {
            super(type, startMark);
            this.name = name;
            this.endMark = endMark;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public List<? extends StructureItem> getNestedItems() {
            return Collections.emptyList();
        }

        @Override
        public long getEndPosition() {
            return endMark;
        }
        
    }
    
    public final static class Collection extends YamlStructureItem {
        private final List<YamlStructureItem> children = new ArrayList<>();
        private long endMark;
        
        public  Collection(NodeType type, long startMark) {
            super(type, startMark);
            this.endMark = startMark;
        }
        
        @Override
        public String getName() {
            return "list item";
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            return children;
        }

        public List<? extends YamlStructureItem> getChildren() {
            return children;
        }
        
        @Override
        public long getEndPosition() {
            return endMark;
        }

        public void add(YamlStructureItem item) {
            endMark = item.getEndPosition();
            children.add(item);
        }
        
        public void setEndMark(int end) {
            endMark = end;
        }
    }
    
    public final static class MapEntry extends YamlStructureItem {
        final YamlStructureItem keyItem;
        final YamlStructureItem valueItem;

        public  MapEntry(YamlStructureItem keyItem, YamlStructureItem valueItem) {
            super(NodeType.MAPPING, keyItem.startMark);
            this.keyItem = keyItem;
            this.valueItem = valueItem;
        }
        
        @Override
        public String getName() {
            return keyItem.getName();
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            return valueItem.getNestedItems();
        }

        @Override
        public long getEndPosition() {
            return valueItem.getEndPosition();
        }
    }
    
}
