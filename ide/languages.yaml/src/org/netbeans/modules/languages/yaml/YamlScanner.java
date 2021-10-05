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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.api.StructureScanner.Configuration;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.snakeyaml.engine.v2.nodes.Node;

/**
 * Structure Scanner for YAML
 *
 * @author Tor Norbye
 */
public class YamlScanner implements StructureScanner {

    private static final Logger LOGGER = Logger.getLogger(YamlScanner.class.getName());

    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        YamlParserResult result = (YamlParserResult) info;
        if (result != null) {
            return result.getItems();
        }

        return Collections.emptyList();
    }

    List<? extends StructureItem> scanStructure(YamlParserResult result) {
        List<Node> nodes = result.getRootNodes();
        if (nodes.size() > 0) {
            // Skip root node
            return YamlStructureItem.initialize(result, nodes);
        }

        return Collections.emptyList();
    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        YamlParserResult result = (YamlParserResult) info;
        if (result == null) {
            return Collections.emptyMap();
        }

        List<? extends StructureItem> items = result.getItems();
        if (items.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, List<OffsetRange>> folds = new HashMap<>();
        List<OffsetRange> codeblocks = new ArrayList<>();
        folds.put("tags", codeblocks); // NOI18N

        BaseDocument doc = (BaseDocument) result.getSnapshot().getSource().getDocument(false);

        for (StructureItem item : items) {
            try {
                addBlocks(result, doc, result.getSnapshot().getText(), codeblocks, item);
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
                break;
            }
        }

        return folds;
    }

    private void addBlocks(YamlParserResult result, BaseDocument doc, CharSequence text, List<OffsetRange> codeblocks, StructureItem item) throws BadLocationException {
        int docLength = doc == null ? text.length() : doc.getLength();
        int begin = Math.min((int) item.getPosition(), docLength);
        int end = Math.min((int) item.getEndPosition(), docLength);
        int firstRowEnd = doc == null ? GsfUtilities.getRowEnd(text, begin) : Utilities.getRowEnd(doc, begin);
        int lastRowEnd = doc == null ? GsfUtilities.getRowEnd(text, end) : Utilities.getRowEnd(doc, end);
        if (begin < end && firstRowEnd != lastRowEnd) {
            codeblocks.add(new OffsetRange(firstRowEnd, end));
        } else {
            return;
        }

        for (StructureItem child : item.getNestedItems()) {
            int childBegin = (int) child.getPosition();
            int childEnd = (int) child.getEndPosition();
            if (childBegin >= begin && childEnd <= end) {
                addBlocks(result, doc, text, codeblocks, child);
            }
        }
    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration(false, false, 0);
    }

    private static class YamlStructureItem implements StructureItem, Comparable<YamlStructureItem> {

        private final String name;
        private List<YamlStructureItem> children;
        private final Node node;

        YamlStructureItem(Node node, String name) {
            this.node = node;
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getSortText() {
            return getName();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            String s = getName();
            try {
                return XMLUtil.toElementContent(s);
            } catch (CharConversionException cce) {
                // fine to just log...probably some UTF8 name (e.g. russian cyrillic, etc.)
                LOGGER.log(Level.FINE, "NAME:" + s, cce);
                return s;
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

        private static List<? extends StructureItem> initialize(YamlParserResult result, List<Node> roots) {
            // Really need IdentitySet or IdentityHashSet but there isn't one built in
            // or in our available libraries...
            Set<Node> seen = new HashSet<>(100);
            List<StructureItem> children = new ArrayList<>();
            for (Node root : roots) {
                YamlStructureItem fakeRoot = new YamlStructureItem(root, null);
                initializeChildren(result, fakeRoot, seen, 0);
                children.addAll(fakeRoot.children);
            }
            return children;
        }

        @SuppressWarnings("unchecked")
        private static void initializeChildren(YamlParserResult result, YamlStructureItem item, Set<Node> seen, int depth) {
            Node node = item.node;
            if (seen.contains(node)) {
                return;                
            }
            seen.add(node);
            
            switch (node.getNodeType()) {
                case MAPPING:
                    break;
                case SEQUENCE:
                    break;
                case SCALAR:
                    break;
            }
            item.children = Collections.emptyList();
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            assert children != null;
            return children;
        }

        @Override
        public long getPosition() {
            return node.getStartMark().get().getPointer();
        }

        @Override
        public long getEndPosition() {
            return node.getEndMark().get().getPointer();
        }

        @Override
        public ImageIcon getCustomIcon() {
            return null;
        }

        @Override
        public int compareTo(YamlStructureItem other) {
            return (int) (getPosition() - other.getPosition());
        }

    }
}
