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

package org.netbeans.lib.profiler.ui.threads;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToggleButton;
import org.netbeans.lib.profiler.charts.axis.TimeAxisUtils;
import org.netbeans.lib.profiler.results.threads.ThreadData;
import org.netbeans.lib.profiler.results.threads.ThreadsDataManager;
import org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;

/**
 *
 * @author Jiri Sedlacek
 */
public class ViewManager extends ProfilerTableContainer.ColumnChangeAdapter {
    
    private static ResourceBundle BUNDLE() {
        return ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.threads.Bundle"); // NOI18N
    }
    
    private static final int MIN_TIMEMARK_STEP = 120; // The minimal distance between two time marks
    public static final String PROP_NEW_OFFSET = "newOffset"; // NOI18N
    
    // Zoom value maximum
    private static final int MAX_ZOOM = 5;
    // Minimum part of view covered by data (1/MIN_VIEW)
    private static final int MIN_VIEW = 3;
    
    private final int column;
    
    private final ThreadsDataManager data;
    private final Map<Integer, RowView> rowViews;
    
    private int offset;
    private int width;
    private int prefWidth;
    
    private boolean fit = false;
    private double zoom = 0.03f;
    private double lastZoom = zoom;
    
    private Action zoomInAction;
    private Action zoomOutAction;
    private Action fitAction;
    
    
    public ViewManager(int column, ThreadsDataManager data) {
        this.column = column;
        this.data = data;
        
        updateTimeMarks(true);
        
        rowViews = new HashMap<>();
    }
    
    
    public int zoomIn() {
        return setZoom(zoom * 1.2d);
    }
    
    public int zoomOut() {
        return setZoom(zoom * 0.8d);
    }
    
    public int setZoom(double zoom) {
        int newOffset = offset;
        if (this.zoom != zoom) {
            double oldZoom = this.zoom;
            zoomChanged(this.zoom, zoom);
            if (!isFit()) {
                double tt = (offset + width / 2) / oldZoom;
                newOffset = Math.max((int)(tt * zoom - width / 2), 0);
            }
            updateActions();
        }
        return newOffset;
    }
    
    public double getZoom() {
        return zoom;
    }
    
    public Action zoomInAction() {
        if (zoomInAction == null) zoomInAction = new AbstractAction(null, Icons.getIcon(GeneralIcons.ZOOM_IN)) {
            {
                putValue(Action.SHORT_DESCRIPTION, BUNDLE().getString("ACT_ZoomIn")); // NOI18N
            }
            public void actionPerformed(ActionEvent e) {
                int newOffset = zoomIn();
                Integer _newOffset = newOffset == offset ? null : newOffset;
                zoomInAction.putValue(PROP_NEW_OFFSET, _newOffset);
            }
        };
        return zoomInAction;
    }
    
    public Action zoomOutAction() {
        if (zoomOutAction == null) zoomOutAction = new AbstractAction(null, Icons.getIcon(GeneralIcons.ZOOM_OUT)) {
            {
                putValue(Action.SHORT_DESCRIPTION, BUNDLE().getString("ACT_ZoomOut")); // NOI18N
            }
            public void actionPerformed(ActionEvent e) {
                int newOffset = zoomOut();
                Integer _newOffset = newOffset == offset ? null : newOffset;
                zoomOutAction.putValue(PROP_NEW_OFFSET, _newOffset);
            }
        };
        return zoomOutAction;
    }
    
    public Action fitAction() {
        if (fitAction == null) fitAction = new AbstractAction(null, Icons.getIcon(GeneralIcons.SCALE_TO_FIT)) {
            {
                putValue(Action.SHORT_DESCRIPTION, BUNDLE().getString("ACT_ScaleToFit")); // NOI18N
            }
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if (source instanceof JToggleButton) {
                    fit = ((JToggleButton)source).isSelected();
                    if (fit) lastZoom = zoom;
                    else zoom = lastZoom;
                    updateTimeMarks(true);
                    updateActions();
                }
            }
        };
        return fitAction;
    }
    
    private void updateActions() {
        if (zoomInAction != null) {
            zoomInAction.setEnabled(isFit() ? false : getViewWidth() > 0 && zoom <= MAX_ZOOM);
        }
        if (zoomOutAction != null) {
            zoomOutAction.setEnabled(isFit() ? false : prefWidth >= width / MIN_VIEW);
        }
    }
    
    
    private long getFirstTime() {
        return data.getStartTime();
    }
    
    public int getViewWidth() {
        return (int)(getDataWidth() * zoom);
    }
    
    private int getDataWidth() {
        return (int)(data.getEndTime() - data.getStartTime());
    }
    
    
    public long getFirstTimeMark(boolean _offset) {
        return _offset ? _firstTimeMark : firstTimeMark;
    }
    
    public long getTimeMarksStep() {
        return timeMarksStep;
    }
    
    public String getTimeMarksFormat() {
        return format;
    }
    
    public int getTimePosition(long time, boolean _offset) {
        return !_offset || isFit() ? (int)((time - data.getStartTime()) * zoom) :
                                     (int)((time - data.getStartTime()) * zoom) - offset;
    }
    
    private long firstTimeMark;
    private long _firstTimeMark;
    private long timeMarksStep;
    private String format;
    private void updateTimeMarks(boolean updateStep) {
        if (updateStep) timeMarksStep = TimeAxisUtils.getTimeUnits(zoom, MIN_TIMEMARK_STEP);
        
        long first = data.getStartTime();
        long _first = first + (long)(offset / zoom);
        firstTimeMark = first / timeMarksStep * timeMarksStep + timeMarksStep;
        _firstTimeMark = _first / timeMarksStep * timeMarksStep + timeMarksStep;
        
        long last = first + (long)(width / zoom);
        format = TimeAxisUtils.getFormatString(timeMarksStep, first, last);
    }
    
    
    public RowView getRowView(int row) {
        RowView rowView = rowViews.get(row);
        if (rowView == null) {
            rowView = new RowView(data.getThreadData(row));
            rowViews.put(row, rowView);
        }
        return rowView;
    }
    
    
    public void update() {
        if (isFit()) zoomChanged(zoom, width / (double)getDataWidth());
    }
    
    public void reset() {
        zoom = 0.03f;
        lastZoom = zoom;
        rowViews.clear();
        updateTimeMarks(true);
    }
    
    public void columnOffsetChanged(int column, int oldO, int newO) {
        if (this.column != column) return;
        offset = newO;
        updateTimeMarks(false);
        for (RowView view : rowViews.values()) view.offsetChanged(oldO, newO);
    }
    
    public void columnWidthChanged(int column, int oldW, int newW) {
        if (this.column != column) return;
        width = newW;
        if (!isFit()) for (RowView view : rowViews.values()) view.widthChanged(oldW, newW);
        updateActions();
    }
    
    public void columnPreferredWidthChanged(int column, int oldW, int newW) {
        if (this.column != column) return;
        prefWidth = newW;
        updateTimeMarks(false);
        if (!isFit()) for (RowView view : rowViews.values()) view.preferredWidthChanged(oldW, newW);
        updateActions();
    }
    
    public void zoomChanged(double oldZoom, double newZoom) {
        zoom = newZoom;
        updateTimeMarks(true);
        for (RowView view : rowViews.values()) view.zoomChanged(oldZoom, newZoom);
    }
    
    
    public void setFit(boolean f) {
        fit = f;
    }
    
    public boolean isFit() {
        return fit;
    }
    
    private boolean isTrackingEnd() {
        return offset + width >= prefWidth;
    }
    
    
    public class RowView implements Comparable<RowView> {
        
        private final ThreadData data;
        
        private int i = -1;
        
        
        RowView(ThreadData data) {
            this.data = data;
            if (getMaxIndex() >= 0) i = findLastIndex();
        }
        
        
        public int getLastIndex() {
            return i == Integer.MIN_VALUE || i == Integer.MAX_VALUE ? -1 : i;
        }
        
        public int getMaxIndex() {
            return data.size() - 1;
        }
        
        public long getTime(int index) {
            return data.getTimeStampAt(index);
        }
        
        public int getState(int index) {
            return data.getStateAt(index);
        }
        
        public int getPosition(long time) {
            return (int)((time - getFirstTime()) * zoom);
        }
        
        // TODO: should return end of last alive state for dead threads
        public int getMaxPosition() {
            return getViewWidth();
        }
        
        
        // TODO: optimize based on current offset / visible area
        private int findLastIndex() {
//            if (data.getThreadRecordsCount(row) == 0) return -1;
            
            if (isTrackingEnd() || isFit()) return getMaxIndex();
            
            i = Integer.MIN_VALUE;
            return findLastIndexLeft();
        }
        
        private int findLastIndexLeft() {
            // All indexes already on right
            if (i == Integer.MAX_VALUE) return i;
            
            int maxIndex = getMaxIndex();
            int newIndex = i == Integer.MIN_VALUE ? maxIndex : i;
            Position position = getIndexPosition(newIndex);
            while (newIndex > 0 && Position.RIGHT == position)
                position = getIndexPosition(--newIndex);
            
            // All indexes on right
            if (Position.RIGHT == position) return Integer.MAX_VALUE;
            
            // All indexes on left
            if (Position.LEFT == position && newIndex == maxIndex &&
                getMaxPosition() - offset < 0) return Integer.MIN_VALUE;
            
            // Last visible index
            return newIndex;
        }
        
        private int findLastIndexRight() {
            // All indexes already on right
            if (i == Integer.MIN_VALUE) return i;
            
            int maxIndex = getMaxIndex();
            int newIndex = i == Integer.MAX_VALUE ? 0 : i;
            Position position = getIndexPosition(newIndex);
            while (newIndex < maxIndex && Position.RIGHT != position)
                position = getIndexPosition(++newIndex);
            
            // First invisible inedx or all indexes on right
            if (Position.RIGHT == position)
                return newIndex == 0 ? Integer.MAX_VALUE : newIndex - 1;
            
            // All indexes on left
            if (Position.LEFT == position && newIndex == maxIndex &&
                getMaxPosition() - offset < 0) return Integer.MIN_VALUE;
            
            // Last visible index
            return newIndex;
        }
        
        private Position getIndexPosition(int index) {
            int position = getPosition(getTime(index)) - offset;
            if (position < 0) return Position.LEFT;
            else if (position >= width) return Position.RIGHT;
            else return Position.WITHIN;
        }
        
        
        private void offsetChanged(int oldOffset, int newOffset) {
            int maxIndex = getMaxIndex();
            if (maxIndex == -1) return;
            
            if (isTrackingEnd()) {
                i = maxIndex;
            } else {
                if (newOffset > oldOffset) {
                    i = i == -1 ? findLastIndex() : findLastIndexRight();
                } else {
                    i = i == -1 ? findLastIndex() : findLastIndexLeft();
                }
            }
        }
        
        private void widthChanged(int oldWidth, int newWidth) {
            int maxIndex = getMaxIndex();
            if (maxIndex == -1) return;
            
            if (isTrackingEnd() || isFit()) {
                i = maxIndex;
            } else {
                if (newWidth > oldWidth) {
                    i = i == -1 ? findLastIndex() : findLastIndexRight();
                } else {
                    i = i == -1 ? findLastIndex() : findLastIndexLeft();
                }
            }
        }
        
        private boolean lastMaxIn = true;
        private void preferredWidthChanged(int oldWidth, int newWidth) {
            int maxIndex = getMaxIndex();
            if (maxIndex == -1) return;
            
            int currPos = getMaxPosition() - offset;
            if (currPos >= 0 && currPos < width) { // TODO: verify
                i = maxIndex;
                lastMaxIn = true;
            } else {
                if (lastMaxIn && currPos >= width) {
                    // preferred width increases with new data
                    i = maxIndex;
                    findLastIndexLeft();
                }
                lastMaxIn = false;
            }
        }
        
        private void zoomChanged(double oldZoom, double newZoom) {
            int maxIndex = getMaxIndex();
            if (maxIndex == -1) return;
            
            if (isTrackingEnd() || isFit()) {
                i = maxIndex;
            } else {
                i = findLastIndex();
            }
        }

        public int compareTo(RowView view) {
            return Long.compare(data.getFirstTimeStamp(), view.data.getFirstTimeStamp());
        }
        
        public String toString() {
            return BUNDLE().getString("COL_Timeline"); // NOI18N
        }
        
    }
    
    private static enum Position { LEFT, WITHIN, RIGHT }
    
}
