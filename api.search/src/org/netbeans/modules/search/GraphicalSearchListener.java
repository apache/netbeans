/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.actions.Savable;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.search.ui.FileObjectPropertySet;
import org.netbeans.modules.search.ui.UiUtils;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.netbeans.spi.search.provider.SearchComposition;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

/**
 *
 * @author jhavlin
 */
class GraphicalSearchListener extends SearchListener {

    private static final int INFO_EVENT_LIMIT = 100;
    private static final Logger LOG = Logger.getLogger(
            GraphicalSearchListener.class.getName());

    /**
     * Limit for length of path shown in the progress bar.
     */
    private static final int PATH_LENGTH_LIMIT = 153;
    /**
     * Underlying search composition.
     */
    private SearchComposition<?> searchComposition;
    /**
     * Progress handle instance.
     */
    private ProgressHandle progressHandle;
    /**
     * String in the middle of long text, usually three dots (...).
     */
    private String longTextMiddle = null;
    
    private ResultViewPanel resultViewPanel;

    private RootInfoNode rootInfoNode;
    private EventChildren eventChildren;

    public GraphicalSearchListener(SearchComposition<?> searchComposition,
            ResultViewPanel resultViewPanel) {
        this.searchComposition = searchComposition;
        this.resultViewPanel = resultViewPanel;
        this.rootInfoNode = new RootInfoNode();
    }

    public void searchStarted() {

        progressHandle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(ResultView.class,
                "TEXT_SEARCHING___"), new Cancellable() {               //NOI18N

            @Override
            public boolean cancel() {
                searchComposition.terminate();
                return true;
            }
        });
        progressHandle.start();
        resultViewPanel.searchStarted();
        searchComposition.getSearchResultsDisplayer().searchStarted();
        Collection<? extends Savable> unsaved =
                Savable.REGISTRY.lookupAll(Savable.class);
        if (unsaved.size() > 0) {
            String msg = NbBundle.getMessage(ResultView.class,
                    "TEXT_INFO_WARNING_UNSAVED");
            eventChildren.addEvent(new EventNode(EventType.WARNING, msg));
        }
    }

    public void searchFinished() {
        if (progressHandle != null) {
            progressHandle.finish();
            progressHandle = null;
        }
        resultViewPanel.searchFinished();
        searchComposition.getSearchResultsDisplayer().searchFinished();
    }

    @Override
    public void directoryEntered(String path) {
        if (progressHandle != null) {
            progressHandle.progress(shortenPath(path));
        }
    }

    @Override
    public void fileContentMatchingStarted(String fileName) {
        if (progressHandle != null) {
            progressHandle.progress(shortenPath(fileName));
        }
    }

    /**
     * Shorten long part string
     */
    private String shortenPath(String p) {
        if (p.length() <= PATH_LENGTH_LIMIT) {
            return p;
        } else {
            String mid = getLongTextMiddle();
            int halfLength = (PATH_LENGTH_LIMIT - mid.length()) / 2;
            return p.substring(0, halfLength) + mid
                    + p.substring(p.length() - halfLength);
        }
    }

    /**
     * Get text replacement for middle part of long strings.
     */
    private String getLongTextMiddle() {
        if (longTextMiddle == null) {
            longTextMiddle = NbBundle.getMessage(SearchTask.class,
                    "TEXT_SEARCH_LONG_STRING_MIDDLE");                  //NOI18N
        }
        return longTextMiddle;
    }

    @Override
    public void generalError(Throwable t) {
        String msg = NbBundle.getMessage(ResultView.class,
                "TEXT_INFO_ERROR", t.getMessage());                     //NOI18N
        eventChildren.addEvent(new EventNode(EventType.ERROR, msg));
        LOG.log(Level.INFO, t.getMessage(), t);
    }

    @Override
    public void fileContentMatchingError(String path, Throwable t) {
        String msg = NbBundle.getMessage(ResultView.class,
                "TEXT_INFO_ERROR_MATCHING", fileName(path), //NOI18N
                t.getMessage());
        String tooltip = NbBundle.getMessage(ResultView.class,
                "TEXT_INFO_ERROR_MATCHING", path, //NOI18N
                t.getMessage());
        eventChildren.addEvent(new PathEventNode(EventType.ERROR, msg, path,
                tooltip));
        String logMsg = path + ": " + t.getMessage();                   //NOI18N
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, logMsg, t);
        } else {
            LOG.log(Level.INFO, logMsg);
        }
    }

    /**
     * Extract file name from file path.
     */
    private String fileName(String filePath) {
        Pattern p = Pattern.compile("(/|\\\\)([^/\\\\]+)(/|\\\\)?$");   //NOI18N
        Matcher m = p.matcher(filePath);
        if (m.find()) {
            return m.group(2);
        } else {
            return filePath;
        }
    }

    @Override
    public void fileSkipped(FileObject fileObject,
            SearchFilterDefinition filter, String message) {
        fileSkipped(fileObject.toURI(), filter, message);
    }

    @Override
    public void fileSkipped(URI uri, SearchFilterDefinition filter,
            String message) {
        LOG.log(Level.FINE, "{0} skipped {1} {2}", new Object[]{ //NOI18N
                    uri.toString(),
                    filter != null ? filter.getClass().getName() : "", //NOI18N
                    message != null ? message : ""});                   //NOI18N
    }

    public Node getInfoNode() {
        return rootInfoNode;
    }

    private class RootInfoNode extends AbstractNode {

        public RootInfoNode() {
            this(new EventChildren());
        }

        private RootInfoNode(EventChildren eventChildren) {

            super(eventChildren);
            GraphicalSearchListener.this.eventChildren = eventChildren;
            setDisplayName(UiUtils.getText("TEXT_INFO_TITLE"));           //TODO
            setIcon(EventType.INFO);
        }

        public final void setIcon(EventType mostSeriousEventType) {
            setIconBaseWithExtension(getIconForEventType(mostSeriousEventType));
        }

        public void fireUpdate() {
        }
    }

    private enum EventType {

        INFO(1), WARNING(2), ERROR(3);
        private int badness;

        private EventType(int badness) {
            this.badness = badness;
        }

        public boolean worseThan(EventType eventType) {
            return this.badness > eventType.badness;
        }
    }

    private class EventChildren extends Children.Keys<EventNode> {

        private List<EventNode> events = new ArrayList<EventNode>();
        EventType worstType = EventType.INFO;

        public synchronized void addEvent(EventNode event) {
            if (events.size() < INFO_EVENT_LIMIT) {
                this.events.add(event);
                setKeys(events);
                if (event.getType().worseThan(worstType)) {
                    worstType = event.getType();
                }
                rootInfoNode.setIconBaseWithExtension(
                        getIconForEventType(worstType));
            } else if (events.size() == INFO_EVENT_LIMIT) {
                this.events.add(new EventNode(EventType.INFO,
                        UiUtils.getText("TEXT_INFO_LIMIT_REACHED")));   //NOI18N
                setKeys(events);
            }
        }

        @Override
        protected Node[] createNodes(EventNode key) {
            return new Node[]{key};
        }
    }

    private static String getIconForEventType(EventType eventType) {

        String iconBase = "org/netbeans/modules/search/res/";           //NOI18N
        String icon;

        switch (eventType) {
            case INFO:
                icon = "info.png";                                      //NOI18N
                break;
            case WARNING:
                icon = "warning.gif";                                   //NOI18N
                break;
            case ERROR:
                icon = "error.gif";                                     //NOI18N
                break;
            default:
                icon = "info.png";                                      //NOI18N
            }
        return iconBase + icon;
    }

    private class FileObjectEventNode extends EventNode {

        private FileObject fileObject;

        public FileObjectEventNode(EventType type, String message,
                FileObject fileObject) {
            super(type, message);
            this.fileObject = fileObject;
        }

        @Override
        public PropertySet[] createPropertySets() {
            PropertySet[] propertySets;
            propertySets = new PropertySet[1];
            propertySets[0] = new FileObjectPropertySet(fileObject);
            return propertySets;
        }
    }

    private class PathEventNode extends EventNode {

        private String path;
        private String tooltip;

        public PathEventNode(EventType type, String message, String path,
                String tooltip) {
            super(type, message);
            this.path = path;
            this.tooltip = tooltip;
        }

        @Override
        public String getShortDescription() {
            return tooltip;
        }

        @Override
        public PropertySet[] createPropertySets() {
            final Property<String> pathProperty = new Property<String>(
                    String.class) {
                @Override
                public boolean canRead() {
                    return true;
                }

                @Override
                public String getValue() throws IllegalAccessException,
                        InvocationTargetException {
                    return path;
                }

                @Override
                public boolean canWrite() {
                    return false;
                }

                @Override
                public void setValue(String val) throws IllegalAccessException,
                        IllegalArgumentException, InvocationTargetException {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String getName() {
                    return "path";                                      //NOI18N
                }
            };
            final Property<?>[] properties = new Property<?>[]{pathProperty};
            PropertySet[] sets = new PropertySet[1];
            sets[0] = new PropertySet() {
                @Override
                public Property<?>[] getProperties() {
                    return properties;
                }
            };
            return sets;
        }
    }

    private class EventNode extends AbstractNode {

        private PropertySet[] propertySets;
        private EventType type;

        public EventNode(EventType type, String message) {
            super(Children.LEAF);
            this.type = type;
            this.setDisplayName(message);
            this.setIconBaseWithExtension(getIconForEventType(type));
        }

        @Override
        public PropertySet[] getPropertySets() {
            if (propertySets == null) {
                propertySets = createPropertySets();
            }
            return propertySets;
        }

        protected PropertySet[] createPropertySets() {
            return new PropertySet[0];
        }

        public EventType getType() {
            return type;
        }
    }
}
