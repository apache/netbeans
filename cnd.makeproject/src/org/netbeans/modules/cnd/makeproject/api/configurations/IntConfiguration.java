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

public class IntConfiguration implements Cloneable {

    private IntConfiguration master;
    private byte def;
    private String[] names;
    private String[] options;
    private byte value;
    private byte previousValue;
    private boolean modified;
    private boolean dirty = false;

    public IntConfiguration(IntConfiguration master, int def, String[] names, String[] options) {
        this.master = master;
        this.def = (byte) def;
        this.names = names;
        this.options = options;
        reset();
    }
    
    /** Needed for CompilerSetConfiguration to maintain compatibility */
    protected IntConfiguration() {
    }

    public void setMaster(IntConfiguration master) {
        this.master = master;
    }

    public void setValue(int value) {
        this.previousValue = this.value;
        this.value = (byte) value;
        if (master != null) {
            setModified(true);
        } else {
            setModified(value != getDefault());
        }
    }

    public void setValue(String s) {
        if (s != null) {
            for (int i = 0; i < names.length; i++) {
                if (s.equals(names[i])) {
                    setValue(i);
                    break;
                }
            }
        }
    }

    public int getValue() {
        if (master != null && !getModified()) {
            return master.getValue();
        } else {
            return value;
        }
    }

    public void setModified(boolean b) {
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

    public int getDefault() {
        return def;
    }

    public void setDefault(int def) {
        this.def = (byte) def;
        setModified(value != getDefault());
    }

    public void reset() {
        previousValue = value;
        value = (byte) getDefault();
        setModified(false);
    }

    public String getName() {
        if (getValue() < names.length) {
            return names[getValue()];
        } else {
            return "???"; // FIXUP // NOI18N
        }
    }

    public String[] getNames() {
        return names;
    }

    public String getOption() {
        return options[getValue()] + " "; // NOI18N
    }

    // Clone and Assign
    public void assign(IntConfiguration conf) {
        dirty = getValue() != conf.getValue();
        setValue(conf.getValue());
        setModified(conf.getModified());
    }

    @Override
    @org.netbeans.api.annotations.common.SuppressWarnings("CN") // each subclass implemented Clonable must override this method
    public IntConfiguration clone() {
        IntConfiguration clone = new IntConfiguration(master, def, names, options);
        clone.setValue(getValue());
        clone.setModified(getModified());
        return clone;
    }

    public byte getPreviousValue() {
        return previousValue;
    }

    @Override
    public String toString() {
        return "(" + getValue() + ")" + getName(); // NOI18N
    }

}
