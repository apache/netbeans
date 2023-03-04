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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.Path;

// ToDo:
// stopwords configuration
// verbose mode

/** Task to run JavaHelp search indexer.
 * Creates the proper binary search database from source HTML.
 * @author Jesse Glick
 * @see <a href="http://java.sun.com/products/javahelp/">JavaHelp home page</a>
 */
public class JHIndexer extends MatchingTask {

    private Path classpath;
    private File db;
    private File basedir;
    private String locale;
    private List<BrandedFileSet> brandings = new LinkedList<>();

    /** Set the location of <samp>jhall.jar</samp> or <samp>jsearch.jar</samp> (JavaHelp tools library). */
    public Path createClasspath() {
        // JavaHelp release notes say jhtools.jar is enough, but class NoClassDefFoundError
        // on javax.help.search.IndexBuilder when I tried it...
        if (classpath == null) {
            classpath = new Path(getProject());
        }
        return classpath.createPath();
    }

    /** Set the location of the output database.
     * E.g. <samp>JavaHelpSearch</samp>).
     * <strong>Warning:</strong> the directory will be deleted and recreated.
     */
    public void setDb (File db) {
        this.db = db;
    }

    /** Set the base directory from which to scan files.
     * This should be the directory containing the helpset for the database to work correctly.
     */
    public void setBasedir (File basedir) {
        this.basedir = basedir;
    }
    public void setLocale (String locale) {
        this.locale = locale;
    }

    /**
     * A set of additional files forming a branding variant.
     * @see #addBrandedFileSet
     */
    public static final class BrandedFileSet extends FileSet {
        String branding;
        public void setBranding(String b) {
            this.branding = b;
        }
    }

    /**
     * Add a set of branded files to be indexed.
     * For example, you may have in <samp>/the/base/dir</samp>
     * <ul>
     * <li><samp>foo.html</samp>
     * <li><samp>bar.html</samp>
     * <li><samp>baz.html</samp>
     * </ul>
     * Now create a new directory <samp>/the/new/dir</samp>:
     * <ul>
     * <li><samp>foo_brand.html</samp>
     * <li><samp>baz_brand.html</samp>
     * </ul>
     * If you include this with:
     * <pre>
&lt;jhindexer basedir="/the/base/dir"&gt;
    &lt;include name="*&#42;/*.html"/&gt;
    &lt;brandedfileset dir="/the/new/dir" branding="brand"&gt;
        &lt;include name="*&#42;/*.html"/&gt;
    &lt;/brandedfileset&gt;
&lt;/jhindexer&gt;
     * </pre>
     * then the search database will contain entries:
     * <table border="1">
     * <tr><th>JH name</th><th>From file</th></tr>
     * <tr><td><samp>foo.html</samp></td><td><samp>/the/new/dir/foo_brand.html</samp></td></tr>
     * <tr><td><samp>bar.html</samp></td><td><samp>/the/base/dir/bar.html</samp></td></tr>
     * <tr><td><samp>baz.html</samp></td><td><samp>/the/new/dir/baz_brand.html</samp></td></tr>
     * </table>
     * and every file in the database (<samp>TMAP</samp> etc.)
     * will receive the special suffix <samp>_brand</samp>.
     * <p>You may give multiple branding filesets, so long as the branding
     * tokens supplied are nested: i.e. for every pair of tokens among the supplied
     * filesets, one is a prefix of the other (with <samp>_</samp> being the
     * separator between the prefix and suffix). The search database suffix is then
     * an underscore followed by the longest branding token.
     * <p>Such a database is suitable for branding NetBeans: consider a module
     * with documentation entries such as the following:
     * <ul>
     * <li><samp>modules/docs/foo.jar!/some/pkg/foo/foo.html</samp>
     * <li><samp>modules/docs/foo.jar!/some/pkg/foo/bar.html</samp>
     * <li><samp>modules/docs/foo.jar!/some/pkg/foo/baz.html</samp>
     * <li><samp>modules/docs/foo.jar!/some/pkg/foo/JavaHelpSearch/TMAP</samp> (etc.)
     * <li><samp>modules/docs/locale/foo_brand.jar!/some/pkg/foo/foo_brand.html</samp>
     * <li><samp>modules/docs/locale/foo_brand.jar!/some/pkg/foo/baz_brand.html</samp>
     * <li><samp>modules/docs/locale/foo_brand.jar!/some/pkg/foo/JavaHelpSearch/TMAP_brand</samp> (etc.)
     * </ul>
     * where the files in <samp>modules/docs/foo.jar!/some/pkg/foo/JavaHelpSearch/</samp>
     * were generated by a regular invocation of this task and the files in
     * <samp>modules/docs/locale/foo_brand.jar!/some/pkg/foo/JavaHelpSearch/</samp>
     * were generated by the variant above. Then a help set reference using a URL such as
     * <samp>nbdocs:/some/pkg/foo/helpset.xml</samp> will, when running with branding
     * <samp>brand</samp>, not only display the expected variants of <samp>foo.html</samp>
     * and <samp>baz.html</samp>, but be able to search for strings specifically in them
     * (including correct offsets).
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=31044">Issue #31044</a>
     */
    public void addBrandedFileSet(BrandedFileSet s) {
        brandings.add(s);
    }

    /** @deprecated Use {@link #createClasspath} instead. */
    @Deprecated
    public void setJhall(File f) {
        log("The 'jhall' attribute to <jhindexer> is deprecated. Use a nested <classpath> instead.", Project.MSG_WARN);
        createClasspath().setLocation(f);
    }

    /**
     *
     */
    public void setClassPath(Path cp) {
        classpath = cp;
    }

    public @Override void execute() throws BuildException {
        if (classpath == null) {
            throw new BuildException ("Must specify the classpath attribute to find jhall.jar or jsearch.jar");
        }
        if (db == null) {
            throw new BuildException ("Must specify the db attribute");
        }
        if (basedir == null) {
            throw new BuildException ("Must specify the basedir attribute");
        }
        FileScanner scanner = getDirectoryScanner (basedir);
        scanner.scan ();
        String[] files = scanner.getIncludedFiles ();
        // First, an up-to-date check. ;-)
        if (basedir.exists () && db.exists ()) {
            long lastModified = Long.MIN_VALUE;
            // First scan output dir for any files.
            FileScanner output = new DirectoryScanner ();
            output.setBasedir (db);
            output.scan ();
            String[] outfiles = output.getIncludedFiles ();
            if (outfiles.length > 0) {
                for (int i = 0; i < outfiles.length; i++) {
                    long mod = new File (db, outfiles[i]).lastModified ();
                    if (mod > lastModified) {
                        lastModified = mod;
                    }
                }
                // Now check to see if any source files are newer.
                boolean ok = true;
                for (int i = 0; i < files.length; i++) {
                    long mod = new File (basedir, files[i]).lastModified ();
                    if (mod > lastModified) {
                        ok = false;
                        break;
                    }
                }
                if (!brandings.isEmpty()) {
                    // Check these too!
                    for(FileSet fs: brandings) {
                        FileScanner scanner2 = fs.getDirectoryScanner(getProject());
                        String[] files2 = scanner2.getIncludedFiles();
                        for (int i = 0; i < files2.length; i++) {
                            long mod = new File (basedir, files2[i]).lastModified ();
                            if (mod > lastModified) {
                                ok = false;
                                break;
                            }
                        }
                    }
                }
                if (ok) {
                    // No need to rebuild.
                    return;
                }
            }
        }
        Delete delete = (Delete) getProject().createTask("delete");
        delete.setDir (db);
        delete.init ();
        delete.setLocation(getLocation());
        delete.execute ();
        Mkdir mkdir = (Mkdir) getProject().createTask("mkdir");
        mkdir.setDir (db);
        mkdir.init ();
        mkdir.setLocation(getLocation());
        mkdir.execute ();
        String maxbranding = null;
        if (!brandings.isEmpty()) {
            // Copy all files, overriding by branding, to a fresh dir somewhere.
            // Does not suffice to simply use IndexRemove to strip off the basedirs
            // of files in branded filesets, since their filenames will also include
            // the branding token, and this will mess up the search database: it needs
            // to store just the simple file name with no branding infix.
            File tmp = new File(System.getProperty("java.io.tmpdir"), "jhindexer-branding-merge");
            delete = (Delete) getProject().createTask("delete");
            delete.setDir(tmp);
            delete.init();
            delete.setLocation(getLocation());
            delete.execute();
            tmp.mkdir();
            // Start with the base files.
            Copy copy = (Copy) getProject().createTask("copy");
            copy.setTodir(tmp);
            copy.addFileset(fileset);
            copy.init();
            copy.setLocation(getLocation());
            copy.execute();
            // Now branded filesets. Must be done in order of branding, so that
            // more specific files override generic ones.
            class BrandingLengthComparator implements Comparator<BrandedFileSet> {
                public int compare(BrandedFileSet a, BrandedFileSet b) {
                    return a.branding.length() - b.branding.length();
                }
            }
            Collections.sort(brandings, new BrandingLengthComparator());
            for (BrandedFileSet s: brandings) {
                if (maxbranding != null && !s.branding.startsWith(maxbranding + "_")) {
                    throw new BuildException("Illegal branding: " + s.branding, getLocation());
                }
                maxbranding = s.branding; // only last one will be kept
                String[] suffixes = {
                    ".html",
                    ".htm",
                    ".xhtml",
                    // XXX any others? unpleasant to hardcode but this is easiest,
                    // since glob mappers do not permit *_x* -> ** syntax.
                };
                for (int i = 0; i < suffixes.length; i++) {
                    String suffix = suffixes[i];
                    copy = (Copy) getProject().createTask("copy");
                    copy.setTodir(tmp);
                    copy.setOverwrite(true);
                    copy.addFileset(s);
                    Mapper m = copy.createMapper();
                    Mapper.MapperType mt = new Mapper.MapperType();
                    mt.setValue("glob");
                    m.setType(mt);
                    m.setFrom("*_" + s.branding + suffix);
                    m.setTo("*" + suffix);
                    copy.init();
                    copy.setLocation(getLocation());
                    copy.execute();
                    if (locale != null) {
                        // Possibly have e.g. x_f4j_ja.html.
                        suffix = "_" + locale + suffix;
                        copy = (Copy) getProject().createTask("copy");
                        copy.setTodir(tmp);
                        copy.setOverwrite(true);
                        copy.addFileset(s);
                        m = copy.createMapper();
                        mt = new Mapper.MapperType();
                        mt.setValue("glob");
                        m.setType(mt);
                        m.setFrom("*_" + s.branding + suffix);
                        m.setTo("*" + suffix);
                        copy.init();
                        copy.setLocation(getLocation());
                        copy.execute();
                    }
                }
            }
            // Now replace basedir & files with this temp dir.
            basedir = tmp;
            FileSet tmpf = new FileSet();
            tmpf.setProject(getProject());
            tmpf.setDir(tmp);
            files = tmpf.getDirectoryScanner(getProject()).getIncludedFiles();
        }
        log ("Running JavaHelp search database indexer...");
        try {
            File config = File.createTempFile ("jhindexer-config", ".txt");
            try {
                try (OutputStream os = new FileOutputStream (config)) {
                    PrintWriter pw = new PrintWriter (os);
                    pw.println ("IndexRemove " + basedir + File.separator);
                    String message = "Files to be indexed:";
                    for (int i = 0; i < files.length; i++) {
                        // [PENDING] JavaHelp docs say to use / as file sep for File directives;
                        // so what should the complete path be? Someone should test this on Windoze...
                        String path = basedir + File.separator + files[i];
                        pw.println ("File " + path);
                        message += "\n\t" + path;
                    }
                    log (message, Project.MSG_VERBOSE);
                    pw.flush ();
                }
                AntClassLoader loader = new AntClassLoader(getProject(), classpath);
                loader.addLoaderPackageRoot("com.sun.java.help.search");
                try {
                    Class<?> clazz = loader.loadClass("com.sun.java.help.search.Indexer");
                    Method main = clazz.getMethod("main", String[].class);
                    List<String> args = Arrays.asList(
                        "-c", config.getAbsolutePath(),
                        "-db", db.getAbsolutePath()
                    );
                    if (locale != null) {
                        args = new ArrayList<>(args); // #35244
                        args.add("-locale");
                        args.add(locale);
                    }
                    main.invoke(null, new Object[] {args.toArray(new String[args.size()])});
                } catch (InvocationTargetException ite) {
                    throw new BuildException(ite.getTargetException(), getLocation());
                } catch (Exception e) { // ClassNotFoundException, NoSuchMethodException, ...
                    throw new BuildException(e, getLocation());
                }
            } finally {
                config.delete ();
            }
        } catch (IOException ioe) {
            throw new BuildException("Could not make temporary config file", ioe, getLocation());
        }
        if (maxbranding != null) {
            // Now rename search DB files to include branding suffix.
            // Note that DOCS.TAB -> DOCS_brand.TAB to work with nbdocs: protocol.
            String[] dbfiles = db.list();
            for (int i = 0; i < dbfiles.length; i++) {
                String basename, ext;
                int idx = dbfiles[i].lastIndexOf('.');
                if (idx != -1) {
                    basename = dbfiles[i].substring(0, idx);
                    ext = dbfiles[i].substring(idx);
                } else {
                    basename = dbfiles[i];
                    ext = "";
                }
                File old = new File(db, dbfiles[i]);
                File nue = new File(db, basename + "_" + maxbranding + ext);
                log("Moving " + old + " to " + nue, Project.MSG_VERBOSE);
                old.renameTo(nue);
            }
        }
    }

}
