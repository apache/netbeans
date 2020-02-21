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

package org.netbeans.modules.cnd.debugger.common2.debugger.remote;

import org.netbeans.modules.cnd.debugger.common2.utils.options.Option;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSetSupport;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;

public class HostOptionSet extends OptionSetSupport {

    private static final Option[] options = {
	HostOption.HOST_PROP_PLATFORM,
	HostOption.HOST_PROP_HOSTNAME,
	HostOption.HOST_PROP_LOCATION,
	HostOption.HOST_PROP_LOGINNAME,
	HostOption.HOST_PROP_SSH_PORT,
	HostOption.HOST_PROP_REMEMBER_PASSWORD,
    };
    
    public HostOptionSet () {
	setup(options);
    } 

    public HostOptionSet (HostOptionSet that) {
	setup(options);
	copy(that);
    } 

    @Override
    public OptionSet makeCopy() {
	return new HostOptionSet (this);
    }

    // interface OptionSet
    @Override
    public void save() {
	// noop
    }

    // interface OptionSet
    @Override
    public void open() {
	// noop
    }

    // interface OptionSet
    @Override
    public String tag() {
	return "Host_Settings";		// NOI18N
    }

    // interface OptionSet
    @Override
    public String description() {
	return "remote host settings";	// NOI18N
    }
}

