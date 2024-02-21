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
package org.netbeans.modules.csl.navigation;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.core.CancelSupportImplementation;
import org.netbeans.modules.csl.core.SchedulerTaskCancelSupportImpl;
import org.netbeans.modules.csl.core.SpiSupportAccessor;
import org.netbeans.modules.csl.navigation.ElementNode.Description;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.*;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 * 
 * XXX Remove the ElementScanner class from here it should be wenough to
 * consult the Elements class. It should also permit for showing inherited members.
 *
 * @author phrebejk
 */
public abstract class ElementScanningTask extends IndexingAwareParserResultTask<ParserResult> {

    private static final Logger LOG = Logger.getLogger(ElementScanningTask.class.getName());

    private volatile boolean canceled;
    
    /**
     * Reference to the last result of parsing processed into structure.
     */
    private static final Map<Snapshot, Reference<ResultStructure>> lastResults = new WeakHashMap<>();

    /**
     * Cache the parser result and the resulting structure.
     */
    private static class ResultStructure {
        private Parser.Result result;
        private List<? extends StructureItem> structure;

        public ResultStructure(Result result, List<? extends StructureItem> structure) {
            this.result = result;
            this.structure = structure;
        }
        
        
    }

    public ElementScanningTask() {
        super(TaskIndexingMode.ALLOWED_DURING_SCAN);
    }
    
    // not synchronized as the Task invocation itself is synced by parsing API
    public static List<? extends StructureItem> findCachedStructure(Snapshot s, Parser.Result r) {
        if (!(r instanceof ParserResult)) {
            return null;
        }
        Reference<ResultStructure> previousRef;
        previousRef = lastResults.get(s);
        if (previousRef == null) {
            return null;
        }
        ResultStructure cached = previousRef.get();
        if (cached == null || cached.result != r) {
            // remove from cache:
            lastResults.remove(s);
            return null;
        }
        return cached.structure;
    }
    
    // not synchronized as the Task invocation itself is synced by parsing API
    public static void markProcessed(Parser.Result r, List<? extends StructureItem> structure) {
        lastResults.put(r.getSnapshot(), new WeakReference(new ResultStructure(r, structure)));
    }

    protected final StructureItem computeStructureRoot(Source source) {
        //System.out.println("The task is running" + info.getFileObject().getNameExt() + "=====================================" ) ;

        final FileObject fileObject = source.getFileObject();
        if (fileObject == null) {
            return null;
        }

        final int [] mimetypesWithElements = new int [] { 0 };
        final List<MimetypeRootNode> roots = new ArrayList<MimetypeRootNode>();
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                public @Override void run(ResultIterator resultIterator) throws Exception {
                    Language language = LanguageRegistry.getInstance().getLanguageByMimeType(resultIterator.getSnapshot().getMimeType());
                    if(language != null) { //check for non csl results
                        StructureScanner scanner = language.getStructure();
                        if (scanner != null) {
                            Parser.Result r = resultIterator.getParserResult();
                            if (r instanceof ParserResult) {
                                List<? extends StructureItem> children = findCachedStructure(resultIterator.getSnapshot(), r);
                                if (children == null) {
                                    long startTime = System.currentTimeMillis();
//                                    children = convert(scanner.scan((ParserResult) r), language);
                                    children = scanner.scan((ParserResult) r);
                                    long endTime = System.currentTimeMillis();
                                    Logger.getLogger("TIMER").log(Level.FINE, "Structure (" + language.getMimeType() + ")",
                                            new Object[]{fileObject, endTime - startTime});
                                }
                                if (children.size() > 0) {
                                    mimetypesWithElements[0]++;
                                }
                                if (isCancelled()) {
                                    //Don't cache if cancelled
                                    return;
                                }
                                markProcessed(r, children);
                                roots.add(new MimetypeRootNode(language, children, resultIterator.getSnapshot().getMimePath()));
                            }
                        }
                    }

                    for(Embedding e : resultIterator.getEmbeddings()) {
                        if (isCancelled()) {
                            return;
                        }
                        run(resultIterator.getResultIterator(e));
                    }
                }
            });
        } catch (ParseException e) {
            LOG.log(Level.WARNING, null, e);
        }
        if (isCancelled()) {
            return null;
        }
        //ensure we do display a language structure items only once per mimetype.
        //there is a problem that a language may be embedded in various mimepaths.
        //For example javascript virtual source is provided for both text/html
        //and text/x-jsp mimetypes.
        //So we have following javascript embedding:
        //
        //MimePath[text/x-jsp/text/html/text/javascript]
        //MimePath[text/x-jsp/text/javascript]
        //
        //...which results in multiple javascript nodes in the navigator
        //
        //So the solution is to use the shortest mimepath per mimetype only
        HashMap<String, MimetypeRootNode> map = new HashMap<String, MimetypeRootNode>();
        for(MimetypeRootNode mtRootNode : roots) {
            MimePath path = mtRootNode.getMimePath();
            String mimeType = path.getMimeType(path.size() - 1); //get leaf language

            MimetypeRootNode node = map.get(mimeType);
            if(node != null) {
                MimePath nodeMimePath = node.getMimePath();
                if(path.size() < nodeMimePath.size()) {
                    //the actual path is shorter, use this one
                    map.put(mimeType, mtRootNode);
                }
            } else {
                map.put(mimeType, mtRootNode);
            }
        }

        roots.clear();
        roots.addAll(map.values());

        if (roots.size() > 1) {
            roots.sort(new Comparator<MimetypeRootNode>() {
                public int compare(MimetypeRootNode o1, MimetypeRootNode o2) {
                    return o1.getSortText().compareTo(o2.getSortText());
                }
            });
        }

        final List<StructureItem> items = new ArrayList<StructureItem>();
        if (mimetypesWithElements[0] > 1) {
            //at least two languages provided some elements - use the root mimetype nodes
            for (MimetypeRootNode root : roots) {
                items.add(root);
            }
        } else {
            //just one or none language provided elements - put them to the root
            for (MimetypeRootNode root : roots) {
                items.addAll(root.getNestedItems());
            }
        }

        return new RootStructureItem(items);
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    protected final void resume() {
        canceled = false;
    }

    protected final boolean isCancelled() {
        return canceled;
    }

    protected final void runWithCancelService(@NonNull final Runnable r) {
        final CancelSupportImplementation cs = SchedulerTaskCancelSupportImpl.create(this);
        SpiSupportAccessor.getInstance().setCancelSupport(cs);
        try {
            r.run();
        } finally {
            SpiSupportAccessor.getInstance().removeCancelSupport(cs);
        }
    }
    private static final class RootStructureItem implements StructureItem {

        private final List<? extends StructureItem> items;

        public RootStructureItem(List<? extends StructureItem> items) {
            this.items = items;
        }
        
        public String getName() {
            return null;
        }

        public String getHtml(HtmlFormatter formatter) {
            return null;
        }

        public ElementHandle getElementHandle() {
            return null;
        }

        public ElementKind getKind() {
            return ElementKind.OTHER;
        }

        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        public boolean isLeaf() {
            return false;
        }

        public List<? extends StructureItem> getNestedItems() {
            return items;
        }

        public long getPosition() {
            return 0;
        }

        public long getEndPosition() {
            return Long.MAX_VALUE;
        }

        public ImageIcon getCustomIcon() {
            return null;
        }

        public String getSortText() {
            return null;
        }
    } // End of RootStructureItem class

    static final class MimetypeRootNode implements StructureItem {

        //hack - see the getSortText() comment
        private static final String CSS_MIMETYPE = "text/css"; //NOI18N
        private static final String CSS_SORT_TEXT = "2";//NOI18N
        private static final String JAVASCRIPT_MIMETYPE = "text/javascript";//NOI18N
        private static final String RUBY_MIMETYPE = "text/x-ruby";//NOI18N
        private static final String YAML_MIMETYPE = "text/x-yaml";//NOI18N
        private static final String PHP_MIME_TYPE = "text/x-php5"; // NOI18N
        private static final String PHP_SORT_TEXT = "0";//NOI18N
        private static final String JAVASCRIPT_SORT_TEXT = "1";//NOI18N
        private static final String HTML_MIMETYPE = "text/html";//NOI18N
        private static final String HTML_SORT_TEXT = "3";//NOI18N
        // Ensure YAML is sorted before Ruby to make navigator look primarily in the YAML offsets first
        private static final String YAML_SORT_TEXT = "4";//NOI18N
        private static final String RUBY_SORT_TEXT = "5";//NOI18N
        private static final String OTHER_SORT_TEXT = "9";//NOI18N
        Language language;
        private List<? extends StructureItem> items;
        long from, to;
        private MimePath mimePath;

        private MimetypeRootNode(Language lang, List<? extends StructureItem> items, MimePath mimePath) {
            this.language = lang;
            this.items = new ArrayList<StructureItem>(items);
            items.sort(Description.POSITION_COMPARATOR);
            this.from = items.size() > 0 ? items.get(0).getPosition() : 0;
            this.to = items.size() > 0 ? items.get(items.size() - 1).getEndPosition() : 0;
            this.mimePath = mimePath;
        }
        
        public MimePath getMimePath() {
            return mimePath;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MimetypeRootNode)) {
                return false;
            }
            MimetypeRootNode compared = (MimetypeRootNode) o;
            return language.equals(compared.language);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + (this.language != null ? this.language.hashCode() : 0);
            return hash;
        }

        public String getName() {
            return language.getDisplayName();
        }

        public String getSortText() {
            //hack -> I need to ensure that the navigator selects the top most
            //node when user moves caret in the source.
            //Since the navigator tree is a generic graph if there are more
            //embedded languages we need to use some tricks...
            if (language.getMimeType().equals(CSS_MIMETYPE)) {
                return CSS_SORT_TEXT;
            } else if (language.getMimeType().equals(JAVASCRIPT_MIMETYPE)) {
                return JAVASCRIPT_SORT_TEXT;
            } else if (language.getMimeType().equals(HTML_MIMETYPE)) {
                return HTML_SORT_TEXT;
            } else if (language.getMimeType().equals(YAML_MIMETYPE)) {
                return YAML_SORT_TEXT;
            } else if (language.getMimeType().equals(RUBY_MIMETYPE)) {
                return RUBY_SORT_TEXT;
            } else if (language.getMimeType().equals(PHP_MIME_TYPE)) {
                return PHP_SORT_TEXT;
            } else {
                return OTHER_SORT_TEXT + getName();
            }

        }

        public String getHtml(HtmlFormatter formatter) {
            return getName();
        }

        public ElementHandle getElementHandle() {
            return null;
        }

        public ElementKind getKind() {
            return ElementKind.OTHER;
        }

        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        public boolean isLeaf() {
            return false;
        }

        public List<? extends StructureItem> getNestedItems() {
            return items;
        }

        public long getPosition() {
            return from;
        }

        public long getEndPosition() {
            return to;
        }

        public ImageIcon getCustomIcon() {
            String iconBase = language.getIconBase();
            return iconBase == null ? null : new ImageIcon(ImageUtilities.loadImage(iconBase));
        }
    }
}    
    
