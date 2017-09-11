/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.spi;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bugtracking.tasks.SchedulingPickerImpl;

/**
 * Provides a component for setting and updating Issue schedule info. The UI
 * component can be placed into an Issue editor, get the component from
 * {@link #getComponent()}.
 *
 * @author Ondrej Vrabec
 */
public final class SchedulePicker {

    private final SchedulingPickerImpl impl;

    /**
     * Creates an instance of the picker.
     */
    public SchedulePicker () {
        impl = new SchedulingPickerImpl();
    }

    /**
     * Returns a UI component that can be placed into a component hierarchy. The
     * component is able to edit and set Issue schedule info.
     *
     * @return UI component
     */
    public JComponent getComponent () {
        return impl.getComponent();
    }

    /**
     * Returns the current scheduling info either previously set by
     * {@link #setScheduleDate(IssueScheduleInfo)} or manually edited by user.
     *
     * @return scheduling info currently held by the UI component.
     * <code>null</code> can be returned and means not scheduled.
     */
    public IssueScheduleInfo getScheduleDate () {
        return impl.getScheduleDate();
    }

    /**
     * Resets the current scheduling info in the component to a given value. You
     * may pass <code>null</code> to specify no scheduling.
     *
     * @param info new scheduling info to set inside the component,
     * <code>null</code> means not scheduled.
     */
    public void setScheduleDate (IssueScheduleInfo info) {
        impl.setScheduleDate(info);
    }

    /**
     * Adds a listener that will be notified when the selected schedule changes.
     *
     * @param listener listener to add
     */
    public void addChangeListener (ChangeListener listener) {
        impl.addChangeListener(listener);
    }

    /**
     * Removes a listener, it will be no longer notified about changes.
     *
     * @param listener listener to remove
     */
    public void removeChangeListener (ChangeListener listener) {
        impl.removeChangeListener(listener);
    }
}
