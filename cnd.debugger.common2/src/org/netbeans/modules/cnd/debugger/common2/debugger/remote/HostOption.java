/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
