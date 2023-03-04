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
package org.netbeans.modules.html.editor.gsf;

import java.util.*;
import javax.swing.ImageIcon;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.parsing.api.*;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
final class HtmlStructureItem implements StructureItem {

    private final HtmlElementHandle handle;
    private final OffsetRange documentOffsetRange;
    private final String idAttributeValue;
    private final String classAttributeValue;
    private List<StructureItem> items;
    //remember from what mime path the snapshot came from
    private final MimePath mimePath;
    private final boolean isLeaf;

    public HtmlStructureItem(OpenTag node, HtmlElementHandle handle, Snapshot snapshot) {
        this.handle = handle;
        int dfrom = snapshot.getOriginalOffset(node.from());
        int dto = snapshot.getOriginalOffset(node.semanticEnd());

        if (dfrom == -1 && dto == -1) {
            //correct - it is a virtual node
            documentOffsetRange = OffsetRange.NONE;
        } else if (dfrom == -1 || dto == -1) {
            //erroneous - from or to is invalid
            documentOffsetRange = OffsetRange.NONE;
        } else if (dfrom > dto) {
            //erroneous - from or to is invalid
            documentOffsetRange = OffsetRange.NONE;
        } else {
            documentOffsetRange = new OffsetRange(dfrom, dto);
        }

        this.idAttributeValue = getAttributeValue(node, "id"); //NOI18N
        this.classAttributeValue = getAttributeValue(node, "class"); //NOI18N
        this.mimePath = snapshot.getMimePath();
        //acceptable, not 100% correct - may say it is not leaf, but then there 
        //won't be children if all children are virtual with no non-virtual ancestors
        this.isLeaf = node.children(OpenTag.class).isEmpty();
    }

    @Override
    public ElementHandle getElementHandle() {
        return handle;
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public String getSortText() {
        //return getName();
        // Use position-based sorting text instead; alphabetical sorting in the
        // outline (the default) doesn't really make sense for HTML tag names
        return Integer.toHexString(10000 + (int) getPosition());
    }

    @Override
    public String getHtml(HtmlFormatter formatter) {
        formatter.appendHtml(getName());
        if (idAttributeValue != null) {
            formatter.appendHtml("&nbsp;<font color=808080>id=");
            formatter.appendText(idAttributeValue);
            formatter.appendHtml("</font>"); //NOI18N
        }
        if (classAttributeValue != null) {
            formatter.appendHtml("&nbsp;<font color=808080>class=");
            formatter.appendText(classAttributeValue);
            formatter.appendHtml("</font>"); //NOI18N
        }
        return formatter.getText();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HtmlStructureItem)) {
            return false;
        }
        HtmlStructureItem item = (HtmlStructureItem) o;
        return item.getElementHandle().equals(getElementHandle());
    }

    @Override
    public int hashCode() {
        return getElementHandle().hashCode();
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.TAG;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public boolean isLeaf() {
        return isLeaf;
    }

    public void runTask(final Task task) throws ParseException {
        Source source = Source.create(getElementHandle().getFileObject());
        if (source == null) {
            //file deleted
            return;
        }
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                HtmlParserResult result;
                if (mimePath.size() == 1) {
                    result = (HtmlParserResult) resultIterator.getParserResult();
                } else {
                    for (int i = 0; i < mimePath.size(); i++) {
                        String mimeType = mimePath.getMimeType(i);
                        Iterator<Embedding> embeddings = resultIterator.getEmbeddings().iterator();
                        while (embeddings.hasNext()) {
                            Embedding embedding = embeddings.next();
                            if (embedding.getMimeType().equals(mimeType)) {
                                resultIterator = resultIterator.getResultIterator(embedding);
                            }
                        }
                    }
                    result = (HtmlParserResult) resultIterator.getParserResult();
                }

                task.run(result);
            }
        });

    }

    @Override
    public synchronized List<? extends StructureItem> getNestedItems() {
        if (items == null) {
            //lazy load the nested items
            //we need a parser result to be able to find Element for the ElementHandle
            try {
                runTask(new Task() {
                    @Override
                    public void run(HtmlParserResult result) {
                        Node node = handle.resolve(result);
                        // #214628 -- getNestedItems() must not return null
                        items = new ArrayList<>();
                        if (node != null) {
                            List<OpenTag> nonVirtualChildren = gatherNonVirtualChildren(node);
                            for (OpenTag child : nonVirtualChildren) {
                                HtmlElementHandle childHandle = new HtmlElementHandle(child, handle.getFileObject());
                                items.add(new HtmlStructureItem(child, childHandle, result.getSnapshot()));
                            }
                        }
                    }
                });

            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (items == null){
            return Collections.emptyList();
        } else {
            return items;
        }
    }

    @Override
    public long getPosition() {
        return documentOffsetRange.getStart();
    }

    @Override
    public long getEndPosition() {
        return documentOffsetRange.getEnd();
    }

    @Override
    public ImageIcon getCustomIcon() {
        return null;
    }

    //private
    private String getAttributeValue(Element node, String key) {
        String value = _getAttributeValue(node, key.toUpperCase(Locale.ENGLISH));
        if (value == null) {
            return _getAttributeValue(node, key.toLowerCase(Locale.ENGLISH));
        } else {
            return value;
        }
    }

    private String _getAttributeValue(Element node, String key) {
        if (node.type() != ElementType.OPEN_TAG) {
            return null;
        }
        OpenTag t = (OpenTag) node;
        Attribute attr = t.getAttribute(key); //try lowercase
        if (attr == null) {
            return null;
        }
        CharSequence value = attr.unquotedValue();
        return value != null ? value.toString() : null;
    }

    static List<OpenTag> gatherNonVirtualChildren(Node element) {
        List<OpenTag> collected = new LinkedList<>();
        for (OpenTag child : element.children(OpenTag.class)) {
            if (child.type() == ElementType.OPEN_TAG) {
                if (!ElementUtils.isVirtualNode(child)) {
                    collected.add(child);
                } else {
                    collected.addAll(gatherNonVirtualChildren(child));
                }
            }
        }
        return collected;
    }

    public static interface Task {

        public void run(HtmlParserResult result);
        
    }
}
