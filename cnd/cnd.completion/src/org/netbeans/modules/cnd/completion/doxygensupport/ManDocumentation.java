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
package org.netbeans.modules.cnd.completion.doxygensupport;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeProjectSupport.NativeExitStatus;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectSupport;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public class ManDocumentation {
    private static final String MAN_PAGES_CACHE_ENCODING = "UTF-8"; //NOI18N

//    private static final Logger LOG = Logger.getLogger(ManDocumentation.class.getName());
//    private static String manPath = null;
//
//    private static String getPath(String cmd) {
//        String path = null;
//        path = Path.findCommand(cmd);
//        if (path == null) {
//            if (new File("/usr/bin/" + cmd).exists()) { // NOI18N
//                path = "/usr/bin/" + cmd; // NOI18N
//            }
//        }
//        if (path == null) {
//            if (new File("/bin/" + cmd).exists()) { // NOI18N
//                path = "/bin/" + cmd; // NOI18N
//            }
//        }
//        return path;
//    }
//
//    private static String getManPath() {
//        if (manPath == null) {
//            manPath = getPath("man"); // NOI18N
//        }
//        return manPath;
//    }
    public static CompletionDocumentation getDocumentation(CsmObject obj, CsmFile file) throws IOException {
        if (CsmKindUtilities.isFunction(obj) && !CsmKindUtilities.isClassMember(obj)) {
            CsmFunction function = (CsmFunction) obj;
            return getDoc(stripTemplate(function.getQualifiedName().toString()), file);
        } else if (CsmKindUtilities.isClass(obj)) {
            CsmClass cls = (CsmClass) obj;
            return getDoc(stripTemplate(cls.getQualifiedName().toString()), file);
        } else if (CsmKindUtilities.isClassMember(obj)) {
            CsmScope scope = ((CsmMember) obj).getScope();
            if (CsmKindUtilities.isClass(scope)) {
                CsmClass cls = (CsmClass) scope;
                return getMemberDoc(stripTemplate(cls.getQualifiedName().toString())+"::"+stripTemplate(((CsmMember) obj).getName().toString()), file); //NOI18N
            }
        }
        return null;
    }
    
    private static String stripTemplate(String name) {
        if (name.indexOf('<') > 0) { //NOI18N
            name = name.substring(0, name.indexOf('<')); //NOI18N
        }
        return name;
    }

    private static CompletionDocumentation getMemberDoc(String name, CsmFile file) throws IOException {
        try {
            CompletionDocumentation documentation = getDocumentation(name, file);
            if (documentation != null) {
                return documentation;
            }
        } catch (IOException ex) {
            if (!name.contains("::")) { //NOI18N
                throw ex;
            }
            // try class name
        }
        if (name.contains("::")) { //NOI18N
            return getDoc(name.substring(0, name.lastIndexOf(':')-1), file);
        }
        return null;
    }

    private static CompletionDocumentation getDoc(String name, CsmFile file) throws IOException {
        try {
            CompletionDocumentation documentation = getDocumentation(name, file);
            if (documentation != null) {
                return documentation;
            }
        } catch (IOException ex) {
            if (!name.contains("::")) { //NOI18N
                throw ex;
            }
            // try name
        }
        if (name.contains("::")) { //NOI18N
            name = name.substring(name.lastIndexOf(':')+1); //NOI18N
            return getDocumentation(name, file);
        }
        return null;
    }

    public static CompletionDocumentation getDocumentation(String name, CsmFile file) throws IOException {
        return getDocumentation(name, 3, file);
        /**Supposing all functions goes from chapter 3*/
    }

    public static CompletionDocumentation getDocumentation(String name, int chapter, CsmFile file) throws IOException {
        String doc = getDocumentationForName(name, chapter, file);

        if (doc == null) {
            return null;
        }

        return new CompletionDocumentationImpl(doc, file);
    }

    public static String getDocumentationForName(String name, int chapter, CsmFile file) throws IOException {
        NativeProject np = getNativeProject(file);
        if (np == null) {
            return "";
        }
        String platformName = NativeProjectSupport.getPlatformName(np);
        File cache = getCacheFile(name, chapter, platformName);

        if (cache.exists()) {
            return readFile(cache);
        }

        String doc = createDocumentationForName(name, chapter, np);

        if (doc != null) {
            OutputStream out = null;

            try {
                out = new FileOutputStream(cache);

                out.write(doc.getBytes(MAN_PAGES_CACHE_ENCODING));
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

            return doc;
        }

        return null;
    }

//    public static String constructWarning(CsmObject obj) {
//        if (obj instanceof CsmFunction) {
//            StringBuilder w = new StringBuilder();
//
//            if (getManPath() == null) { // NOI18N
//                w.append("<p>"); // NOI18N
//                w.append(getString("MAN_NOT_INSTALLED")); // NOI18N
//                w.append("</p>\n"); // NOI18N
//            }
//
//            return w.toString();
//        }
//
//        return "";
//    }
    private static File getCacheFile(String name, int chapter, String platformName) {
        // name might look like "operator /=", so we need to escape it
        String safeName;
        try {
            safeName = URLEncoder.encode(name, MAN_PAGES_CACHE_ENCODING); // NOI18N
        } catch (UnsupportedEncodingException ex) {
            // UTF-8 should always be supported, but anyway...
            safeName = name;
        }
        return Places.getCacheSubfile("cnd/manpages/" + safeName + "." + platformName + "." + chapter); // NOI18N
    }

    static NativeProject getNativeProject(CsmFile csmFile) {
        NativeProject nativeProject = null;
        if (csmFile != null) {
            CsmProject csmProject = csmFile.getProject();
            if (csmProject.getPlatformProject() instanceof NativeProject) {
                nativeProject = (NativeProject) csmProject.getPlatformProject();
            } else {
                loop:
                for (CsmProject project : CsmModelAccessor.getModel().projects()) {
                    for (CsmProject lib : project.getLibraries()) {
                        if (lib.equals(csmProject)) {
                            if (project.getPlatformProject() instanceof NativeProject) {
                                nativeProject = (NativeProject) project.getPlatformProject();
                                break loop;
                            }
                        }
                    }
                }
            }
        }
        return nativeProject;
    }

    private static String createDocumentationForName(String name, int chapter, NativeProject np) throws IOException {
        NativeExitStatus exitStatus;
        String platformName = NativeProjectSupport.getPlatformName(np);
        if (platformName == null) {
            exitStatus = NativeProjectSupport.execute(np, "man", new String[]{"MANWIDTH=" + Man2HTML.MAX_WIDTH}, name); // NOI18N
        } else if (platformName.contains("Solaris")) { // NOI18N
            NativeExitStatus es = NativeProjectSupport.execute(np, "man", new String[]{}, "-l", name); // NOI18N
            String section = getSection(es.output, "(2"); // NOI18N
            if (section == null) {
                section = getSection(es.output, "(3"); // NOI18N
            }
            if (section != null) {
                exitStatus = NativeProjectSupport.execute(np, "man", null, "-s" + section, name); // NOI18N
            } else {
                exitStatus = NativeProjectSupport.execute(np, "man", null, name); // NOI18N
            }
        } else {
            // Current host locale is used here, because user possibly wants to see man pages
            // in locale of his development host, not in remote's host one.
            final String DOT_UTF8 = ".UTF-8";  // NOI18N
            exitStatus = NativeProjectSupport.execute(np, "man", new String[]{"MANWIDTH=" + Man2HTML.MAX_WIDTH, "LANG=" + Locale.getDefault().toString().trim().replace(DOT_UTF8, "") + DOT_UTF8}, "-S2:3", name); // NOI18N
        }
        StringReader sr;
        if (exitStatus != null) {
            if (exitStatus.isOK() && exitStatus.output.length() > 0) {
                if (exitStatus.output.split("\n").length <= 2) { // NOI18N
                    return null;
                }
                sr = new StringReader(exitStatus.output);
            } else {
                throw new IOException(exitStatus.error);
            }
        } else {
            return null;
        }
        BufferedReader br = new BufferedReader(sr);
        String text = new Man2HTML(br).getHTML();
        br.close();
        sr.close();
        return text;
    }

    private static String getSection(String output, String number) {
        String section = null;
        int index1 = output.indexOf(number);
        int index2;
        while (index1 >= 0) {
            if (output.charAt(index1 + 2) != 'f') { // Don't want fortran!
                index2 = output.substring(index1).indexOf(")"); // NOI18N
                section = output.substring(index1 + 1, index1 + index2);
                break;
            }
            output = output.substring(index1 + 1);
            index1 = output.indexOf(number);
        }
        return section;
    }

    private static final Map<String, String> TRANSLATE;

    static {
        TRANSLATE = new HashMap<String, String>();

        TRANSLATE.put("&minus;", "-"); // NOI18N
        TRANSLATE.put("&lsquo;", "'"); // NOI18N
        TRANSLATE.put("&rsquo;", "'"); // NOI18N
    }

    private static String readFile(File f) throws IOException {
        InputStream fin = null;
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            fin = new FileInputStream(f);

            if (f.getName().endsWith(".gz")) { // NOI18N
                in = new GZIPInputStream(fin);
            } else {
                in = fin;
            }

            FileUtil.copy(in, out);

            return out.toString(MAN_PAGES_CACHE_ENCODING);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
            try {
                out.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static final class CompletionDocumentationImpl implements CompletionDocumentation {

        private String doc;
        private CsmFile file;

        public CompletionDocumentationImpl(String doc, CsmFile file) {
            this.doc = doc;
            this.file = file;
        }

        @Override
        public String getText() {
            return doc;
        }

        @Override
        public URL getURL() {
            return null;
        }

        @Override
        public CompletionDocumentation resolveLink(String link) {
            String[] parts = link.split("\\?"); // NOI18N

            if (parts.length != 2) {
                return null;
            }

            String[] chapterAndName = parts[1].split("\\+"); // NOI18N

            if (chapterAndName.length != 2) {
                return null;
            }

            int chapter = Integer.parseInt(chapterAndName[0]);
            String name = chapterAndName[1];

            try {
                return ManDocumentation.getDocumentation(name, chapter, file);
            } catch (IOException ioe) {
                return new CompletionDocumentationImpl(ioe.getMessage(), file);
            }
        }

        @Override
        public Action getGotoSourceAction() {
            return null;
        }
    }

    private static String getString(String s) {
        return NbBundle.getBundle(ManDocumentation.class).getString(s);
    }
}
