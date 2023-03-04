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
package org.netbeans.modules.web.wizards;

/**
 * Filter Mapping representation
 * @author ana.von.klopp@sun.com
 */
public class FilterMappingData {

    private String name = null;
    private Type type = null;
    private String pattern = null;
    private Dispatcher[] dispatch = new Dispatcher[0];

    FilterMappingData() {
    }

    FilterMappingData(String name) {
        this.name = name;
        this.type = FilterMappingData.Type.URL;
        this.pattern = "/*"; //NOI18N
    }

    FilterMappingData(String name, Type type, String pattern, Dispatcher[] d) {
        this.name = name;
        this.type = type;
        this.pattern = pattern;
        this.dispatch = d;
    }

    @Override
    public Object clone() {
        return new FilterMappingData(name, type, pattern, dispatch);
    }

    /**
     * Get the Name value.
     * @return the Name value.
     */
    String getName() {
        return name;
    }

    /**
     * Set the Name value.
     * @param newName The new Name value.
     */
    void setName(String newName) {
        this.name = newName;
    }

    /**
     * Get the Type value.
     * @return the Type value.
     */
    Type getType() {
        return type;
    }

    /**
     * Set the Type value.
     * @param newType The new Type value.
     */
    void setType(Type newType) {
        this.type = newType;
    }

    /**
     * Get the Pattern value.
     * @return the Pattern value.
     */
    String getPattern() {
        return pattern;
    }

    /**
     * Set the Pattern value.
     * @param newPattern The new Pattern value.
     */
    void setPattern(String newPattern) {
        this.pattern = newPattern;
    }

    /**
     * Get the DispatchConfig value.
     * @return the DispatchConfig value.
     */
    Dispatcher[] getDispatcher() {
        return dispatch;
    }

    /**
     * Set the DispatchConfig value.
     * @param new dc new DispatchConfig value.
     */
    void setDispatcher(Dispatcher[] d) {
        this.dispatch = d;
    }

    @Override
    public String toString() {
        StringBuffer buf =
                new StringBuffer("FilterMapping for filter: "); //NOI18N
        buf.append(name);
        buf.append("\nMapping type: ");
        buf.append(type.toString());
        buf.append(" for pattern: ");
        buf.append(pattern);
        buf.append("\nDispatch conditions: ");
        if (dispatch.length == 0) {
            buf.append("REQUEST (not set)\n\n");
        } else {
            for (int i = 0; i < dispatch.length; ++i) {
                buf.append(dispatch[i].toString());
                buf.append(", ");
            }
            buf.append("\n\n");
        }
        return buf.toString();
    }

    static class Type {
        private String name;

        private Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
        public static final Type URL = new Type("URL pattern");
        public static final Type SERVLET = new Type("Servlet");
    }

    static class Dispatcher {
        private String name;

        private Dispatcher(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
        public static final Dispatcher BLANK = new Dispatcher("");
        public static final Dispatcher REQUEST = new Dispatcher("REQUEST");
        public static final Dispatcher INCLUDE = new Dispatcher("INCLUDE");
        public static final Dispatcher FORWARD = new Dispatcher("FORWARD");
        public static final Dispatcher ERROR = new Dispatcher("ERROR");

        public static final Dispatcher findDispatcher(String s) {
            if (s.equals(REQUEST.toString())) {
                return REQUEST;
            } else if (s.equals(INCLUDE.toString())) {
                return INCLUDE;
            } else if (s.equals(FORWARD.toString())) {
                return FORWARD;
            } else if (s.equals(ERROR.toString())) {
                return ERROR;
            } else {
                return BLANK;
            }
        }

        public static final Dispatcher[] getAll() {
            Dispatcher[] d = new Dispatcher[4];
            d[0] = REQUEST;
            d[1] = FORWARD;
            d[2] = INCLUDE;
            d[3] = ERROR;
            return d;
        }
    }
}
