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
package org.netbeans.spi.debugger.ui;

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.debugger.ui.WatchesReader;
import org.openide.filesystems.FileObject;

/**
 * Implementation of watch pin in editor.
 * 
 * @author Ralph Benjamin Ruijs
 * @since 2.53
 */
public final class EditorPin implements Watch.Pin {

    /**
     * Line property, fired when a line change.
     */
    public static final String PROP_LINE = "line";
    /**
     * Location property, fired when a location change.
     */
    public static final String PROP_LOCATION = "location";
    /**
     * Comment property, fired when a comment change.
     */
    public static final String PROP_COMMENT = "comment";

    private final PropertyChangeSupport pchs = new PropertyChangeSupport(this);

    private final FileObject file;
    private volatile int line;
    private volatile Point location;
    private volatile String comment;
    private String vpId;
    
    static {
        WatchesReader.PIN_READER_ACCESS = new WatchesReader.PinReaderAccess() {
            @Override public String getVpId(EditorPin pin) {
                return pin.getVpId();
            }
            @Override public void setVpId(EditorPin pin, String vpId) {
                pin.setVpId(vpId);
            }
        };
    }

    /**
     * Create a new pin location in editor.
     * @param file The editor's file
     * @param line The line location of the pin
     * @param location Coordinates of the pin location in editor
     */
    public EditorPin(FileObject file, int line, Point location) {
        this.file = file;
        this.line = line;
        this.location = location;
    }

    /**
     * Get the line location of the pin.
     * @return The line.
     */
    public int getLine() {
        return line;
    }

    /**
     * Get the file object associated with the editor containing the pin.
     * @return The file object.
     */
    public FileObject getFile() {
        return file;
    }

    /**
     * Location of the pin in editor.
     * @return The location point.
     */
    public Point getLocation() {
        return location;
    }

    /**
     * Move the pin to a different location in the editor pane.
     * @param line A new line
     * @param location Coordinates of the new location
     */
    public void move(int line, Point location) {
        int oldLine = this.line;
        this.line = line;
        if (oldLine != line) {
            pchs.firePropertyChange(PROP_LINE, oldLine, line);
        }
        Point oldLocation = this.location;
        this.location = location;
        if (!oldLocation.equals(location)) {
            pchs.firePropertyChange(PROP_LOCATION, oldLocation, location);
        }
    }

    /**
     * Set a textual comment to this pin.
     * @param comment The user comment.
     */
    public void setComment(String comment) {
        String oldComment = this.comment;
        this.comment = comment;
        if (!Objects.equals(oldComment, comment)) {
            pchs.firePropertyChange(PROP_COMMENT, oldComment, comment);
        }
    }

    /**
     * Get the comment of this pin.
     * @return The comment or <code>null</code> when no comment is set.
     */
    public String getComment() {
        return comment;
    }
    
    void setVpId(String vpId) {
        this.vpId = vpId;
        pchs.firePropertyChange("valueProviderId", null, vpId);
    }

    String getVpId() {
        return vpId;
    }

    /**
     * Add a listener for property change events to this editor pin.
     * @param listener The listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pchs.addPropertyChangeListener(listener);
    }

    /**
     * Remove a listener for property change events from this editor pin.
     * @param listener The listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pchs.removePropertyChangeListener(listener);
    }
}
