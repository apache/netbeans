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

package org.netbeans.modules.cnd.utils.filters;

import org.netbeans.modules.cnd.utils.FileAndFileObjectFilter;
import org.openide.util.NbBundle;

public class PeExecutableFileFilter extends FileAndFileObjectFilter {

    private static final String suffixes[] = {"exe"}; // NOI18N
    private static PeExecutableFileFilter instance = null;

    public PeExecutableFileFilter() {
	super();
    }

    public static PeExecutableFileFilter getInstance() {
	if (instance == null) {
            instance = new PeExecutableFileFilter();
        }
	return instance;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(PeExecutableFileFilter.class, "PE_EXECUTABLE_FILTER"); // NOI18N
    }
    
    @Override
    protected String[] getSuffixes() {
        return suffixes;
    }
}
