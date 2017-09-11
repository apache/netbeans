/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
/*
 * TabActionEvent.java
 *
 * Created on March 17, 2004, 3:48 PM
 */

package org.netbeans.swing.tabcontrol.event;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 * An action event which may be consumed by a listener.  These are fired by
 * TabControl and TabbedContainer to determine if outside code wants to handle
 * an event, such as clicking the close button (which might be vetoed), or if
 * the control should handle it itself.
 *
 * @author Tim Boudreau
 */
public final class TabActionEvent extends ActionEvent {
    private MouseEvent mouseEvent = null;
    private int tabIndex;
    private String groupName = null;

    /**
     * Creates a new instance of TabActionEvent
     */
    public TabActionEvent(Object source, String command, int tabIndex) {
        super(source, ActionEvent.ACTION_PERFORMED, command);
        this.tabIndex = tabIndex;
        consumed = false;
    }

    public TabActionEvent(Object source, String command, int tabIndex,
                          MouseEvent mouseEvent) {
        this(source, command, tabIndex);
        this.mouseEvent = mouseEvent;
        consumed = false;
    }
    
    /**
     * Consume this event - any changes that should be performed as a result
     * will be done by external code by manipulating the models or other means
     */
    @Override
    public void consume() {
        consumed = true;
    }

    /**
     * Determine if the event has been consumed
     */
    @Override
    public boolean isConsumed() {
        return super.isConsumed();
    }

    /**
     * If the action event was triggered by a mouse event, get the mouse event
     * in question
     *
     * @return The mouse event, or null
     */
    public MouseEvent getMouseEvent() {
        return mouseEvent;
    }

    public int getTabIndex() {
        return tabIndex;
    }
    
    /**
     * @return Name of window group this command applies to or null.
     * @since 1.27
     */
    public String getGroupName() {
        return groupName;
    }
    
    /**
     * Set the name of window group this command applies to.
     * @param groupName 
     * @since 1.27
     */
    public void setGroupName( String groupName ) {
        this.groupName = groupName;
    }

    @Override
    public void setSource(Object source) {
        //Skip some native peer silliness in AWTEvent
        this.source = source;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("TabActionEvent:"); //NOI18N
        sb.append ("Tab " + tabIndex + " " + getActionCommand()); //NOI18N
        return sb.toString();
    }

}
