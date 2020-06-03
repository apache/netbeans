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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

import org.netbeans.modules.cnd.debugger.common2.values.LwpIdEditor;
import org.netbeans.modules.cnd.debugger.common2.values.ThreadIdEditor;
import org.netbeans.modules.cnd.debugger.common2.values.CountLimitEditor;
import org.netbeans.modules.cnd.debugger.common2.values.StringEditor;


import org.netbeans.modules.cnd.debugger.common2.debugger.Constants;
import org.netbeans.modules.cnd.debugger.common2.debugger.AbstractColumnModel;


/*
 * Convenience container for individual ColumnModels specified as inner classes.
 *
 * Registered in
 *	META-INF/debugger/netbeans-DbxDebuggerEngine/BreakpointsView/
 *	org.netbeans.spi.viewmodel.Column
 *	NOTE: Use '...debugger.BreakpointColumnModels$<column-name>'
 */

public final class BreakpointColumnModels implements Constants {
    public static final class Lwp extends AbstractColumnModel {
        public Lwp() {
	    super(Constants.PROP_BREAKPOINT_LWP,
	          Catalog.get("ACSD_LWP"), // NOI18N
		  Catalog.get("ACSD_LWP"), Object.class,false, new LwpIdEditor()); // NOI18N
       }
    }

    public static final class Id extends AbstractColumnModel {
        public Id() {
	    super(Constants.PROP_BREAKPOINT_ID,
	          Catalog.get("ACSD_ID"), // NOI18N
		  Catalog.get("ACSD_ID"), Integer.class, false, new StringEditor()); // NOI18N
        }
    }

    public static final class Count extends AbstractColumnModel {
        public Count() {
	    super(Constants.PROP_BREAKPOINT_COUNT,
	          Catalog.get("ACSD_Count"),  // NOI18N
		  Catalog.get("ACSD_Count"), Integer.class, false, new StringEditor()); // NOI18N
       }
    }

    public static final class CountLimit extends AbstractColumnModel {
        public CountLimit() {
	    super(Constants.PROP_BREAKPOINT_COUNTLIMIT,
	          Catalog.get("ACSD_CountLimit"), // NOI18N
		  Catalog.get("ACSD_CountLimit"), Object.class, false, new CountLimitEditor()); // NOI18N
        }
    }
    
    public static final class WhileIn extends AbstractColumnModel {
        public WhileIn() {
	    super(Constants.PROP_BREAKPOINT_WHILEIN,
	          Catalog.get("ACSD_WhileIn"),  // NOI18N
		  Catalog.get("ACSD_WhileIn"), Object.class, false, new StringEditor()); // NOI18N
        }
    }

    public static final class Condition extends AbstractColumnModel {
        public Condition() {
	    super(Constants.PROP_BREAKPOINT_CONDITION,
	          Catalog.get("ACSD_Condition"),  // NOI18N
		  Catalog.get("ACSD_Condition"),String.class, false, new StringEditor());  // NOI18N
        }
    }

    public static final class Thread extends AbstractColumnModel {
        public Thread() {
	    super(Constants.PROP_BREAKPOINT_THREAD,
	          Catalog.get("ACSD_Thread"),  // NOI18N
		  Catalog.get("ACSD_Thread"),Object.class, false, new ThreadIdEditor());  // NOI18N
        }
    }


    public static final class Temp extends AbstractColumnModel {
        public Temp() {
	    super(Constants.PROP_BREAKPOINT_TEMP,
	          Catalog.get("ACSD_Temp"),  // NOI18N
		  Catalog.get("ACSD_Temp"), Boolean.TYPE, false, null); // NOI18N
        }
    }

    public static final class Java extends AbstractColumnModel {
        public Java() {
	    super(Constants.PROP_BREAKPOINT_JAVA,
	          Catalog.get("ACSD_Java"),  // NOI18N
		  Catalog.get("ACSD_Java"), Boolean.TYPE, false, null); // NOI18N
        }
    }

    public static final class Timestamp extends AbstractColumnModel {
        public Timestamp() {
	    super(Constants.PROP_BREAKPOINT_TIMESTAMP,
	          Catalog.get("ACSD_Timestamp"),  // NOI18N
		  Catalog.get("ACSD_Timestamp"), String.class,  // NOI18N
		  false, new StringEditor());
        }
    }

    public static final class Context extends AbstractColumnModel {
        public Context() {
	    super(Constants.PROP_BREAKPOINT_CONTEXT,
	          Catalog.get("ACSD_Context"), // NOI18N
		  Catalog.get("ACSD_Context"), // NOI18N
		  String.class, true, new StringEditor());
        }
    }
}
