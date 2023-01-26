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

package org.netbeans.modules.java.hints.legacy.spi;

import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.spi.java.hints.CustomizerProvider;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintDescription.Worker;
import org.netbeans.modules.java.hints.providers.spi.HintDescriptionFactory;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.providers.spi.HintProvider;
import org.netbeans.modules.java.hints.providers.spi.Trigger.Kinds;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.Rule;
import org.netbeans.modules.java.hints.spi.TreeRule;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

import static org.netbeans.spi.editor.hints.ErrorDescriptionFactory.createErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.Hint.Kind;

/** Manages rules read from the system filesystem.
 *
 * @author Petr Hrebejk
 */
@SuppressWarnings("deprecation")
public class RulesManager implements FileChangeListener {

    // The logger
    public static Logger LOG = Logger.getLogger("org.netbeans.modules.java.hints"); // NOI18N

    // Extensions of files
    private static final String INSTANCE_EXT = ".instance";

    // Non GUI attribute for NON GUI rules
    private static final String NON_GUI = "nonGUI"; // NOI18N
    
    private static final String RULES_FOLDER = "org-netbeans-modules-java-hints/rules/";  // NOI18N
    private static final String ERRORS = "errors"; // NOI18N
    private static final String HINTS = "hints"; // NOI18N
    private static final String SUGGESTIONS = "suggestions"; // NOI18N

    // Maps of registered rules
    private final Map<String, Map<String,List<ErrorRule>>> mimeType2Errors = new HashMap<String, Map<String, List<ErrorRule>>>();
    private final Map<HintMetadata, Collection<? extends HintDescription>> metadata = new HashMap<HintMetadata, Collection<? extends HintDescription>>();

    private static RulesManager INSTANCE;

    private RulesManager() {
        doInit();
    }

    public static synchronized RulesManager getInstance() {
        if ( INSTANCE == null ) {
            INSTANCE = new RulesManager();
        }
        return INSTANCE;
    }

    public synchronized Map<String,List<ErrorRule>> getErrors(String mimeType) {
        Map<String,List<ErrorRule>> res = mimeType2Errors.get(mimeType);

        if (res == null) {
            res = Collections.emptyMap();
        }

        return res;
    }

    private synchronized void doInit() {
        initErrors();
        initHints();
        initSuggestions();
    }

    // Private methods ---------------------------------------------------------

    private void initErrors() {
        FileObject folder = FileUtil.getConfigFile( RULES_FOLDER + ERRORS );
        List<Pair<Rule,FileObject>> rules = readRules( folder );
        categorizeErrorRules(rules, mimeType2Errors, folder);
    }

    private void initHints() {
        FileObject folder = FileUtil.getConfigFile( RULES_FOLDER + HINTS );
        List<Pair<Rule,FileObject>> rules = readRules(folder);
        categorizeTreeRules( rules, Kind.INSPECTION, metadata);
    }


    private void initSuggestions() {
        FileObject folder = FileUtil.getConfigFile( RULES_FOLDER + SUGGESTIONS );
        List<Pair<Rule,FileObject>> rules = readRules(folder);
        categorizeTreeRules( rules, Kind.ACTION, metadata);
    }

    /** Read rules from system filesystem */
    private List<Pair<Rule,FileObject>> readRules( FileObject folder ) {
        List<Pair<Rule,FileObject>> rules = new LinkedList<Pair<Rule,FileObject>>();
        
        if (folder == null) {
            return rules;
        }

        Queue<FileObject> q = new LinkedList<FileObject>();
        
        q.offer(folder);
        
        while(!q.isEmpty()) {
            FileObject o = q.poll();
            
            o.removeFileChangeListener(this);
            o.addFileChangeListener(this);
            
            if (o.isFolder()) {
                q.addAll(Arrays.asList(o.getChildren()));
                continue;
            }
            
            if (!o.isData()) {
                continue;
            }
            
            String name = o.getNameExt().toLowerCase();

            if ( o.canRead() ) {
                Rule r = null;
                if ( name.endsWith( INSTANCE_EXT ) ) {
                    r = instantiateRule(o);
                }
                if ( r != null ) {
                    rules.add( new Pair<Rule,FileObject>( r, o ) );
                }
            }
        }
        return rules;
    }

    private static void categorizeErrorRules( List<Pair<Rule,FileObject>> rules,
                                             Map<String, Map<String,List<ErrorRule>>> dest,
                                             FileObject rootFolder) {
        dest.clear();

        for( Pair<Rule,FileObject> pair : rules ) {
            Rule rule = pair.getA();
            FileObject fo = pair.getB();
            String mime = FileUtil.getRelativePath(rootFolder, fo.getParent());

            if (mime.length() == 0) {
                mime = "text/x-java"; //TODO: use a predefined constant
            }

            if ( rule instanceof ErrorRule ) {
                Map<String, List<ErrorRule>> map = dest.get(mime);

                if (map == null) {
                    dest.put(mime, map = new HashMap<String, List<ErrorRule>>());
                    // first encounter the MIME type; read the 'inherit' rule from
                    // the rule folder. Further definitions 
                    FileObject mimeFolder = fo.getParent();
                    Object o = mimeFolder.getAttribute("inherit.rules");
                    if (Boolean.TRUE == o) {
                        Map<String, List<ErrorRule>> inheritMap = dest.get("text/x-java");
                        for (String c : inheritMap.keySet()) {
                            map.put(c, new ArrayList<ErrorRule>(inheritMap.get(c)));
                        }
                    }
                }

                addRule( (ErrorRule)rule, map );
            }
            else {
                LOG.log( Level.WARNING, "The rule defined in " + fo.getPath() + "is not instance of ErrorRule" );
            }
        }
    }

    private static void categorizeTreeRules( List<Pair<Rule,FileObject>> rules,
                                             Kind kind,
                                             Map<HintMetadata, Collection<? extends HintDescription>> metadata) {
        for( Pair<Rule,FileObject> pair : rules ) {
            Rule rule = pair.getA();
            FileObject fo = pair.getB();

            if ( rule instanceof TreeRule ) {
                final TreeRule tr = (TreeRule) rule;
                Object nonGuiObject = fo.getAttribute(NON_GUI);
                boolean toGui = true;
                
                if ( nonGuiObject instanceof Boolean && ((Boolean)nonGuiObject).booleanValue() ) {
                    toGui = false;
                }

                FileObject parent = fo.getParent();
                HintMetadata.Builder hmb = HintMetadata.Builder.create(tr.getId())
                                                               .setCategory(parent.getName())
                                                               .setKind(kind);

                if (!toGui) hmb = hmb.addOptions(Options.NON_GUI);
                
                if (rule instanceof AbstractHint) {
                    final AbstractHint h = (AbstractHint) rule;
                    hmb = hmb.setDescription(toGui ? h.getDisplayName() : "", toGui ? h.getDescription() : "");
                    hmb = hmb.setEnabled(ACCESSOR.isEnabledDefault(h));
                    hmb = hmb.setSeverity(ACCESSOR.severiryDefault(h).toOfficialSeverity());
                    hmb = hmb.setCustomizerProvider(new CustomizerProviderImpl(h));
                    hmb = hmb.addSuppressWarnings(ACCESSOR.getSuppressBy(h));
                    if (!ACCESSOR.isShowInTaskListDefault(h)) hmb = hmb.addOptions(Options.NO_BATCH);
                    else if (h.getClass().getClassLoader() != RulesManager.class.getClassLoader()) hmb = hmb.addOptions(Options.QUERY);
                } else {
                    hmb = hmb.setDescription(toGui ? tr.getDisplayName() : "", toGui ? tr.getDisplayName() : "");
                    hmb = hmb.setSeverity(Severity.VERIFIER);
                }

                HintMetadata hm = hmb.build();
                List<HintDescription> hd = new LinkedList<HintDescription>();

                hd.add(HintDescriptionFactory.create()
                                             .setTrigger(new Kinds(tr.getTreeKinds()))
                                             .setMetadata(hm)
                                             .setWorker(new WorkerImpl(tr))
                                             .produce());

                metadata.put(hm, hd);
            }
            else {
                LOG.log( Level.WARNING, "The rule defined in " + fo.getPath() + "is not instance of TreeRule" );
            }

        }
    }

    private static void addRule( TreeRule rule, Map<Tree.Kind,List<TreeRule>> dest ) {

        for( Tree.Kind kind : rule.getTreeKinds() ) {
            List<TreeRule> l = dest.get( kind );
            if ( l == null ) {
                l = new LinkedList<TreeRule>();
                dest.put( kind, l );
            }
            l.add( rule );
        }

    }

    @SuppressWarnings("unchecked")
    private static void addRule( ErrorRule rule, Map<String,List<ErrorRule>> dest ) {

        for(String code : (Set<String>) rule.getCodes()) {
            List<ErrorRule> l = dest.get( code );
            if ( l == null ) {
                l = new LinkedList<ErrorRule>();
                dest.put( code, l );
            }
            l.add( rule );
        }

    }

    private static Rule instantiateRule( FileObject fileObject ) {
        try {
            DataObject dobj = DataObject.find(fileObject);
            InstanceCookie ic = dobj.getCookie( InstanceCookie.class );
            Object instance = ic.instanceCreate();
            
            if (instance instanceof Rule) {
                return (Rule) instance;
            } else {
                return null;
            }
        } catch( IOException e ) {
            LOG.log(Level.INFO, null, e);
        } catch ( ClassNotFoundException e ) {
            LOG.log(Level.INFO, null, e);
        }

        return null;
    }

    public void fileFolderCreated(FileEvent fe) {
        hintsChanged();
    }

    public void fileDataCreated(FileEvent fe) {
        hintsChanged();
    }

    public void fileChanged(FileEvent fe) {
        hintsChanged();
    }

    public void fileDeleted(FileEvent fe) {
        hintsChanged();
    }

    public void fileRenamed(FileRenameEvent fe) {
        hintsChanged();
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        hintsChanged();
    }

    private void hintsChanged() {
        refreshHints.cancel();
        refreshHints.schedule(50);
    }
    
    private final RequestProcessor.Task refreshHints = new RequestProcessor(RulesManager.class.getName()).create(new Runnable() {
        public void run() {
            doInit();
        }
    });

    private static final class CustomizerProviderImpl implements CustomizerProvider {
        private final AbstractHint hint;

        public CustomizerProviderImpl(AbstractHint hint) {
            this.hint = hint;
        }

        public JComponent getCustomizer(Preferences prefs) {
            return hint.getCustomizer(prefs);
        }
    }

    public static final ThreadLocal<LegacyHintConfiguration> currentHintPreferences = new ThreadLocal<LegacyHintConfiguration>();
    public static class LegacyHintConfiguration {
        public final boolean enabled;
        public final Severity severity;
        public final Preferences preferences;
        public LegacyHintConfiguration(boolean enabled, Severity severity, Preferences preferences) {
            this.enabled = enabled;
            this.severity = severity;
            this.preferences = preferences;
        }
    }
    
    private static class WorkerImpl implements Worker {
        private final TreeRule tr;

        public WorkerImpl(TreeRule tr) {
            this.tr = tr;
        }

        public Collection<? extends ErrorDescription> createErrors(HintContext ctx) {
            currentHintPreferences.set(new LegacyHintConfiguration(true, ctx.getSeverity(), ctx.getPreferences()));
            Collection<? extends ErrorDescription> result = tr.run(ctx.getInfo(), ctx.getPath());
            currentHintPreferences.set(null); //XXX: in finally

            if (result == null) return result;

            Collection<ErrorDescription> wrapped = new LinkedList<ErrorDescription>();
            String id = tr instanceof AbstractHint ? ((AbstractHint) tr).getId() : "no-id";
            String description = tr instanceof AbstractHint ? ((AbstractHint) tr).getDescription() : null;

            for (ErrorDescription ed : result) {
                if (ed == null || ed.getRange() == null) continue;
                if (!ctx.getInfo().getFileObject().equals(ed.getFile())) {
                    LOG.log(Level.SEVERE, "Got an ErrorDescription for different file, current file: {0}, error's file: {1}", new Object[] {ctx.getInfo().getFileObject().toURI(), ed.getFile().toURI()});
                    continue;
                }
                List<Fix> fixesForED = JavaFixImpl.Accessor.INSTANCE.resolveDefaultFixes(ctx, ed.getFixes().getFixes().toArray(new Fix[0]));

                ErrorDescription nue = createErrorDescription("text/x-java:" + id,
                                                              ed.getSeverity(),
                                                              ed.getDescription(),
                                                              description,
                                                              org.netbeans.spi.editor.hints.ErrorDescriptionFactory.lazyListForFixes(fixesForED),
                                                              ed.getFile(),
                                                              ed.getRange());
                wrapped.add(nue);
            }

            return wrapped;
        }
    }
    
    @ServiceProvider(service=HintProvider.class)
    public static final class HintProviderImpl implements HintProvider {

        public Map<HintMetadata, Collection<? extends HintDescription>> computeHints() {
            return RulesManager.getInstance().metadata;
        }
        
    }

    public static final class Pair<A, B> {

        private A a;
        private B b;

        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }

        public A getA() {
            return a;
        }

        public B getB() {
            return b;
        }

        @Override
        public String toString() {
            return "[" + String.valueOf(a) + "/" + String.valueOf(b) + "]";
        }
    }

    public static APIAccessor ACCESSOR;
    
    public static interface APIAccessor {

        public boolean isEnabledDefault( AbstractHint hint );

        public boolean isShowInTaskListDefault( AbstractHint hint );

        public AbstractHint.HintSeverity severiryDefault( AbstractHint hint );

        public String[] getSuppressBy(AbstractHint hint);
    }
}
