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

package org.netbeans.modules.cnd.api.model;

import org.openide.util.Lookup;

/**
 * Maintains lists of model listeners of different kind
 */
public abstract class CsmListeners {

    private static CsmListeners DEFAULT;
    
    private static final CsmListeners EMPTY = new Empty();
	    
    public abstract void addModelListener(CsmModelListener listener);

    public abstract void removeModelListener(CsmModelListener listener);
    
    public abstract void addProgressListener(CsmProgressListener listener);
    
    public abstract void removeProgressListener(CsmProgressListener listener);
    
    public abstract void addModelStateListener(CsmModelStateListener listener);
    
    public abstract void removeModelStateListener(CsmModelStateListener listener);
    
    public static CsmListeners getDefault() {
	if( DEFAULT == null ) {
	    DEFAULT = Lookup.getDefault().lookup(CsmListeners.class);
	}
	return (DEFAULT == null) ? EMPTY : DEFAULT;
    }
    
    private static class Empty extends CsmListeners {

	@Override
	public void addModelListener(CsmModelListener listener) {}

	@Override
	public void addProgressListener(CsmProgressListener listener) {}

	@Override
	public void removeModelListener(CsmModelListener listener) {}

	@Override
	public void removeProgressListener(CsmProgressListener listener) {}
	
	@Override
	public void addModelStateListener(CsmModelStateListener listener) {}
    
	@Override
	public void removeModelStateListener(CsmModelStateListener listener) {}
	
    }
}
