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

package org.netbeans.modules.cnd.debugger.common2.debugger;

public interface Constants extends org.netbeans.spi.debugger.ui.Constants {

    // Used for ColumnModel ID's.
    //
    // Note how some of "our" PROPs are aliased to debuggercore
    // COLUMN_IDs.

    // Thread view
    public static final String PROP_THREAD_STATE =
	THREAD_STATE_COLUMN_ID;
    public static final String PROP_THREAD_SUSPENDED =
	THREAD_SUSPENDED_COLUMN_ID;

    public static final String PROP_THREAD_PRIORITY =
	"PROP_THREAD_PRIORITY"; // NOI18N
    public static final String PROP_THREAD_LWP =
	"PROP_THREAD_LWP"; // NOI18N
    public static final String PROP_THREAD_STARTUP_FLAGS =
	"PROP_THREAD_STARTUP_FLAGS"; // NOI18N
    public static final String PROP_THREAD_EXECUTING_FUNCTION =
	"PROP_THREAD_EXECUTING_FUNCTION"; // NOI18N
    public static final String PROP_THREAD_START_FUNCTION =
	"PROP_THREAD_START_FUNCTION"; // NOI18N
    public static final String PROP_THREAD_ADDRESS =
	"PROP_THREAD_ADDRESS"; // NOI18N
    public static final String PROP_THREAD_SIZE =
	"PROP_THREAD_SIZE"; // NOI18N
    public static final String PROP_THREAD_ID =
	"PROP_THREAD_ID"; // NOI18N
    public static final String PROP_THREAD_FILE =
	"PROP_THREAD_FILE"; // NOI18N
    public static final String PROP_THREAD_LINE =
	"PROP_THREAD_LINE"; // NOI18N

    // Stack view
    public static final String PROP_FRAME_LOCATION =
	CALL_STACK_FRAME_LOCATION_COLUMN_ID;

    public static final String PROP_FRAME_NUMBER =
	"PROP_FRAME_NUMBER"; // NOI18N
    public static final String PROP_FRAME_OPTIMIZED =
	"PROP_FRAME_OPTIMIZED"; // NOI18N
    public static final String PROP_FRAME_CURRENT_PC =
	"PROP_FRAME_CURRENT_PC"; // NOI18N
    public static final String PROP_FRAME_LOADOBJ =
	"PROP_FRAME_LOADOBJ"; // NOI18N

    public static final String PROP_BREAKPOINT_ENABLE  =
	"PROP_BREAKPOINT_ENABLE"; // NOI18N
    public static final String PROP_BREAKPOINT_COUNTLIMIT  =
	"PROP_BREAKPOINT_COUNTLIMIT"; // NOI18N
    public static final String PROP_BREAKPOINT_COUNT  =
	"PROP_BREAKPOINT_COUNT"; // NOI18N
    public static final String PROP_BREAKPOINT_LWP =
	"PROP_BREAKPOINT_LWP"; // NOI18N
    public static final String PROP_BREAKPOINT_ID =
	"PROP_BREAKPOINT_ID"; // NOI18N
    public static final String PROP_BREAKPOINT_CONTEXT =
	"PROP_BREAKPOINT_CONTEXT"; // NOI18N
    public static final String PROP_BREAKPOINT_WHILEIN =
	"PROP_BREAKPOINT_WHILEIN"; // NOI18N
    public static final String PROP_BREAKPOINT_QWHILEIN =
	"PROP_BREAKPOINT_QWHILEIN"; // NOI18N
    public static final String PROP_BREAKPOINT_CONDITION =
	"PROP_BREAKPOINT_CONDITION"; // NOI18N
    public static final String PROP_BREAKPOINT_QCONDITION =
	"PROP_BREAKPOINT_QCONDITION"; // NOI18N
    public static final String PROP_BREAKPOINT_THREAD =
	"PROP_BREAKPOINT_THREAD"; // NOI18N
    public static final String PROP_BREAKPOINT_TEMP =
	"PROP_BREAKPOINT_TEMP"; // NOI18N
    public static final String PROP_BREAKPOINT_JAVA =
	"PROP_BREAKPOINT_JAVA"; // NOI18N
    public static final String PROP_BREAKPOINT_TIMESTAMP =
	"PROP_BREAKPOINT_TIMESTAMP"; // NOI18N


    // Session view
    public static final String PROP_SESSION_PID =
	"PROP_SESSION_PID"; // NOI18N
    public static final String PROP_SESSION_CURRENT_LANGUAGE =
	SESSION_LANGUAGE_COLUMN_ID;
    public static final String PROP_SESSION_LOCATION =
	"PROP_SESSION_LOCATION"; // NOI18N
    public static final String SESSION_DEBUGGER_COLUMN_ID =
	"SESSION_DEBUGGER_COLUMN_ID"; // NOI18N
    public static final String PROP_SESSION_MODE =
	"PROP_SESSION_MODE"; // NOI18N
    public static final String PROP_SESSION_ARGS =
	"PROP_SESSION_ARGS"; // NOI18N
    public static final String PROP_SESSION_CORE =
	"PROP_SESSION_CORE"; // NOI18N
    public static final String PROP_SESSION_HOST =
	"PROP_SESSION_HOST"; // NOI18N

    // Local view
    public static final String PROP_LOCAL_TYPE =
	LOCALS_TYPE_COLUMN_ID;
    public static final String PROP_LOCAL_VALUE =
	LOCALS_VALUE_COLUMN_ID;
    public static final String PROP_LOCAL_TO_STRING =
	LOCALS_TO_STRING_COLUMN_ID;
    public static final String PROP_LOCAL_ADDRESS =
	"PROP_ADDRESS_COLUMN_ID"; // NOI18N
    public static final String PROP_LOCAL_DTYPE =
	"PROP_DTYPE_COLUMN_ID"; // NOI18N

    // Watch view
    public static final String PROP_WATCH_TYPE =
	WATCH_TYPE_COLUMN_ID;
    public static final String PROP_WATCH_VALUE =
	WATCH_VALUE_COLUMN_ID;
    public static final String PROP_WATCH_TO_STRING =
	WATCH_TO_STRING_COLUMN_ID;

}
