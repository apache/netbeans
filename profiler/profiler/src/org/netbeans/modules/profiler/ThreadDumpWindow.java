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
package org.netbeans.modules.profiler;

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.net.URL;
import java.text.SimpleDateFormat;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.lib.profiler.results.threads.ThreadDump;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.netbeans.lib.profiler.utils.StringUtils;
import static org.netbeans.modules.profiler.SampledCPUSnapshot.OPEN_THREADS_URL;
import org.netbeans.modules.profiler.api.GoToSource;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * IDE topcomponent to display thread dump.
 *
 * @author Tomas Hurka
 */
@NbBundle.Messages({
    "ThreadDumpWindow_Caption=Thread dump {0}",})
public class ThreadDumpWindow extends ProfilerTopComponent {

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    private static final String HELP_CTX_KEY = "ThreadDumpWindow.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);
    
    
    private HTMLTextArea a;

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    /**
     * This constructor cannot be called, instances of this window cannot be
     * persisted.
     */
    public ThreadDumpWindow() {
        throw new InternalError("This constructor should never be called"); // NOI18N
    }

    public ThreadDumpWindow(ThreadDump td) {
        setLayout(new BorderLayout());
        setFocusable(true);
        setRequestFocusEnabled(true);
        setName(Bundle.ThreadDumpWindow_Caption(StringUtils.formatUserDate(td.getTime())));
        setIcon(Icons.getImage(ProfilerIcons.THREAD));

        StringBuilder text = new StringBuilder();
        printThreads(text, td);
        a = new HTMLTextArea() {
            protected void showURL(URL url) {
                if (url == null) {
                    return;
                }
                String urls = url.toString();
                ThreadDumpWindow.this.showURL(urls);
            }
        };
        a.setEditorKit(new CustomHtmlEditorKit());
        a.setText(text.toString());
        a.setCaretPosition(0);
        JScrollPane sp = new JScrollPane(a);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(BorderFactory.createEmptyBorder());
        add(sp, BorderLayout.CENTER);
    }
    
    protected Component defaultFocusOwner() {
        return a;
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }

    protected String preferredID() {
        return this.getClass().getName();
    }

    private void printThreads(final StringBuilder sb, ThreadDump td) {
        ThreadInfo[] threads = td.getThreads();
        boolean goToSourceAvailable = GoToSource.isAvailable();
        boolean jdk15 = td.isJDK15();

        sb.append("<pre>"); // NOI18N
        sb.append(" <b>Full thread dump: "); // NOI18N
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  // NOI18N
        sb.append(df.format(td.getTime()) + "</b><br><br>");
        for (ThreadInfo thread : threads) {
            if (thread != null) {
                if (jdk15) {
                    print15Thread(sb, thread, goToSourceAvailable);
                } else {
                    print16Thread(sb, thread, goToSourceAvailable);
                }
            }
        }
        sb.append("</pre>"); // NOI18N
    }

    private void print15Thread(final StringBuilder sb, final ThreadInfo thread, boolean goToSourceAvailable) {
        sb.append("<br>\"" + thread.getThreadName() + // NOI18N
                "\" - Thread t@" + thread.getThreadId() + "<br>");    // NOI18N
        sb.append("   java.lang.Thread.State: " + thread.getThreadState()); // NOI18N
        if (thread.getLockName() != null) {
            sb.append(" on " + thread.getLockName());   // NOI18N
            if (thread.getLockOwnerName() != null) {
                sb.append(" owned by: " + thread.getLockOwnerName());   // NOI18N
            }
        }
        sb.append("<br>");    // NOI18N
        for (StackTraceElement st : thread.getStackTrace()) {
            String stackElementText = htmlize(st.toString());
            String stackEl = stackElementText;
            if (goToSourceAvailable) {
                String className = st.getClassName();
                String method = st.getMethodName();
                int lineNo = st.getLineNumber();
                String stackUrl = OPEN_THREADS_URL + className + "|" + method + "|" + lineNo; // NOI18N
                stackEl = "<a href=\"" + stackUrl + "\">" + stackElementText + "</a>";    // NOI18N
            }
            sb.append("        at ").append(stackEl).append("<br>");    // NOI18N
        }
    }

    private void print16Thread(final StringBuilder sb, final ThreadInfo thread, boolean goToSourceAvailable) {
        MonitorInfo[] monitors = thread.getLockedMonitors();
        sb.append("&nbsp;<b>");   // NOI18N
        sb.append("\"").append(thread.getThreadName()).append("\" - Thread t@").append(thread.getThreadId()).append("<br>");    // NOI18N
        sb.append("    java.lang.Thread.State: ").append(thread.getThreadState()); // NOI18N
        sb.append("</b><br>");   // NOI18N
        int index = 0;
        for (StackTraceElement st : thread.getStackTrace()) {
            LockInfo lock = thread.getLockInfo();
            String stackElementText = htmlize(st.toString());
            String lockOwner = thread.getLockOwnerName();

            String stackEl = stackElementText;
            if (goToSourceAvailable) {
                String className = st.getClassName();
                String method = st.getMethodName();
                int lineNo = st.getLineNumber();
                String stackUrl = OPEN_THREADS_URL + className + "|" + method + "|" + lineNo; // NOI18N
                stackEl = "<a href=\"" + stackUrl + "\">" + stackElementText + "</a>";    // NOI18N
            }

            sb.append("    at ").append(stackEl).append("<br>");    // NOI18N
            if (index == 0) {
                if ("java.lang.Object".equals(st.getClassName()) && // NOI18N
                        "wait".equals(st.getMethodName())) {                // NOI18N
                    if (lock != null) {
                        sb.append("    - waiting on ");    // NOI18N
                        printLock(sb, lock);
                        sb.append("<br>");    // NOI18N
                    }
                } else if (lock != null) {
                    if (lockOwner == null) {
                        sb.append("    - parking to wait for ");      // NOI18N
                        printLock(sb, lock);
                        sb.append("<br>");            // NOI18N
                    } else {
                        sb.append("    - waiting to lock ");      // NOI18N
                        printLock(sb, lock);
                        sb.append(" owned by \"").append(lockOwner).append("\" t@").append(thread.getLockOwnerId()).append("<br>");   // NOI18N
                    }
                }
            }
            printMonitors(sb, monitors, index);
            index++;
        }
        StringBuilder jnisb = new StringBuilder();
        printMonitors(jnisb, monitors, -1);
        if (jnisb.length() > 0) {
            sb.append("   JNI locked monitors:<br>");
            sb.append(jnisb);
        }
        LockInfo[] synchronizers = thread.getLockedSynchronizers();
        if (synchronizers != null) {
            sb.append("<br>   Locked ownable synchronizers:");    // NOI18N
            if (synchronizers.length == 0) {
                sb.append("<br>    - None\n");  // NOI18N
            } else {
                for (LockInfo li : synchronizers) {
                    sb.append("<br>    - locked ");         // NOI18N
                    printLock(sb, li);
                    sb.append("<br>");  // NOI18N
                }
            }
        }
        sb.append("<br>");
    }

    private void printMonitors(final StringBuilder sb, final MonitorInfo[] monitors, final int index) {
        if (monitors != null) {
            for (MonitorInfo mi : monitors) {
                if (mi.getLockedStackDepth() == index) {
                    sb.append("    - locked ");   // NOI18N
                    printLock(sb, mi);
                    sb.append("<br>");    // NOI18N
                }
            }
        }
    }

    private void printLock(StringBuilder sb, LockInfo lock) {
        String id = Integer.toHexString(lock.getIdentityHashCode());
        String className = lock.getClassName();

        sb.append("&lt;").append(id).append("&gt; (a ").append(className).append(")");       // NOI18N
    }

    private static String htmlize(String value) {
        return value.replace(">", "&gt;").replace("<", "&lt;");     // NOI18N
    }

    private void showURL(String urls) {
        if (urls.startsWith(SampledCPUSnapshot.OPEN_THREADS_URL)) {
            urls = urls.substring(SampledCPUSnapshot.OPEN_THREADS_URL.length());
            String parts[] = urls.split("\\|"); // NOI18N
            String className = parts[0];
            String method = parts[1];
            int linenumber = Integer.parseInt(parts[2]);
            GoToSource.openSource(null, className, method, linenumber);
        }
    }

    private class CustomHtmlEditorKit extends HTMLEditorKit {

        @Override
        public Document createDefaultDocument() {
            StyleSheet styles = getStyleSheet();
            StyleSheet ss = new StyleSheet();

            ss.addStyleSheet(styles);

            HTMLDocument doc = new CustomHTMLDocument(ss);
            doc.setParser(getParser());
            doc.setAsynchronousLoadPriority(4);
            doc.setTokenThreshold(100);
            return doc;
        }
    }

    private class CustomHTMLDocument extends HTMLDocument {

        private static final int CACHE_BOUNDARY = 1000;
        private char[] segArray;
        private int segOffset;
        private int segCount;
        private boolean segPartialReturn;
        private int lastOffset;
        private int lastLength;

        private CustomHTMLDocument(StyleSheet ss) {
            super(ss);
            lastOffset = -1;
            lastLength = -1;
            putProperty("multiByte", Boolean.TRUE);      // NOI18N
        }

        @Override
        public void getText(int offset, int length, Segment txt) throws BadLocationException {
            if (lastOffset == offset && lastLength == length) {
                txt.array = segArray;
                txt.offset = segOffset;
                txt.count = segCount;
                txt.setPartialReturn(segPartialReturn);
                return;
            }
            super.getText(offset, length, txt);
            if (length > CACHE_BOUNDARY || lastLength <= CACHE_BOUNDARY) {
                segArray = txt.array;
                segOffset = txt.offset;
                segCount = txt.count;
                segPartialReturn = txt.isPartialReturn();
                lastOffset = offset;
                lastLength = length;
            }
        }
    }

}
