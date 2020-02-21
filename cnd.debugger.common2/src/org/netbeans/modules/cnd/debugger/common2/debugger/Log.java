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
