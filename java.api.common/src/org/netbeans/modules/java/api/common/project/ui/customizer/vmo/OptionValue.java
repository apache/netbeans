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

import java.util.AbstractMap;
import java.util.Map;

/**
 * @author Rastislav Komara
 */
public abstract class OptionValue<V> {
    private boolean present = false;
    private V value;
    private final String kind;

    protected OptionValue(String kind) {
        this.kind = kind;
    }

    /**
     * Indicates if current value is considered present. For a lot of options the empty string is not "present" value.
     * @return true if value is present.
     */
    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }


    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"{" +
                "present=" + present +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OptionValue)) return false;

        OptionValue that = (OptionValue) o;

        if (!kind.equals(that.kind)) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + kind.hashCode();
        return result;
    }

    /*
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OptionValue)) return false;

        OptionValue that = (OptionValue) o;

        if (present != that.present) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (present ? 1 : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
*/

    public static OptionValue<Boolean> createSwitch() {
        return new SwitchOnly(true);
    }

    public static class SwitchOnly extends OptionValue<Boolean> {
        private static final String KIND = "Boolean";

        public SwitchOnly(boolean present) {
            super(KIND);
            setPresent(present);
        }

        @Override
        public void setValue(Boolean value) {
            setPresent(value);
        }

        @Override
        public Boolean getValue() {
            return isPresent();
        }


    }

    public static class StringPair extends OptionValue<Map.Entry<String, String>> {
        private static final String KIND = "Map.Entry<String,String>";

        public StringPair() {
            this(null, null);
        }

        public StringPair(String name, String value) {
            super(KIND);
            setValue(new AbstractMap.SimpleEntry<java.lang.String,java.lang.String>(name, value));            
        }

        @Override
        public void setValue(Map.Entry<String, String> value) {
            super.setValue(value);
            setPresent(value != null && value.getKey() != null && !value.getKey().isEmpty());
        }
    }

    public static class SimpleString extends OptionValue<String> {
        private static final String KIND = "String";

        public SimpleString() {
            super(KIND);
        }

        public SimpleString(String value) {
            this();
            setValue(value);
        }

        @Override
        public void setValue(String value) {
            super.setValue(value);
            setPresent(value != null && !value.isEmpty());
        }
    }

}
