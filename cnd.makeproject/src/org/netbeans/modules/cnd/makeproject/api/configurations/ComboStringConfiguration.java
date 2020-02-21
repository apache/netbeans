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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.netbeans.modules.cnd.api.picklist.DefaultPicklistModel;

public class ComboStringConfiguration implements Cloneable {

    private ComboStringConfiguration master;
    private String def;
    private String value;
    private boolean modified;
    private boolean dirty = false;
    private DefaultPicklistModel picklist;

    public ComboStringConfiguration(ComboStringConfiguration master, String def, DefaultPicklistModel picklist) {
        this.master = master;
        this.def = def;

        this.picklist = picklist;
        reset();
    }

    /** Needed for CompilerSetConfiguration to maintain compatibility */
    protected ComboStringConfiguration() {
    }

    public void setMaster(ComboStringConfiguration master) {
        this.master = master;
    }

    public void setValue(String value) {
        value = value.trim();
        this.value = value;
        if (master != null) {
            setModified(true);
        } else {
            setModified(!value.equals(getDefault()));
        }
    }

    public String getValue() {
        if (master != null && !getModified()) {
            return master.getValue();
        } else {
            return value;
        }
    }

    public final void setModified(boolean b) {
        this.modified = b;
    }

    public boolean getModified() {
        return modified;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean getDirty() {
        return dirty;
    }

    public String getDefault() {
        return def;
    }

    public void setDefault(String def) {
        this.def = def;
        setModified(!value.equals(getDefault()));
    }

    public final void reset() {
        value = getDefault();
        setModified(false);
    }

    public DefaultPicklistModel getPicklist() {
        return picklist;
    }

    // Clone and Assign
    public void assign(ComboStringConfiguration conf) {
        dirty = !getValue().equals(conf.getValue());
        picklist = (DefaultPicklistModel)conf.getPicklist().clonePicklist();
        setValue(conf.getValue());
        setModified(conf.getModified());
    }

    @Override
    public ComboStringConfiguration clone() {
        ComboStringConfiguration clone = new ComboStringConfiguration(master, def, (DefaultPicklistModel)picklist.clonePicklist());
        clone.setValue(getValue());
        clone.setModified(getModified());
        return clone;
    }

    @Override
    public String toString() {
        return "(" + getValue() + ")"; // NOI18N
    }

}
