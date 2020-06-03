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
 *	META-INF/debugger/netbeans-DbxDebuggerEngine/LocalView/
 *	META-INF/debugger/netbeans-DbxDebuggerEngine/WatchView/
 *	org.netbeans.spi.viewmodel.ColumnModel
 */

public final class VariableColumnModels {

    public static final class ValueColumn extends AbstractColumnModel {

  	public ValueColumn() {
	    super(Constants.PROP_LOCAL_VALUE,
		  Catalog.get("PROP_value"),  // NOI18N
		  Catalog.get("HINT_value"), String.class, true, null); // NOI18N
	}
    }

    public static final class TypeColumn extends AbstractColumnModel {
  	
	public TypeColumn() {
	    super(Constants.PROP_LOCAL_TYPE, Catalog.get("PROP_type"),  // NOI18N
		  Catalog.get("HINT_type"), Object.class, false, new StringEditor()); // NOI18N
	}
    }

    public static final class DTypeColumn extends AbstractColumnModel {
  	
	public DTypeColumn() {
	    super(Constants.PROP_LOCAL_DTYPE, Catalog.get("PROP_dtype"), // NOI18N
		  Catalog.get("HINT_dtype"), Object.class, false, new StringEditor()); // NOI18N
	}
    }

    public static final class AddressColumn extends AbstractColumnModel {

	public AddressColumn() {
	    super(Constants.PROP_LOCAL_ADDRESS, Catalog.get("PROP_address"), // NOI18N
		  Catalog.get("HINT_address"), Object.class, false, new StringEditor()); // NOI18N
	}
    }
/* not used
    public static final class WatchValueColumn extends AbstractColumnModel {

  	public WatchValueColumn() {
	    super(Constants.PROP_WATCH_VALUE,
		  Catalog.get("PROP_value"), // NOI18N
		  Catalog.get("HINT_value"), String.class, true, null); // NOI18N
	}
    }

    public static final class WatchTypeColumn extends AbstractColumnModel {
  	
	public WatchTypeColumn() {
	    super(Constants.PROP_WATCH_TYPE, Catalog.get("PROP_type"), // NOI18N
		  Catalog.get("HINT_type"), Object.class, false, new StringEditor()); // NOI18N
	}
    }
*/
}
