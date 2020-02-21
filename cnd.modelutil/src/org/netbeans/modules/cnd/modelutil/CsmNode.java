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

package org.netbeans.modules.cnd.modelutil;

import java.lang.ref.WeakReference;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 */
public class CsmNode extends AbstractCsmNode {
    
    private WeakReference<CsmObject> data = null;
    
    public CsmNode(Children children, Lookup lookup) {
        super(children, lookup);
    }

    public CsmNode(Children children) {
        this(children, null);
    }

    public void setData(CsmObject data) {
	if( data == null ) {
	    this.data = null;
	}
	else {
	    this.data = new WeakReference<CsmObject>(data);
	}
    }
    
    @Override
    public CsmObject getCsmObject() {
	return (data == null) ? null : data.get();
    }

}
