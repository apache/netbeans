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

package org.netbeans.modules.db.api.sql.execute;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.db.core.SQLOptions;

/**
 * Configurable options for SQL execution
 *
 * @author David Van Couvering
 */
public class SQLExecuteOptions {

    private static final SQLExecuteOptions DEFAULT = new SQLExecuteOptions();

    public static final String PROP_KEEP_OLD_TABS = SQLOptions.PROP_KEEP_OLD_RESULT_TABS;

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public static SQLExecuteOptions getDefault() {
        return DEFAULT;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public boolean isKeepOldResultTabs() {
        return SQLOptions.getDefault().isKeepOldResultTabs();
    }

    public void setKeepOldResultTabs(boolean keepOldResultTabs) {
        boolean oldKeepOldResultTabs = SQLOptions.getDefault().isKeepOldResultTabs();
        if (oldKeepOldResultTabs != keepOldResultTabs) {
            SQLOptions.getDefault().setKeepOldResultTabs(keepOldResultTabs);
            pcs.firePropertyChange(PROP_KEEP_OLD_TABS, oldKeepOldResultTabs, keepOldResultTabs);
        }
    }
}
