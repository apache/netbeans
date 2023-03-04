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
package org.netbeans.modules.editor.lib2.view;

import java.util.logging.Level;
import javax.swing.event.DocumentEvent;

/**
 * Info about change in view hierarchy as basis for ViewHierarchyEvent.
 *
 * @author Miloslav Metelka
 */
public final class ViewHierarchyChange {
    
    DocumentEvent documentEvent;
    
    int changeStartOffset;
    
    int changeEndOffset;
    
    boolean changeY;
    
    double startY;
    
    double endY;
    
    double deltaY;
    
    ViewHierarchyChange() {
        this.changeStartOffset = -1;
    }
    
    void addChange(int changeStartOffset, int changeEndOffset) {
        if (this.changeStartOffset == -1) {
            this.changeStartOffset = changeStartOffset;
            this.changeEndOffset = changeEndOffset;
            if (ViewHierarchyImpl.EVENT_LOG.isLoggable(Level.FINE)) {
                ViewUtils.log(ViewHierarchyImpl.EVENT_LOG,
                        "addChange-New: Offset-range <" + changeStartOffset + "," + changeEndOffset + ">\n"); // NOI18N
            }

        } else {
            this.changeStartOffset = Math.min(changeStartOffset, this.changeStartOffset);
            this.changeEndOffset = Math.max(changeEndOffset, this.changeEndOffset);
            if (ViewHierarchyImpl.EVENT_LOG.isLoggable(Level.FINE)) {
                ViewUtils.log(ViewHierarchyImpl.EVENT_LOG,
                        "addChange-Merge: Offset-range <" + changeStartOffset + "," + changeEndOffset + // NOI18N
                        "> => <" + this.changeStartOffset + "," + this.changeEndOffset + ">\n"); // NOI18N
            }
        }
    }

    void addChangeY(double startY, double endY, double deltaY) {
        // Change should already be non-null (change offsets should always be present)
        if (!changeY) {
            changeY = true;
            this.startY = startY;
            this.endY = endY;
            this.deltaY = deltaY;
            if (ViewHierarchyImpl.EVENT_LOG.isLoggable(Level.FINE)) {
                ViewUtils.log(ViewHierarchyImpl.EVENT_LOG,
                        "addChangeY-New: Y:<" + startY + "," + endY + "> dY=" + deltaY + '\n'); // NOI18N
            }

        } else { // Merge new change with original one
            this.startY = Math.min(startY, this.startY);
            if (endY > this.endY) { // Lies before the shifted area
                endY -= this.deltaY; // make original coordinate
                this.endY = Math.max(endY, this.endY);
            }
            this.deltaY += deltaY;
            if (ViewHierarchyImpl.EVENT_LOG.isLoggable(Level.FINE)) {
                ViewUtils.log(ViewHierarchyImpl.EVENT_LOG,
                        "addChangeY-Merge: Y:<" + startY + "," + endY + "> dY=" + deltaY + " => Y:<" + // NOI18N
                        this.startY + "," + this.endY + "> dY=" + this.deltaY + '\n'); // NOI18N
            }
        }
    }

    
    public DocumentEvent documentEvent() {
        return documentEvent;
    }
    
    public int changeStartOffset() {
        return changeStartOffset;
    }
    
    public int changeEndOffset() {
        return changeEndOffset;
    }
    
    public boolean isChangeY() {
        return changeY;
    }
    
    public double startY() {
        return startY;
    }
    
    public double endY() {
        return endY;
    }

    public double deltaY() {
        return deltaY;
    }
    
    @Override
    public String toString() {
        return "<" + changeStartOffset + "," + changeEndOffset + "> " + // NOI18N
                (changeY ? ("Y:<" + startY() + "," + endY() + "> dY=" + deltaY()) : "No-changeY"); // NOI18N
    }

}
