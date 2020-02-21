
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

import java.util.ArrayList;
import java.util.List;

public class VectorConfiguration<E> implements Cloneable {

    private final VectorConfiguration<E> master;
    private List<E> value;
    private boolean dirty = false;

    public VectorConfiguration(VectorConfiguration<E> master) {
        this.master = master;
        value = new ArrayList<>(0);
        reset();
    }

    public VectorConfiguration<E> getMaster() {
        return master;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean getDirty() {
        return dirty;
    }

    public void add(E o) {
        getValue().add(o);
    }

    public void setValue(List<E> l) {
        if (l == null) {
            return; // See evaluation in IZ 193164
        }
        this.value = l;
    }

    public List<E> getValue() {
        return value;
    /*
    if (master != null && !getModified())
    return master.getValue();
    else
    return value;
     */
    }

    public boolean getModified() {
        return !value.isEmpty();
    }

    public final void reset() {
        //value.removeAll(); // FIXUP
        value = new ArrayList<>(0);
    }

    // Clone and Assign
    public void assign(VectorConfiguration<E> conf) {
        if (conf == null) {
            return;
        }
        setDirty(!this.equals(conf));
        reset();
        getValue().addAll(conf.getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VectorConfiguration<?>)) {
            return false;
        }
        VectorConfiguration<?> conf = (VectorConfiguration<?>)obj;
        List<?> list1 = getValue();
        List<?> list2 = conf.getValue();
        if (list1 == null && list2 == null) {
            return true;
        } else if (list1 != null && list2 != null) {
            if (list1.size() != list2.size()) {
                return false;
            }
            for (int i = 0; i < list1.size(); i++) {
                if (!list1.get(i).equals(list2.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = 3;
        for (Object obj : getValue()) {
            code = 17 * code + obj.hashCode();
        }
        return code;
    }

    @Override
    @org.netbeans.api.annotations.common.SuppressWarnings("CN") // each subclass implemented Clonable must override this method
    public VectorConfiguration<E> clone() {
        VectorConfiguration<E> clone = new VectorConfiguration<>(master);
        clone.setValue(new ArrayList<>(getValue()));
        return clone;
    }

    /**
     * Converts each element of the vector to <code>String</code>
     * and concatenates the results into a single <code>String</code>.
     * Elements are separated with spaces.
     *
     * @param visitor  will be used to convert each element to <code>String</code>
     * @return concatenated <code>String</code>
     */
    public String toString(ToString<E> visitor) {
        return toString(visitor, " "); // NOI18N
    }

    public String toString(ToString<E> visitor, String separator) {
        StringBuilder buf = new StringBuilder();
        List<E> list = getValue();
        for (E item : list) {
            String s = visitor.toString(item);
            if (s != null && 0 < s.length()) {
                buf.append(s).append(separator);
            }
        }
        return buf.toString();
    }

    
    /**
     * Used to convert vector elements to <code>String</code>.
     * See {@link VectorConfiguration#toString(ToString)}.
     *
     * @param <E> vector element type
     */
    public static interface ToString<E> {
        String toString(E item);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        value.forEach((e) -> {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(e);
        });
        return "{value=[" + sb + "] dirty=" + dirty +'}'; // NOI18N
    }
}
