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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
