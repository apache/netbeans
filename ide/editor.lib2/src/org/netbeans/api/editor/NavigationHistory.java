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

package org.netbeans.api.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.modules.editor.lib2.URLMapper;
import org.netbeans.modules.editor.lib2.WeakPositions;

/**
 *                       0    1    2    3    4    waypoints.size()
 *                     | W1 | W2 | W3 | W4 | W5 |
 *                                 ^
 *                                 |
 * previous waypoints &lt;-     pointer      -&gt; next Waypoints
 *                          current waypoint
 * 
 * getPreviousWaypoints() == { W1, W2, W3 }
 * getNextWaypoints() == { W4, W5 }
 * navigateBack() == W3, moves pointer one position left
 * navigateForward() == W4, moves pointer one position right
 * 
 * @since 1.74
 * @author Vita Stejskal
 */
public final class NavigationHistory {

    public static final String PROP_WAYPOINTS = "NavigationHHistory.PROP_WAYPOINTS"; //NOI18N

    /**
     * @return list of waypoints where user navigated.
     */
    public static NavigationHistory getNavigations() {
        return get("navigation-history"); //NOI18N
    }

    /**
     * @return list of waypoints where user edited content.
     */
    public static NavigationHistory getEdits() {
        return get("last-edit-history"); //NOI18N
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        PCS.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        PCS.removePropertyChangeListener(l);
    }
    
    /**
     * Create and add new waypoint to navigation history
     * @param offset A valid ofset inside the component's document or -1 if the
     *   offset is unspecified.
     */
    public Waypoint markWaypoint(JTextComponent comp, int offset, boolean currentPosition, boolean append) throws BadLocationException {
        assert comp != null : "The comp parameter must not be null"; //NOI18N
        if (comp.getClientProperty("AsTextField") != null) {
            return null;
        }
        Waypoint newWpt = null;
                
        synchronized (LOCK) {
            // Get the current position
            Position pos = offset == -1 ? null : WeakPositions.get(comp.getDocument(), offset);
            
            // Remove all next waypoints and the current waypoint
            if (!append) {
                while (waypoints.size() > pointer) {
                    Waypoint wpt = waypoints.remove(waypoints.size() - 1);
                    wpt.dispose();
                }
            }
            
            // compare the new position with the current waypoint
            if (waypoints.size() > 0) {
                Waypoint wpt = waypoints.get(waypoints.size() - 1);
                JTextComponent wptComp = wpt.getComponent();
                int wptOffset = wpt.getOffset();
                if (wptComp != null && wptComp.equals(comp) && wptOffset == offset) {
                    // Current waypoint has the same position, do not add anything
                    newWpt = wpt;
                }
            }

            if (newWpt == null) {
                // Add the new Waypoint
                newWpt = new Waypoint(this, comp, pos);
                int rawIndex = waypoints.addEx(newWpt);
                newWpt.initRawIndex(rawIndex);
            }
            
            // Update the pointer
            if (currentPosition) {
                pointer = waypoints.size() - 1;
            } else {
                pointer = waypoints.size();
            }
            
            // Reset the cache
            sublistsCache = null;
        }
        
        PCS.firePropertyChange(PROP_WAYPOINTS, null, null);
        
        return newWpt;
    }

    /**
     * @return waypoint under pointer
     */
    public Waypoint getCurrentWaypoint() {
        synchronized (LOCK) {
            if (pointer < waypoints.size()) {
                return waypoints.get(pointer);
            } else {
                return null;
            }
        }
    }

    /**
     * Is pointer showing other than first waypoint entry
     */
    public boolean hasPreviousWaypoints() {
        synchronized (LOCK) {
            return pointer > 0;
        }
    }

    /**
     * Is pointer showing other than last waypoint entry
     */
    public boolean hasNextWaypoints() {
        synchronized (LOCK) {
            return pointer + 1 < waypoints.size();
        }
    }

    /**
     * @return all waypoints before waypoint under pointer
     */
    public List<Waypoint> getPreviousWaypoints() {
        synchronized (LOCK) {
            if (hasPreviousWaypoints()) {
                return getSublistsCache().subList(0, pointer);
            } else {
                return Collections.<Waypoint>emptyList();
            }
        }
    }

    /**
     * @return all waypoints after waypoint under pointer
     */
    public List<Waypoint> getNextWaypoints() {
        synchronized (LOCK) {
            if (hasNextWaypoints()) {
                return getSublistsCache().subList(pointer + 1, waypoints.size());
            } else {
                return Collections.<Waypoint>emptyList();
            }
        }
    }

    /**
     * Change selected waypoint by moving pointer to previous waypoint
     */
    public Waypoint navigateBack() {
        Waypoint waypoint = null;
        
        synchronized (LOCK) {
            if (hasPreviousWaypoints()) {
                pointer--;
                waypoint = waypoints.get(pointer);
            }
        }

        if (waypoint != null) {
            PCS.firePropertyChange(PROP_WAYPOINTS, null, null);
        }
        
        return waypoint;
    }

    /**
     * Change selected waypoint by moving pointer to next waypoint
     */
    public Waypoint navigateForward() {
        Waypoint waypoint = null;
        
        synchronized (LOCK) {
            if (hasNextWaypoints()) {
                pointer++;
                waypoint = waypoints.get(pointer);
            }
        }
        
        if (waypoint != null) {
            PCS.firePropertyChange(PROP_WAYPOINTS, null, null);
        }
        
        return waypoint;
    }

    /**
     * Change selected waypoint by moving pointer to waypoint in parameter
     */
    public Waypoint navigateTo(Waypoint waypoint) {
        assert waypoint != null : "The waypoint parameter must not be null"; //NOI18N
        
        synchronized (LOCK) {
            int rawIndex = waypoint.getRawIndex();
            if (rawIndex == -1) {
                // invalid waypoint
                waypoint = null;
            } else {
                int wptPointer = waypoints.getIndex(rawIndex);
                if (pointer != wptPointer) {
                    // Move to the waypoint
                    pointer = wptPointer;
                } else {
                    // We are already there no need for navigation
                    waypoint = null;
                }
            }
        }
        
        if (waypoint != null) {
            PCS.firePropertyChange(PROP_WAYPOINTS, null, null);
        }
        
        return waypoint;
    }

    /**
     * Change selected waypoint by moving pointer to first waypoint
     */
    public Waypoint navigateFirst() {
        Waypoint waypoint = null;
        
        synchronized (LOCK) {
            if (waypoints.size() > 0) {
                pointer = 0;
                waypoint = waypoints.get(pointer);
            }
        }

        if (waypoint != null) {
            PCS.firePropertyChange(PROP_WAYPOINTS, null, null);
        }
        
        return waypoint;
    }

    /**
     * Change selected waypoint by moving pointer to last waypoint
     */
    public Waypoint navigateLast() {
        Waypoint waypoint = null;
        
        synchronized (LOCK) {
            if (waypoints.size() > 0) {
                pointer = waypoints.size() - 1;
                waypoint = waypoints.get(pointer);
            }
        }
        
        if (waypoint != null) {
            PCS.firePropertyChange(PROP_WAYPOINTS, null, null);
        }
        
        return waypoint;
    }

    /*
     * @since 1.74
     */
    public static final class Waypoint {

        private final NavigationHistory navigationHistory;
        private Reference<JTextComponent> compRef;
        private Position pos;
        private URL url;
        
        private int rawIndex = -2;
        
        private Waypoint(NavigationHistory nh, JTextComponent comp, Position pos) throws BadLocationException {
            this.navigationHistory = nh;
            this.compRef = new WeakReference<JTextComponent>(comp);
            this.pos = pos;
            this.url = URLMapper.findUrl(comp);
            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(navigationHistory.id + ": waypoint added: " + getUrl()); //NOI18N
            }
        }
        
        public URL getUrl() {
            synchronized (navigationHistory.LOCK) {
                return url;
            }
        }
        
        public JTextComponent getComponent() {
            synchronized (navigationHistory.LOCK) {
                return compRef == null ? null : compRef.get();
            }
        }
        
        public int getOffset() {
            synchronized (navigationHistory.LOCK) {
                return pos == null ? -1 : pos.getOffset();
            }
        }

        // the following methods are called under the getDefault().LOCK
        
        private int getRawIndex() {
            return rawIndex;
        }
        
        private void initRawIndex(int rawIndex) {
            assert this.rawIndex == -2 : "Can't call initRawIndex more than once."; //NOI18N
            this.rawIndex = rawIndex;
        }
        
        private void dispose() {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(navigationHistory.id + ": waypoint disposed: " + getUrl()); //NOI18N
            }
            
            this.rawIndex = -1;
            this.url = null;
            this.compRef = null;
            this.pos = null;
        }
    } // End of Waypoint class

    // ----------------------------------------------
    // Private implementation
    // ----------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(NavigationHistory.class.getName());
    
    private static final Map<String, NavigationHistory> instances = new HashMap<String, NavigationHistory>();

    private final String id;
    private final String LOCK = new String("NavigationHistory.LOCK"); //NOI18N
    private final RingBuffer<Waypoint> waypoints = new RingBuffer<Waypoint>(new Waypoint [50]);
    private int pointer = 0;
    private List<Waypoint> sublistsCache = null;
    
    private final PropertyChangeSupport PCS = new PropertyChangeSupport(this);

    private static NavigationHistory get(String id) {
        synchronized (instances) {
            NavigationHistory nh = instances.get(id);

            if (nh == null) {
                nh = new NavigationHistory(id);
                instances.put(id, nh);
            }

            return nh;
        }
    }
    
    private NavigationHistory(String id) {
        this.id = id;
    }
    
    private List<Waypoint> getSublistsCache() {
        if (sublistsCache == null) {
            sublistsCache = Collections.unmodifiableList(new ArrayList<Waypoint>(waypoints));
        }
        return sublistsCache;
    }
    
    private static final class RingBuffer<E> extends AbstractList<E> {

        private final E [] buffer;
        private int head = 0;
        private int tail = 0;
        
        public RingBuffer(E [] buffer) {
            assert buffer != null : "The buffer parameter must not be null"; //NOI18N
            assert buffer.length >= 2 : "The buffer size must be at least 2."; //NOI18N
            
            this.buffer = buffer;
        }

        @Override
        public E set(int index, E element) {
            int rawIndex = getRawIndex(index);
            E old = buffer[rawIndex];
            buffer[rawIndex] = element;
            return old;
        }

        @Override
        public void add(int index, E element) {
            int rawIndex = (head + index) % buffer.length;
            if (rawIndex == tail) {
                addEx(element);
            } else {
                throw new UnsupportedOperationException("This ring buffer only allows adding to the end of the buffer."); //NOI18N
            }
        }
        
        public int addEx(E element) {
            int rawIndex = tail;
            buffer[rawIndex] = element;
            
            tail = (tail + 1) % buffer.length;
            if (tail == head) {
                // XXX: hack, not very nice
                if (buffer[head] instanceof Waypoint) {
                    ((Waypoint) buffer[head]).dispose();
                }
                buffer[head] = null;
                head = (head + 1) % buffer.length;
            }
            
            return rawIndex;
        }

        @Override
        public E remove(int index) {
            int rawIndex = getRawIndex(index);
            
            if (rawIndex == head) {
                head = (head + 1) % buffer.length;
            } else {
                int tailMinusOne = (tail - 1 + buffer.length) % buffer.length;
                if (rawIndex == tailMinusOne) {
                    tail = tailMinusOne;
                } else {
                    throw new UnsupportedOperationException("This ring buffer only allows removing at the beginning or end of the buffer."); //NOI18N
                }
            }
            
            E old = buffer[rawIndex];
            buffer[rawIndex] = null;
            
            return old;
        }

        @Override
        public E get(int index) {
            return buffer[getRawIndex(index)];
        }

        @Override
        public int size() {
            return (tail - head + buffer.length) % buffer.length;
        }

        private int getRawIndex(int index) {
            if (index >= 0 && index < size()) {
                return (head + index) % buffer.length;
            } else {
                throw new IndexOutOfBoundsException("Index = " + index + ", size = " + size());
            }
        }
        
        public int getIndex(int rawIndex) {
            boolean valid;
            
            if (tail < head) {
                valid = (rawIndex >= 0 && rawIndex < tail) || (rawIndex >= head && rawIndex < buffer.length);
            } else {
                valid = rawIndex >= head && rawIndex < tail;
            }
            
            if (valid) {
                return (rawIndex - head + buffer.length) % buffer.length;
            } else {
                throw new IndexOutOfBoundsException("Invalid raw index. RawIndex = " + rawIndex + ", head = " + head + ", tail = " + tail);
            }
        }
    } // End of RingBuffer class
    
}
