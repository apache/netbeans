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

package org.netbeans.modules.cnd.debugger.dbx.rtc;

import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;
import org.netbeans.modules.cnd.debugger.common2.utils.options.Option;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSetSupport;

public class RtcOptionSet extends OptionSetSupport {

    private static final Option[] options = {
	RtcOption.RTC_ACCESS_ENABLE,
	RtcOption.RTC_ENABLE_AT_DEBUG,
	RtcOption.RTC_LEAKS_MEMUSE_ENABLE,
	RtcOption.RTC_AUTO_CONTINUE,
	RtcOption.RTC_ERROR_LOG_FILENAME,
	RtcOption.RTC_EXPERIMENT_NAME,
	RtcOption.RTC_EXPERIMENT_DIR,
	RtcOption.RTC_CUSTOM_STACK_MATCH2,
	RtcOption.RTC_CUSTOM_STACK_FRAMES2,
	RtcOption.RTC_AUTO_SUPPRESS,
	RtcOption.RTC_ERROR_LIMIT,
	// RtcOption.RTC_SKIP_PATCH,
	RtcOption.RTC_INHERIT,
	RtcOption.RTC_BIU_AT_EXIT,
	RtcOption.RTC_MEL_AT_EXIT,
    };


    public RtcOptionSet() {
	setup(options);
    } 

    public RtcOptionSet(RtcOptionSet that) {
	setup(options);
	copy(that);
    } 

    public OptionSet makeCopy() {
	return new RtcOptionSet(this);
    }

    // interface OptionSet
    public void save() {
	// noop
    }

    // interface OptionSet
    public void open() {
	// noop
    }

    // interface OptionSet
    public String tag() {
	// We renamed the class form RtcOptions to RtcProfile but need to
	// keep the tag the same for bwd compatibility
	return "RtcOptions";		// NOI18N
    }

    // interface OptionSet
    public String description() {
	return "RTC options";		// NOI18N
    }
}

