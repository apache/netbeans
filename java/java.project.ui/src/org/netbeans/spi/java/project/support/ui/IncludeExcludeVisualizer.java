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

package org.netbeans.spi.java.project.support.ui;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.spi.project.support.ant.PathMatcher;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * Utility permitting a user to easily see the effect of changing include
 * and exclude patterns on a source group (or several).
 * Intended for use in project creation wizards and project properties dialogs.
 * The exact appearance of the panel is not specified but it should
 * permit the user to see, and edit, the current set of includes and excludes;
 * and display the set of files included and excluded by the current pattern.
 * @see PathMatcher
 * @since org.netbeans.modules.java.project/1 1.12
 * @author Jesse Glick
 */
public class IncludeExcludeVisualizer {

    private File[] roots = {};
    private String includes = "**"; // NOI18N
    private String excludes = ""; // NOI18N
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>(1);
    private IncludeExcludeVisualizerPanel panel;
    private SortedSet<File> included = new TreeSet<File>();
    private SortedSet<File> excluded = new TreeSet<File>();
    private boolean busy = false;
    private boolean interrupted = false;
    private static final RequestProcessor RP = new RequestProcessor(IncludeExcludeVisualizer.class.getName());
    private final RequestProcessor.Task task = RP.create(new RecalculateTask());

    /**
     * Create a new visualizer.
     * Initially has no roots and includes anything (equivalent to
     * an include pattern of <code>**</code> and an empty exclude pattern).
     */
    public IncludeExcludeVisualizer() {}

    /**
     * Configure a set of root directories to which the includes and excludes apply.
     * @param roots a set of valid root directories to search
     * @throws IllegalArgumentException if roots contains a non-directory or 
     *  directory does not exist
     */
    public synchronized void setRoots(File[] roots) throws IllegalArgumentException {
        Parameters.notNull("roots", roots);
        for (File root : roots) {
            if (!root.isDirectory()) {
                throw new IllegalArgumentException(root.getAbsolutePath());
            }
        }
        this.roots = roots;
        recalculate();
    }

    /**
     * Get the current include pattern.
     * @return the current pattern (never null)
     */
    public synchronized String getIncludePattern() {
        return includes;
    }

    /**
     * Set the include pattern.
     * This does not fire a change event.
     * @param pattern the new pattern (never null)
     */
    public synchronized void setIncludePattern(String pattern) {
        Parameters.notNull("pattern", pattern);
        includes = pattern;
        updateIncludesExcludes();
        recalculate();
    }

    /**
     * Get the current exclude pattern.
     * @return the current pattern (never null)
     */
    public synchronized String getExcludePattern() {
        return excludes;
    }

    /**
     * Set the exclude pattern.
     * This does not fire a change event.
     * @param pattern the new pattern (never null)
     */
    public synchronized void setExcludePattern(String pattern) {
        Parameters.notNull("pattern", pattern);
        excludes = pattern;
        updateIncludesExcludes();
        recalculate();
    }

    private synchronized void updateIncludesExcludes() {
        if (panel != null) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    panel.setFields(includes, excludes);
                }
            });
        }
    }

    /**
     * Add a listener to changes made by the user in the includes or excludes.
     * @param l the listener
     */
    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    /**
     * Remove a change listener.
     * @param l the listener
     */
    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    /**
     * Can be called from IncludeExcludeVisualizerPanel.
     */
    synchronized void changedPatterns(String includes, String excludes) {
        this.includes = includes;
        this.excludes = excludes;
        recalculate();
        fireChange();
    }

    private synchronized void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(e);
        }
    }

    /**
     * Get the associated visual panel.
     * @return a panel displaying this include and exclude information
     * @throws IllegalThreadStateException if not called in the event thread
     */
    public synchronized JComponent getVisualizerPanel() {
        if (!EventQueue.isDispatchThread()) {
            throw new IllegalThreadStateException("must be called in EQ");
        }
        if (panel == null) {
            panel = new IncludeExcludeVisualizerPanel(this);
            panel.setFields(includes, excludes);
            updatePanelFiles();
        }
        return panel;
    }

    private void updatePanelFiles() {
        panel.setFiles(included.toArray(new File[0]), excluded.toArray(new File[0]), busy, roots.length == 1 ? roots[0] : null);
    }

    private static final int DELAY = 200;
    private synchronized void recalculate() {
        interrupted = true;
        task.schedule(DELAY);
    }

    private void updateFiles() {
        assert Thread.holdsLock(this);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                synchronized (IncludeExcludeVisualizer.this) {
                    if (panel != null) {
                        updatePanelFiles();
                    }
                }
            }
        });
    }

    private int scanCounter;
    private static final int GRANULARITY = 1000;
    private void scan(File d, String prefix, PathMatcher matcher) {
        String[] children = d.list();
        if (children == null) {
            return;
        }
        for (String child : children) {
            File f = new File(d, child);
            if (!VisibilityQuery.getDefault().isVisible(f)) {
                continue;
            }
            boolean dir = f.isDirectory();
            if (dir) {
                scan(f, prefix + child + "/", matcher); // NOI18N
            } else {
                synchronized (this) {
                    if (interrupted) {
                        return;
                    }
                    if (matcher.matches(prefix + child, false)) {
                        included.add(f);
                    } else {
                        excluded.add(f);
                    }
                    if (++scanCounter % GRANULARITY == 0) {
                        updateFiles();
                    }
                }
            }
        }
    }

    private final class RecalculateTask implements Runnable {


        public void run() {
            File[] _roots;
            String _includes, _excludes;
            synchronized (IncludeExcludeVisualizer.this) {
                busy = true;
                included.clear();
                excluded.clear();
                _roots = roots.clone();
                _includes = includes;
                _excludes = excludes;
                interrupted = false;
                updateFiles();
            }
            PathMatcher matcher = new PathMatcher(_includes, _excludes, null);
            for (File root : _roots) {
                scan(root, "", matcher);
            }
            synchronized (IncludeExcludeVisualizer.this) {
                busy = false;
                updateFiles();
            }
        }

    }

}
