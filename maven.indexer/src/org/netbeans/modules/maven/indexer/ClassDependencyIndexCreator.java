/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.indexer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
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
import org.codehaus.plexus.util.Base64;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.ClassUsage;
import org.openide.filesystems.FileUtil;

/**
 * Scans classes in (local) JARs for their Java dependencies.
 */
class ClassDependencyIndexCreator extends AbstractIndexCreator {

    private static final Logger LOG = Logger.getLogger(ClassDependencyIndexCreator.class.getName());

    private static final String NB_DEPENDENCY_CLASSES = "nbdc";
    private static final IndexerField FLD_NB_DEPENDENCY_CLASS = new IndexerField(new Field(null, "urn:NbClassDependenciesIndexCreator", NB_DEPENDENCY_CLASSES, "Java dependencies"), IndexerFieldVersion.V3, NB_DEPENDENCY_CLASSES, "Java dependencies", Store.YES, Index.ANALYZED);

    ClassDependencyIndexCreator() {
        super(ClassDependencyIndexCreator.class.getName(), Arrays.asList(MinimalArtifactInfoIndexCreator.ID));
    }

    // XXX should rather be Map<ArtifactInfo,...> so we do not rely on interleaving of populateArtifactInfo vs. updateDocument
    /** class/in/this/Jar -> [foreign/Class, other/foreign/Nested$Class] */
    private Map<String,Set<String>> classDeps;

    @Override public void populateArtifactInfo(ArtifactContext context) throws IOException {
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
        String packaging = ai.getPackaging();
        if (packaging == null || (!packaging.equals("jar") && !isArchiveFile(jar))) {
            LOG.log(Level.FINE, "skipping artifact {0} with unrecognized packaging based on {1}", new Object[] {ai, jar});
            return;
        }
        LOG.log(Level.FINER, "reading {0}", jar);
        Map<String, byte[]> classfiles = read(jar);
        classDeps = new HashMap<String, Set<String>>();
        Set<String> classes = classfiles.keySet();
        for (Map.Entry<String, byte[]> entry : classfiles.entrySet()) {
            addDependenciesToMap(entry.getKey(), entry.getValue(), classDeps, classes, jar);
        }
    }

    // adapted from FileUtil, since we do not want to have to use FileObject's here
    private static boolean isArchiveFile(File jar) throws IOException {
        InputStream in = new FileInputStream(jar);
        try {
            byte[] buffer = new byte[4];
            return in.read(buffer, 0, 4) == 4 && (Arrays.equals(ZIP_HEADER_1, buffer) || Arrays.equals(ZIP_HEADER_2, buffer));
        } finally {
            in.close();
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
                    b.append(crc32base64(referee));
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
        String searchString = crc32base64(className.replace('.', '/'));
        Query refClassQuery = indexer.constructQuery(ClassDependencyIndexCreator.FLD_NB_DEPENDENCY_CLASS.getOntology(), new StringSearchExpression(searchString));
        TopScoreDocCollector collector = TopScoreDocCollector.create(NexusRepositoryIndexerImpl.MAX_RESULT_COUNT, null);
        for (IndexingContext context : contexts) {
            IndexSearcher searcher = context.acquireIndexSearcher();
            try {
        searcher.search(refClassQuery, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        LOG.log(Level.FINER, "for {0} ~ {1} found {2} hits", new Object[] {className, searchString, hits.length});
        for (ScoreDoc hit : hits) {
            int docId = hit.doc;
            Document d = searcher.doc(docId);
            String fldValue = d.get(ClassDependencyIndexCreator.NB_DEPENDENCY_CLASSES);
            LOG.log(Level.FINER, "{0} uses: {1}", new Object[] {className, fldValue});
            Set<String> refClasses = parseField(searchString, fldValue, d.get(ArtifactInfo.NAMES));
            if (!refClasses.isEmpty()) {
                ArtifactInfo ai = IndexUtils.constructArtifactInfo(d, context);
                if (ai != null) {
                    ai.setRepository(context.getRepositoryId());
                    List<NBVersionInfo> version = NexusRepositoryIndexerImpl.convertToNBVersionInfo(Collections.singleton(ai));
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
        Set<String> referrers = new TreeSet<String>();
        int p = 0;
        for (String referrer : referrersNL.split("\n")) {
            while (true) {
                if (field.charAt(p) == ' ') {
                    p++;
                    break;
                }
                if (field.substring(p, p + 6).equals(refereeCRC)) {
                    referrers.add(referrer.substring(1).replace('/', '.'));
                }
                p += 7;
            }
        }
        return referrers;
    }

    /**
     * @param referrer a referring class, as {@code pkg/Outer$Inner}
     * @param data its bytecode
     * @param depsMap map from referring outer classes (as {@code pkg/Outer}) to referred-to classes (as {@code pkg/Outer$Inner})
     * @param siblings other referring classes in the same artifact (including this one), as {@code pkg/Outer$Inner}
     * @param jar the jar file, for diagnostics
     */
    private static void addDependenciesToMap(String referrer, byte[] data, Map<String, Set<String>> depsMap, Set<String> siblings, File jar) throws IOException {
        ClassLoader jre = ClassLoader.getSystemClassLoader().getParent();
        int shell = referrer.indexOf('$', referrer.lastIndexOf('/') + 1);
        String referrerTopLevel = shell == -1 ? referrer : referrer.substring(0, shell);
        for (String referee : dependencies(data, referrer, jar)) {
            if (siblings.contains(referee)) {
                continue; // in same JAR, not interesting
            }
            try {
                jre.loadClass(referee.replace('/', '.')); // XXX ought to cache this result
                continue; // in JRE, not interesting
            } catch (ClassNotFoundException x) {
            }
            Set<String> referees = depsMap.get(referrerTopLevel);
            if (referees == null) {
                referees = new TreeSet<String>();
                depsMap.put(referrerTopLevel, referees);
            }
            referees.add(referee);
        }
    }

    static Map<String,byte[]> read(File jar) throws IOException {
        JarFile jf = new JarFile(jar, false);
        try {
            Map<String, byte[]> classfiles = new TreeMap<String, byte[]>();
            Enumeration<JarEntry> e = jf.entries();
            while (e.hasMoreElements()) {
                JarEntry entry = e.nextElement();
                String name = entry.getName();
                if (!name.endsWith(".class")) {
                    continue;
                }
                String clazz = name.substring(0, name.length() - 6);
                ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max((int) entry.getSize(), 0));
                InputStream is = jf.getInputStream(entry);
                try {
                    FileUtil.copy(is, baos);
                } finally {
                    is.close();
                }
                classfiles.put(clazz, baos.toByteArray());
            }
            return classfiles;
        } catch (SecurityException x) {
            throw new IOException(x);
        } finally {
            jf.close();
        }
    }

    // adapted from org.netbeans.nbbuild.VerifyClassLinkage
    private static Set<String> dependencies(byte[] data, String clazz, File jar) throws IOException {
        Set<String> result = new TreeSet<String>();
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(data));
        ClassFile cf = new ClassFile(input);
        
        Set<ClassName> cl = cf.getAllClassNames();
        for (ClassName className : cl) {
            result.add(className.getInternalName());
        }
        return result;
    }

    private static void skip(DataInput input, int bytes) throws IOException {
        int skipped = input.skipBytes(bytes);
        if (skipped != bytes) {
            throw new IOException("Truncated class file");
        }
    }

    @Override public Collection<IndexerField> getIndexerFields() {
        return Arrays.asList(FLD_NB_DEPENDENCY_CLASS);
    }

    /**
     * @param s a string, such as a class name
     * @return the CRC-32 of its UTF-8 representation, as big-endian Base-64 without padding (so six chars), with _ for + (safer for Lucene)
     */
    static String crc32base64(String s) {
        crc.reset();
        crc.update(s.getBytes(UTF8));
        long v = crc.getValue();
        byte[] b64 = Base64.encodeBase64(new byte[] {(byte) (v >> 24 & 0xFF), (byte) (v >> 16 & 0xFF), (byte) (v >> 8 & 0xFF), (byte) (v & 0xFF)});
        assert b64.length == 8;
        assert b64[6] == '=';
        assert b64[7] == '=';
        return new String(b64, 0, 6, LATIN1).replace('+', '_');
    }
    private static final CRC32 crc = new CRC32();
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Charset LATIN1 = Charset.forName("ISO-8859-1");

}
