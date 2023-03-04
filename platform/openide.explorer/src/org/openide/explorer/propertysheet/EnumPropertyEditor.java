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

package org.openide.explorer.propertysheet;

import java.beans.PropertyEditorSupport;

/**
 * Property editor for enumeration types.
 * @author Jesse Glick
 */
final class EnumPropertyEditor extends PropertyEditorSupport {

    private final Class<? extends Enum> c;

    public EnumPropertyEditor(Class<? extends Enum> c) {
        this.c = c;
    }

    private Object[] getValues() {
        try {
            return (Object[]) c.getMethod("values").invoke(null); // NOI18N
        } catch (Exception x) {
            throw new AssertionError(x);
        }
    }

    @Override
    public String[] getTags() {
        Object[] values = getValues();
        String[] tags = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            tags[i] = values[i].toString();
        }
        return tags;
    }

    @Override
    public String getAsText() {
        Object o = getValue();
        return o != null ? o.toString() : "";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text.length() > 0) {
            Object[] values = getValues();
            for (int i = 0; i < values.length; i++) {
                String p = values[i].toString();
                if (text.equals(p)) {
                    setValue(values[i]);
                    return;
                }
            }
            setValue(Enum.valueOf(c, text));
        } else {
            setValue(null);
        }
    }

    @Override
    public String getJavaInitializationString() {
        Enum<?> e = (Enum<?>) getValue();
        if (e == null) {
            return "null"; // NOI18N
        }
        String name = c.getCanonicalName();
        if (name == null) {
            return super.getJavaInitializationString();
        }
        return name + '.' + e.name();
    }

}
