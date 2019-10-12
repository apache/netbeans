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
package org.netbeans.modules.payara.eecommon.api;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.payara.tooling.utils.StringPrefixTree;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.GlobalPathRegistryEvent;
import org.netbeans.api.java.classpath.GlobalPathRegistryListener;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * <code>LogSupport</code> class for creating links in the output window.
 *
 * @author  Stepan Herold
 */
public class LogHyperLinkSupport {

    private Map<Link, Link> links = Collections.synchronizedMap(new HashMap<Link, Link>());
    private Annotation errAnnot;

    /**
     * Return a link which implements <code>OutputListener</code> interface. Link
     * is then used to represent a link in the output window. This class also 
     * handles error annotations which are shown after a line is clicked.
     * 
     * @return link which implements <code>OutputListener</code> interface. Link
     *         is then used to represent a link in the output window.
     */
    public Link getLink(String errorMsg, String path, int line) {
        Link newLink = new Link(errorMsg, path, line);
        Link cachedLink = links.get(newLink);
        if (cachedLink != null) {
            return cachedLink;
        }
        links.put(newLink, newLink);
        return newLink;
    }

    /**
     * Detach error annotation.
     */
    public void detachAnnotation() {
        if (errAnnot != null) {
            errAnnot.detach();
        }
    }

    /**
     * <code>LineInfo</code> is used to store info about the parsed line.
     */
    public static class LineInfo {

        private String path;
        private int line;
        private String message;
        private boolean error;
        private boolean accessible;

        /**
         * <code>LineInfo</code> is used to store info about the parsed line.
         *
         * @param path path to file
         * @param line line number where the error occurred
         * @param message error message
         * @param error represents the line an error?
         * @param accessible is the file accessible?
         */
        public LineInfo(String path, int line, String message, boolean error, boolean accessible) {
            this.path = path;
            this.line = line;
            this.message = message;
            this.error = error;
            this.accessible = accessible;
        }

        public String path() {
            return path;
        }

        public int line() {
            return line;
        }

        public String message() {
            return message;
        }

        public boolean isError() {
            return error;
        }

        public boolean isAccessible() {
            return accessible;
        }

        @Override
        public String toString() {
            return "path=" + path + " line=" + line + " message=" + message + " isError=" + error + " isAccessible=" + accessible;
        }
    }

    /**
     * Error annotation.
     */
    static class ErrorAnnotation extends Annotation {

        private String shortDesc = null;

        public ErrorAnnotation(String desc) {
            shortDesc = desc;
        }

        @Override
        public String getAnnotationType() {
            return "org-netbeans-modules-j2ee-sunserver"; // NOI18N
        }

        @Override
        public String getShortDescription() {
            return shortDesc;
        }
    }

    /**
     * <code>Link</code> is used to create a link in the output window. To create
     * a link use the <code>getLink</code> method of the <code>LogSupport</code>
     * class. This prevents from memory vast by returning already existing instance,
     * if one with such values exists.
     */
    public class Link implements OutputListener {

        private String msg;
        private String path;
        private int line;
        private int hashCode = 0;

        Link(String msg, String path, int line) {
            this.msg = msg;
            this.path = path;
            this.line = line;
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                int result = 17;
                result = 37 * result + line;
                result = 37 * result + (path != null ? path.hashCode() : 0);
                result = 37 * result + (msg != null ? msg.hashCode() : 0);
                hashCode = result;
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof Link) {
                Link anotherLink = (Link) obj;
                if (((msg != null && msg.equals(anotherLink.msg)) || msg == anotherLink.msg) && 
                        ((path != null && path.equals(anotherLink.path)) || path == anotherLink.path) &&
                        line == anotherLink.line) {
                    return true;
                }
            }
            return false;
        }

        /**
         * If the link is clicked, required file is opened in the editor and an 
         * <code>ErrorAnnotation</code> is attached.
         */
        @Override
        public void outputLineAction(OutputEvent ev) {
            FileObject sourceFile = GlobalPathRegistry.getDefault().findResource(path);
            if (sourceFile == null) {
                sourceFile = FileUtil.toFileObject(FileUtil.normalizeFile(new File(path)));
            }
            DataObject dataObject = null;
            if (sourceFile != null) {
                try {
                    dataObject = DataObject.find(sourceFile);
                } catch (DataObjectNotFoundException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            if (dataObject != null) {
                EditorCookie editorCookie
                        = dataObject.getLookup().lookup(EditorCookie.class);
                if (editorCookie == null) {
                    return;
                }
                editorCookie.open();
                Line errorLine;
                try {
                    errorLine = editorCookie.getLineSet().getCurrent(line - 1);
                } catch (IndexOutOfBoundsException ex) {
                    return;
                }
                if (errAnnot != null) {
                    errAnnot.detach();
                }
                String errorMsg = msg;
                if (errorMsg == null || errorMsg.length() == 0) { //NOI18N
                    errorMsg = NbBundle.getMessage(Link.class, "MSG_ExceptionOccurred");
                }
                errAnnot = new ErrorAnnotation(errorMsg);
                errAnnot.attach(errorLine);
                errAnnot.moveToFront();
                errorLine.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
            }
        }

        /**
         * If a link is cleared, error annotation is detached and link cache is 
         * clared.
         */
        @Override
        public void outputLineCleared(OutputEvent ev) {
            if (errAnnot != null) {
                errAnnot.detach();
            }
            if (!links.isEmpty()) {
                links.clear();
            }
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {
        }
    }

    /**
     * Support class for context log line analyzation and for creating links in 
     * the output window.
     */
    public static class AppServerLogSupport extends LogHyperLinkSupport {

        /**
         * Search for class in global class registry and cache known search
         * results.
         * <p/>
         * Search results are stored in prefix tree based structure to speed up
         * text matching as much as possible.
         * <i>Known drawback:</i> Cache does not implement any kind of dirty
         * flags so search won't find newly added classes and if will keep
         * finding removed classes.
         */
        private static class PathAccess {

            /** Particular source file search result to be cached. */
            private static class ClassAccess {

                /** Is source file accessible in NetBeans? */
                boolean accessible;

                /** Source file path from full class name (including package).
                 */
                String path;

                /**
                 * Creates an instance of particular source file search result.
                 * <p/>
                 * @param accessible Is source file accessible in NetBeans?
                 * @param path Source file path from full class name (including
                 * package).
                 */
                ClassAccess(boolean accessible, String path) {
                    this.accessible = accessible;
                    this.path = path;
                }
            }

            /**
             * Event listener for being notified of changes in the set of
             * available paths.
             */
            private static class PathRegistryListener
                implements GlobalPathRegistryListener {

                /**
                 * Called when some paths are added.
                 * <p/>
                 * Only applies to the first copy of a path that is added.
                 * <p/>
                 * @param event An event giving details.
                 */                @Override
                public void pathsAdded(GlobalPathRegistryEvent event) {
                    synchronized (accessCache) {
                        accessCache.clear();
                    }
                }

                /**
                 * Called when some paths are removed.
                 * <p/>
                 * Only applies to the last copy of a path that is removed.
                 * <p/>
                 * @param event An event giving details.
                 */
                @Override
                public void pathsRemoved(GlobalPathRegistryEvent event) {
                    synchronized (accessCache) {
                        accessCache.clear();
                    }
                }
                
            }

            /** Classes search results cache.
             *  <p/>
             *  Shared data structure which is not thread safe. Accessing code
             *  should use locking on this cache instance.
             */
            private static final StringPrefixTree<ClassAccess> accessCache
                    = new StringPrefixTree<ClassAccess>(true);

            /** NetBeans global class path registry. */
            private final GlobalPathRegistry globalPathRegistry;

            /** Payara server installation root. */
            private final String serverRoot;

            /** Payara server application context. */
            private final String appContext;

            /**
             * Creates an instance of global class registry cached search.
             * <p/>
             * @param globalPathRegistry NetBeans global class path registry.
             * @param serverRoot         Payara server installation root.
             * @param appContext         Payara server application context.
             */
            PathAccess(final GlobalPathRegistry globalPathRegistry,
                    final String serverRoot, final String appContext) {
                this.globalPathRegistry = globalPathRegistry;
                this.serverRoot = serverRoot;
                this.appContext = appContext;
                this.globalPathRegistry.addGlobalPathRegistryListener(
                        new PathRegistryListener());
            }

            /**
             * Search for class in global class registry and cache known search
             * results.
             * <p/>
             * Search results are stored in prefix tree based structure
             * to speed up text matching as much as possible.
             * <p/>
             * <i>Known drawback:</i> Cache does not implement any kind of dirty
             * flags so search won't find newly added classes and if will keep
             * finding removed classes.
             * <p/>
             * @param className
             * @return 
             */
            ClassAccess find(String className) {
                ClassAccess result;
                synchronized(accessCache) {
                    result = accessCache.match(className);
                }
                if (result == null) {
                    String path = className.replace('.', '/') + ".java";
                    boolean accessible;
                    if (className.startsWith("org.apache.jsp.")
                            && appContext != null) {
                        String contextPath = appContext.equals("/")
                                ? "/_"                     // hande ROOT context
                                : appContext;
                        path = serverRoot + contextPath + "/" + path;
                        accessible = new File(path).exists();
                    } else {
                        FileObject resource
                                = globalPathRegistry.findResource(path);
                        accessible = resource != null;
                    }
                    synchronized(accessCache) {
                        result = accessCache.match(className);
                        if (result == null) {
                            result = new ClassAccess(accessible, path);
                            accessCache.add(className, result);
                        }
                    }
                }
                return result;
            }
            
        }

        /** Payara server application context. */
        private String context;

        private String prevMessage = null;
        private static final String STANDARD_CONTEXT = "StandardContext["; // NOI18N
        private static final int STANDARD_CONTEXT_LENGTH = STANDARD_CONTEXT.length();

        /** NetBeans global class path registry. */
        private final GlobalPathRegistry globalPathReg;

        /** Support to search for class in global class registry and cache known
         *  search results. */
        private final PathAccess pathAccess;

        public AppServerLogSupport(String catalinaWork, String webAppContext) {
            context = webAppContext;
            globalPathReg = GlobalPathRegistry.getDefault();
            pathAccess = new PathAccess(
                    globalPathReg, catalinaWork, webAppContext);
        }

        public LineInfo analyzeLine(String logLine) {
            String path = null;
            int line = -1;
            String message = null;
            boolean error = false;
            boolean accessible = false;

            logLine = logLine.trim();
            int lineLength = logLine.length();

            // look for unix file links (e.g. /foo/bar.java:51: 'error msg')
            if (lineLength > 0 && '/' == logLine.charAt(0)) {
                error = true;
                int colonIdx = logLine.indexOf(':');
                if (colonIdx > -1) {
                    path = logLine.substring(0, colonIdx);
                    accessible = true;
                    if (lineLength > colonIdx) {
                        int nextColonIdx = logLine.indexOf(':', colonIdx + 1);
                        if (nextColonIdx > -1 && nextColonIdx > colonIdx) {
                            String lineNum = logLine.substring(colonIdx + 1, nextColonIdx);
                            try {
                                line = Integer.valueOf(lineNum).intValue();
                            } catch (NumberFormatException ex) {
                                accessible = true;
                            // ignore it
                            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, nfe);
                            }
                            if (lineLength > nextColonIdx) {
                                message = logLine.substring(nextColonIdx + 1, lineLength);
                            }
                        }
                    }
                }
            } // look for windows file links (e.g. c:\foo\bar.java:51: 'error msg')
            else if (lineLength > 3 && Character.isLetter(logLine.charAt(0)) && (logLine.charAt(1) == ':') && (logLine.charAt(2) == '\\')) {
                error = true;
                int secondColonIdx = logLine.indexOf(':', 2);
                if (secondColonIdx > -1) {
                    path = logLine.substring(0, secondColonIdx);
                    accessible = true;
                    if (lineLength > secondColonIdx) {
                        int thirdColonIdx = logLine.indexOf(':', secondColonIdx + 1);
                        if (thirdColonIdx > -1 && thirdColonIdx > secondColonIdx) {
                            String lineNum = logLine.substring(secondColonIdx + 1, thirdColonIdx);
                            try {
                                line = Integer.valueOf(lineNum).intValue();
                            } catch (NumberFormatException ex) { // ignore it
                                accessible = true;
                            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, nfe);
                            }
                            if (lineLength > thirdColonIdx) {
                                message = logLine.substring(thirdColonIdx + 1, lineLength);
                            }
                        }
                    }
                }
            } // look for stacktrace links (e.g. at java.lang.Thread.run(Thread.java:595)
            //                                 at t.HyperlinkTest$1.run(HyperlinkTest.java:24))
            else if (logLine.startsWith("at ") && lineLength > 3) { // NOI18N
                error = true;
                int parenthIdx = logLine.indexOf('(');
                if (parenthIdx > 2) {
                    String classWithMethod = logLine.substring(3, parenthIdx);
                    int lastDotIdx = classWithMethod.lastIndexOf('.');
                    if (lastDotIdx > -1) {
                        int lastParenthIdx = logLine.lastIndexOf(')');
                        int lastColonIdx = logLine.lastIndexOf(':');
                        if (lastParenthIdx > -1 && lastColonIdx > -1 &&  lastParenthIdx > lastColonIdx) {
                            String lineNum = logLine.substring(lastColonIdx + 1, lastParenthIdx);
                            try {
                                line = Integer.valueOf(lineNum).intValue();
                            } catch (NumberFormatException ex) { // ignore it
                                //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, nfe);
                                error = true;
                            }
                            message = prevMessage;
                        }
                        int firstDolarIdx = classWithMethod.indexOf('$'); // > -1 for inner classes
                        String className = classWithMethod.substring(0, firstDolarIdx > -1 ? firstDolarIdx : lastDotIdx);

                        PathAccess.ClassAccess access
                                = pathAccess.find(className);
                        accessible = access.accessible;
                        path = access.path;
                    }
                }
            } // every other message treat as normal info message
            else {
                prevMessage = logLine;
                // try to get context, if stored
                int stdContextIdx = logLine.indexOf(STANDARD_CONTEXT);
                int lBracketIdx = -1;
                if (stdContextIdx > -1) {
                    lBracketIdx = stdContextIdx + STANDARD_CONTEXT_LENGTH;
                }
                int rBracketIdx = logLine.indexOf(']');
                if (lBracketIdx > -1 && rBracketIdx > -1 && rBracketIdx > lBracketIdx) {
                    context = logLine.substring(lBracketIdx, rBracketIdx);
                }
            }
            return new LineInfo(path, line, message, error, accessible);
        }
    }
}
