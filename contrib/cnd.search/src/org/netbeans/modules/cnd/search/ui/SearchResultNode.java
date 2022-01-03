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
package org.netbeans.modules.cnd.search.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.FeatureDescriptor;
import java.io.CharConversionException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.StyledDocument;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.modules.cnd.search.IconsCache;
import org.netbeans.modules.cnd.search.MatchingFileData.Entry;
import org.netbeans.modules.cnd.search.SearchParams;
import org.netbeans.modules.cnd.search.SearchResult;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 *
 */
public final class SearchResultNode extends AbstractNode {

    private static final String DOB_REF_PROP = "dataObjRef"; // NOI18N
    private final SearchResult result;
    private PropertySet[] propertySets;

    public SearchResultNode(SearchResult result) {
        super(result.data.hasEntries() ? Children.create(new SearchChildFactory(result), true) : Children.LEAF);
        this.result = result;
    }

    @Override
    public String getName() {
        return result.data.getFileName();
    }

    @Override
    public Image getIcon(int type) {
        Object dobRefProp = getValue(DOB_REF_PROP);
        if (dobRefProp instanceof WeakReference) {
            WeakReference<DataObject> dobRef = (WeakReference<DataObject>)dobRefProp;
            DataObject dob = dobRef.get();
            if (dob != null) {
                return dob.getNodeDelegate().getIcon(type);
            }
        }
        return IconsCache.getIcon(result.data.getFileName(), type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public PropertySet[] getPropertySets() {
        if (propertySets == null) {
            propertySets = new PropertySet[]{new SearchResultPropertySet(result)};
        }
        return propertySets;
    }

    @Override
    public Action getPreferredAction() {
        return new OpenResultAction(result, 0, this);
    }

    @Override
    public void setValue(String attributeName, Object value) {
        // No synchronization as this method supposed to be called from EDT only
        if (DOB_REF_PROP.equals(attributeName)) {
            Object current = getValue(attributeName);
            if (!(value.equals(current))) {
                super.setValue(attributeName, value);
                fireIconChange();
            }
        } else {
            super.setValue(attributeName, value);
        }
    }

    private static class SearchChildFactory extends ChildFactory<SearchResult> {

        private final SearchResult result;

        public SearchChildFactory(SearchResult result) {
            this.result = result;
        }

        @Override
        protected boolean createKeys(List<SearchResult> toPopulate) {
            return toPopulate.add(result);
        }

        @Override
        protected Node[] createNodesForKey(SearchResult res) {
            List<Entry> entries = res.data.getEntries();
            Node[] nodes = new Node[entries.size()];
            int i = 0;
            for (Entry entry : entries) {
                nodes[i++] = new EntryNode(result, entry);
            }
            return nodes;
        }
    }

    private static class OpenResultAction extends AbstractAction {

        private final SearchResult result;
        private final int line;
        private final FeatureDescriptor descriptor;

        public OpenResultAction(SearchResult result, int line, FeatureDescriptor descriptor) {
            this.result = result;
            this.line = line;
            this.descriptor = descriptor;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FileObject f = result.getFileObject();

            if (f == null) {
                return;
            }

            try {
                DataObject dataObj = DataObject.find(f);
                if (line > 0) {
                    EditorCookie ec = dataObj.getLookup().lookup(EditorCookie.class);
                    LineCookie lc = dataObj.getLookup().lookup(LineCookie.class);

                    if (ec != null && lc != null) {

                        StyledDocument doc = ec.openDocument();
                        if (doc != null) {
                            Line l = null;
                            try {
                                l = lc.getLineSet().getCurrent(line - 1);
                            } catch (IndexOutOfBoundsException ex) {
//                              BugtrackingManager.LOG.log(Level.FINE, null, ex);
                                ec.open();
                            }
                            if (l != null) {
                                l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                            }
                        }
                    }
                } else {
                    Openable open = DataObject.find(f).getLookup().lookup(Openable.class);
                    if (open != null) {
                        open.open();
                    }
                }

                if (descriptor != null) {
                    descriptor.setValue(DOB_REF_PROP, new WeakReference<DataObject>(dataObj));
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static class EntryNode extends AbstractNode {

        private final SearchResult result;
        private final Entry entry;
        private String htmlName;

        public EntryNode(SearchResult result, Entry entry) {
            super(Children.LEAF, Lookups.fixed(result, entry));
            this.entry = entry;
            this.result = result;
        }

        @Override
        public String getName() {
            return entry.getContext()
                    + "      " // NOI18N
                    + NbBundle.getMessage(SearchResultNode.class, "EntryNode.line.text", entry.getLineNumber()); // NOI18N
        }

        @Override
        public String getHtmlDisplayName() {
            if (htmlName == null) {
                htmlName = composeHtmlName();
            }
            return htmlName;
        }

        @Override
        public Action getPreferredAction() {
            return new OpenResultAction(result, entry.getLineNumber(), this);
        }

        @Override
        public void setValue(String attributeName, Object value) {
            if (DOB_REF_PROP.equals(attributeName)) {
                getParentNode().setValue(attributeName, value);
            } else {
                super.setValue(attributeName, value);
            }
        }

        private String composeHtmlName() {
            SearchParams params = result.data.getSearchParams();
            String context = entry.getContext();
            Pattern p;
            SearchPattern sp = params.getSearchPattern();
            try {
                p = sp.isMatchCase()
                        ? Pattern.compile(sp.getSearchExpression())
                        : Pattern.compile(sp.getSearchExpression(), Pattern.CASE_INSENSITIVE);
            } catch (Exception ex) {
                return null;
            }

            int pos = 0;
            Matcher m = p.matcher(context);

            try {
                StringBuilder text = new StringBuilder();

                while (m.find(pos)) {
                    text.append(XMLUtil.toElementContent(context.substring(pos, m.start())));
                    text.append("<b>"); // NOI18N
                    text.append(XMLUtil.toElementContent(context.substring(m.start(), m.end())));
                    text.append("</b>"); // NOI18N
                    if (pos == m.end()) {
                        break;
                    }
                    pos = m.end();
                }

                text.append(XMLUtil.toElementContent(context.substring(pos)));

                text.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"); // NOI18N
                text.append("<font color='#808080'>"); // NOI18N
                text.append(NbBundle.getMessage(SearchResultNode.class, "EntryNode.line.text", entry.getLineNumber())); // NOI18N
                return text.toString();
            } catch (CharConversionException e) {
                return null;
            }
        }
    }
}
