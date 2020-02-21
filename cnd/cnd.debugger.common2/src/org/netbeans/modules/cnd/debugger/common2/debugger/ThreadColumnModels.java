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
