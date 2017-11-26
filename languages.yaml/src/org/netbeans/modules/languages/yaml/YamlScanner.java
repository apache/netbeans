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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import org.jruby.util.ByteList;
import org.jvyamlb.Positionable;
import org.jvyamlb.nodes.Node;
import org.jvyamlb.nodes.PositionedScalarNode;
import org.jvyamlb.nodes.PositionedSequenceNode;
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

        Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
        List<OffsetRange> codeblocks = new ArrayList<OffsetRange>();
        folds.put("tags", codeblocks); // NOI18N

        BaseDocument doc = (BaseDocument) result.getSnapshot().getSource().getDocument(false);

        //if (doc != null) {
            for (StructureItem item : items) {
                try {
                    addBlocks(result, doc, result.getSnapshot().getText(), codeblocks, item);
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                    break;
                }
            }
        //}

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
        private final long begin;
        private final long end;

        YamlStructureItem(Node node, String name, long begin, long end) {
            this.node = node;
            this.name = name;
            this.begin = begin;
            this.end = end;
        }

        YamlStructureItem(Node node, String name, OffsetRange positions) {
            this(node, name, positions.getStart(), positions.getEnd());
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
            IdentityHashMap<Object, Boolean> seen = new IdentityHashMap<Object, Boolean>(100);
            List<StructureItem> children = new ArrayList<StructureItem>();
            for (Node root : roots) {
                YamlStructureItem fakeRoot = new YamlStructureItem(root, null, OffsetRange.NONE);
                initializeChildren(result, fakeRoot, seen, 0);
                children.addAll(fakeRoot.children);
            }
            return children;
        }

        @SuppressWarnings("unchecked")
        private static void initializeChildren(YamlParserResult result, YamlStructureItem item, IdentityHashMap<Object, Boolean> seen, int depth) {
            if (depth > 20) {
                // Avoid boundless recursion in some yaml parse trees
                // This should already be handled now with the seen map, but
                // leave this just in case since we're right before code freeze
                item.children = Collections.emptyList();
                return;
            }
            Node node = item.node;
            Object value = node.getValue();
            if (value == null) {
                item.children = Collections.emptyList();
                return;
            }

            boolean alreadySeen = false;
            if (seen.containsKey(value)) {
                alreadySeen = true;
            }

            seen.put(value, Boolean.TRUE);
            if (value instanceof Map) {
                Map map = (Map) value;
                List<YamlStructureItem> children = new ArrayList<YamlStructureItem>();
                item.children = children;

                Set<Map.Entry> entrySet = map.entrySet();

                for (Map.Entry entry : entrySet) {

                    Object key = entry.getKey();
                    if (key instanceof PositionedSequenceNode) {
                        PositionedSequenceNode psn = (PositionedSequenceNode) key;
                        Object keyValue = psn.getValue();
                        assert keyValue instanceof List;
                        @SuppressWarnings("unchecked")
                        List<Node> list = (List<Node>) keyValue;
                        for (Node o : list) {
                            //String childName = o.getValue().toString();
                            Object childValue = o.getValue();
                            if (childValue instanceof List || childValue instanceof Map) {
                                children.add(new YamlStructureItem(o, "list item", result.getAstRange(o)));
                            } else {
                                String childName = childValue.toString();
                                children.add(new YamlStructureItem(o, childName, result.getAstRange(o)));
                            }
                        }
                        Object entryValue = entry.getValue();
                        if (entryValue instanceof PositionedSequenceNode) {
                            psn = (PositionedSequenceNode) entryValue;
                            keyValue = psn.getValue();
                            assert keyValue instanceof List;
                            list = (List<Node>) keyValue;
                            for (Node o : list) {
                                //String childName = o.getValue().toString();
                                Object childValue = o.getValue();
                                if (childValue instanceof List || childValue instanceof Map) {
                                    children.add(new YamlStructureItem(o, "list item", result.getAstRange(o)));
                                } else {
                                    String childName = childValue.toString();
                                    children.add(new YamlStructureItem(o, childName, result.getAstRange(o)));
                                }
                            }
                        }
                    } else if (key instanceof PositionedScalarNode) {
                        //ScalarNode scalar = (ScalarNode)key;
                        PositionedScalarNode scalar = (PositionedScalarNode) key;
                        Object childNameValue = scalar.getValue();
                        assert childNameValue instanceof ByteList;
                        ByteList byteListChildName = (ByteList) childNameValue;
                        String childName = byteListChildName.toString();
                        try {
                            childName = new String(byteListChildName.bytes, "UTF-8"); //NOI18N
                        } catch (UnsupportedEncodingException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        Node child = (Node) entry.getValue();
                        if (child != null) {
                            int e = result.convertByteToUtf8(((Positionable) child).getRange().end.offset);
                            // If you have an "empty" key, e.g.
                            //   foo:
                            //   bar: Hello World
                            // here foo is "empty" but I get a child of "" positioned at the beginning
                            // of "bar", which is wrong. In this case, don't include the child in the
                            // position bounds.
                            if (child.getValue() instanceof ByteList && ((ByteList) child.getValue()).length() == 0) {
                                e = result.convertByteToUtf8(((Positionable) scalar).getRange().end.offset);
                            }
                            children.add(new YamlStructureItem(child, childName.trim(),
                                    // Range: beginning of -key- to ending of -value-
                                    result.convertByteToUtf8(((Positionable) scalar).getRange().start.offset),
                                    e));
                        }
                    }
                }
                // Keep the list ordered, same order as in the document!!
                Collections.sort(children);
            } else if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<Node> list = (List<Node>) value;

                List<YamlStructureItem> children = new ArrayList<YamlStructureItem>(list.size());
                item.children = children;
                for (Node o : list) {
                    //String childName = o.getValue().toString();
                    Object childValue = o.getValue();
                    if (childValue instanceof List || childValue instanceof Map) {
                        children.add(new YamlStructureItem(o, "list item", result.getAstRange(o)));
                    } else {
                        String childName = childValue.toString();
                        children.add(new YamlStructureItem(o, childName, result.getAstRange(o)));
                    }
                }
            } else {
                item.children = Collections.emptyList();
            }

            if (item.children.size() > 0) {
                for (YamlStructureItem child : item.children) {
                    if (alreadySeen) {
                        // I delayed the alreadySeen abort to the creation of
                        // children rather than processing the main node itself
                        // such that we include one level of referenced data.
                        // See the fixtures3.yml test for example, where we want
                        // to include the created_on attribute in the sites that
                        // include it <<.
                        child.children = Collections.emptyList();
                    } else {
                        initializeChildren(result, child, seen, depth + 1);
                    }
                }
            }
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            assert children != null;
            return children;
        }

        @Override
        public long getPosition() {
            return begin;
        }

        @Override
        public long getEndPosition() {
            return end;
        }

        @Override
        public ImageIcon getCustomIcon() {
            return null;
        }

        @Override
        public int compareTo(YamlStructureItem other) {
            return (int) (begin - other.begin);
        }

    }
}
