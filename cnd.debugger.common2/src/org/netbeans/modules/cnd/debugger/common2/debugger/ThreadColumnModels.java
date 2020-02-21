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


import org.netbeans.modules.cnd.debugger.common2.values.StringEditor;

/**
 * Convenience container for individual ColumnModels specified as inner classes.
 *
 * Registered in
 *	META-INF/debugger/netbeans-DbxDebuggerEngine/ThreadsView/
 *	org.netbeans.spi.viewmodel.ColumnModel
 *	NOTE: Use '...debugger.ThreadColumnModels$StartFunction'
 */

public final class ThreadColumnModels {

    public static final class State extends AbstractColumnModel {
        public State() {
            super(Constants.THREAD_STATE_COLUMN_ID,
                  Catalog.get("PROP_thread_state"), Catalog.get("HINT_thread_state"), // NOI18N
                  String.class, true, new StringEditor());
        }
    }

    public static final class Suspended extends AbstractColumnModel {
        public Suspended() {
            super(Constants.THREAD_SUSPENDED_COLUMN_ID,
                  Catalog.get("PROP_thread_suspended"), Catalog.get("HINT_thread_suspended"), // NOI18N
                  Boolean.TYPE, false, null);
        }
    }

    public static final class Priority extends AbstractColumnModel {
        public Priority() {
            super(Constants.PROP_THREAD_PRIORITY,
                  Catalog.get("PROP_priority"), Catalog.get("HINT_priority"), // NOI18N
                  Integer.class, false, new StringEditor());
        }
    }

    public static final class Lwp extends AbstractColumnModel {
        public Lwp() {
            super(Constants.PROP_THREAD_LWP,
                  Catalog.get("PROP_lwp"), Catalog.get("HINT_lwp"), // NOI18N
                  Object.class, false, new StringEditor());
        }
    }

    public static final class StartupFlags extends AbstractColumnModel {
        public StartupFlags() {
            super(Constants.PROP_THREAD_STARTUP_FLAGS,
                  Catalog.get("PROP_startup_flags"), Catalog.get("HINT_startup_flags"), // NOI18N
                  Object.class, false, new StringEditor());
        }
    }

    public static final class ExecutingFunction extends AbstractColumnModel {
        public ExecutingFunction() {
            super(Constants.PROP_THREAD_EXECUTING_FUNCTION,
                  Catalog.get("PROP_executing_function"), Catalog.get("HINT_executing_function"), // NOI18N
                  Object.class, true, new StringEditor());
        }
    }

    public static final class StartFunction extends AbstractColumnModel {
        public StartFunction() {
            super(Constants.PROP_THREAD_START_FUNCTION,
                  Catalog.get("PROP_start_function"), Catalog.get("HINT_start_function"), // NOI18N
                  Object.class, true, new StringEditor());
        }
    }

    public static final class Address extends AbstractColumnModel {
        public Address() {
            super(Constants.PROP_THREAD_ADDRESS,
                  Catalog.get("PROP_thread_address"), Catalog.get("HINT_thread_address"), // NOI18N
                  Object.class, false, new StringEditor());
        }
    }

    public static final class Size extends AbstractColumnModel {
        public Size() {
            super(Constants.PROP_THREAD_SIZE,
                  Catalog.get("PROP_size"), Catalog.get("HINT_size"), // NOI18N
                  Integer.class, false, new StringEditor());
        }
    }

    public static final class Id extends AbstractColumnModel {
        public Id() {
            super(Constants.PROP_THREAD_ID,
                  Catalog.get("PROP_id"), Catalog.get("HINT_id"), // NOI18N
                  String.class, false, new StringEditor());
        }
    }

    public static final class File extends AbstractColumnModel {
        public File() { // GDB 
            super(Constants.PROP_THREAD_FILE,
                  Catalog.get("PROP_file"), Catalog.get("HINT_file"), // NOI18N
                  String.class, false, new StringEditor());
        }
    }

    public static final class Line extends AbstractColumnModel {
        public Line() { // GDB
            super(Constants.PROP_THREAD_LINE,
                  Catalog.get("PROP_line"), Catalog.get("HINT_line"), // NOI18N
                  String.class, false, new StringEditor());
        }
    }
}
