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

package org.openide.filesystems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbCollections;
import org.openide.util.TopologicalSortException;
import org.openide.util.BaseUtilities;

/**
 * Implements folder ordering logic in {@link FileUtil}.
 */
class Ordering {

    private Ordering() {}

    private static final Logger LOG = Logger.getLogger(Ordering.class.getName());
    private static final String ATTR_POSITION = "position"; // NOI18N

    static List<FileObject> getOrder(Collection<FileObject> children, final boolean logWarnings) throws IllegalArgumentException {
        LOG.log(Level.FINE, "getOrder for {0}", children); // NOI18N
        Iterator<FileObject> it = children.iterator();
        if (!it.hasNext()) {
            return Collections.emptyList();
        }
        int unordered = 0;
        int ordered = 0;
        Map<String,FileObject> childrenByName = new HashMap<String,FileObject>();
        class ChildAndPosition implements Comparable<ChildAndPosition> {
            ChildAndPosition(FileObject child, Number position) {
                this.child = child;
                this.position = position;
            }
            final FileObject child;
            private final Number position;
            @Override
            public int compareTo(ChildAndPosition o) {
                int res;
                if (position instanceof Float || position instanceof Double || o.position instanceof Float || o.position instanceof Double) {
                    res = Double.compare(position.doubleValue(), o.position.doubleValue());
                } else {
                    long v1 = position.longValue();
                    long v2 = o.position.longValue();
                    res = v1 < v2 ? -1 : (v1 == v2 ? 0 : 1);
                }
                if (res != 0) {
                    return res;
                } else {
                    if (logWarnings && o != this && !position.equals(0)) {
                        LOG.log(Level.WARNING, "Found same position {0} for both {1} and {2}", new Object[]{position, o.child.getPath(), child.getPath()});
                    }
                    return child.getNameExt().compareTo(o.child.getNameExt());
                }
            }
        }
        SortedSet<ChildAndPosition> childrenByPosition = new TreeSet<ChildAndPosition>();
        FileObject parent = null;
        while (it.hasNext()) {
            FileObject child = it.next();
            if (childrenByName.put(child.getNameExt(), child) != null) {
                throw new IllegalArgumentException("Duplicate in children list: " + child.getPath() + "\nChildren: " + children); // NOI18N
            }
            Object pos = child.getAttribute(ATTR_POSITION);
            if (pos instanceof Number) {
                childrenByPosition.add(new ChildAndPosition(child, (Number) pos));
                ordered |= child.isFolder() ? 1 : 2;
            } else if (logWarnings && pos != null) {
                LOG.log(Level.WARNING, "Encountered nonnumeric position attribute {0} of {1} for {2}\nChildren: {3}", new Object[]{pos, pos.getClass(), child.getPath(), children});
            }
            if (parent == null) {
                parent = child.getParent();
            } else {
                if (child.getParent() != parent) {
                    throw new IllegalArgumentException("All children must have the same parent: " + child.getParent().getPath() + " vs. " + parent.getPath() + "\nChildren: " + children); // NOI18N
                }
            }
            if (pos == null) {
                unordered |= child.isFolder() ? 1 : 2;
            }
        }
        Map<FileObject,Set<FileObject>> edges = new HashMap<FileObject,Set<FileObject>>();
        for (String attr : NbCollections.iterable(parent.getAttributes())) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "  attr found {0}({1})", new Object[]{parent, attr}); // NOI18N
            }
            int slash = attr.indexOf('/');
            if (slash == -1) {
                continue;
            }
            Object val = parent.getAttribute(attr);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "  reading attribute {0}({1}) -> {2}", new Object[] {parent, attr, val}); // NOI18N
            }
            if (!Boolean.TRUE.equals(val)) {
                if (logWarnings && /* somehow this can happen, not sure how... */ val != null && !(val instanceof Boolean)) {
                    LOG.log(Level.WARNING, "Encountered non-boolean relative ordering attribute {0} from {1} on {2}", new Object[]{val, attr, parent.getPath()});
                }
                continue;
            }
            FileObject f1 = childrenByName.get(attr.substring(0, slash));
            FileObject f2 = childrenByName.get(attr.substring(slash + 1));
            if (f1 != null && f2 != null) {
                Set<FileObject> edge = edges.get(f1);
                if (edge == null) {
                    edges.put(f1, edge = new HashSet<FileObject>());
                }
                edge.add(f2);
                if (logWarnings) {
                    LOG.log(
                        Level.WARNING, "Relative ordering attribute {0} on {1} is deprecated in favor of numeric position attributes",
                        new Object[]{attr, parent.getPath()}
                    );
                }
            } else if (logWarnings) {
                LOG.log(
                    Level.WARNING, "Could not find both sides of relative ordering attribute {0} on {1}",
                    new Object[]{attr, parent.getPath()}
                );
            }
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "  no more attribs {0}", parent); // NOI18N
        }
        Iterator<ChildAndPosition> it2 = childrenByPosition.iterator();
        if (it2.hasNext()) {
            FileObject previousChild = it2.next().child;
            while (it2.hasNext()) {
                FileObject subsequentChild = it2.next().child;
                Set<FileObject> edge = edges.get(previousChild);
                if (edge == null) {
                    edges.put(previousChild, edge = new HashSet<FileObject>());
                }
                edge.add(subsequentChild);
                previousChild = subsequentChild;
            }
        }
        boolean shouldLog = logWarnings && !childrenByPosition.isEmpty() && childrenByPosition.size() < children.size();
        if (shouldLog) {
            // exceptions:  See Bugzilla#201893 
            if (parent.getPath().matches("Projects/.+/Lookup")) {
                // do not report any order issues for Projects/Lookup, even between files, or between folders.
                shouldLog = false;
            } else if (FileUtil.isParentOf(FileUtil.getConfigRoot(), parent) && ((ordered & unordered) == 0)) {
                // do not report folder / file mismatch on configfs, but DO report mismatch inside folder/file category.
                shouldLog = false;
            }
        }
        if (shouldLog) {
            List<FileObject> missingPositions = new ArrayList<FileObject>(children);
            for (ChildAndPosition cap : childrenByPosition) {
                missingPositions.remove(cap.child);
            }
            for (Iterator<FileObject> mis = missingPositions.iterator(); mis.hasNext();) {
                FileObject fileObject = mis.next();
                if (fileObject.getExt().endsWith("_hidden")) { // NOI18N
                    mis.remove();
                }
            }
            IGNORE_ERGO: if (!missingPositions.isEmpty()) {
                List<String> missingNames = new ArrayList<String>(missingPositions.size());
                for (FileObject f : missingPositions) {
                    final String n = f.getNameExt();
                    if (n.contains("ergonomics")) { // NOI18N
                        break IGNORE_ERGO;
                    }
                    missingNames.add(n);
                }
                List<String> presentNames = new ArrayList<String>(childrenByPosition.size());
                for (ChildAndPosition cap : childrenByPosition) {
                    final String n = cap.child.getNameExt();
                    if (n.contains("ergonomics")) { // NOI18N
                        break IGNORE_ERGO;
                    }
                    presentNames.add(n + "@" + cap.position);
                }
                LOG.log(
                    Level.WARNING, "Not all children in {0}/ marked with the position attribute: {1}, but some are: {2}",
                    new Object[]{parent.getPath(), missingNames, presentNames}
                );
            }
        }
        if (edges.isEmpty()) {
            // Shortcut.
            return new ArrayList<FileObject>(children);
        } else {
            try {
                return BaseUtilities.topologicalSort(children, edges);
            } catch (TopologicalSortException x) {
                if (logWarnings) {
                    LOG.log(Level.WARNING, "Contradictory partial ordering in " + parent.getPath(), x);
                }
                return NbCollections.checkedListByCopy(x.partialSort(), FileObject.class, true);
            }
        }
    }

    static void setOrder(List<FileObject> children) throws IllegalArgumentException, IOException {
        boolean oneNewChild = false;
        boolean fullySpecified = true;
        for (FileObject f : children) {
            if (findPosition(f) == null) {
                fullySpecified = false;
                break;
            }
        }
        FileObject toBeMoved = null, before = null, after = null;
        if (fullySpecified) {
            List<FileObject> oldOrder = getOrder(children, false);
            if (children.equals(oldOrder)) {
                // Nothing to do.
                return;
            }
            // First check to see if the change can be represented as a single move.
            // If so, we prefer to change just one position attribute.
            int length = children.size();
            int start = 0;
            while (start < length && children.get(start).equals(oldOrder.get(start))) {
                start++;
            }
            int end = length - 1;
            while (end >= 0 && children.get(end).equals(oldOrder.get(end))) {
                end--;
            }
            int rangeLength = end - start + 1;
            if (rangeLength > 2) {
                // Check if the permutation within this range is just a rotation by one element.
                if (children.get(end).equals(oldOrder.get(start)) && children.subList(start, end).equals(oldOrder.subList(start + 1, end + 1))) {
                    // Left rotation.
                    toBeMoved = children.get(end);
                    before = children.get(end - 1);
                    after = end + 1 < length ? children.get(end + 1) : null;
                } else if (children.get(start).equals(oldOrder.get(end)) && children.subList(start + 1, end + 1).equals(oldOrder.subList(start, end))) {
                    // Right rotation.
                    toBeMoved = children.get(start);
                    before = start - 1 >= 0 ? children.get(start - 1) : null;
                    after = children.get(start + 1);
                }
            } else if (rangeLength == 2) {
                // Adjacent swap.
                if (start == 0) {
                    // Use space on the left.
                    toBeMoved = children.get(start);
                    after = children.get(end);
                } else if (end == length - 1) {
                    // Use space on the right.
                    toBeMoved = children.get(end);
                    before = children.get(start);
                } else {
                    // Prefer to move the one which is closer to its outside neighbor.
                    Float outLeft = findPosition(children.get(start - 1));
                    Float outRight = findPosition(children.get(end + 1));
                    Float left = findPosition(children.get(start));
                    Float right = findPosition(children.get(end));
                    if (outLeft != null && outRight != null && left != null && right != null) {
                        if (left - outLeft < outRight - right) {
                            toBeMoved = children.get(end);
                            before = children.get(start);
                            after = children.get(end + 1);
                        } else {
                            toBeMoved = children.get(start);
                            before = children.get(start - 1);
                            after = children.get(end);
                        }
                    }
                }
            } else {
                assert rangeLength == 0 : oldOrder + " => " + children;
            }
        } else if (children.size() > 1) {
            // #110981: check to see if just one is new.
            FileObject nue = null;
            for (FileObject f : children) {
                if (findPosition(f) == null) {
                    if (nue == null) {
                        nue = f;
                    } else {
                        // More than one, skip.
                        nue = null;
                        break;
                    }
                }
            }
            if (nue != null) {
                oneNewChild = true;
                toBeMoved = nue;
                int idx = children.indexOf(nue);
                before = (idx == 0) ? null : children.get(idx - 1);
                after = (idx == children.size() - 1) ? null : children.get(idx + 1);
            }
        }
        if (toBeMoved != null) {
            // Do the swap.
            if (before == null) {
                toBeMoved.setAttribute(ATTR_POSITION, Math.round(findPosition(after) - 100));
            } else if (after == null) {
                toBeMoved.setAttribute(ATTR_POSITION, Math.round(findPosition(before) + 100));
            } else {
                Float beforePos = findPosition(before);
                Float afterPos = findPosition(after);
                int proposed = Math.round(beforePos + afterPos) / 2;
                if (beforePos < proposed && proposed < afterPos) {
                    toBeMoved.setAttribute(ATTR_POSITION, proposed);
                } else {
                    toBeMoved = null; // #115343
                }
            }
        }
        if (toBeMoved == null) {
            // More complex rearrangement. Fall back to a crude but correct behavior.
            int pos = 100;
            for (FileObject f : children) {
                f.setAttribute(ATTR_POSITION, pos);
                pos += 100;
            }
        }
        if(oneNewChild && toBeMoved != null) {
            // #131021 - If just one new child added and its position attr is set,
            // call setOrder again to potentially reorder others.
            setOrder(children);
            return;
        }
        // Kill off any old relative ordering attributes.
        FileObject d = children.get(0).getParent();
        for (String attr : NbCollections.iterable(d.getAttributes())) {
            if (attr.indexOf('/') != -1 && d.getAttribute(attr) instanceof Boolean) {
                d.setAttribute(attr, null);
            }
        }
        boolean asserts = false;
        assert asserts = true;
        if (asserts) {
            List<FileObject> actual = getOrder(children, false);
            assert actual.equals(children) : "setOrder(" + children + ") -> " + actual;
        }
    }

    private static Float findPosition(FileObject f) {
        Object o = f.getAttribute(ATTR_POSITION);
        if (o instanceof Number) {
            return ((Number) o).floatValue();
        } else {
            return null;
        }
    }

    static boolean affectsOrder(FileAttributeEvent event) {
        String name = event.getName();
        if (name == null) {
            // Unknown attrs changed. Conservatively assume it might affect order.
            return true;
        }
        return name.equals(ATTR_POSITION) || (event.getFile().isFolder() && name.indexOf('/') != -1);
    }

}
