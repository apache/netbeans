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
package org.netbeans.modules.css.editor.module;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.css.editor.module.spi.Browser;
import org.netbeans.modules.css.editor.module.spi.CompletionContext;
import org.netbeans.modules.css.editor.module.spi.CssEditorModule;
import org.netbeans.modules.css.editor.module.spi.EditorFeatureContext;
import org.netbeans.modules.css.editor.module.spi.FeatureCancel;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.css.editor.module.spi.FutureParamTask;
import org.netbeans.modules.css.editor.module.spi.HelpResolver;
import org.netbeans.modules.css.editor.module.spi.PropertySupportResolver;
import org.netbeans.modules.css.editor.module.spi.SemanticAnalyzer;
import org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Pair;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssModuleSupport {

    private static final Logger LOGGER = Logger.getLogger(CssModuleSupport.class.getSimpleName());

    public static Collection<? extends CssEditorModule> getModules() {
        return Lookup.getDefault().lookupAll(CssEditorModule.class);
    }
    
    //TODO refine the logic OK/UNKNOWN/ERRONEOUS ???
    public static SemanticAnalyzerResult analyzeDeclaration(Node declarationNode) {
        SemanticAnalyzerResult result = SemanticAnalyzerResult.UNKNOWN;
        for(CssEditorModule module : getModules()) {
            SemanticAnalyzer semanticAnalyzer = module.getSemanticAnalyzer();
            if(semanticAnalyzer != null) {
                SemanticAnalyzerResult local = semanticAnalyzer.analyzeDeclaration(declarationNode);
                assert local != null;
                switch(local.getType()) {
                    case VALID:
                        //if at least one says VALID, then it is valid until someone else says ERRONEOUS
                        result = local;
                        break;
                    case ERRONEOUS:
                        //use first ERRONEOUS result 
                        return local;
                    case UNKNOWN:
                        //unknown
                        break;
                }
            }
        }
        return result;
    }

    public static Map<OffsetRange, Set<ColoringAttributes>> getSemanticHighlights(FeatureContext context, FeatureCancel cancel) {
        long start = System.nanoTime();

        Map<OffsetRange, Set<ColoringAttributes>> allPrecursor = new HashMap<>();
        final Collection<NodeVisitor<Map<OffsetRange, Set<ColoringAttributes>>>> visitors = new ArrayList<>();

        for (CssEditorModule module : getModules()) {
            NodeVisitor<Map<OffsetRange, Set<ColoringAttributes>>> visitor = module.getSemanticHighlightingNodeVisitor(context, allPrecursor);
            //modules may return null visitor instead of a dummy empty visitor 
            //to speed up the parse tree visiting when there're no result
            if (visitor != null) {
                visitors.add(visitor);
            }
        }

        if (cancel.isCancelled()) {
            return Collections.emptyMap();
        }

        cancel.attachCancelAction(new Runnable() {

            @Override
            public void run() {
                for (NodeVisitor visitor : visitors) {
                    visitor.cancel();
                }
            }
        });

        NodeVisitor.visitChildren(context.getParseTreeRoot(), visitors);

        long preparation = System.nanoTime();

        Map<OffsetRange, Set<ColoringAttributes>> all = new HashMap<>();

        List<OffsetRange> sortedRanges = new ArrayList<>(allPrecursor.keySet());
        sortedRanges.sort(null);


        List<OffsetRange> stack = new ArrayList<>();
        OffsetRange lastAdded;

        for(int i = 0; i < sortedRanges.size(); i++) {
            OffsetRange currentItem = sortedRanges.get(i);
            Set<ColoringAttributes> attributes = allPrecursor.get(currentItem);
            OffsetRange nextItem = (i < (sortedRanges.size() - 1)) ? sortedRanges.get(i + 1) : null;
            if(nextItem != null && currentItem.getEnd() > nextItem.getStart()) {
                stack.add(currentItem);
                currentItem = currentItem.boundTo(0, nextItem.getStart());
            }
            if(! currentItem.isEmpty()) {
                all.put(currentItem, attributes);
            }
            lastAdded = currentItem;
            while(true) {
                if(stack.isEmpty()) {
                    break;
                }
                OffsetRange stackElement = stack.remove(stack.size() - 1);
                boolean endStackProcessing = false;
                if(nextItem != null && stackElement.getEnd() > nextItem.getStart()) {
                    stack.add(stackElement);
                    endStackProcessing = true;
                }
                Set<ColoringAttributes> stackAttributes = allPrecursor.get(stackElement);
                stackElement = stackElement.boundTo(lastAdded.getEnd(), nextItem != null ? nextItem.getStart() : Integer.MAX_VALUE);
                if(! stackElement.isEmpty()) {
                    all.put(stackElement, stackAttributes);
                    lastAdded = stackElement;
                }
                if(endStackProcessing) {
                    break;
                }
            }
        }

        long consolidation = System.nanoTime();

        if(LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Preparation: {0} ms, Consolidation: {1} ms", new Object[]{preparation - start, consolidation - preparation});
            LOGGER.log(Level.FINER, "Precursor: {0}", allPrecursor);
            LOGGER.log(Level.FINER, "Final: {0}", all);
        }

        return all;
    }

    public static Set<OffsetRange> getMarkOccurrences(EditorFeatureContext context, FeatureCancel cancel) {
        Set<OffsetRange> all = new HashSet<>();
        final Collection<NodeVisitor<Set<OffsetRange>>> visitors = new ArrayList<>();

        for (CssEditorModule module : getModules()) {
            NodeVisitor<Set<OffsetRange>> visitor = module.getMarkOccurrencesNodeVisitor(context, all);
            //modules may return null visitor instead of a dummy empty visitor 
            //to speed up the parse tree visiting when there're no result
            if (visitor != null) {
                visitors.add(visitor);
            }
        }

        if (cancel.isCancelled()) {
            return Collections.emptySet();
        }

        cancel.attachCancelAction(new Runnable() {

            @Override
            public void run() {
                for (NodeVisitor visitor : visitors) {
                    visitor.cancel();
                }
            }
        });

        NodeVisitor.visitChildren(context.getParseTreeRoot(), visitors);

        return all;

    }

    public static Map<String, List<OffsetRange>> getFolds(FeatureContext context, FeatureCancel cancel) {
        Map<String, List<OffsetRange>> all = new HashMap<>();
        final Collection<NodeVisitor<Map<String, List<OffsetRange>>>> visitors = new ArrayList<>();

        for (CssEditorModule module : getModules()) {
            NodeVisitor<Map<String, List<OffsetRange>>> visitor = module.getFoldsNodeVisitor(context, all);
            //modules may return null visitor instead of a dummy empty visitor 
            //to speed up the parse tree visiting when there're no result
            if (visitor != null) {
                visitors.add(visitor);
            }
        }

        if (cancel.isCancelled()) {
            return Collections.emptyMap();
        }

        cancel.attachCancelAction(new Runnable() {

            @Override
            public void run() {
                for (NodeVisitor visitor : visitors) {
                    visitor.cancel();
                }
            }
        });

        NodeVisitor.visitChildren(context.getParseTreeRoot(), visitors);

        return all;

    }

    public static Pair<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>> getDeclarationLocation(final Document document, final int caretOffset, final FeatureCancel cancel) {
        final AtomicReference<Pair<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>>> result =
                new AtomicReference<>();
        document.render(new Runnable() {

            @Override
            public void run() {
                for (CssEditorModule module : getModules()) {
                    if (cancel.isCancelled()) {
                        return ;
                    }
                    Pair<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>> declarationLocation = module.getDeclaration(document, caretOffset);
                    if (declarationLocation != null) {
                        result.set(declarationLocation);
                        return ;
                    }
                }
            }
            
        });
        return result.get();
    }

    public static List<StructureItem> getStructureItems(FeatureContext context, FeatureCancel cancel) {
        List<StructureItem> all = new ArrayList<>();
        final Collection<NodeVisitor<List<StructureItem>>> visitors = new ArrayList<>();

        for (CssEditorModule module : getModules()) {
            NodeVisitor<List<StructureItem>> visitor = module.getStructureItemsNodeVisitor(context, all);
            //modules may return null visitor instead of a dummy empty visitor 
            //to speed up the parse tree visiting when there're no result
            if (visitor != null) {
                visitors.add(visitor);
            }
        }

        if (cancel.isCancelled()) {
            return Collections.emptyList();
        }

        cancel.attachCancelAction(new Runnable() {

            @Override
            public void run() {
                for (NodeVisitor visitor : visitors) {
                    visitor.cancel();
                }
            }
        });

        NodeVisitor.visitChildren(context.getParseTreeRoot(), visitors);

        return all;

    }
    
    /**
     * @since 1.42
     * @param context
     * @return 
     */
    public static CssEditorModule getModuleForInstantRename(EditorFeatureContext context) {
        Set<OffsetRange> all = new HashSet<>();
        //first module allowing to instant rename the context will win and do the rename
        for (CssEditorModule module : getModules()) {
            if (module.isInstantRenameAllowed(context)) {
                return module;
            }
        }
        return null;
    }

    /**
     * @since 1.42
     * @param context
     * @param module
     * @return 
     */
    public static Set<OffsetRange> getInstantRenameRegions(EditorFeatureContext context, CssEditorModule module) {
        Set<OffsetRange> all = new HashSet<>();
        //first module allowing to instant rename the context will win and do the rename
        assert module.isInstantRenameAllowed(context);

        final NodeVisitor<Set<OffsetRange>> visitor = module.getInstantRenamerVisitor(context, all);
        assert visitor != null;
        
        visitor.visitChildren(context.getParseTreeRoot());
        return all;
    }
    
//    //hotfix for Bug 214819 - Completion list is corrupted after IDE upgrade 
//    //http://netbeans.org/bugzilla/show_bug.cgi?id=214819
//    //o.n.m.javafx2.editor.css.JavaFXCSSModule
//    private static final String JAVA_FX_CSS_EDITOR_MODULE_NAME = "javafx2_css"; //NOI18N
//    
//    private static Collection<Property> NON_JAVA_FX_PROPERTIES;
//    
//    public static boolean isJavaFxCssFile(FileObject file) {
//        if(file == null) {
//            return false;
//        }
//        
//        Project project = FileOwnerQuery.getOwner(file);
//        if(project == null) {
//            return false;
//        }
//        
//        return isJavaFxProject(project);
//    }
//    
//    private static boolean isJavaFxProject(Project project) {
//        //hotfix for Bug 214819 - Completion list is corrupted after IDE upgrade 
//        //http://netbeans.org/bugzilla/show_bug.cgi?id=214819
//        Preferences prefs = ProjectUtils.getPreferences(project, Project.class, false);
//        String isFX = prefs.get("issue214819_fx_enabled", "false"); //NOI18N
//        if(isFX != null && isFX.equals("true")) {
//            return true;
//        }
//        return false;
//    }
//    
//    /**
//     * 
//     * @param filter_out_java_fx if true the returned collection won't contain
//     *                           properties defined by the javafx2.editor module.
//     */
//    public static synchronized Collection<Property> getProperties(boolean filter_out_java_fx) {
//        if(!filter_out_java_fx) {
//            return getProperties();
//        }
//
//        //better cache the non-java fx properties
//        if (NON_JAVA_FX_PROPERTIES == null) {
//            NON_JAVA_FX_PROPERTIES = new ArrayList<Property>();
//            for (Property p : getProperties()) {
//                if (!JAVA_FX_CSS_EDITOR_MODULE_NAME.equals(p.getCssModule().getName())) {
//                    NON_JAVA_FX_PROPERTIES.add(p);
//                }
//            }
//        }
//
//        return NON_JAVA_FX_PROPERTIES;
//    }
//    
//    public static Collection<Property> getProperties(FileObject file) {
//        return getProperties(!isJavaFxCssFile(file));
//    }
//    
//    public static Collection<Property> getProperties(FeatureContext featureContext) {
//        return getProperties(featureContext.getSource().getFileObject());
//    }
//
//    public static PropertyModel getPropertyModel(String name, FileObject file) {
//        PropertyModel pm = getPropertyModel(name);
//        if(pm == null) {
//            return null;
//        }
//        
//        Property p = pm.getProperty();
//        return getProperties(file).contains(p) ? pm : null; 
//    }
//    
//    //eof hotfix

    public static List<CompletionProposal> getCompletionProposals(CompletionContext context) {
        List<CompletionProposal> all = new ArrayList<>();
        for (CssEditorModule module : getModules()) {
            all.addAll(module.getCompletionProposals(context));
        }
        return all;
    }

    public static Collection<String> getPseudoClasses(EditorFeatureContext context) {
        Collection<String> all = new HashSet<>();
        for (CssEditorModule module : getModules()) {
            Collection<String> vals = module.getPseudoClasses(context);
            if (vals != null) {
                all.addAll(vals);
            }
        }
        return all;
    }

    public static Collection<String> getPseudoElements(EditorFeatureContext context) {
        Collection<String> all = new HashSet<>();
        for (CssEditorModule module : getModules()) {
            Collection<String> vals = module.getPseudoElements(context);
            if (vals != null) {
                all.addAll(vals);
            }
        }
        return all;
    }

    public static SortedSet<Browser> getBrowsers(FileObject file) {
        //sort by browser name
        SortedSet<Browser> all = new TreeSet<>(new Comparator<Browser>() {

            @Override
            public int compare(Browser t, Browser t1) {
                return t.getName().compareTo(t1.getName());
            }
        });
        for (CssEditorModule module : getModules()) {
            Collection<Browser> extraBrowsers = module.getExtraBrowsers(file);
            if (extraBrowsers != null) {
                all.addAll(extraBrowsers);
            }
        }
        return all;
    }

    public static boolean isPropertySupported(String propertyName, Browser browser) {
        for (CssEditorModule module : getModules()) {
            PropertySupportResolver.Factory factory = module.getPropertySupportResolverFactory();
            if (factory != null) {
                PropertySupportResolver resolver = factory.createPropertySupportResolver(browser);
                if (resolver != null) {
                    if (resolver.isPropertySupported(propertyName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static HelpResolver getHelpResolver() {
        return new HelpResolver() {

            @Override
            public String getHelp(FileObject context, PropertyDefinition property) {
                StringBuilder sb = new StringBuilder();
                for (HelpResolver resolver : getSortedHelpResolvers(context)) {
                    String help = resolver.getHelp(context, property);
                    if (help != null) {
                        sb.append(help);
                    }
                }
                return sb.toString();
            }

            @Override
            public URL resolveLink(FileObject context, PropertyDefinition property, String link) {
                for (HelpResolver resolver : getSortedHelpResolvers(context)) {
                    URL url = resolver.resolveLink(context, property, link);
                    if (url != null) {
                        return url;
                    }
                }
                return null;
            }

            @Override
            public int getPriority() {
                return 0;
            }
        };

    }

    private static Collection<HelpResolver> getSortedHelpResolvers(FileObject file) {
        List<HelpResolver> list = new ArrayList<>();
        for (CssEditorModule module : getModules()) {
            Collection<HelpResolver> resolvers = module.getHelpResolvers(file);
            if (resolvers != null) {
                list.addAll(resolvers);
            }
        }

        list.sort(new Comparator<HelpResolver>() {

            @Override
            public int compare(HelpResolver t1, HelpResolver t2) {
                int i1 = t1.getPriority();
                int i2 = t2.getPriority();
                return Integer.valueOf(i1).compareTo(Integer.valueOf(i2));
            }
        });

        return list;
    }
}
