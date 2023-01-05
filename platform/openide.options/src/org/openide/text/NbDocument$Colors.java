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
package org.openide.text;

import java.awt.Color;


/** Dummy class holding utility methods for working with NetBeans document conventions.
*
* @author Jaroslav Tulach
*/


/** @deprecated Not useful for anything. */
@Deprecated
public final class NbDocument$Colors extends org.openide.options.SystemOption {
    public static final String PROP_BREAKPOINT = "NbBreakpointStyle"; // NOI18N
    public static final String PROP_ERROR = "NbErrorStyle"; // NOI18N
    public static final String PROP_CURRENT = "NbCurrentStyle"; // NOI18N
    static final long serialVersionUID = -9152250591365746193L;

    public void setBreakpoint(Color c) {
    }

    public Color getBreakpoint() {
        return new Color(127, 127, 255);
    }

    public void setError(Color c) {
    }

    public Color getError() {
        return Color.red;
    }

    public void setCurrent(Color c) {
    }

    public Color getCurrent() {
        return Color.magenta;
    }

    public String displayName() {
        return "COLORS"; // NOI18N
    }
}
