/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
