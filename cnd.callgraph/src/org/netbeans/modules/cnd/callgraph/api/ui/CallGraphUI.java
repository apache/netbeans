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

package org.netbeans.modules.cnd.callgraph.api.ui;

import java.util.Collection;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.callgraph.api.Call;

/**
 *
 */
public interface CallGraphUI {
    /**
     * if method returns true the call graph shows tree and graph views.
     * if method returns false the call graph shows only tree view.
     * @return 
     */
    boolean showGraph();
    
    Catalog getCatalog();
    
    JPanel getContextPanel(Call call);    
    
    Collection<CallGraphAction> getActions(CallGraphActionEDTRunnable runnable);
}
