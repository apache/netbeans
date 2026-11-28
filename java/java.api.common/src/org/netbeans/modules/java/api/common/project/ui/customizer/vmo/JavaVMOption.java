/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.api.common.project.ui.customizer.vmo;

/**
 * A single Java VM option, e.g. {@code -Xmx512m}, {@code -Dfoo=bar} or
 * {@code -server}.
 *
 * @param <V> type of the option value
 * @author Rastislav Komara
 */
public class JavaVMOption<V extends OptionValue<?>> implements Comparable<JavaVMOption<?>> {
    private String name;
    private V value;
    /**
     * The option's description, e.g. obtained from the JDK's help output.
     */
    private String description;
    /**
     * A placeholder for the option value, e.g. {@code <file>}, as printed
     * in the JDK's help output.
     */
    private String hint;
    /**
     * Indicated that this option should not be specified by user alone (e.g. classpath, bootclasspath)
     */
    private boolean valid = true;
    protected static final String SPACE = " ";
    protected static final char HYPHEN = '-';

    protected JavaVMOption() {
    }

    public String getName() {
        return name;
    }

    public V getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
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
        JavaVMOption<?> other = (JavaVMOption<?>) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    protected void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public int compareTo(JavaVMOption<?> o) {
        return getName().compareTo(o.getName());
    }
}
