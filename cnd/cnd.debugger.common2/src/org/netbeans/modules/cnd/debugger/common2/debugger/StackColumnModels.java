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
 *	META-INF/debugger/netbeans-DbxDebuggerEngine/CallStackView/
 *	org.netbeans.spi.viewmodel.ColumnModel
 *	NOTE: Use '...debugger.StackColumnModels$Location'
 */

public final class StackColumnModels {

    public static final class Location extends AbstractColumnModel {
	public Location() {
	     super(Constants.PROP_FRAME_LOCATION,
		   Catalog.get("PROP_frame_location"), // NOI18N
		   Catalog.get("HINT_frame_location"), String.class, false, new StringEditor()); // NOI18N
	}
    }

    public static final class Number extends AbstractColumnModel {

	public Number() {
	     super(Constants.PROP_FRAME_NUMBER,
		   Catalog.get("PROP_frame_number"), // NOI18N
		  Catalog.get("HINT_frame_number"), String.class, false, new StringEditor()); // NOI18N
	}
    }

    public static final class Optimized extends AbstractColumnModel {

	public Optimized() {
	     super(Constants.PROP_FRAME_OPTIMIZED,
		   Catalog.get("PROP_frame_optimized"), // NOI18N
		  Catalog.get("HINT_frame_optimized"), String.class, false, new StringEditor()); // NOI18N
	}
    }

    public static final class CurrentPC extends AbstractColumnModel {

	public CurrentPC() {
	     super(Constants.PROP_FRAME_CURRENT_PC,
		   Catalog.get("PROP_frame_current_pc"), // NOI18N
		  Catalog.get("HINT_frame_current_pc"), String.class, false, new StringEditor()); // NOI18N
	}
    }

    public static final class LoadObj extends AbstractColumnModel {

	public LoadObj() {
	     super(Constants.PROP_FRAME_LOADOBJ,
		   Catalog.get("PROP_frame_loadobj"), // NOI18N
		  Catalog.get("HINT_frame_loadobj"), String.class, false, new StringEditor()); // NOI18N
	}
    }
}
