/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.terminal.nb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.terminal.support.TerminalPinSupport;
import org.netbeans.modules.terminal.support.TerminalPinSupport.TerminalCreationDetails;
import org.netbeans.modules.terminal.support.TerminalPinSupport.TerminalDetails;
import org.netbeans.modules.terminal.support.TerminalPinSupport.TerminalPinningDetails;
import org.openide.util.NbPreferences;

/**
 *
 * @author ilia
 */
public class TerminalPinnedTabOptions {

    private static final Preferences defaultPrefs = NbPreferences.forModule(TerminalPinnedTabOptions.class).node("TerminalPinTabs"); //NOI18N
    private static final TerminalPinnedTabOptions INSTANCE = new TerminalPinnedTabOptions(defaultPrefs);

    private static final String DELIMITER = "_";//NOI18N
    private static final String CUSTOM_TITLE = "customTitle";//NOI18N
    private static final String CWD = "cwd";//NOI18N
    private static final String EXEC_ENV = "execEnv";//NOI18N
    private static final String PWD_FLAG = "pwdFlag";//NOI18N
    private static final String TITLE = "title";//NOI18N

    private final Preferences prefs;

    private TerminalPinnedTabOptions(Preferences prefs) {
	this.prefs = prefs;
    }

    public static TerminalPinnedTabOptions getDefault() {
	return INSTANCE;
    }

    public void persist(TerminalDetails details) {
	TerminalPinSupport.TerminalCreationDetails creationDetails = details.getCreationDetails();
	TerminalPinSupport.TerminalPinningDetails pinningDetails = details.getPinningDetails();
	long id = creationDetails.getId();

	writeProp(id, CUSTOM_TITLE, String.valueOf(pinningDetails.isCustomTitle()));
	writeProp(id, CWD, pinningDetails.getCwd());
	writeProp(id, EXEC_ENV, creationDetails.getExecEnv());
	writeProp(id, PWD_FLAG, String.valueOf(creationDetails.isPwdFlag()));
	writeProp(id, TITLE, pinningDetails.getTitle());
    }

    public void forget(long id) {
	removeProp(id, CUSTOM_TITLE);
	removeProp(id, CWD);
	removeProp(id, EXEC_ENV);
	removeProp(id, PWD_FLAG);
	removeProp(id, TITLE);
    }

    public void clear() {
	try {
	    prefs.clear();
	} catch (BackingStoreException ex) {
	}
    }

    private void writeProp(long id, String postfix, String value) {
	String key = Long.toString(id).concat(DELIMITER).concat(postfix);
	if (value != null) {
	    prefs.put(key, value);
	} else {
	    prefs.put(key, "");
	}
    }

    private void removeProp(long id, String postfix) {
	String key = Long.toString(id).concat(DELIMITER).concat(postfix);
	prefs.remove(key);
    }

    public List<TerminalDetails> readStoredDetails() {
	Map<Long, TerminalDetails> map = new HashMap<Long, TerminalDetails>();

	try {
	    String[] keys = prefs.keys();

	    for (int i = 0; i < keys.length;) {
		final long id = getId(keys[i]);
		final boolean customTitle = Boolean.valueOf(prefs.get(keys[i], ""));
		i++;
		final String cwd = prefs.get(keys[i], "");
		i++;
		final String execEnv = prefs.get(keys[i], "");
		i++;
		final boolean pwdFlag = Boolean.valueOf(prefs.get(keys[i], ""));
		i++;
		final String title = prefs.get(keys[i], "");
		i++;

		map.put(id, new TerminalDetails(
			TerminalCreationDetails.create(id, execEnv, pwdFlag),
			TerminalPinningDetails.create(customTitle, title, cwd, false)
		));
	    }
	} catch (Exception ex) {
	    try {
		prefs.clear();
	    } catch (BackingStoreException ex1) {
	    }
	}
	return new ArrayList<TerminalDetails>(map.values());
    }

    private long getId(String value) {
	String[] split = value.split(DELIMITER);
	return Long.parseLong(split[0]);
    }
}
