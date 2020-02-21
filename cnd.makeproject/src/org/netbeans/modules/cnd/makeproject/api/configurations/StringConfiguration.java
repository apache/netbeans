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

import java.util.StringTokenizer;

public class StringConfiguration implements Cloneable {

    private StringConfiguration master;
    private String def;
    private String value;
    private boolean modified;

    public StringConfiguration(StringConfiguration master, String def) {
        this.master = master;
        this.def = def;
        reset();
    }

    protected StringConfiguration() {
    }

    public void setMaster(StringConfiguration master) {
        this.master = master;
    }

    public void setValue(String b) {
        if (b == null) {
            b = ""; // NOI18N
        }
        this.value = b;
        if (master != null) {
            setModified(true);
        } else {
            setModified(!b.equals(getDefault()));
        }
    }

    public String getValue() {
        if (master != null && !getModified()) {
            return master.getValue();
        } else {
            return value;
        }
    }

    public String getValueDef(String def) {
        if (master != null && !getModified() && !master.getModified() && def != null) {
            return def;
        }
        if (master != null && !getModified()) {
            return master.getValue();
        } else if (!getModified() && def != null) {
            return def;
        } else {
            return value;
        }
    }

    public String getValue(String delim) {
        StringBuilder ret = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(getValue());
        while (tokenizer.hasMoreTokens()) {
            ret.append(tokenizer.nextToken());
            if (tokenizer.hasMoreTokens()) {
                ret.append(delim);
            }
        }
        return ret.toString();
    }

    public void setModified(boolean b) {
        this.modified = b;
    }

    public boolean getModified() {
        return modified;
    }

    public String getDefault() {
        return def;
    }

    public void reset() {
        value = getDefault();
        setModified(false);
    }

    public void setDefaultValue(String def) {
        this.def = def;
    }

    // Clone and Assign
    public void assign(StringConfiguration conf) {
        setValue(conf.getValue());
        setModified(conf.getModified());
    }

    @Override
    public StringConfiguration clone() {
        StringConfiguration clone = new StringConfiguration(master, def);
        clone.setValue(getValue());
        clone.setModified(getModified());
        return clone;
    }

    @Override
    public String toString() {
        return "{value=" + value + " modified=" + modified + '}'; // NOI18N
    }
}
