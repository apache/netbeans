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
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionValue;
import org.netbeans.modules.cnd.debugger.common2.utils.options.Validity;
import org.netbeans.modules.cnd.debugger.common2.utils.options.CatalogDynamic;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

public class HostOption extends Option {

    static CatalogDynamic catalog = new CatalogDynamic(HostOption.class);

    public HostOption(String inName, String[] inValues,
			   String inDefaultValue, boolean inIsEngineOption,
			   Type inType, boolean hasTooltip) {
	super(inName, catalog, inValues, inDefaultValue, inIsEngineOption, inType,
	      hasTooltip, false);
    }

    public HostOption(String inName, String inDefaultValue,
                           boolean inIsEngineOption, Type inType, boolean hasTip) {
        super(inName, catalog, null, inDefaultValue, inIsEngineOption, inType, hasTip, false);
    }


    /**
     * Given the option and a value, return the sub option
     * this function is created for special cases where an option
     * is enabled/disabled by the behaviour of another option
     */

    // interface Option
    @Override
    public Option getSubOption(String value) {
	return null;   
    }

    /** Hack to fix width rendering */
    // interface Option
    @Override
    public boolean verticalLayout() {
	return false;
    }

    /** Hack to fix width rendering */
    // interface Option
    @Override
    public boolean overrideHasLabel() {
	return true;
    }
    
    
    /**
     * return the sub option if any
     */
    // interface Option
    @Override
    public Option getSubOption() {
	return null;
    }


    /**
     * returns if this is a sub option or not
     */
    // interface Option
    @Override
    public boolean isSubOption() {
	return false;
    }

    // interface Option
    @Override
    public Validity getValidity(String text) {
	return Validity.TRUE;
    }

    // interface Option
    @Override
    protected boolean canWrite(OptionSet optionSet) {
	return true;
    }

    // interface Option
    @Override
    public boolean isTrim() {
	return false;
    }

    // interface Option
    @Override
    public boolean persist(OptionValue value) {
	return true;
    }

    private static String default_platform = Platform.local().name();

    public static final HostOption HOST_PROP_PLATFORM =
	new HostOption("platform",     // NOI18N //name   
		       new String[] {Platform.Solaris_Sparc.name(), // NOI18N
				     Platform.Solaris_x86.name(), // NOI18N
				     Platform.Linux_x86.name() }, // NOI18N
		       default_platform,  //default value
		       false, // is engine option
		       Type.COMBO_BOX, true);	//type, hasToolTip

    public static final  HostOption HOST_PROP_LOCATION  =
	new HostOption("studio_location", // NOI18N //name  
		       null,
		       false, // is engine option
		       Type.TEXT_AREA, true);  //type , hasToolTip
		       // Until we have a remote file chooser
		       // DIRECTORY, true);

    private static String username = System.getProperty("user.name"); // NOI18N 

    public static final HostOption HOST_PROP_LOGINNAME  =
	new  HostOption("login_name", // NOI18N //name  
			username,  // NOI18N 
			false, //is engine option
			Type.TEXT_AREA, true);  //type , hasToolTip

    public static final HostOption HOST_PROP_REMEMBER_PASSWORD  =
	new  HostOption("remember_password", // NOI18N //name
			"off",  // NOI18N
			false, //is engine option
			Type.CHECK_BOX, true) {  //type , hasToolTip

	    @Override
	    protected boolean canWrite(OptionSet optionSet) {
		// readonly if we're localhost
		if (Host.localhost.equals(HOST_PROP_HOSTNAME.getCurrValue(optionSet)))
		    return false;
		else
		    return true;
	    }
	};

    public static final HostOption HOST_PROP_SSH_PORT  =
	new  HostOption("ssh_port", // NOI18N //name
			"22",  // NOI18N
			false, //is engine option
			Type.TEXT_AREA, true) {  //type , hasToolTip

	    @Override
	    public Validity getValidity(String text) {
		try {
		    Integer.parseInt(text);
		} catch(NumberFormatException e) {
		    String err = Catalog.format("ERROR_BADPORT"); // NOI18N
		    return Validity.FALSE(err);
		}
		return Validity.TRUE;
	    }
    };

    public static final HostOption HOST_PROP_HOSTNAME  =
	new HostOption("host_name", // NOI18N //name  
		       null,
		       false, // is engine option
		       Type.TEXT_AREA, true) { //type , hasToolTip

	    // override HostOption
	    @Override
	    public Validity getValidity(String text) {

		// Disallow 'localhost' as a hostname for any record so the
		// archetypal one stays unique.
		// Works hand-in-hand with canWrite()

		if (IpeUtils.isEmpty(text)) {
		    String err = Catalog.get("ERROR_NOEMPTYHOSTNAME");// NOI18N
		    return Validity.FALSE(Catalog.get("ERROR_NOEMPTYHOSTNAME")); // NOI18N

		} else if (Host.localhost.equals(text)) {
		    String err = Catalog.format("ERROR_NOLOCALHOST", // NOI18N
						Host.localhost);
		    return Validity.FALSE(err);
		} else {
		    return Validity.TRUE;
		}
	    }

	    // override HostOption
	    @Override
	    protected boolean canWrite(OptionSet optionSet) {

		// Disallow changing of the name 'localhost'
		// Works hand-in-hand with getValidity()

		if (Host.localhost.equals(getCurrValue(optionSet)))
		    return false;
		else
		    return true;
	    }
	};
}
