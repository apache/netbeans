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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import org.netbeans.modules.cnd.debugger.common2.utils.LogSupport;

public class Log extends LogSupport {

    public static class Editor {
	public static final boolean debug =
	    booleanProperty("cnd.nativedebugger.Editor.debug", false); // NOI18N
    }

    public static class Bpt {
	public static final boolean pathway =
	    booleanProperty("cnd.nativedebugger.Bpt.pathway", false); // NOI18N
    }

    public static class Watch {
	public static final boolean varprefix =
	    booleanProperty("cnd.nativedebugger.Watch.varprefix", false); // NOI18N
	public static final boolean pathway =
	    booleanProperty("cnd.nativedebugger.Watch.pathway", false); // NOI18N
	public static final boolean xml =
	    booleanProperty("cnd.nativedebugger.Watch.xml", false); // NOI18N
	public static final boolean map =
	    booleanProperty("cnd.nativedebugger.Watch.map", false); // NOI18N
    }

    public static class Variable {
	public static final boolean tipdebug =
	    booleanProperty("cnd.nativedebugger.Variable.tipdebug", false); // NOI18N
	public static final boolean traffic =
	    booleanProperty("cnd.nativedebugger.Variable.traffic", false); // NOI18N
	public static final boolean debug =
	    booleanProperty("cnd.nativedebugger.Variable.debug", false); // NOI18N
	public static final boolean expansion =
	    booleanProperty("cnd.nativedebugger.Variable.expansion", false); // NOI18N
	public static final boolean leaf =
	    booleanProperty("cnd.nativedebugger.Variable.leaf", false); // NOI18N
	public static final boolean expanded =
	    booleanProperty("cnd.nativedebugger.Variable.expanded", false); // NOI18N
	public static final boolean ctx =
	    booleanProperty("cnd.nativedebugger.Variable.ctx", false); // NOI18N
	public static final boolean children =
	    booleanProperty("cnd.nativedebugger.Variable.children", false); // NOI18N
	public static final boolean mi_vars =
	    booleanProperty("cnd.nativedebugger.Variable.mi_vars", false); // NOI18N
	public static final boolean mi_threads =
	    booleanProperty("cnd.nativedebugger.Variable.mi_threads", false); // NOI18N
	public static final boolean mi_frame =
	    booleanProperty("cnd.nativedebugger.Variable.mi_frame", false); // NOI18N
    }

    public static class XML {
	public static final boolean debug =
	    booleanProperty("cnd.nativedebugger.XML.debug", false); // NOI18N
    }
    
    /**
     * Starup of the whole IDE.
     */
    public static class Startup {
	public static final boolean debug = 
	    booleanProperty("cnd.nativedebugger.Startup.debug", false); // NOI18N
    }

    /**
     * Starup of engine.
     */
    public static class Start {
	public static final boolean debug =
	    booleanProperty("cnd.nativedebugger.Start.debug", false); // NOI18N
	public static final boolean capture_engine_startup =
	    booleanProperty("cnd.nativedebugger.Start.capture_engine_startup", false); // NOI18N
	public static final boolean preload_rtc =
	    booleanProperty("cnd.nativedebugger.Start.preload_rtc", false); // NOI18N
    }

    public static class Capture {
	public static final boolean state =
	    booleanProperty("cnd.nativedebugger.Capture.state", false); // NOI18N
	public static final boolean info =
	    booleanProperty("cnd.nativedebugger.Capture.info", false); // NOI18N
    }

    public static class PathMap {
	public static final boolean debug =
	    booleanProperty("cnd.nativedebugger.PathMap.debug", false); // NOI18N
    }

}
