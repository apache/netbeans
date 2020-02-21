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

package org.netbeans.modules.remote.api.ui;

import java.beans.PropertyEditor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.api.ServerRecord;
import org.openide.util.NotImplementedException;

/**
 * Displayes Edit Servers List dialog.
 */
public final class ServerListUI {

    public interface Accessor<T> {
        T get();
        void set(T value);
    }

    private ServerListUI() {}

    /**
     * Shows servers list dialog.
     * @param selectedRecord currently selected serverRecord.
     * Can be null; in this case "localhost" will be selected.
     * @return selected ServerRecord if user changed selectioh and pressed enter, otherwise null
     */
    public static ServerRecord showServerListDialog(ServerRecord selectedRecord) {
        return showServerListDialog(selectedRecord.getExecutionEnvironment());
    }

    /**
     * Shows servers list dialog.
     * @param selectedRecord currently selected serverRecord.
     * Can be null; in this case "localhost" will be selected.
     * @return selected ServerRecord if user changed selectioh and pressed enter, otherwise null
     */
    public static ServerRecord showServerListDialog(ExecutionEnvironment selectedEnv) {
        throw new NotImplementedException();
    }

    /**
     * To be used as a custom property editor
     */
    public static PropertyEditor getCustomPropertyEditor(Accessor<ServerRecord> selectedRecordAccessor) {
        throw new NotImplementedException();
    }
}
