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

package org.netbeans.modules.maven.indexer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import org.apache.commons.codec.binary.Base32;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.Field;
import org.apache.maven.index.Indexer;
import org.apache.maven.index.IndexerField;
import org.apache.maven.index.IndexerFieldVersion;
import org.apache.maven.index.context.IndexUtils;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.creator.AbstractIndexCreator;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;
import org.apache.maven.index.expr.StringSearchExpression;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.ClassUsage;


/**
 * Scans classes in (local) JARs for their Java dependencies.
 */
class ClassDependencyIndexCreator extends AbstractIndexCreator {

    private static final Logger LOG = Logger.getLogger(ClassDependencyIndexCreator.class.getName());

    private static final String NB_DEPENDENCY_CLASSES = "nbdc";
    private static final IndexerField FLD_NB_DEPENDENCY_CLASS = new IndexerField(
            new Field(null, "urn:NbClassDependenciesIndexCreator", NB_DEPENDENCY_CLASSES, "Java dependencies"),
            IndexerFieldVersion.V3, NB_DEPENDENCY_CLASSES, "Java dependencies", IndexerField.ANALYZED_STORED);

    ClassDependencyIndexCreator() {
        super(ClassDependencyIndexCreator.class.getName(), Arrays.asList(MinimalArtifactInfoIndexCreator.ID));
    }

    // XXX should rather be Map<ArtifactInfo,...> so we do not rely on interleaving of populateArtifactInfo vs. updateDocument
    /** class/in/this/Jar -> [foreign/Class, other/foreign/Nested$Class] */
    private Map<String,Set<String>> classDeps;

    @Override
    public void populateArtifactInfo(ArtifactContext context) throws IOException {
        classDeps = null;
        ArtifactInfo ai = context.getArtifactInfo();
        if (ai.getClassifier() != null) {
            return;
        }
        if ("pom".equals(ai.getPackaging()) || ai.getFileExtension().endsWith(".lastUpdated")) {
            return;
        }
        File jar = context.getArtifact();
        if (jar == null || !jar.isFile()) {
            LOG.log(Level.FINER, "no artifact for {0}", ai); // not a big deal, maybe just *.pom (or *.pom + *.nbm) here
            return;
        }
        if (jar.length() == 0) {
            LOG.log(Level.FINER, "zero length jar for {0}", ai); // Don't try to index zero length files
            return;
        }
        String packaging = ai.getPackaging();
        if (packaging == null || (!packaging.equals("jar") && !isArchiveFile(jar))) {
            LOG.log(Level.FINE, "skipping artifact {0} with unrecognized packaging based on {1}", new Object[] {ai, jar});
            return;
        }
        LOG.log(Level.FINER, "reading {0}", jar);
        classDeps = new HashMap<>();
        read(jar, (String name, InputStream stream, Set<String> classes) -> {
            try {
                addDependenciesToMap(name, stream, classDeps, classes);
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Exception indexing " + jar, ex);
            }
        });
    }

    // adapted from FileUtil, since we do not want to have to use FileObject's here
    private static boolean isArchiveFile(File jar) throws IOException {
        try (InputStream in = new FileInputStream(jar)) {
            byte[] buffer = new byte[4];
            return in.read(buffer, 0, 4) == 4 && (Arrays.equals(ZIP_HEADER_1, buffer) || Arrays.equals(ZIP_HEADER_2, buffer));
        }
    }
    private static final byte[] ZIP_HEADER_1 = {80, 75, 3, 4};
    private static final byte[] ZIP_HEADER_2 = {80, 75, 5, 6};
    
    @Override public boolean updateArtifactInfo(Document document, ArtifactInfo artifactInfo) {
        return false;
    }
    
    @Override public void updateDocument(ArtifactInfo ai, Document doc) {
        if (classDeps == null || classDeps.isEmpty()) {
            return;
        }
        if (ai.getClassNames() == null) {
            // Might be *.hpi, *.war, etc. - so JarFileContentsIndexCreator ignores it (and our results would anyway be wrong due to WEB-INF/classes/ prefix)
            LOG.log(Level.FINE, "no class names in index for {0}; therefore cannot store class usages", ai);
            return;
        }
        StringBuilder b = new StringBuilder();
        String[] classNamesSplit = ai.getClassNames().split("\n");
        for (String referrerTopLevel : classNamesSplit) {
            Set<String> referees = classDeps.remove(referrerTopLevel.substring(1));
            if (referees != null) {
                for (String referee : referees) {
                    b.append(crc32base32(referee));
                    b.append(' ');
                }
            }
            b.append(' ');
        }
        if (!classDeps.isEmpty()) {
            // E.g. findbugs-1.2.0.jar has TigerSubstitutes.class, TigerSubstitutesTest$Foo.class, etc., but no TigerSubstitutesTest.class (?)
            // Or guice-3.0-rc2.jar has e.g. $Transformer.class with no source equivalent.
            LOG.log(Level.FINE, "found dependencies for {0} from classes {1} not among {2}", new Object[] {ai, classDeps.keySet(), Arrays.asList(classNamesSplit)});
        }
        LOG.log(Level.FINER, "Class dependencies index field: {0}", b);
        // XXX is it possible to _store_ something more compact (binary) using a custom tokenizer?
        // seems like DefaultIndexingContext hardcodes NexusAnalyzer
        doc.add(FLD_NB_DEPENDENCY_CLASS.toField(b.toString()));
    }

    static void search(String className, Indexer indexer, Collection<IndexingContext> contexts, List<? super ClassUsage> results) throws IOException {
        String searchString = crc32base32(className.replace('.', '/'));
        Query refClassQuery = indexer.constructQuery(FLD_NB_DEPENDENCY_CLASS.getOntology(), new StringSearchExpression(searchString));
        TopScoreDocCollector collector = TopScoreDocCollector.create(NexusRepositoryIndexerImpl.MAX_RESULT_COUNT, Integer.MAX_VALUE);
        for (IndexingContext context : contexts) {
            IndexSearcher searcher = context.acquireIndexSearcher();
            try {
                StoredFields storedFields = searcher.storedFields();
                searcher.search(refClassQuery, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;
                LOG.log(Level.FINER, "for {0} ~ {1} found {2} hits", new Object[] {className, searchString, hits.length});
                for (ScoreDoc hit : hits) {
                    Document d = storedFields.document(hit.doc);
                    String fldValue = d.get(NB_DEPENDENCY_CLASSES);
                    LOG.log(Level.FINER, "{0} uses: {1}", new Object[] {className, fldValue});
                    Set<String> refClasses = parseField(searchString, fldValue, d.get(ArtifactInfo.NAMES));
                    if (!refClasses.isEmpty()) {
                        ArtifactInfo ai = IndexUtils.constructArtifactInfo(d, context);
                        if (ai != null) {
                            ai.setRepository(context.getRepositoryId());
                            List<NBVersionInfo> version = NexusRepositoryIndexerImpl.convertToNBVersionInfo(List.of(ai));
                            if (!version.isEmpty()) {
                                results.add(new ClassUsage(version.get(0), refClasses));
                            }
                        }
                    }
                }
            } finally {
                context.releaseIndexSearcher(searcher);
            }
        }
    }

    private static Set<String> parseField(String refereeCRC, String field, String referrersNL) {
        assert refereeCRC.length() == 7;
        Set<String> referrers = new TreeSet<>();
        int p = 0;
        for (String referrer : referrersNL.split("\n")) {
            while (true) {
                if (field.charAt(p) == ' ') {
                    p++;
                    break;
                }
                if (field.regionMatches(p, refereeCRC, 0, refereeCRC.length())) {
                    referrers.add(referrer.substring(1).replace('/', '.'));
                }
                p += 8;
            }
        }
        return referrers;
    }

    private static final String[] JDK_CLASS_TEST = new String[] {
        "apple/applescript", "apple/laf", "apple/launcher", "apple/security",
        "com/apple/concurrent", "com/apple/eawt", "com/apple/eio", "com/apple/laf", "com/oracle/net",
        "com/oracle/nio", "com/oracle/util", "com/oracle/webservices", "com/oracle/xmlns",
        "com/sun/accessibility", "com/sun/activation", "com/sun/awt", "com/sun/beans", "com/sun/corba",
        "com/sun/demo", "com/sun/image", "com/sun/imageio", "com/sun/istack", "com/sun/java",
        "com/sun/java_cup", "com/sun/jmx", "com/sun/jndi", "com/sun/management", "com/sun/media",
        "com/sun/naming", "com/sun/net", "com/sun/nio", "com/sun/org", "com/sun/rmi", "com/sun/rowset",
        "com/sun/security", "com/sun/swing", "com/sun/tracing", "com/sun/xml", "java/applet", "java/awt",
        "java/awt/color", "java/awt/datatransfer", "java/awt/dnd", "java/awt/event", "java/awt/font",
        "java/awt/geom", "java/awt/im", "java/awt/image", "java/awt/peer", "java/awt/print",
        "java/beans", "java/beans/beancontext", "java/io", "java/lang", "java/lang/annotation",
        "java/lang/instrument", "java/lang/invoke", "java/lang/management", "java/lang/ref",
        "java/lang/reflect", "java/math", "java/net", "java/nio", "java/nio/channels", "java/nio/charset",
        "java/nio/file", "java/rmi", "java/rmi/activation", "java/rmi/dgc", "java/rmi/registry",
        "java/rmi/server", "java/security", "java/security/acl", "java/security/cert",
        "java/security/interfaces", "java/security/spec", "java/sql", "java/text", "java/text/spi", "java/time",
        "java/time/chrono", "java/time/format", "java/time/temporal", "java/time/zone", "java/util",
        "java/util/concurrent", "java/util/function", "java/util/jar", "java/util/logging",
        "java/util/prefs", "java/util/regex", "java/util/spi", "java/util/stream", "java/util/zip",
        "javax/accessibility", "javax/activation", "javax/activity", "javax/annotation",
        "javax/annotation/processing", "javax/imageio", "javax/imageio/event", "javax/imageio/metadata",
        "javax/imageio/plugins", "javax/imageio/spi", "javax/imageio/stream", "javax/jws", "javax/jws/soap",
        "javax/lang/model", "javax/management", "javax/management/loading",
        "javax/management/modelmbean", "javax/management/monitor", "javax/management/openmbean",
        "javax/management/relation", "javax/management/remote", "javax/management/timer", "javax/naming",
        "javax/naming/directory", "javax/naming/event", "javax/naming/ldap", "javax/naming/spi", "javax/net",
        "javax/net/ssl", "javax/print", "javax/print/attribute", "javax/print/event", "javax/rmi",
        "javax/rmi/CORBA", "javax/rmi/ssl", "javax/script", "javax/security/auth",
        "javax/security/cert", "javax/security/sasl", "javax/smartcardio", "javax/sound/midi",
        "javax/sound/sampled", "javax/sql", "javax/sql/rowset", "javax/swing", "javax/swing/border",
        "javax/swing/colorchooser", "javax/swing/event", "javax/swing/filechooser", "javax/swing/plaf",
        "javax/swing/table", "javax/swing/text", "javax/swing/tree", "javax/swing/undo", "javax/tools",
        "javax/transaction", "javax/transaction/xa", "javax/xml", "javax/xml/bind", "javax/xml/crypto",
        "javax/xml/datatype", "javax/xml/namespace", "javax/xml/parsers", "javax/xml/soap",
        "javax/xml/stream", "javax/xml/transform", "javax/xml/validation", "javax/xml/ws",
        "javax/xml/xpath", "jdk/incubator", "jdk/internal/cmm", "jdk/internal/instrumentation", "jdk/internal/org",
        "jdk/internal/util", "jdk/management/cmm", "jdk/management/resource", "jdk/net",
        "jdk/xml/internal", "org/ietf/jgss", "org/jcp/xml", "org/omg/CORBA", "org/omg/CORBA_2_3",
        "org/omg/CosNaming", "org/omg/Dynamic", "org/omg/DynamicAny", "org/omg/IOP", "org/omg/Messaging",
        "org/omg/PortableInterceptor", "org/omg/PortableServer", "org/omg/SendingContext", "org/omg/stub",
        "org/w3c/dom", "org/xml/sax"
    };

    /**
     * @param referrer a referring class, as {@code pkg/Outer$Inner}
     * @param classData its bytecode
     * @param depsMap map from referring outer classes (as {@code pkg/Outer}) to referred-to classes (as {@code pkg/Outer$Inner})
     * @param siblings other referring classes in the same artifact (including this one), as {@code pkg/Outer$Inner}
     */
    private static void addDependenciesToMap(String referrer, InputStream classData, Map<String, Set<String>> depsMap, Set<String> siblings) throws IOException {

        int shell = referrer.indexOf('$', referrer.lastIndexOf('/') + 1);
        String referrerTopLevel = shell == -1 ? referrer : referrer.substring(0, shell);

        Set<String> referees = depsMap.computeIfAbsent(referrerTopLevel, k -> new HashSet<>());

        dependenciesOf(classData)
            .filter((referee) -> !referrer.equals(referee))
            .filter((referee) -> !siblings.contains(referee)) // in same JAR, not interesting
            .filter((referee) -> {
                for (int i = 0; i < JDK_CLASS_TEST.length; i++)
                    if (referee.startsWith(JDK_CLASS_TEST[i]))
                        return false;
                return true;
            })
            .forEach((referee) -> referees.add(referee));
    }

    @FunctionalInterface
    interface JarClassEntryConsumer {

        void accept(String name, InputStream classData, Set<String> siblings) throws IOException;
    }

    // XXX in unit tests, indexing is always single-threaded,
    // in which case the byte array can be a field instead of
    // a thread local.  Not clear if that is the case in the IDE.
    final ThreadLocal<byte[]> BYTES = new ThreadLocal<>();
    // A reasonable base array size that will accommodate typical
    // class files, to avoid reallocating more than necessary
    private static final int MIN_ARRAY_SIZE = 16384;

    byte[] bytes(int size) {
        // There is a pretty significant performance benefit
        // to not allocating vast numbers of byte arrays
        byte[] result = BYTES.get();
        if (result == null || result.length < size) {
            result = new byte[Math.max(MIN_ARRAY_SIZE, size)];
            BYTES.set(result);
        }
        return result;
    }

    void read(File jar, JarClassEntryConsumer consumer) throws IOException {
        Set<String> classNames = new HashSet<>(512);
        try (JarFile jf = new JarFile(jar, false)) {
            // XXX the original code ignores siblings by first having a list
            // of the class names.  Getting this before processing JAR entries
            // means iterating the zip index twice.  Not horrible, but would
            // be nice to avoid it
            Enumeration<JarEntry> e = jf.entries();
            while (e.hasMoreElements()) {
                JarEntry entry = e.nextElement();
                String name = entry.getName();
                if (!name.endsWith(".class")) {
                    continue;
                }
                String clazz = name.substring(0, name.length() - 6);
                classNames.add(clazz);
            }
            e = jf.entries();
            while (e.hasMoreElements()) {
                JarEntry entry = e.nextElement();
                String name = entry.getName();
                if (!name.endsWith(".class")) {
                    continue;
                }
                int size = Math.max((int) entry.getSize(), 0);
                if (size > 0) {
                    // Parsing is considerably faster if the data is preloaded
                    // into a byte array, likely due to random access
                    byte[] target = bytes(size);
                    try (InputStream in = jf.getInputStream(entry)) {
                        int pos = 0;
                        int count = 0;
                        while (count != -1 && pos < size) {
                            count = in.read(target, pos, size - pos);
                            pos += count == -1 ? 0 : count;
                        }
                    }
                    try (InputStream in = new ByteArrayInputStream(target, 0, size)) {
                        String clazz = name.substring(0, name.length() - 6);
                        consumer.accept(clazz, in, classNames);
                    }
                }
            }
        } catch (SecurityException x) {
            throw new IOException(x);
        }
    }

    private static Stream<String> dependenciesOf(InputStream classData) throws IOException {
        return new ClassFile(classData).getAllClassNames()
                .stream().unordered().map(ClassName::getInternalName).distinct();
    }

    static final List<IndexerField> INDEXER_FIELDS = List.of(FLD_NB_DEPENDENCY_CLASS);
    @Override
    public Collection<IndexerField> getIndexerFields() {
        return INDEXER_FIELDS;
    }

    /**
     * @param s a String, such as a class name
     * @return the CRC-32 of its UTF-8 representation, as big-endian Base-32 without padding (so seven chars), lower case (to not confuse maven-indexer)
     */
    static String crc32base32(String s) {
        CRC32 crc = new CRC32();
        crc.update(s.getBytes(StandardCharsets.UTF_8));
        long v = crc.getValue();
        byte[] b32 = base32.encode(new byte[] {(byte) (v >> 24 & 0xFF), (byte) (v >> 16 & 0xFF), (byte) (v >> 8 & 0xFF), (byte) (v & 0xFF)});
        assert b32.length == 8;
        assert b32[7] == '=';
        return new String(b32, 0, 7, StandardCharsets.ISO_8859_1).toLowerCase();
    }

    private static final Base32 base32 = new Base32();

}
