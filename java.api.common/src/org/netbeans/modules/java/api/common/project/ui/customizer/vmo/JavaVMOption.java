/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.api.common.project.ui.customizer.vmo;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

/**
 * @author Rastislav Komara
 */
public class JavaVMOption<V extends OptionValue<?>> extends CommonTree implements Comparable<JavaVMOption<?>>{
    private String name;
    private V value;
    /**
     * Indicated that this option should not be specified by user alone (e.g. classpath, bootclasspath)
     */
    private boolean valid = true;
    protected static final String SPACE = " ";
    protected static final char HYPHEN = '-';

    protected JavaVMOption(Token t) {
        super(t);
    }

    protected JavaVMOption(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public V getValue() {
        return value;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setValue(V value) {
        this.value = value;
    }

    public StringBuilder print(StringBuilder builder) {
        return ensureBuilder(builder);
    }

    protected StringBuilder ensureBuilder(StringBuilder builder) {
        if (builder == null) {
            builder = new StringBuilder();
        }
        return builder;
    }

    @Override
    public String toString() {
        return "JavaVMOption{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", valid=" + valid +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JavaVMOption<V> other = (JavaVMOption<V>) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
        
    @Override
    public int hashCode() {
        int result = name.hashCode();
        return result;
    }

    protected void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }

    public int compareTo(JavaVMOption<?> o) {
        return getName().compareTo(o.getName());
    }
}
