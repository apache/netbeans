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

package org.netbeans.modules.java.j2seproject.copylibstask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Iterator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.Manifest.Section;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

/**
 *
 * @author Tomas Zezula
 */
public class CopyLibs extends Jar {

    private static final String LIB = "lib";    //NOI18N
    private static final String ATTR_CLASS_PATH = "Class-Path"; //NOI18N
    private static final String MANIFEST = "META-INF/MANIFEST.MF";  //NOI18N
    private static final String INDEX = "META-INF/INDEX.LIST";  //NOI18N
    private static final String UTF_8 = "UTF-8";    //NOI18N
    private static final String UTF8 = "UTF8";      //NOI18N
    private static final String URL_SEPARATOR = "/";    //NOI18N
    private static final String CP_SEPARATOR = " ";     //NOI18N

    Path runtimePath;
    Path excludeFromCopy;

    private boolean rebase;

    /** Creates a new instance of CopyLibs */
    public CopyLibs () {
        this.rebase = true;
    }

    public void setRuntimeClassPath (final Path path) {
        assert path != null;
        this.runtimePath = path;
    }

    public Path getRuntimeClassPath () {
        return this.runtimePath;
    }

    public void setExcludeFromCopy(final Path path) {
        assert path != null;
        this.excludeFromCopy = path;
    }

    public Path getExcludeFromCopy() {
        return this.excludeFromCopy;
    }

    public boolean isRebase() {
        return this.rebase;
    }

    public void setRebase(final boolean rebase) {
        this.rebase = rebase;
    }

    @Override
    public void setEncoding(String encoding) {
        if (!isUTF8(encoding)) {
            getProject().log(
            "It is not recommended to change encoding from UTF-8 as the created archive will be unreadable for Java. ", //NOI18N
            Project.MSG_WARN);
        }
        super.setEncoding(encoding);
    }

    @Override
    public void addConfiguredManifest(Manifest newManifest) throws ManifestException {
        if (newManifest != null && runtimePath != null) {
            final Manifest.Attribute cpAttr = newManifest.getMainSection().getAttribute(ATTR_CLASS_PATH);
            String value;
            if (cpAttr != null && (value = cpAttr.getValue()) != null) {
                final Set<String> folders = new HashSet<>();
                for (Iterator<Resource> it = runtimePath.iterator(); it.hasNext();) {
                    final Resource res = it.next();
                    final String simpleName = basename(res.getName(), File.separator);
                    if (res.isDirectory()) {
                        folders.add(simpleName);
                    } else {
                        //In case of conflict in jar and folder simple name the last wins.
                        folders.remove(simpleName);
                    }
                }
                final String[] parts = value.split(CP_SEPARATOR);
                boolean changed = false;
                for (int i=0; i<parts.length; i++) {
                    final String name = parts[i];
                    final String simpleName = basename(name, URL_SEPARATOR);
                    if (folders.contains(simpleName) && !name.endsWith(URL_SEPARATOR)) {
                        parts[i] = name + URL_SEPARATOR;
                        changed = true;
                    }
                }
                if (changed) {
                    value = stringJoin(CP_SEPARATOR, parts);    //Replace by String.join in JDK 8 when allowed
                    cpAttr.setValue(value);
                }
            }
        }
        super.addConfiguredManifest(newManifest);
    }


    @Override
    public void execute() throws BuildException {
        if (this.runtimePath == null) {
            throw new BuildException ("RuntimeClassPath must be set.");
        }
        final String[] pathElements = this.runtimePath.list();
        final List<File> filesToCopy = new ArrayList<>(pathElements.length);
        for (String pathElement : pathElements) {
            final File f = new File(pathElement);
            if (!f.canRead()) {
                this.log(String.format("Not copying library %s , it can't be read.", f.getAbsolutePath()), Project.MSG_WARN);
            } else if (f.isDirectory()) {
                this.log(String.format("Not copying library %s , it's a directory.", f.getAbsolutePath()), Project.MSG_WARN);
            } else {
                filesToCopy.add(f);
            }
        }
        final File destFile = this.getDestFile();
        final File destFolder = destFile.getParentFile();
        assert destFolder != null && destFolder.canWrite();
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.java.j2seproject.copylibstask.Bundle");  //NOI18N
            assert bundle != null;
            final File readme = new File (destFolder,bundle.getString("TXT_README_FILE_NAME"));
            if (!readme.exists()) {
                readme.createNewFile();
            }
            try (PrintWriter out = new PrintWriter (new FileWriter (readme))) {
                final String content = bundle.getString("TXT_README_FILE_CONTENT");
                out.println (MessageFormat.format(content,new Object[] {destFile.getName()}));
            }
        } catch (IOException ioe) {
            this.log("Cannot generate readme file.",Project.MSG_VERBOSE);
        }

        if (!filesToCopy.isEmpty()) {
            final File libFolder = new File (destFolder,LIB);
            if (!libFolder.exists()) {
                libFolder.mkdir ();
                this.log("Create lib folder " + libFolder.toString() + ".", Project.MSG_VERBOSE);
            }
            assert libFolder.canWrite();

            final Set<File> ignoreList = new HashSet<>();
            if (this.excludeFromCopy != null) {
                for (String excludeElement : this.excludeFromCopy.list()) {
                    ignoreList.add(new File (excludeElement));
                }
            }

            FileUtils utils = FileUtils.getFileUtils();
            this.log("Copy libraries to " + libFolder.toString() + ".");
            for (final File fileToCopy : filesToCopy) {
                if (ignoreList.contains(fileToCopy)) {
                    this.log(
                        String.format(
                            "Not copying library %s, due to exclude.",  //NOI18N
                            fileToCopy),
                        Project.MSG_INFO);
                    continue;
                }
                this.log("Copy " + fileToCopy.getName() + " to " + libFolder + ".", Project.MSG_VERBOSE);
                try {
                    File libFile = new File (libFolder,fileToCopy.getName());
                    if (!rebase(fileToCopy, libFile)) {
                        libFile.delete();
                        utils.copyFile(fileToCopy,libFile);
                    }
                } catch (IOException ioe) {
                    throw new BuildException (ioe);
                }
            }
            final FileSet fs = new FileSet();
            fs.setDir(libFolder);
            final Path p = new Path(getProject());
            p.addFileset(fs);
            addConfiguredIndexJars(p);
        }
        else {
            this.log("Nothing to copy.");
        }

        super.execute();
    }

    private boolean rebase(final File source, final File target) {
        if (!rebase) {
            return false;
        }
        try {
            Manifest manifest = null;
            final ZipFile zf = new ZipFile(source, getEncoding());
            try {
                if (zf.getEntry(INDEX) != null) {
                    return false;
                }
                final ZipEntry manifestEntry = zf.getEntry(MANIFEST);
                if (manifestEntry != null) {
                    try (Reader in = new InputStreamReader(zf.getInputStream(manifestEntry), Charset.forName(UTF_8))) {
                        manifest = new Manifest(in);
                    }
                }
                if (manifest == null) {
                    return false;
                }
                final Section mainSection = manifest.getMainSection();
                final String classPath = mainSection.getAttributeValue(ATTR_CLASS_PATH);   //NOI18N
                if (classPath == null) {
                    return false;
                }
                if (isSigned(manifest)) {
                    return false;
                }
                final StringBuilder result = new StringBuilder();
                boolean changed = false;
                for (String path : classPath.split(" ")) {  //NOI18N
                    if (result.length() > 0) {
                        result.append(' ');                 //NOI18N
                    }
                    int index = path.lastIndexOf('/');      //NOI18N
                    if (index >=0 && index < path.length()-1) {
                        path = path.substring(index+1);
                        changed = true;
                    }
                    result.append(path);
                }
                if (!changed) {
                    return false;
                }
                final Enumeration<? extends ZipEntry> zent = zf.getEntries();
                try (final ZipOutputStream out = new ZipOutputStream(target)) {
                    out.setEncoding(getEncoding());   //NOI18N
    //              out.setUseLanguageEncodingFlag(getUseLanguageEnodingFlag());      requires Ant 1.8
//                  out.setCreateUnicodeExtraFields(getCreateUnicodeExtraFields().getPolicy());   requires Ant 1.8
//                  out.setFallbackToUTF8(getFallBackToUTF8());   requires Ant 1.8
                    while (zent.hasMoreElements()) {
                        final ZipEntry entry = zent.nextElement();
                        try (InputStream in = zf.getInputStream(entry)) {

                            if (MANIFEST.equals(entry.getName())) {
                                out.putNextEntry(entry);
                                mainSection.removeAttribute(ATTR_CLASS_PATH);
                                mainSection.addAttributeAndCheck(new Manifest.Attribute(ATTR_CLASS_PATH, result.toString()));
                                final PrintWriter manifestOut = new PrintWriter(new OutputStreamWriter(out, Charset.forName(UTF_8)));
                                manifest.write(manifestOut);
                                manifestOut.flush();
                            } else {
                                out.putNextEntry(entry);
                                copy(in,out);
                            }
                        }
                    }
                    return true;
                }
            } finally {
                zf.close();
            }
        } catch (IOException | ManifestException e) {
            this.log("Cannot fix dependencies for: " + target.getAbsolutePath(), Project.MSG_WARN);   //NOI18N
        }
        return false;
    }

    private static boolean isSigned(final Manifest manifest) {
        Section section = manifest.getSection(MANIFEST);
        if (section != null) {
            final Enumeration<String> sectionKeys = (Enumeration<String>) section.getAttributeKeys();
            while (sectionKeys.hasMoreElements()) {
                if (sectionKeys.nextElement().endsWith("-Digest")) {    //NOI18N
                    return true;
                }
            }
        }
        return false;
    }

    private static void copy(final InputStream in, final OutputStream out) throws IOException {
        final byte[] BUFFER = new byte[4096];
        int len;
        for (;;) {
            len = in.read(BUFFER);
            if (len == -1) {
                return;
            }
            out.write(BUFFER, 0, len);
        }
    }

    private static boolean isUTF8(final String encoding) {
        return UTF_8.equalsIgnoreCase(encoding) || UTF8.equalsIgnoreCase(encoding);
    }

    private static String basename(final String name, final String separator) {
        final int endIndex = name.endsWith(separator) ?
                name.length() - 1 :
                name.length();
        final int startIndex = name.lastIndexOf(separator.charAt(0), endIndex -1);
        return endIndex == name.length() && startIndex == -1 ?
                name :
                name.substring(startIndex + 1, endIndex);
    }

    private static String stringJoin(CharSequence delimiter, CharSequence... elements) {
        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (CharSequence element : elements) {
            if (first) {
                first = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(element);
        }
        return sb.toString();
    }
}
