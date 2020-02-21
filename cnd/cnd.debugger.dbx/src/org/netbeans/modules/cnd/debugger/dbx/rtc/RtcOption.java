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

import org.netbeans.modules.cnd.debugger.common2.utils.options.CatalogDynamic;
import org.netbeans.modules.cnd.debugger.common2.utils.options.Validity;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionValue;
import org.netbeans.modules.cnd.debugger.common2.utils.options.Option;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;

public class RtcOption extends Option {

    static CatalogDynamic catalog = new CatalogDynamic(RtcOption.class);

    private RtcOption(String inName, String[] inValues,
			   String inDefaultValue, boolean isClientOption,
			   Type inType, boolean hasTooltip) {

	super(inName, catalog, inValues, inDefaultValue, isClientOption, inType,
	      hasTooltip, false);
    }

    /**
     * This constructor is here for the options where
     * there are no defined values, but only default value
     * mostly for TextArea options
     */
    private RtcOption(String inName, String inDefaultValue,
			   boolean isClientOption, Type inType, boolean hasTip) {
	super(inName, catalog, null, inDefaultValue, isClientOption, inType, hasTip, false);
    }

    /**
     * Given the option and a value, return the sub option
     * this function is created for special cases where an option
     * is enabled/disabled by the behaviour of another option
     */

    // interface Option
    public Option getSubOption(String value) {

	if (this == RTC_AUTO_CONTINUE && value.equals("on")) // NOI18N
	    return RTC_ERROR_LOG_FILENAME ;

	/* OLD
	else if (this == RTC_CUSTOM_STACK_FRAMES && value.equals("custom"))
	    return RTC_CUSTOM_STACK_FRAMES_VALUE;

	else if (this == RTC_CUSTOM_STACK_MATCH && value.equals("custom"))
	    return RTC_CUSTOM_STACK_MATCH_VALUE;
	*/

	return null;   
    }

    /** Hack to fix width rendering */
    // interface Option
    public boolean verticalLayout() {
	// OLD return (this == RUN_IO);
	return false;
    }

    /** Hack to fix width rendering */
    // interface Option
    public boolean overrideHasLabel() {
	// OLD return (this != RUN_IO);
	return true;
    }
    
    
    /**
     * return the sub option if any
     */
    // interface Option
    public Option getSubOption() {
	if (this == RTC_AUTO_CONTINUE)
	    return RTC_ERROR_LOG_FILENAME ;
	/* OLD
	else if (this == RTC_CUSTOM_STACK_MATCH)
	    return RTC_CUSTOM_STACK_MATCH_VALUE;
	else if (this == RTC_CUSTOM_STACK_FRAMES)
	    return RTC_CUSTOM_STACK_FRAMES_VALUE;
	*/
	return null;
    }


    /**
     * returns if this is a sub option or not
     */
    // interface Option
    public boolean isSubOption() {
	if (this == RTC_ERROR_LOG_FILENAME)
	    return true;
	/* OLD
	if (this == RTC_CUSTOM_STACK_MATCH_VALUE)
	    return true;
	if (this == RTC_CUSTOM_STACK_FRAMES_VALUE)
	    return true;
	*/
	return false;
    }

    private final static String SUFFIX = "." + RtcDataObject.EXTENSION; // NOI18N

    // interface Option
    public Validity getValidity(String text) {
	if (this == RTC_EXPERIMENT_NAME) {
	    String why = Catalog.format("WHY_RTC_EXPERIMENT_NAME", SUFFIX);

	    text = text.trim();

	    if (IpeUtils.isEmpty(text)) 
		return Validity.TRUE;
	    else if (text.endsWith(SUFFIX) && !text.equals(SUFFIX))
		return Validity.TRUE(why);
	    else
		return Validity.FALSE(why);

	} else {
	    return Validity.TRUE;
	}
    }

    // interface Option
    public boolean isTrim() {
	if (this == RTC_EXPERIMENT_NAME)
	    return true;
	else
	    return false;
    }


    // interface Option
    public boolean persist(OptionValue value) {

	/* LATER?

	This is tricky. The notion of what is default might change with time
	either as dbx changes it's defaults or as we start considering what's
	in .dbxrc as overriden defaults

	if (isSameAsDefault())
	    return false;
	*/

	return true;
    }

    

    public static final RtcOption RTC_ACCESS_ENABLE  =
	new RtcOption( // NOI18N
		    "rtc_access_enable",   // NOI18N //name
		    new String[] {"on", "off"}, // NOI18N //values
		    NativeDebuggerManager.isStandalone() ? "off" : "on",  // NOI18N //default value
		    false, // isClientOption
		    Type.CHECK_BOX, false); //type

    public static final RtcOption RTC_AUTO_CONTINUE  =
	new RtcOption( // NOI18N
			    "rtc_auto_continue",   // NOI18N //name
			    new String[] {"on", "off"}, // NOI18N //values
			    "off",  // NOI18N //default value
			    true, // isClientOption
			    Type.CHECK_BOX, false); //type



    /* OLD
    public static final RtcOption RTC_MEMUSE_ENABLE  =
	new RtcOption( // NOI18N
			    "rtc_memuse_enable",   // NOI18N //name
			    new String[] {"on", "off"}, // NOI18N //values
			    "off",  // NOI18N //default value
			    false, // isClientOption
			    CHECK_BOX, false); //type

    public static final RtcOption RTC_LEAKS_ENABLE  =
	new RtcOption( // NOI18N
			    "rtc_leaks_enable",   // NOI18N //name
			    new String[] {"on", "off"}, // NOI18N //values
			    "off",  // NOI18N //default value
			    false, // isClientOption
			    CHECK_BOX, false); //type
    */
    public static final RtcOption RTC_LEAKS_MEMUSE_ENABLE  =
	new RtcOption( // NOI18N
		    "rtc_leaks_memuse_enable",   // NOI18N //name
		    new String[] {"on", "off"}, // NOI18N //values
		    NativeDebuggerManager.isStandalone() ? "off" : "on",  // NOI18N //default value
		    false, // isClientOption
		    Type.CHECK_BOX, false); //type


    public static final RtcOption RTC_EXPERIMENT_NAME  =
	new RtcOption( // NOI18N
			    "rtc_experiment_name",	   // NOI18N //name
			    "",			  // NOI18N //default value
			    false, // isClientOption
			    Type.TEXT_AREA, false);  //type

    public static final RtcOption RTC_EXPERIMENT_DIR  =
	new RtcOption( // NOI18N
			    "rtc_experiment_dir",	   // NOI18N //name
			    ".", // NOI18N //default value
			    false, // isClientOption
			    Type.DIRECTORY, false);  //type




    // The following option is in the OptionSet but doesn't manifest
    // itself in the UI.
    // See also CR 6511959

    public static final RtcOption RTC_ERROR_LOG_FILENAME  =
	new RtcOption( // NOI18N
			    "rtc_error_log_file_name",   // NOI18N //name
			    "",	  // NOI18N //default value
			    true, // isClientOption
			    Type.TEXT_AREA, false);  //type




    //
    // -m The default value of <m> is 3 for C++, 2 otherwise.
    // 

    public static final RtcOption RTC_CUSTOM_STACK_MATCH2 =
	new RtcOption( // NOI18N
			    "RTC_CUSTOM_STACK_MATCH2",		// name // NOI18N
			    new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"},	// NOI18N // values
			    "3",	// NOI18N //default value
			    false,	//  isClientOption
			    Type.RADIO_BUTTON, false);

    /* OLD
    public static final RtcOption RTC_CUSTOM_STACK_MATCH =
	new RtcOption( // NOI18N
			    "RTC_CUSTOM_STACK_MATCH",		// name
			    new String[] {"default", "custom"},	// NOI18N // values
			    "default",	// NOI18N //default value
			    false,	//  isClientOption
			    RADIO_BUTTON, false);

    public static final RtcOption RTC_CUSTOM_STACK_MATCH_VALUE  =
	new RtcOption( // NOI18N
			    "RTC_CUSTOM_STACK_MATCH_VALUE",   // NOI18N //name
			    "2",		  // NOI18N //default value
			    false, // isClientOption
			    TEXT_AREA, false);  //type
    */


    //
    // -n
    // The default value of <n> is 8 or the value of <m> (whichever is larger).
    // Maximum value of <n> is 16.
    //
    public static final RtcOption RTC_CUSTOM_STACK_FRAMES2 =
	new RtcOption( // NOI18N
			    "RTC_CUSTOM_STACK_FRAMES2",		// name // NOI18N
			    new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"},	// NOI18N // values
			    "8",	// NOI18N //default value
			    false,	//  isClientOption
			    Type.RADIO_BUTTON, false);

    /* OLD
    public static final RtcOption RTC_CUSTOM_STACK_FRAMES =
	new RtcOption( // NOI18N
			    "RTC_CUSTOM_STACK_FRAMES",		// name
			    new String[] {"default", "custom"},	// NOI18N // values
			    "default",	// NOI18N //default value
			    false,	//  isClientOption
			    RADIO_BUTTON, false);

    public static final RtcOption RTC_CUSTOM_STACK_FRAMES_VALUE  =
	new RtcOption( // NOI18N
			    "RTC_CUSTOM_STACK_FRAMES_VALUE",   // NOI18N //name
			    "2",		  // NOI18N //default value
			    false, // isClientOption
			    TEXT_AREA, false);  //type
    */

    public static final RtcOption RTC_AUTO_SUPPRESS  =
	new RtcOption( // NOI18N
			    "rtc_auto_suppress",   // NOI18N //name
			    new String[] {"on", "off"}, // NOI18N //values
			    "on",  // NOI18N //default value
			    true, // isClientOption
			    Type.CHECK_BOX, false); //type

    public static final RtcOption RTC_INHERIT  =
	new RtcOption( // NOI18N
			    "rtc_inherit",   // NOI18N //name
			    new String[] {"on", "off"}, // NOI18N //values
			    "off",  // NOI18N //default value
			    true, // isClientOption
			    Type.CHECK_BOX, false); //type
/*
    public static final RtcOption RTC_SKIP_PATCH  =
	new RtcOption( // NOI18N
			    "rtc_skip_patch",   // NOI18N //name
			    "",  // NOI18N //default value
			    true, // isClientOption
			    TEXT_AREA, false);  //type
*/
    public static final RtcOption RTC_ERROR_LIMIT  =
	new RtcOption( // NOI18N
			    "rtc_error_limit",   // NOI18N //name
			    "1000",  // NOI18N //default value
			    true, // isClientOption
			    Type.TEXT_AREA, false);  //type

    public static final RtcOption RTC_BIU_AT_EXIT  =
	new RtcOption( // NOI18N
			    "rtc_biu_at_exit",   // NOI18N //name
			    new String[] {"on", "off"}, // NOI18N //values
			    "on",  // NOI18N //default value
			    true, // isClientOption
			    Type.CHECK_BOX, false); //type

    public static final RtcOption RTC_MEL_AT_EXIT  =
	new RtcOption( // NOI18N
			    "rtc_mel_at_exit",   // NOI18N //name
			    new String[] {"on", "off"}, // NOI18N //values
			    "on",  // NOI18N //default value
			    true, // isClientOption
			    Type.CHECK_BOX, false); //type

    public static final RtcOption RTC_ENABLE_AT_DEBUG  =
	new RtcOption( // NOI18N
			    "RTC_ENABLE_AT_DEBUG",   // NOI18N //name
			    new String[] {"on", "off"}, // NOI18N //values
			    "off",  // NOI18N //default value
			    false, // isClientOption
			    Type.CHECK_BOX, false); //type
}
