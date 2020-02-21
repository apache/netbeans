/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.diagnostics.clank.ui.views;

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;
import org.openide.filesystems.FileObject;

/**
 * Implementation of watch pin in editor.
 * 
 * @since 2.53
 */
public final class EditorPin {

    /**
     * Line property, fired when a line change.
     */
    public static final String PROP_LINE = "line";//NOI18N
    /**
     * Location property, fired when a location change.
     */
    public static final String PROP_LOCATION = "location";//NOI18N
    /**
     * Comment property, fired when a comment change.
     */
    public static final String PROP_COMMENT = "comment";//NOI18N

    private final PropertyChangeSupport pchs = new PropertyChangeSupport(this);

    private final FileObject file;
    private volatile int line;
    private volatile Point location;
    private volatile String comment;
    private String vpId;

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
        pchs.firePropertyChange("valueProviderId", null, vpId);//NOI18N
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
