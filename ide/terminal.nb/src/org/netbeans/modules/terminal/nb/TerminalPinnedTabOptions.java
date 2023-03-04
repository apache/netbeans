/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
