/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
