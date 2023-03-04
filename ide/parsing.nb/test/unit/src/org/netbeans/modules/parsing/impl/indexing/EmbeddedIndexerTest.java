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
package org.netbeans.modules.parsing.impl.indexing;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.document.EditorMimeTypes;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.modules.editor.lib2.EditorApiPackageAccessor;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.EmbeddingProviderFactory;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.editor.document.EditorMimeTypesImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.test.TestFileUtils;

/**
 *
 * @author Tomas Zezula
 */
public class EmbeddedIndexerTest extends IndexingTestBase {

    private static final String EXT_TOP = "top";                //NOI18N
    private static final String MIME_TOP = "text/x-top";        //NOI18N
    private static final String MIME_INNER = "text/x-inner";    //NOI18N
    private static final String PATH_TOP_SOURCES = "top-src";   //NOI18N

    private FileObject srcRoot;
    private FileObject srcFile;
    private ClassPath srcCp;
    private final Map<String, Map<ClassPath,Void>> registeredClasspaths = new HashMap<String, Map<ClassPath,Void>>();

    public EmbeddedIndexerTest(@NonNull final String name) {
        super(name);
    }
    
    protected Class[] getMockServices() { return null; }

    @Override
    protected void getAdditionalServices(List<Class> clazz) {
        clazz.add(TopPathRecognizer.class);
        clazz.add(TopLoader.class);
        clazz.add(ClassPathProviderImpl.class);
        clazz.add(MimeProviderImpl.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir();
        final File _wd = this.getWorkDir();
        final FileObject wd = FileUtil.toFileObject(_wd);
        final FileObject cache = wd.createFolder("cache");  //NOI18N
        CacheFolder.setCacheFolder(cache);
        final Map<String,Object> attrMap = new HashMap<String,Object>();
        attrMap.put(EmbeddingProviderFactory.ATTR_TARGET_MIME_TYPE, MIME_INNER);
        attrMap.put(EmbeddingProviderFactory.ATTR_PROVIDER, new TopToInnerEmbProvider());
        MockMimeLookup.setInstances(
                MimePath.get(MIME_TOP),
                new TopParser.Factory(),
                EmbeddingProviderFactory.create(attrMap),
                new TopIndexer.Factory());
        MockMimeLookup.setInstances(
                MimePath.get(MIME_INNER),
                new InnerParser.Factory(),
                new InnerIndexer.Factory());
        srcRoot = wd.createFolder("src");   //NOI18N
        srcFile = FileUtil.toFileObject(
            TestFileUtils.writeFile(
                new File(FileUtil.toFile(srcRoot), "source.top"),   //NOI18N
                "   <A>   <B>   < A> < >"));                        //NOI18N
        FileUtil.setMIMEType(EXT_TOP, MIME_TOP);
        RepositoryUpdaterTest.setMimeTypes(MIME_TOP, MIME_INNER);
        ClassPathProviderImpl.setRoot(srcRoot);
        RepositoryUpdaterTest.waitForRepositoryUpdaterInit();
    }

    @Override
    protected void tearDown() throws Exception {
        for(String id : registeredClasspaths.keySet()) {
            final Map<ClassPath,Void> classpaths = registeredClasspaths.get(id);
            GlobalPathRegistry.getDefault().unregister(id, classpaths.keySet().toArray(new ClassPath[classpaths.size()]));
        }

        super.tearDown();
    }

    public void testEmbeddingIndexerQueryOnOuterAndInner() throws Exception {
        RepositoryUpdater ru = RepositoryUpdater.getDefault();
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedUnknowns().size());

        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);

        srcCp = ClassPath.getClassPath(srcRoot, PATH_TOP_SOURCES);
        assertNotNull(srcCp);
        assertEquals(1, srcCp.getRoots().length);
        assertEquals(srcRoot, srcCp.getRoots()[0]);
        globalPathRegistry_register(PATH_TOP_SOURCES, srcCp);
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(srcRoot.toURL(), handler.getSources().get(0));

        //Symulate EditorRegistry
        final Source src = Source.create(srcFile);
        ParserManager.parse(Collections.<Source>singleton(src), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
            }
        });
        final DataObject dobj = DataObject.find(srcFile);
        final EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
        final StyledDocument doc = ec.openDocument();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                final JEditorPane jp = new JEditorPane() {
                    @Override
                    public boolean isFocusOwner() {
                        return true;
                    }
                };
                jp.setDocument(doc);
                EditorApiPackageAccessor.get().register(jp);
            }
        });

        //Do modification
        NbDocument.runAtomic(doc, new Runnable() {
            @Override
            public void run() {
                try {                    
                    doc.insertString(doc.getLength(), "<C>", null); //NOI18N
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });

        //Query should be updated
        QuerySupport sup = QuerySupport.forRoots(TopIndexer.NAME, TopIndexer.VERSION, srcRoot);
        Collection<? extends IndexResult> res = sup.query("_sn", srcFile.getNameExt(), QuerySupport.Kind.EXACT, (String[]) null);
        assertEquals(1,res.size());
        assertEquals(Boolean.TRUE.toString(), res.iterator().next().getValue("valid")); //NOI18N

        sup = QuerySupport.forRoots(InnerIndexer.NAME, InnerIndexer.VERSION, srcRoot);
        res = sup.query("_sn", srcFile.getNameExt(), QuerySupport.Kind.EXACT, (String[]) null);
        assertEquals(5,res.size());
        Map<? extends Integer,? extends Integer> count = countModes(res);
        assertEquals(Integer.valueOf(1), count.get(0));
        assertEquals(Integer.valueOf(2), count.get(1));
        assertEquals(Integer.valueOf(1), count.get(2));
        assertEquals(Integer.valueOf(1), count.get(3));
    }

    public void testEmbeddingIndexerQueryOnInnerOnly() throws Exception {
        RepositoryUpdater ru = RepositoryUpdater.getDefault();
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedBinaries().size());
        assertEquals(0, ru.getScannedUnknowns().size());

        final RepositoryUpdaterTest.TestHandler handler = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName()+".tests");
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);

        srcCp = ClassPath.getClassPath(srcRoot, PATH_TOP_SOURCES);
        assertNotNull(srcCp);
        assertEquals(1, srcCp.getRoots().length);
        assertEquals(srcRoot, srcCp.getRoots()[0]);
        globalPathRegistry_register(PATH_TOP_SOURCES, srcCp);
        assertTrue (handler.await());
        assertEquals(0, handler.getBinaries().size());
        assertEquals(1, handler.getSources().size());
        assertEquals(srcRoot.toURL(), handler.getSources().get(0));

        QuerySupport sup;
        Collection<? extends IndexResult> res;
        Map<? extends Integer,? extends Integer> count;

        //Symulate EditorRegistry
        final Source src = Source.create(srcFile);
        ParserManager.parse(Collections.<Source>singleton(src), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
            }
        });
        final DataObject dobj = DataObject.find(srcFile);
        final EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
        final StyledDocument doc = ec.openDocument();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                final JEditorPane jp = new JEditorPane() {
                    @Override
                    public boolean isFocusOwner() {
                        return true;
                    }
                };
                jp.setDocument(doc);
                EditorApiPackageAccessor.get().register(jp);
            }
        });

        //Do modification
        NbDocument.runAtomic(doc, new Runnable() {
            @Override
            public void run() {
                try {
                    doc.insertString(doc.getLength(), "<C>", null); //NOI18N
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });

        //Query should be updated
        sup = QuerySupport.forRoots(InnerIndexer.NAME, InnerIndexer.VERSION, srcRoot);
        res = sup.query("_sn", srcFile.getNameExt(), QuerySupport.Kind.EXACT, (String[]) null);
        assertEquals(5,res.size());
        count = countModes(res);
        assertEquals(Integer.valueOf(1), count.get(0));
        assertEquals(Integer.valueOf(2), count.get(1));
        assertEquals(Integer.valueOf(1), count.get(2));
        assertEquals(Integer.valueOf(1), count.get(3));
    }

    private static Map<? extends Integer, ? extends Integer> countModes(@NonNull final Collection<? extends IndexResult> docs)  {
        final Map<Integer,Integer> res = new HashMap<Integer, Integer>();
        for (IndexResult doc : docs) {
            final String value = doc.getValue("mode");  //NOI18N
            if (value != null) {
                try {
                    Integer count = res.get(Integer.parseInt(value));
                    count = count == null ? 1 : count + 1;
                    res.put(Integer.parseInt(value), count);
                } catch (NumberFormatException e) {}
            }
        }
        return res;
    }

    private void globalPathRegistry_register(String id, ClassPath... classpaths) {
        Map<ClassPath,Void> map = registeredClasspaths.get(id);
        if (map == null) {
            map = new IdentityHashMap<ClassPath, Void>();
            registeredClasspaths.put(id, map);
        }
        for (ClassPath cp :  classpaths) {
            map.put(cp,null);
        }
        GlobalPathRegistry.getDefault().register(id, classpaths);
    }

    public static class TopParser extends Parser {

        private TopResult resultCache;

        @Override
        public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
            final CharSequence toParse = snapshot.getText();
            boolean valid = true;
            int embStart = -1;
            for (int i=0; i< toParse.length(); i++) {
                if (toParse.charAt(i) == '<') {         //NOI18N
                    if (embStart != -1) {
                        valid = false;
                        break;
                    }
                    embStart = i+1;
                } else if (toParse.charAt(i) == '>') {  //NOI18N
                    if (embStart == -1 || embStart == i) {
                        valid = false;
                        break;
                    }
                    embStart = -1;
                }
            }
            resultCache = new TopResult(snapshot, valid);
        }

        @Override
        public Result getResult(Task task) throws ParseException {
            assert resultCache != null;
            return resultCache;
        }

        @Override
        public void addChangeListener(ChangeListener changeListener) {
        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {
        }

        static class TopResult extends Result {

            private final boolean valid;

            TopResult(
                @NonNull final Snapshot snapshot,
                final boolean valid) {
                super(snapshot);
                this.valid = valid;
            }

            boolean isValid() {
                return valid;
            }

            @Override
            protected void invalidate() {
            }
        }

        public static class Factory extends ParserFactory {

            @Override
            public Parser createParser(Collection<Snapshot> snapshots) {
                return new TopParser();
            }

        }

    }
    
    public static class InnerParser extends Parser {
        
        private InnerResult resultCache;

        @Override
        public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
            final InnerResult res = new InnerResult(snapshot);
            final CharSequence text = snapshot.getText();
            for (int i=0; i< text.length(); i++) {
                if (text.charAt(i) == 'A') {
                    res.setMode(InnerResult.A);
                    break;
                } else if (text.charAt(i) == 'B') {
                    res.setMode(InnerResult.B);
                    break;
                } else if (text.charAt(i) == 'C') {
                    res.setMode(InnerResult.C);
                    break;
                }
            }
            resultCache = res;
        }

        @Override
        public Result getResult(Task task) throws ParseException {
            assert resultCache != null;
            return resultCache;
        }

        @Override
        public void addChangeListener(ChangeListener changeListener) {
        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {
        }
        
        static class InnerResult extends Parser.Result {
            
            static final int UNKNOWN = 0;
            static final int A = 1;
            static final int B = 2;
            static final int C = 3;
            
            private int mode;
            
            InnerResult(@NonNull final Snapshot snapshot) {
                super(snapshot);
                mode = UNKNOWN;
            }

            @Override
            protected void invalidate() {
            }
            
            void setMode(int mode) {
                this.mode = mode;
            }
            
            int getMode() {
                return mode;
            }
        }
        
        public static class Factory extends ParserFactory {
            @Override
            public Parser createParser(Collection<Snapshot> snapshots) {
                return new InnerParser();
            }            
        }
        
    }

    public static class TopToInnerEmbProvider extends EmbeddingProvider {


        @Override
        public List<Embedding> getEmbeddings(@NonNull final Snapshot snapshot) {
            final List<Embedding> embs = new ArrayList<Embedding>();
            final CharSequence toParse = snapshot.getText();
            int embStart = -1;
            for (int i=0; i< toParse.length(); i++) {
                if (toParse.charAt(i) == '<') {         //NOI18N
                    embStart = i+1;
                } else if (toParse.charAt(i) == '>') {  //NOI18N
                    embs.add(snapshot.create(embStart, i-embStart, MIME_INNER));
                }
            }
            return embs;
        }

        @Override
        public int getPriority() {
            return 10;
        }

        @Override
        public void cancel() {
        }
    }

    public static class TopIndexer extends EmbeddingIndexer {

        public static final String NAME = "top";
        public static final int VERSION = 1;

        @Override
        protected void index(Indexable indexable, Parser.Result parserResult, Context context) {
            try {
                final IndexingSupport support = IndexingSupport.getInstance(context);
                support.removeDocuments(indexable);
                final IndexDocument doc = support.createDocument(indexable);
                doc.addPair(
                    "valid",    //NOI18N
                    ((TopParser.TopResult)parserResult).isValid() ? Boolean.TRUE.toString() : Boolean.FALSE.toString(),
                    false,
                    true);
                support.addDocument(doc);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        public static class Factory extends EmbeddingIndexerFactory {

            @Override
            public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
                return new TopIndexer();
            }

            @Override
            public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            }

            @Override
            public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
                try {
                    final IndexingSupport is = IndexingSupport.getInstance(context);
                    for (Indexable df : dirty) {
                        is.markDirtyDocuments(df);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            @Override
            public String getIndexerName() {
                return NAME;
            }

            @Override
            public int getIndexVersion() {
                return VERSION;
            }
        }
    }

    public static class InnerIndexer extends EmbeddingIndexer {

        public static final String NAME = "inner"; //NOI18N
        public static final int VERSION = 1;

        private final Factory f;

        private InnerIndexer(@NonNull final Factory f) {
            this.f = f;
        }

        @Override
        protected void index(Indexable indexable, Parser.Result parserResult, Context context) {
            try {
                System.err.println("Indexable: " + indexable);
                final IndexingSupport support = IndexingSupport.getInstance(context);
                final InnerParser.InnerResult ir = (InnerParser.InnerResult) parserResult;
                if (!indexable.equals(f.lastIndexable)) {
                    support.removeDocuments(indexable);
                }
                f.lastIndexable = indexable;
                final IndexDocument doc = support.createDocument(indexable);
                doc.addPair("mode", Integer.toString(ir.getMode()), true, true);    //NOI18N
                support.addDocument(doc);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        public static class Factory extends EmbeddingIndexerFactory {

            Indexable lastIndexable;

            @Override
            public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
                return new InnerIndexer(this);
            }

            @Override
            public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            }

            @Override
            public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
                try {
                    final IndexingSupport is = IndexingSupport.getInstance(context);
                    for (Indexable df : dirty) {
                        is.markDirtyDocuments(df);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            @Override
            public boolean scanStarted(Context context) {
                lastIndexable = null;
                return true;
            }

            @Override
            public String getIndexerName() {
                return NAME;
            }

            @Override
            public int getIndexVersion() {
                return VERSION;
            }

        }
    }
    
    public static class MimeProviderImpl implements EditorMimeTypesImplementation {
        private static final Set<String> MIMES;
        
        static {
            MIMES = new HashSet<>(2);
            MIMES.add(MIME_INNER);
            MIMES.add(MIME_TOP);
        }

        @Override
        public Set<String> getSupportedMimeTypes() {
            return MIMES;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    
    }

    public static class TopPathRecognizer extends PathRecognizer {

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.<String>singleton(PATH_TOP_SOURCES);
        }

        @Override
        public Set<String> getLibraryPathIds() {
            return Collections.<String>emptySet();
        }

        @Override
        public Set<String> getBinaryLibraryPathIds() {
            return Collections.<String>emptySet();
        }

        @Override
        public Set<String> getMimeTypes() {
            return Collections.<String>singleton(MIME_TOP);
        }
    }

    public static class ClassPathProviderImpl implements ClassPathProvider {

        //@GuardedBy("ClassPathProviderImpl.class")
        private static FileObject root;
        //@GuardedBy("ClassPathProviderImpl.class")
        private static ClassPath cp;


        static synchronized  void setRoot(@NonNull final FileObject r) {
            cp = null;
            root = r;
        }

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            synchronized(ClassPathProviderImpl.class) {
                if (PATH_TOP_SOURCES.equals(type) &&
                   (FileUtil.isParentOf(root, file) || root.equals(file))) {
                   if (cp == null) {
                       cp = ClassPathSupport.createClassPath(root);
                   }
                   return cp;
                }
            }
            return null;
        }

    }

    public static class TopEditor extends DataEditorSupport implements OpenCookie, EditorCookie {

        public TopEditor(DataObject dobj) {
            super(dobj, new Env(dobj));
        }

        @Override
        protected EditorKit createEditorKit() {
            return new Kit();
        }



        public static class Env extends DataEditorSupport.Env {

            public Env(DataObject dobj) {
                super(dobj);
            }

            @Override
            protected FileObject getFile() {
                return getDataObject().getPrimaryFile();
            }

            @Override
            protected FileLock takeLock() throws IOException {
                return getFile().lock();
            }

        }

        public static class Kit extends DefaultEditorKit {

            @Override
            public Document createDefaultDocument() {
                return new GuardedDocument(Kit.class);
            }
        }

    }
    
    public static class TopDataObject extends MultiDataObject {
        public TopDataObject(FileObject file, MultiFileLoader loader) throws DataObjectExistsException {
            super(file, loader);
            getCookieSet().add(new TopEditor(this));
        }

        @Override
        protected Node createNodeDelegate() {
            final Node n = new AbstractNode(
                    Children.LEAF,
                    Lookup.EMPTY);
            return n;
        }

        @Override
        protected int associateLookup() {
            return 1;
        }        
        
        
        
        
    }

    public static class TopLoader extends UniFileLoader {

        public TopLoader() {
            super(TopDataObject.class);
            final ExtensionList el = new ExtensionList();
            el.addExtension(EXT_TOP);
            setExtensions(el);
        }


        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            if (getExtensions().isRegistered(primaryFile)) {
                return new TopDataObject(primaryFile, this);
            }
            return null;
        }

    }
}
