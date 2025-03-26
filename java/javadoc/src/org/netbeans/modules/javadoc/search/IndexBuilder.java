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

package org.netbeans.modules.javadoc.search;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.lib.editor.util.StringEscapeUtils;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Builds index of Javadoc sets.
 * @author Svata Dedic, Jesse Glick
 */
public class IndexBuilder implements Runnable, ChangeListener {

    private static final String[] INDEX_FILE_NAMES = {
        "overview-summary.html", // NOI18N
        "index.html", // NOI18N
        "index.htm", // NOI18N
    };

    private static IndexBuilder INSTANCE;

    private static RequestProcessor.Task    task;
    
    private static final ErrorManager err =
            ErrorManager.getDefault().getInstance("org.netbeans.modules.javadoc.search.IndexBuilder"); // NOI18N;
    
    private List<Index> cachedData;
    
    private JavadocRegistry jdocRegs;

    /**
     * information extracted from Javadoc.
     */
    Map<URL,Info> filesystemInfo = Collections.emptyMap();

    private static class Info {
        /**
         * Display name / title of the helpset
         */
        String title;

        /**
         * Name of the index/overview file
         */
        String indexFileName;
    }

    @SuppressWarnings("LeakingThisInConstructor")
    private IndexBuilder() {
        this.jdocRegs = JavadocRegistry.getDefault();
        this.jdocRegs.addChangeListener(this);
        if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
            err.log("new IndexBuilder");
        }
    }

    /**
     * Get the default index builder instance.
     * It will start parsing asynch.
     */
    public static synchronized IndexBuilder getDefault() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        INSTANCE = new IndexBuilder();
        scheduleTask();
        return INSTANCE;
    }
    
    public @Override void run() {
        refreshIndex();
    }
    
    public @Override void stateChanged (ChangeEvent event) {
        scheduleTask ();
    }

    public static final class Index implements Comparable<Index> {
        private static final Collator c = Collator.getInstance();
        public final String display;
        public final URL fo;
        private Index(String display, URL fo) {
            this.display = display;
            this.fo = fo;
        }
        public @Override boolean equals(Object obj) {
            if (!(obj instanceof Index)) {
                return false;
            }
            final Index other = (Index) obj;
            return display.equals(other.display) && fo.toString().equals(other.fo.toString());
        }
        public @Override int hashCode() {
            return display.hashCode() ^ fo.toString().hashCode();
        }
        public @Override int compareTo(Index o) {
            return c.compare(display, o.display);
        }
    }

    /**
     * Get the important information from the index builder.
     * Waits for parsing to complete first, if necessary.
     * @param blocking {@code true} in case the current thread should wait for result
     * @return list of index files together with display names,
     *      or null in case it is non blocking call and result is not ready yet.
     */
    public List<Index> getIndices(boolean blocking) {
        if (blocking) {
            task.waitFinished();
        } else if (!task.isFinished()) {
            return null;
        }
        return cachedData;
    }

    private void refreshIndex() {
        if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
            err.log("refreshIndex");
        }
        Map<URL,Info> oldMap;
        synchronized (this) {
            oldMap = this.filesystemInfo;
        }
        URL[] docRoots = jdocRegs.getDocRoots();
        // XXX needs to be able to listen to result; when it changes, call scheduleTask()
        Map<URL,Info> m = new WeakHashMap<>();
        // long startTime = System.nanoTime();

        for ( int ifCount = 0; ifCount < docRoots.length; ifCount++ ) {
            URL fo = docRoots[ifCount];
            Info oldInfo = oldMap.get(fo);
            if (oldInfo != null) {
                // No need to reparse.
                m.put(fo, oldInfo);
                continue;
            }
            
            URL index = URLUtils.findOpenable(fo, INDEX_FILE_NAMES);
            if (index == null || index.toString().endsWith("index.html")) { // NOI18N
                // For single-package doc sets, overview-summary.html is not present,
                // and index.html is less suitable (it is framed). Look for a package
                // summary.
                // [PENDING] Display name is not ideal, e.g. "org.openide.windows (NetBeans Input/Output API)"
                // where simply "NetBeans Input/Output API" is preferable... but standard title filter
                // regexps are not so powerful (to avoid matching e.g. "Servlets (Main Documentation)").
                try (InputStream is = URLUtils.open(fo, "package-list");
                        InputStream is2 = URLUtils.open(fo, "element-list")) { // NOI18N
                    if (is != null || is2 != null) {
                        try (BufferedReader r = new BufferedReader(new InputStreamReader( is != null ? is : is2 ))) {
                            String line = r.readLine();
                            if (line != null && r.readLine() == null) {
                                // Good, exactly one line as expected. A package name.
                                String resName = line.replace('.', '/') + "/package-summary.html"; // NOI18N
                                URL pindex = URLUtils.findOpenable(fo, resName);
                                if (pindex != null) {
                                    index = pindex;
                                }
                                // else fall back to index.html if available
                            }
                        }
                    }
                }
                catch (IOException ioe) {
                    // Oh well, skip this one.
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                }
            }
            if (index != null) {
                // Try to find a title.
                String title = parseTitle(index);
                if (title != null) {
                    JavadocSearchType st = jdocRegs.findSearchType( fo );
                    if (st == null) {
                        continue;
                    }
                    title = st.getOverviewTitleBase(title);
                }
                if (title == null || title.isEmpty() || title.equals("Overview")) { // NOI18N
                    String filename = URLUtils.getDisplayName(index);
                    if (filename.length() > 54) {
                        // trim to display 54 chars
                        filename = filename.substring(0, 10) + "[...]" // NOI18N
                                + filename.substring(filename.length() - 40);
                    }
                    title = NbBundle.getMessage(IndexBuilder.class,
                            "FMT_NoOverviewTitle", new Object[] { filename }); // NOI18N
                }
                Info info = new Info();
                info.title = title;
                info.indexFileName = index.toString().substring(fo.toString().length());
                m.put(fo, info);
            }
            synchronized (this) {
                this.filesystemInfo = m;
            }
        }
        List<Index> data = new ArrayList<>();
        for (Map.Entry<URL,Info> entry : filesystemInfo.entrySet()) {
            Info info = entry.getValue();
            URL fo = URLUtils.findOpenable(entry.getKey(), info.indexFileName);
            if (fo == null) {
                continue;
            }
            data.add(new Index(info.title, fo));
        }
        Collections.sort(data);
        cachedData = data;

        // long elapsedTime = System.nanoTime() - startTime;
        // System.out.println("\nElapsed time[nano]: " + elapsedTime);
    }
    
    /**
     * Attempt to find the title of an HTML file object.
     * May return null if there is no title tag, or "" if it is empty.
     */
    private String parseTitle(URL html) {
        String title = null;
        try (InputStream is = new BufferedInputStream(URLUtils.openStream(html), 1024)) {
            // #71979: html parser used again to fix encoding issues.
            // I have measured no difference if the parser or plain file reading
            // is used (#32551).
            // In case the parser is stopped as soon as it finds the title it is
            // even faster than the previous fix.
            SimpleTitleParser tp = new SimpleTitleParser(is);
            tp.parse();
            title = tp.getTitle();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
        return title != null? title.trim(): title;
    }

    private static synchronized void scheduleTask() {
        if (task == null) {
            task = new RequestProcessor(IndexBuilder.class).create(getDefault()); // NOI18N
        }
        // Give it a small delay to avoid restarting too many times e.g. during
        // project switch:
        task.schedule(100);
    }

    static final class SimpleTitleParser {

        private char cc;
        private InputStream is;
        private String charset;
        private String title;
        private State state = State.CONTINUE;

        enum State {CONTINUE, EXIT}

        SimpleTitleParser(InputStream is) {
            this.is = is;
        }

        public String getTitle() {
            return title;
        }

        public void parse() throws IOException {
            readNext();
            while (state == State.CONTINUE) {
                switch (cc) {
                    case '<' : // start of tags
                        handleOpenBrace();
                        break;
                    case (char) -1 : // EOF
                        return;
                    default:
                        readNext();
                }
            }
        }

        private void readNext() throws IOException {
            cc = (char) is.read();
        }

        @SuppressWarnings("fallthrough") // XXX intentional?
        private void handleOpenBrace() throws IOException {
            StringBuilder sb = new StringBuilder();
            while (true) {
                readNext();
                switch (cc) {
                    case '>':  // end of tag
                        String tag = sb.toString().toLowerCase();
                        if (tag.startsWith("body")) { // NOI18N
                            state = State.EXIT;
                            return; // exit parsing, no title
                        } else if (tag.startsWith("meta")) { // NOI18N
                            handleMetaTag(tag);
                            return;
                        } else if (tag.startsWith("title")) { // NOI18N
                            handleTitleTag();
                            return;

                        }
                        return;
                    case (char) -1:  // EOF
                        return;
                    case ' ':
                        if (sb.length() == 0) {
                            break;
                        }
                    default:
                        sb.append(cc);
                }
            }

        }

        private void handleMetaTag(String txt) {
            // parse something like
            // <META http-equiv="Content-Type" content="text/html; charset=euc-jp">
            // see http://www.w3.org/TR/REC-html32#meta
            String name = ""; // NOI18N
            String value = ""; // NOI18N

            char tc;
            char[] txts = txt.toCharArray();
            int offset = 5; // skip "meta "
            int start = offset;
            int state2 = 0;
            while (offset < txts.length) {
                tc = txt.charAt(offset);
                if (tc == '=' && state2 == 0) { // end of name
                    name = String.valueOf(txts, start, offset++ - start).trim();
                    state2 = 1;
                } else if (state2 == 1 && (tc == '"' || tc == '\'')) { // start of value
                    start = ++offset;
                    state2 = 2;
                } else if (state2 == 2 && (tc == '"' || tc == '\'')) { // end of value
                    value = String.valueOf(txts, start, offset++ - start);
                    if ("content".equals(name)) { // NOI18N
                        break;
                    }
                    name = ""; // NOI18N
                    state2 = 0;
                    start = offset;
                } else {
                    ++offset;
                }

            }

            StringTokenizer tk = new StringTokenizer(value, ";"); // NOI18N
            while (tk.hasMoreTokens()) {
                String str = tk.nextToken().trim();
                if (str.startsWith("charset")) {        //NOI18N
                    str = str.substring(7).trim();
                    if (str.charAt(0) == '=') {
                        this.charset = str.substring(1).trim();
                        return;
                    }
                }
            }
        }

        @SuppressWarnings("fallthrough") // XXX intentional?
        private void handleTitleTag() throws IOException {
            byte[] buf = new byte[200];
            int offset = 0;
            while (true) {
                readNext();
                switch (cc) {
                    case (char) -1:  // EOF
                        return;
                    case '>': // </title>
                        if ("</title".equalsIgnoreCase(new String(buf, offset - 7, 7))) {
                            // title is ready
                            // XXX maybe we should also resolve entities like &gt;
                            state = State.EXIT;
                            if (charset == null) {
                                title = new String(buf, 0, offset - 7).trim();
                            } else {
                                title = new String(buf, 0, offset - 7, charset).trim();
                            }
                            
                            // Unescape the title, done for JDK 11.
                            // JDK 11 title contains &amp; (NETBEANS-4176)
                            title = StringEscapeUtils.unescapeHtml(title);
                            
                            return;
                        }
                    default:
                        cc = (cc == '\n' || cc == '\r')? ' ': cc;
                        if (offset == buf.length) {
                            buf = enlarge(buf);
                        }
                        buf[offset++] = (byte) cc;

                }
            }
        }

        private static byte[] enlarge(byte[] b) {
            byte[] b2 = new byte[b.length + 200];
            System.arraycopy(b, 0, b2, 0, b.length);
            return b2;
        }
    }

}
