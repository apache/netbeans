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
