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

package org.netbeans.nbbuild;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.MatchingTask;

import org.apache.tools.ant.types.Mapper;

// XXX in Ant 1.6, permit <xmlcatalog> entries to make checking of "external" links
// work better in the case of cross-links between APIs

/** Task to check for broken links in HTML.
 * Note that this is a matching task and you must give it a list of things to match.
 * The Java VM's configured HTTP proxy will be used (${http.proxyHost} and ${http.proxyPort}).
 * @author Jesse Glick
 */
public class CheckLinks extends MatchingTask {

    private File basedir;
    private boolean checkexternal = true;
    private boolean checkspaces = true;
    private boolean checkforbidden = true;
    private boolean failbroken = false;
    private List<Mapper> mappers = new LinkedList<>();
    private List<Filter> filters = new ArrayList<>();
    private File report;
    private File externallinksdump;

    /** Set whether to check external links (absolute URLs).
     * Local relative links are always checked.
     * By default, external links are checked.
     */
    public void setCheckexternal (boolean ce) {
        checkexternal = ce;
    }
    
    /** False if spaces in URLs shall not be reported. Default to true.
     */
    public void setCheckspaces (boolean s) {
        checkspaces = s;
    }

    /** Allows to disable check for forbidden links.
     */
    public void setCheckforbidden(boolean s) {
        checkforbidden = s;
    }

    /**
     * Allows to fail build on broken links. Default to false.
     *
     * @param fail fail build on broken links
     */
    public void setFailbroken(boolean fail) {
        failbroken = fail;
    }

    /** Set the base directory from which to scan files.
     */
    public void setBasedir (File basedir) {
        this.basedir = basedir;
    }
    
    public Filter createFilter () {
        Filter f = new Filter ();
        filters.add (f);
        return f;
    }

    /**
     * If set, create a JUnit-style report on failure, rather than halting the build.
     */
    public void setReport(File report) {
        this.report = report;
    }

    /**
     * Folder where we collect all external links for further inspection
     */
    public void setExternallinkslist(File externallinksdump) {
        this.externallinksdump = externallinksdump;
    }
    /**
     * Add a mapper to translate file names to the "originals".
     */
    public Mapper createMapper() {
        Mapper m = new Mapper(getProject());
        mappers.add(m);
        return m;
    }

    public void execute () throws BuildException {
        if (basedir == null) throw new BuildException ("Must specify the basedir attribute");
        FileScanner scanner = getDirectoryScanner (basedir);
        scanner.scan ();
        String message = "Scanning for broken links in " + basedir + " ...";
        if (! checkexternal) message += " (external URLs will be skipped)";
        log (message);
        String[] files = scanner.getIncludedFiles ();
        Set<URI> okurls = new HashSet<>(1000);
        Set<URI> badurls = new HashSet<>(100);
        Set<URI> cleanurls = new HashSet<>(100);
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            File file = new File (basedir, files[i]);
            URI fileurl = file.toURI();
            log ("Scanning " + file, Project.MSG_VERBOSE);
            try {
                scan(this, null, null, getLocation().toString(), "", fileurl, okurls, badurls, cleanurls, checkexternal, checkspaces, checkforbidden, 1, mappers, filters, errors);
            } catch (IOException ioe) {
                throw new BuildException("Could not scan " + file + ": " + ioe, ioe, getLocation());
            }
        }
        String testMessage = null;
        if (!errors.isEmpty()) {
            StringBuilder b = new StringBuilder("There were broken links");
            for (String error : errors) {
                b.append("\n" + error);
            }
            testMessage = b.toString();
        }
        JUnitReportWriter.writeReport(this, null, report, Collections.singletonMap("testBrokenLinks", testMessage));
        if (!errors.isEmpty() && failbroken) {
            throw new BuildException("Broken links found in Javadoc");
        }
    }
    
    private static Pattern hrefOrAnchor = Pattern.compile("<(a|code|div|img|link|h1|h2|h3|h4|h5|li|section|span)(\\s+class=\"[\\w\\-]*\")?(\\s+shape=\"rect\")?(?:\\s+rel=\"stylesheet\")?\\s+(href|name|id|src)=\"([^\"#]*)(#[^\"$]+)?\"(\\s+shape=\"rect\")?(?:\\s+type=\"text/css\")?(\\s+class=\"[\\w\\-]*\")?\\s*/?>", Pattern.CASE_INSENSITIVE);
    private static Pattern lineBreak = Pattern.compile("^", Pattern.MULTILINE);
    
    /**
     * Scan for broken links.
     * @param task an Ant task to associate with this
     * @param referrer the referrer file path (or full URL if not file:)
     * @param referrerLocation the location in the referrer, e.g. ":38:12", or "" if unavailable
     * @param u the URI to check
     * @param okurls a set of URIs known to be fully checked (including all anchored variants etc.)
     * @param badurls a set of URIs known to be bogus
     * @param cleanurls a set of (base) URIs known to have had their contents checked
     * @param checkexternal if true, check external links (all protocols besides file:)
     * @param recurse one of:
     *                0 - just check that it can be opened;
     *                1 - check also that any links from it can be opened;
     *                2 - recurse
     * @param mappers a list of Mappers to apply to get source files from HTML files
     */
    public static void scan
    (Task task, ClassLoader globalClassLoader, java.util.Map<String,URLClassLoader> classLoaderMap,
     String referrer, String referrerLocation, 
     URI u, Set<URI> okurls, Set<URI> badurls, Set<URI> cleanurls, 
     boolean checkexternal, boolean checkspaces, boolean checkforbidden, int recurse, 
     List<Mapper> mappers, List<String> errors) throws IOException {
        scan (task, globalClassLoader, classLoaderMap,
        referrer, referrerLocation, u, okurls, badurls, cleanurls, checkexternal, checkspaces, checkforbidden, recurse, mappers, Collections.<Filter>emptyList(), errors);
    }
    
    private static void scan
    (Task task, ClassLoader globalClassLoader, java.util.Map<String,URLClassLoader> classLoaderMap,
     String referrer, String referrerLocation, 
     URI u, Set<URI> okurls, Set<URI> badurls, Set<URI> cleanurls,
     boolean checkexternal, boolean checkspaces, boolean checkforbidden, int recurse,
     List<Mapper> mappers, List<Filter> filters, List<String> errors) throws IOException {
        //task.log("scan: u=" + u + " referrer=" + referrer + " okurls=" + okurls + " badurls=" + badurls + " cleanurls=" + cleanurls + " recurse=" + recurse, Project.MSG_DEBUG);
        //System.out.println("");
        //System.out.println("CheckLinks.scan ref: " + referrer);
        //System.out.println("CheckLinks.scan   u: " + u);
        if (okurls.contains(u) && recurse == 0) {
            // Yes it is OK.
            return;
        }
        //Check if referrer is jar file and if u is relative if yes make path absolute
        if (referrer.startsWith("jar:file:") && (u.getScheme() == null) && !u.toString().startsWith("#")) {
            if (u.toString().length() == 0) {
                System.out.println("Invalid URL: Empty URL referred from: " + referrer);
                return;
            }
            if (!u.isAbsolute()) {
                //This is to make inner jar path after ! absolute.
                //It uses java.io.File to remove ../ sequences but as file path
                //on Windows is different from inner jar path it requires some
                //'fix' on Windows.
                int pos = referrer.indexOf("!");
                if (pos != -1) {
                    String base = referrer.substring(0,pos+1);
                    String path1 = referrer.substring(pos+1);
                    //System.out.println("base:" + base);
                    //System.out.println("path1:" + path1);
                    File f1 = new File(path1);
                    File p = f1.getParentFile();
                    File f2 = new File(p,u.getPath());
                    //System.out.println("f1:" + f1);
                    //System.out.println("f2:" + f2);
                    String path2 = null;
                    try {
                        path2 = f2.getCanonicalPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Ugly hack to get jar inner path from Win FS path
                    //System.out.println("path2:" + path2);
                    if (System.getProperty("os.name").startsWith("Windows")) {
                        path2 = path2.substring(2).replace('\\','/');
                    }
                    try {
                        u = new URI(base+path2);
                    } catch (URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                    //System.out.println("u:" + u);
                }
            }
        }
        URI base;
        if (u.toString().startsWith("#")) {
            try {
                u = new URI(referrer + u.toString());
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
        }
        String b = u.toString().replaceFirst("[#?].*$", "");
        try {
            base = new URI(b);
            //base = new URI(u.getScheme(), u.getUserInfo(), u.getHost(), u.getPort(), u.toURL().getPath(), u.getQuery(), /*fragment*/null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new Error(e);
        }
        String frag = u.getFragment();
        String basepath = base.toString();
        if ("file".equals(base.getScheme())) {
            try {
                basepath = new File(base).getAbsolutePath();
            } catch (IllegalArgumentException e) {
                errors.add(normalize(referrer, mappers) + referrerLocation + ": malformed URL: " + base + " (" + e.getLocalizedMessage() + ")");
            }
        }
        //task.log("scan: base=" + base + " frag=" + frag, Project.MSG_DEBUG);
        if (badurls.contains(u) || badurls.contains(base)) {
            errors.add(normalize(referrer, mappers) + referrerLocation + ": broken link (already reported): " + u);
            return;
        }

        if (checkforbidden) {
            for (Filter f : filters) {
                Boolean decision = f.isOk (u);
                if (Boolean.TRUE.equals (decision)) {
                    break;
                }
                if (Boolean.FALSE.equals (decision)) {
                    errors.add(normalize(referrer, mappers) + referrerLocation + ": forbidden link: " + base);
                    //System.out.println("badurls ADD1 base:" + base);
                    badurls.add(base);
                    //System.out.println("badurls ADD1    u:" + u);
                    badurls.add(u);
                    return;
                }
            }
        }
        
        if (!checkexternal && !"file".equals(u.getScheme()) && !"jar".equals(u.getScheme()) && !"nbdocs".equals(u.getScheme())) {
            task.log("Skipping external link: " + base, Project.MSG_VERBOSE);
            cleanurls.add(base);
            okurls.add(base);
            okurls.add(u);
            return;
        }
        
         //Translate nbdocs protocol to jar protocol
        if ("nbdocs".equals(u.getScheme())) {
            //If called from CheckHelpSets following params are not set =>
            //we cannot check nbdocs URLs.
            if ((classLoaderMap == null) || (globalClassLoader == null)) {
                return;
            }
            //System.out.println("");
            //System.out.println("r:" + referrer);
            //System.out.println("u:" + u);
            //System.out.println("u.getScheme:" + u.getScheme());
            //System.out.println("u.getHost:" + u.getHost());
            //System.out.println("u.toURL.getHost:" + u.toURL().getHost());
            //System.out.println("u.getPath:" + u.getPath());
            //If no module base name is specified as host name check if given
            //resource is available in current module or globally.
            if (toURL(u).getHost().isEmpty()) {
                errors.add("Missing host in nbdocs protocol URL. URI: " + u);
                errors.add("Referrer: " + referrer);
                String name = u.getPath();
                //Strip leading "/" as findResource does not work when leading slash is present
                if (name.startsWith("/")) {
                    name = name.substring(1);
                    //System.out.println("name:" + name);
                }
                URL res;
                res = globalClassLoader.getResource(name);
                //System.out.println("res:" + res);
                if (res != null) {
                    try {
                        base = res.toURI();
                        u = base;
                        basepath = base.toString();
                        //System.out.println("base:" + base);
                    } catch (URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                    //Try to find out module for link
                    for (Entry<String,URLClassLoader> e: classLoaderMap.entrySet()) {
                        URLClassLoader cl = e.getValue();
                        if (cl != null) {
                            URL moduleRes = cl.findResource(name);
                            if (moduleRes != null) {
                                task.log("INFO: Link found in module:" + e.getKey() + ". URI: " + u, Project.MSG_INFO);
                                task.log("INFO: Referrer: " + referrer, Project.MSG_INFO);
                                break;
                            }
                        }
                    }
                } else {
                    errors.add("Link not found globally. URI: " + u);
                    errors.add("Referrer: " + referrer);
                    return;
                } 
                //System.out.println("res:" + res);
            } else {
                String name = u.getPath();
                //Strip leading "/" as findResource does not work when leading slash is present
                if (name.startsWith("/")) {
                    name = name.substring(1);
                    //System.out.println("name:" + name);
                }
                URL res = null;
                URLClassLoader moduleClassLoader = classLoaderMap.get(toURL(u).getHost());
                //Log warning
                if (moduleClassLoader == null) {
                    errors.add("Module " + toURL(u).getHost() + " not found among modules containing helpsets. URI: " + u);
                    errors.add("Referrer: " + referrer);
                }
                if (moduleClassLoader != null) {
                    res = moduleClassLoader.findResource(name);
                    //System.out.println("res1:" + res);
                    if (res != null) {
                        try {
                            base = res.toURI();
                            u = base;
                            basepath = base.toString();
                            //System.out.println("base:" + base);
                        } catch (URISyntaxException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                if (res == null) {
                    if (moduleClassLoader != null) {
                        errors.add("Link not found in module " + toURL(u).getHost() + " URI: " + u);
                        errors.add("Referrer: " + referrer);
                    }
                    res = globalClassLoader.getResource(name);
                    //System.out.println("res2:" + res);
                    if (res != null) {
                        try {
                            base = res.toURI();
                            u = base;
                            basepath = base.toString();
                            //System.out.println("base:" + base);
                        } catch (URISyntaxException ex) {
                            ex.printStackTrace();
                        }
                        //Try to find out module for link
                        for (Entry<String,URLClassLoader> e: classLoaderMap.entrySet()) {
                            URLClassLoader cl = e.getValue();
                            if (cl != null) {
                                URL moduleRes = cl.findResource(name);
                                if (moduleRes != null) {
                                    task.log("INFO: Link found in module:" + e.getKey() + ". URI: " + u, Project.MSG_INFO);
                                    task.log("INFO: Referrer: " + referrer, Project.MSG_INFO);
                                    break;
                                }
                            }
                        }
                    } else {
                        errors.add("Link not found globally. URI: " + u);
                        errors.add("Referrer: " + referrer);
                        return;
                    } 
                }
            }
        }
        task.log("Checking " + u + " (recursion level " + recurse + ")", Project.MSG_VERBOSE);
        String content;
        String mimeType;
        try {
            // XXX for protocol 'file', could more efficiently use a memmapped char buffer
            URLConnection conn = toURL(base).openConnection();
            //System.out.println("CALL OF connect");
            conn.connect();
            mimeType = conn.getContentType ();
            InputStream is = conn.getInputStream ();
            String enc = conn.getContentEncoding();
            if (enc == null) {
                enc = "UTF-8";
            }
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int read;
                byte[] buf = new byte[4096];
                while ((read = is.read(buf)) != -1) {
                    baos.write(buf, 0, read);
                }
                content = baos.toString(enc);
            } finally {
                is.close();
            }
        } catch (IOException ioe) {
            errors.add(normalize(referrer, mappers) + referrerLocation + ": Broken link: " + base);
            task.log("WARNING: URI: " + u, Project.MSG_VERBOSE);
            task.log("ERROR: " + ioe, Project.MSG_VERBOSE);
            badurls.add(base);
            badurls.add(u);
            //Log exception stack trace only in verbose mode
            StringWriter sw = new StringWriter(500);
            PrintWriter pw = new PrintWriter(sw);
            ioe.printStackTrace(pw);
            task.log(sw.toString(),Project.MSG_VERBOSE);
            return;
        } catch (NullPointerException exc) {
            errors.add("NPE Link referred from: " + normalize(referrer, mappers) + referrerLocation + " Broken link: " + base);
            task.log("WARNING: URI: " + u);
            task.log("ERROR: " + exc, Project.MSG_WARN);
            badurls.add(base);
            badurls.add(u);
            //Log exception stack trace only in verbose mode
            StringWriter sw = new StringWriter(500);
            PrintWriter pw = new PrintWriter(sw);
            exc.printStackTrace(pw);
            task.log(sw.toString(),Project.MSG_WARN);
            return;
        }
        okurls.add(base);
        // map from other URIs (hrefs) to line/col info where they occur in this file (format: ":1:2")
        Map<URI,String> others = null;
        if (recurse > 0 && cleanurls.add(base)) {
            others = new HashMap<>(100);
        }
        if (recurse == 0 && frag == null) {
            // That is all we wanted to check.
            return;
        }
        if ("text/html".equals(mimeType)) {
            task.log("Parsing " + base, Project.MSG_VERBOSE);
            Matcher m = hrefOrAnchor.matcher(content);
            Set<String> names = new HashSet<>(100); // Set<String>
            while (m.find()) {
                // Get the stuff involved:
                String type = m.group(4);
                if (type.equalsIgnoreCase("name") || (type.equalsIgnoreCase("id") && !unescape(m.group(5)).startsWith("#"))) {
                    // We have an anchor, therefore refs to it are valid.
                    String name = unescape(m.group(5));
                    if (names.add(name)) {
                        try {
                            //URI does not handle jar:file: protocol
                            //okurls.add(new URI(base.getScheme(), base.getUserInfo(), base.getHost(), base.getPort(), base.getPath(), base.getQuery(), /*fragment*/name));
                            okurls.add(new URI(base + "#" + name.replace(" ", "%20").replace("<", "%3C").replace(">", "%3E").replace("[", "%5B").replace("]", "%5D")));
                        } catch (URISyntaxException e) {
                            errors.add(normalize(basepath, mappers) + findLocation(content, m.start(4)) + ": bad anchor name: " + e.getMessage());
                        }
                    } else if (recurse == 1) {
                        errors.add(normalize(basepath, mappers) + findLocation(content, m.start(4)) + ": duplicate anchor name: " + name);
                    }
                } else {
                    // A link to some other document: href=, src=.

                    // check whether this URL is not commented out
                    int previousCommentStart = content.lastIndexOf ("<!--", m.start (0));
                    int previousCommentEnd = content.lastIndexOf ("-->", m.start (0));
                    boolean commentedOut = false;
                    if (previousCommentEnd < previousCommentStart) {
                        // comment start is there and end is before it
                        commentedOut = true;
                    }

                    if (others != null && !commentedOut) {
                        String otherbase = unescape(m.group(5));
                        String otheranchor = unescape(m.group(6));
                        String uri = (otheranchor == null) ? otherbase : otherbase + otheranchor;
                        String location = findLocation(content, m.start(5));
                        String fixedUri;
                        if (uri.indexOf(' ') != -1) {
                            fixedUri = uri.replaceAll(" ", "%20");
                            if (checkspaces) {
                                errors.add(normalize(basepath, mappers) + location + ": spaces in URIs should be encoded as \"%20\": " + uri);
                            }
                        } else {
                            fixedUri = uri;
                        }
                        try {
                            URI relUri = new URI(fixedUri);
                            if (!relUri.isOpaque()) {
                                URI o = base.resolve(relUri).normalize();
                                //task.log("href: " + o);
                                if (!others.containsKey(o)) {
                                    // Only keep location info for first reference.
                                    others.put(o, location);
                                }
                            } // else mailto: or similar
                        } catch (URISyntaxException e) {
                            // Message should contain the URI.
                            errors.add(normalize(basepath, mappers) + location + ": bad relative URI: " + e.getMessage());
                        }
                    } // else we are only checking that this one has right anchors
                }
            }
        } else {
            task.log("Not checking contents of " + base, Project.MSG_VERBOSE);
        }
        if (! okurls.contains(u)) {
            errors.add(normalize(referrer, mappers) + referrerLocation + ": broken link: " + u);
            badurls.add(u); // #97784
        }
        if (others != null) {
            for(Entry<URI,String> entry: others.entrySet()) {
                URI other = entry.getKey();
                String location = entry.getValue();
                //System.out.println("CALL OF scan basepath:" + basepath + " location:" + location + " other:" + other);
                scan(task, globalClassLoader, classLoaderMap,
                basepath, location, other, okurls, badurls, cleanurls, checkexternal, checkspaces, checkforbidden, recurse == 1 ? 0 : 2, mappers, filters, errors);
            }
        }
    }
    
    private static String normalize(String path, List<Mapper> mappers) throws IOException {
        try {
            for (Mapper m : mappers) {
                String[] nue = m.getImplementation().mapFileName(path);
                if (nue != null) {
                    for (int i = 0; i < nue.length; i++) {
                        File f = new File(nue[i]);
                        if (f.isFile()) {
                            return new File(f.toURI().normalize()).getAbsolutePath();
                        }
                    }
                }
            }
            return path;
        } catch (BuildException e) {
            throw new IOException(e.toString());
        }
    }
    
    private static String unescape(String text) {
        if (text == null) {
            return null;
        }
        int pos = 0;
        int search;
        while ((search = text.indexOf('&', pos)) != -1) {
            int semi = text.indexOf(';', search + 1);
            if (semi == -1) {
                // Unterminated &... leave rest as is??
                return text;
            }
            String entity = text.substring(search + 1, semi);
            String repl;
            if (entity.equals("amp")) {
                repl = "&";
            } else if (entity.equals("quot")) {
                repl = "\"";
            } else if (entity.equals("lt")) {
                repl = "<";
            } else if (entity.equals("gt")) {
                repl = ">";
            } else if (entity.equals("apos")) {
                repl = "'";
            } else {
                // ???
                pos = semi + 1;
                continue;
            }
            text = text.substring(0, search) + repl + text.substring(semi + 1);
            pos = search + repl.length();
        }
        return text;
    }
    
    private static String findLocation(CharSequence content, int pos) {
        Matcher lbm = lineBreak.matcher(content);
        int line = 0;
        int col = 1;
        while (lbm.find()) {
            if (lbm.start() <= pos) {
                line++;
                col = pos - lbm.start() + 1;
            } else {
                break;
            }
        }
        return ":" + line + ":" + col;
    }

    static final ThreadLocal<URLStreamHandlerFactory> handlerFactory = new ThreadLocal<URLStreamHandlerFactory>();
    static URL toURL(URI uri) throws MalformedURLException {
        URLStreamHandlerFactory f = handlerFactory.get();
        URLStreamHandler h = f != null && uri.getScheme() != null ? f.createURLStreamHandler(uri.getScheme()) : null;
        return h != null ? new URL(null, uri.toString(), h) : uri.toURL();
    }

    public final class Filter extends Object {
        private Boolean accept;
        private Pattern pattern;
        
        public void setAccept (boolean a) {
            accept = Boolean.valueOf (a);
        }
        
        public void setPattern (String s) {
            pattern = Pattern.compile (s, Pattern.CASE_INSENSITIVE);
        }
        
        /** Checks whether a URI is ok. 
         * @return null if not applicable, Boolean.TRUE if the URL is accepted, Boolean.FALSE if not
         */
        final Boolean isOk (URI u) throws BuildException {
            if (accept == null) {
                throw new BuildException ("Each filter must have accept attribute");
            }
            if (pattern == null) {
                throw new BuildException ("Each filter must have pattern attribute");
            }
            
            if (pattern.matcher(u.toString()).matches()) {
                log("Matched " + u + " accepted: " + accept, org.apache.tools.ant.Project.MSG_VERBOSE);
                if (externallinksdump != null) {
                    try {
                        // triage result to file for later processing.
                        String dumpFileName = accept ? "acceptednetbeans.txt" : "rejectednetbeans.txt";

                        Path dumppath = externallinksdump.toPath().resolve(dumpFileName);
                        if (Files.notExists(dumppath)) {
                            Files.createDirectories(dumppath.getParent());
                            Files.createFile(dumppath);
                        }
                        Set<String> sortedEntries = new TreeSet<>(Files.readAllLines(externallinksdump.toPath().resolve(dumpFileName)));
                        sortedEntries.add(u.toString());
                        // ordered and unique per Set usage
                        Files.write(dumppath, sortedEntries, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                    } catch (IOException ex) {
                        Logger.getLogger(CheckLinks.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                return accept;
            }
            return null;
        }
    }
}
