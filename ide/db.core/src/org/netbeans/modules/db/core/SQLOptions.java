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

package org.netbeans.modules.db.core;

import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Andrei Badea
 */
public class SQLOptions  {
    private static SQLOptions INSTANCE = new SQLOptions();
    private static final String PROP_FETCH_STEP = "fetchStep"; // NOI18N
    private static final int DEFAULT_FETCH_STEP = 200;
    private static final String PROP_MAX_ROWS = "maxRows";
    private static final int DEFAULT_MAX_ROWS = 200000;

    public static final String PROP_KEEP_OLD_RESULT_TABS = "keepOldResultTabs"; // NOI18N

    public static SQLOptions getDefault() {
        return INSTANCE;
    }
    
    public String displayName() {
        return NbBundle.getMessage(SQLOptions.class, "LBL_SQLOptions");
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(SQLOptions.class);
    }
        
    public int getFetchStep() {
        return getPreferences().getInt(PROP_FETCH_STEP, DEFAULT_FETCH_STEP);
    }
    
    public void setFetchStep(int value) {
        getPreferences().putInt(PROP_FETCH_STEP, value);
    }   
    
    public int getMaxRows() {
        return getPreferences().getInt(PROP_MAX_ROWS, DEFAULT_MAX_ROWS);
    }
    
    public void setMaxRows(int rows) {
        getPreferences().putInt(PROP_MAX_ROWS, rows);
    }
    public boolean isKeepOldResultTabs() {
        return getPreferences().getBoolean(PROP_KEEP_OLD_RESULT_TABS, false);
    }

    public void setKeepOldResultTabs(boolean keepOldTabs) {
        getPreferences().putBoolean(PROP_KEEP_OLD_RESULT_TABS, keepOldTabs);
    }

}
