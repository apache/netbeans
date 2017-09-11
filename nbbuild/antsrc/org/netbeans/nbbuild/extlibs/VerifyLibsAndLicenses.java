/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.nbbuild.extlibs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.netbeans.nbbuild.JUnitReportWriter;

/**
 * Task to check that external libraries have legitimate licenses, etc.
 */
public class VerifyLibsAndLicenses extends Task {

    private File nball;
    public void setNball(File nball) {
        this.nball = nball;
    }

    private File reportFile;
    /** JUnit-format XML result file to generate, rather than halting the build. */
    public void setReport(File report) {
        this.reportFile = report;
    }

    private Map<String,String> pseudoTests;
    private Set<String> modules;

    public @Override void execute() throws BuildException {
        try { // XXX workaround for http://issues.apache.org/bugzilla/show_bug.cgi?id=43398
        pseudoTests = new LinkedHashMap<String,String>();
        modules = new TreeSet<String>();
        for (String cluster : getProject().getProperty("nb.clusters.list").split("[, ]+")) {
            modules.addAll(Arrays.asList(getProject().getProperty(cluster).split("[, ]+")));
        }
        try {
            testNoStrayThirdPartyBinaries();
            testLicenseFilesAreProperlyFormattedPhysically();
            testLicenses();
            testBinaryUniqueness();
        } catch (IOException x) {
            throw new BuildException(x, getLocation());
        }
        JUnitReportWriter.writeReport(this, null, reportFile, pseudoTests);
        } catch (NullPointerException x) {x.printStackTrace(); throw x;}
    }

    private void testBinaryUniqueness() throws IOException {
        List<String> ignoredPatterns = loadPatterns("ignored-overlaps");
        StringBuffer msg = new StringBuffer();
        Map<Long,String> binaries = new HashMap<Long,String>();
        for (String module : modules) {
            File d = new File(new File(nball, module), "external");
            Set<String> hgFiles = findHgControlledFiles(d);
            for (String n : hgFiles) {
                if (!n.endsWith(".jar") && !n.endsWith(".zip")) {
                    continue;
                }
                File f = new File(d, n);
                String path = module + "/external/" + n;
                InputStream is = new FileInputStream(f);
                try {
                    byte[] buf = new byte[4096];
                    int read;
                    CRC32 crc = new CRC32();
                    while ((read = is.read(buf)) != -1) {
                        crc.update(buf, 0, read);
                    }
                    maybeAppendDuplicateMessage(msg, binaries.put(crc.getValue(), path), path, ignoredPatterns);
                } finally {
                    is.close();
                }
                ZipFile zf = new ZipFile(f);
                try {
                    Enumeration<? extends ZipEntry> entries = zf.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        String innerName = entry.getName();
                        if (!innerName.endsWith(".jar") && !innerName.endsWith(".zip")) {
                            continue;
                        }
                        String innerPath = innerName + " in " + path;
                        is = zf.getInputStream(entry);
                        try {
                            byte[] buf = new byte[4096];
                            int read;
                            CRC32 crc = new CRC32();
                            while ((read = is.read(buf)) != -1) {
                                crc.update(buf, 0, read);
                            }
                            maybeAppendDuplicateMessage(msg, binaries.put(crc.getValue(), innerPath), innerPath, ignoredPatterns);
                        } finally {
                            is.close();
                        }
                    }
                } finally {
                    zf.close();
                }
            }
        }
        //System.err.println("binaries: " + new TreeSet<String>(binaries.values()));
        pseudoTests.put("testBinaryUniqueness", msg.length() > 0 ? "Some binaries are duplicated (edit nbbuild/antsrc/org/netbeans/nbbuild/extlibs/ignored-overlaps as needed)" + msg : null);
    }
    private static void maybeAppendDuplicateMessage(StringBuffer msg, String path1, String path2, List<String> ignoredPatterns) {
        if (path1 == null || path2 == null) {
            return;
        }
        for (String pattern : ignoredPatterns) {
            String[] parts = pattern.split(" ");
            assert parts.length == 2 : pattern;
            if (SelectorUtils.matchPath(parts[0], path1.replaceFirst("^.+ in ", "")) &&
                    SelectorUtils.matchPath(parts[1], path2.replaceFirst("^.+ in ", ""))) {
                return;
            }
        }
        msg.append("\n" + path1 + " and " + path2 + " are identical");
    }

    private void testLicenseFilesAreProperlyFormattedPhysically() throws IOException {
        StringBuffer msg = new StringBuffer();
        for (String module : modules) {
            File d = new File(new File(nball, module), "external");
            FILE: for (String n : findHgControlledFiles(d)) {
                String path = module + "/external/" + n;
                if (!n.endsWith("-license.txt")) {
                    continue;
                }
                File f = new File(d, n);
                InputStream is = new FileInputStream(f);
                int line = 1;
                try {
                    CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder().
                            onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
                    Reader r = new InputStreamReader(is, decoder);
                    int column = 0;
                    boolean pastHeader = false;
                    boolean trailingSpace = false;
                    int c;
                    while ((c = r.read()) != -1) {
                        if (trailingSpace && (c == '\r' || c == '\n')) {
                            msg.append("\n" + path + " has a trailing space on line #" + line);
                            continue FILE;
                        }
                        if (c == '\r') {
                            column = 0;
                        } else if (c == '\n') {
                            if (column == 0 && line > 1 && !pastHeader) {
                                pastHeader = true;
                                //System.err.println("encountered header end in " + path + " at line " + line);
                            }
                            column = 0;
                            line++;
                        } else if (c == '\f') {
                            msg.append("\n" + path + " uses a form feed (^L) on line #" + line);
                            continue FILE;
                        } else {
                            trailingSpace = c == ' ';
                            column++;
                            if (pastHeader && column > 80) {
                                msg.append("\n" + path + " has line #" + line + " longer than 80 characters");
                                continue FILE;
                            }
                        }
                    }
                    if (column > 0) {
                        msg.append("\n" + path + " must end in a newline");
                    }
                } catch (IOException x) {
                    msg.append("\n" + path + " at line #" + line + ": " + x);
                } finally {
                    is.close();
                }
            }
        }
        pseudoTests.put("testLicenseFilesAreProperlyFormattedPhysically", msg.length() > 0 ? "Some license files were badly formatted" + msg : null);
    }

    private void testLicenses() throws IOException {
        File licenses = new File(new File(nball, "nbbuild"), "licenses");
        Set<String> requiredHeaders = new TreeSet<String>(Arrays.asList("Name", "Version", "Description", "License", "OSR", "Origin"));
        Set<String> optionalHeaders = new HashSet<String>(Arrays.asList("Files", "Source", "Comment"));
        StringBuffer msg = new StringBuffer();
        for (String module : modules) {
            File d = new File(new File(nball, module), "external");
            Set<String> hgFiles = findHgControlledFiles(d);
            Set<String> referencedBinaries = new HashSet<String>();
            for (String n : hgFiles) {
                if (!n.endsWith("-license.txt")) {
                    continue;
                }
                File f = new File(d, n);
                String path = module + "/external/" + n;
                Map<String,String> headers = new HashMap<String,String>();
                InputStream is = new FileInputStream(f);
                StringBuffer body = new StringBuffer();
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String line;
                    while ((line = r.readLine()) != null && line.length() > 0) {
                        Matcher m = Pattern.compile("([a-zA-Z]+): (.+)").matcher(line);
                        if (!m.matches()) {
                            msg.append("\n" + path + " has a non-header line in the header block: \"" + line + "\"");
                            headers = null;
                            break;
                        }
                        headers.put(m.group(1), m.group(2));
                    }
                    while ((line = r.readLine()) != null) {
                        body.append(line).append('\n');
                    }
                } finally {
                    is.close();
                }
                if (headers == null) {
                    headers = Collections.emptyMap();
                } else if (headers.isEmpty()) {
                    msg.append("\n" + path + " has no headers");
                } else {
                    for (String header : requiredHeaders) {
                        if (!headers.containsKey(header)) {
                            if (header.equals("OSR") && (headers.get("License") != null)) {
                                if (headers.get("License").startsWith("CDDL")) { // CDDL does not require OSR
                                    continue;
                                }
                                if (headers.get("License").startsWith("SLA")) { // SLA does not require OSR
                                    continue;
                                }
                            }
                            msg.append("\n" + path + " is missing a required header: " + header);
                        }
                    }
                }
                for (String header : headers.keySet()) {
                    if (!requiredHeaders.contains(header) && !optionalHeaders.contains(header)) {
                        msg.append("\n" + path + " has an unrecognized header: " + header);
                        continue;
                    }
                }
                String version = headers.get("Version");
                if (version != null && !n.contains(version)) {
                    msg.append("\n" + path + " does not contain the version " + version + " in its name");
                }
                String license = headers.get("License");
                if (license != null) {
                    if (license.contains("GPL") && !headers.containsKey("Source")) {
                        msg.append("\n" + path + " has a GPL-family license but is missing the Source header");
                    }
                    File licenseFile = new File(licenses, license);
                    if (licenseFile.isFile()) {
                        StringBuffer masterBody = new StringBuffer();
                        is = new FileInputStream(licenseFile);
                        try {
                            BufferedReader r = new BufferedReader(new InputStreamReader(is));
                            int c;
                            while ((c = r.read()) != -1) {
                                masterBody.append((char) c);
                            }
                        } finally {
                            is.close();
                        }
                        String master = masterBody.toString().replaceAll("[ \n\t]+", " ").trim();
                        String actual = body.toString().replaceAll("[ \n\t]+", " ").trim();
                        String problem = templateMatch(actual, master, false);
                        if (problem != null) {
                            msg.append("\n" + path + " contains a license body which does not match that in nbbuild/licenses/" + license + ": " + problem);
                        }
                    } else {
                        msg.append("\n" + path + " refers to nonexistent nbbuild/licenses/" + license);
                    }
                }
                for (String urlHeader : new String[] {"Source", "Origin"}) {
                    String value = headers.get("Source");
                    if (value != null) {
                        try {
                            new URL(value);
                        } catch (MalformedURLException x) {
                            msg.append("\n" + path + " has malformed " + urlHeader + " value: " + value);
                        }
                    }
                }
                String files = headers.get("Files");
                if (files != null) {
                    for (String file : files.split("[, ]+")) {
                        referencedBinaries.add(file);
                        if (!hgFiles.contains(file)) {
                            msg.append("\n" + path + " mentions a nonexistent binary in Files: " + file);
                        }
                    }
                } else {
                    String matchingJar = n.replaceFirst("-license\\.txt$", ".jar");
                    String matchingZip = n.replaceFirst("-license\\.txt$", ".zip");
                    referencedBinaries.add(matchingJar);
                    referencedBinaries.add(matchingZip);
                    if (!hgFiles.contains(matchingJar) && !hgFiles.contains(matchingZip)) {
                        msg.append("\n" + path + " has no Files header and no corresponding " + matchingJar + " or " + matchingZip + " could be found");
                    }
                }
            }
            for (String n : hgFiles) {
                if (!n.endsWith(".jar") && !n.endsWith(".zip")) {
                    continue;
                }
                String path = module + "/external/" + n;
                if (!n.matches(".+[0-9].+")) {
                    msg.append("\n" + path + " does not appear to include a version number");
                }
                if (!referencedBinaries.contains(n)) {
                    msg.append("\n" + path + " is not associated with any license file");
                }
            }
        }
        pseudoTests.put("testLicenses", msg.length() > 0 ? "Some license files have incorrect headers" + msg : null);
    }
    private static String templateMatch(String actual, String expected, boolean left) {
        String reason = null;
        boolean expectReason = false;
        String mismatch = null;
        while (true) {
            if (actual.matches(expected.replaceAll("([\\\\\\[\\].^$?*+{}()|])", "\\\\$1").replaceAll(" *__[A-Z_]+__ *", ".*"))) {
                reason = null;
                break;
            } else if (expected.length() == 0) {
                reason = "unexpected extra content";
                break;
            } else if (actual.length() == 0) {
                reason = "missing content";
                break;
            } else if (!expected.startsWith("__")) {
                if (expected.charAt(0) != actual.charAt(0)) {
                    reason = mismatch(actual, expected, true);
                    break;
                } else {
                    expectReason = true;
                    mismatch = mismatch(actual, expected, true);
                    actual = actual.substring(1);
                    expected = expected.substring(1);
                    continue;
                }
            } else if (!expected.endsWith("__")) {
                if (expected.charAt(expected.length() - 1) != actual.charAt(actual.length() - 1)) {
                    reason = mismatch(actual, expected, false);
                    break;
                } else {
                    expectReason = true;
                    mismatch = mismatch(actual, expected, false);
                    actual = actual.substring(0, actual.length() - 1);
                    expected = expected.substring(0, expected.length() - 1);
                    continue;
                }
            } else {
                String absorbed = expected.replaceFirst(left ? "^(__[A-Z_]+__)." : ".(__[A-Z_]+__)$", "$1");
                assert !expected.equals(absorbed) : expected;
                mismatch = mismatch(actual, expected, left);
                expected = absorbed;
                left = !left;
                continue;
            }
        }
        if (reason == null) {
            assert !expectReason : mismatch;
            return mismatch;
        } else {
            return reason;
        }
    }
    private static String mismatch(String actual, String expected, boolean useHead) {
        return "mismatch around: '" + headOrTail(actual, useHead) + "' vs. '" + headOrTail(expected, useHead) + "'";
    }
    private static String headOrTail(String text, boolean useHead) {
        int context = 20;
        return text.length() > context ? (useHead ? text.substring(0, context) : text.substring(text.length() - context, text.length())) : text;
    }

    static List<String> loadPatterns(String resource) throws IOException {
        List<String> patterns = new ArrayList<String>();
        InputStream is = VerifyLibsAndLicenses.class.getResourceAsStream(resource);
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#") && line.length() > 0) {
                    patterns.add(line.replaceAll("/(?=( |$))", "/**"));
                }
            }
        } finally {
            is.close();
        }
        return patterns;
    }

    private void testNoStrayThirdPartyBinaries() throws IOException {
        List<String> ignoredPatterns = loadPatterns("ignored-binaries");
        Set<String> violations = new TreeSet<String>();
        findStrayThirdPartyBinaries(nball, "", violations, ignoredPatterns);
        if (violations.isEmpty()) {
            pseudoTests.put("testNoStrayThirdPartyBinaries", null);
        } else {
            StringBuffer msg = new StringBuffer("Some binaries were found outside of <module>/external/ directories (edit nbbuild/antsrc/org/netbeans/nbbuild/extlibs/ignored-binaries as needed)");
            for (String v : violations) {
                msg.append("\n" + v);
            }
            pseudoTests.put("testNoStrayThirdPartyBinaries", msg.toString());
        }
    }
    private void findStrayThirdPartyBinaries(File dir, String prefix, Set<String> violations, List<String> ignoredPatterns) throws IOException {
        for (String n : findHgControlledFiles(dir)) {
            File f = new File(dir, n);
            if (f.isDirectory()) {
                findStrayThirdPartyBinaries(f, prefix + n + "/", violations, ignoredPatterns);
            } else if (n.matches(".*\\.(jar|zip)")) {
                String path = prefix + n;
                boolean ignored = false;
                for (String pattern : ignoredPatterns) {
                    if (SelectorUtils.matchPath(pattern, path)) {
                        ignored = true;
                        break;
                    }
                }
                if (!ignored && dir.getName().equals("external") &&
                        new File(new File(dir.getParentFile(), "nbproject"), "project.xml").isFile() &&
                        new File(dir.getParentFile(), "src").isDirectory()) {
                    ignored = true;
                }
                if (!ignored) {
                    violations.add(path);
                } else {
                    //System.err.println("accepted: " + path);
                }
            }
        }
    }

    private static final Map<File,List<Pattern>> hgignores = new HashMap<File,List<Pattern>>();
    /**
     * Find files tracked by Mercurial.
     * Rather than actually running 'hg locate',
     * which might be too slow and also requires hg to be in the path,
     * try to just look for files not ignored by Mercurial.
     * Not as precise:
     * 1. Might be some '?' status files (though these should be fixed by someone).
     * 2. Some tracked files/dirs might for some reason be listed as ignored.
     * Also adds in external binaries listed in a binaries-list file, if present.
     */
    static Set<String> findHgControlledFiles(File dir) throws IOException {
        File[] kids = dir.listFiles();
        if (kids == null) {
            return Collections.emptySet();
        }
        File root = dir;
        String path = "";
        File hgignore = null;
        while (root != null && !(hgignore = new File(root, ".hgignore")).isFile()) {
            path = root.getName() + "/" + path;
            root = root.getParentFile();
        }
        List<Pattern> ignoredPatterns;
        synchronized (hgignores) {
            if (root == null) {
                ignoredPatterns = Collections.emptyList();
            } else if (hgignores.containsKey(root)) {
                ignoredPatterns = hgignores.get(root);
            } else {
                ignoredPatterns = new ArrayList<Pattern>();
                Reader r = new FileReader(hgignore);
                try {
                    BufferedReader br = new BufferedReader(r);
                    String line;
                    while ((line = br.readLine()) != null) {
                        ignoredPatterns.add(Pattern.compile(line));
                    }
                } finally {
                    r.close();
                }
                hgignores.put(root, ignoredPatterns);
            }
        }
        Set<String> files = new TreeSet<String>();
        FILES: for (File f : kids) {
            String n = f.getName();
            if (n.equals(".hg")) {
                continue;
            }
            String fullname = path + n;
            boolean isDir = f.isDirectory();
            if (isDir && new File(f, ".hg").isDirectory()) {
                continue; // skip contrib, misc repos if present
            }
            for (Pattern p : ignoredPatterns) {
                if (p.matcher(fullname).find() || (isDir && p.matcher(fullname + "/").find())) {
                    continue FILES;
                }
            }
            files.add(n);
        }
        File list = new File(dir, "binaries-list");
        if (list.isFile()) {
            Reader r = new FileReader(list);
            try {
                BufferedReader br = new BufferedReader(r);
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("#")) {
                        continue;
                    }
                    if (line.trim().length() == 0) {
                        continue;
                    }
                    String[] hashAndFile = line.split(" ", 2);
                    if (hashAndFile.length < 2) {
                        throw new BuildException("Bad line '" + line + "' in " + list);
                    }
                    files.add(hashAndFile[1]);
                }
            } finally {
                r.close();
            }
        }
        return files;
    }

}
