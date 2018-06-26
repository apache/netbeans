/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.glassfish.tooling;

import java.util.HashMap;
import java.util.Map;

/**
 * Current state of GlassFish server administration command execution
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public enum TaskState {

    ////////////////////////////////////////////////////////////////////////////
    // Enum values                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /** Value representing task waiting in executor queue. */
    READY,

    /** Value representing running task. */
    RUNNING,

    /** Value representing successfully completed task (with no errors). */
    COMPLETED,

    /** Value representing failed task. */
    FAILED;

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**  A <code>String</code> representation of READY value. */
    private static final String READY_STR = "READY";

    /**  A <code>String</code> representation of RUNNING value. */
    private static final String RUNNING_STR = "RUNNING";

    /**  A <code>String</code> representation of COMPLETED value. */
    private static final String COMPLETED_STR = "COMPLETED";

    /**  A <code>String</code> representation of FAILED value. */
    private static final String FAILED_STR = "FAILED";

    /** 
     * Stored <code>String</code> values for backward <code>String</code>
     * conversion.
     */
    private static final Map<String, TaskState> stringValuesMap
            = new HashMap(values().length);

    // Initialize backward String conversion <code>Map</code>.
    static {
        for (TaskState state : TaskState.values()) {
            stringValuesMap.put(state.toString().toUpperCase(), state);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a <code>TaskState</code> with a value represented by the
     * specified <code>String</code>. The <code>TaskState</code> returned
     * represents existing value only if specified <code>String</code>
     * matches any <code>String</code> returned by <code>toString</code> method.
     * Otherwise <code>null</code> value is returned.
     * <p>
     * @param stateStr Value containing <code>TaskState</code> 
     *                 <code>toString</code> representation.
     * @return <code>TaskState</code> value represented by <code>String</code>
     *         or <code>null</code> if value was not recognized.
     */
    public static TaskState toValue(final String stateStr) {
        if (stateStr != null) {
            return (stringValuesMap.get(stateStr.toUpperCase()));
        } else {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert <code>TaskState</code> value to <code>String</code>.
     * <p>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        switch (this) {
            case READY:     return READY_STR;
            case RUNNING:   return RUNNING_STR;
            case COMPLETED: return COMPLETED_STR;
            case FAILED:    return FAILED_STR;
            // This is unrecheable. Returned null value means that some
            // enum value is not handled correctly.
            default:        return null;
        }
    }

}
