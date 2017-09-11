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

package org.netbeans.modules.notifications.checklist;

/**
 * Default model for a CheckList
 */
public class DefaultCheckListModel extends AbstractCheckListModel {

    private static final long serialVersionUID = 1;

    private final boolean state[];
    private final Object[] values;
    private final String[] descriptions;

    /**
     * Creates a new model with the given state of checkboxes and the given
     * values
     *
     * @param state state of the checkboxes. A copy of this array will NOT be
     * created.
     * @param values values. A copy of this array will NOT be
     * created.
     */
    public DefaultCheckListModel(boolean[] state, Object[] values, String[] descriptions) {
        if (state.length != values.length)
            throw new IllegalArgumentException("state.length != values.length"); //NOI18N
        if (state.length != descriptions.length) {
            throw new IllegalArgumentException();
        }
        this.state = state;
        this.values = values;
        this.descriptions = descriptions;
    }
    
    @Override
    public boolean isChecked(int index) {
        return state[index];
    }
    
    @Override
    public void setChecked(int index, boolean c) {
        state[index] = c;
        fireContentsChanged(this, index, index);
    }

    @Override
    public int getSize() {
        return values.length;
    }

    @Override
    public Object getElementAt(int index) {
        return values[index];
    }

    @Override public String getDescription(int index) {
        return descriptions[index];
    }

}
