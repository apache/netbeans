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
package org.netbeans.modules.cnd.debugger.common2.ui.processlist;


import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfo;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfoDescriptor;
import org.netbeans.modules.cnd.debugger.common2.utils.ProcessListSupport;
import org.openide.nodes.AbstractNode;

public interface ProcessPanelCustomizer extends Comparator<AbstractNode> {

    String getDisplayName(ProcessInfo info);

    List<ProcessInfoDescriptor> getValues(ProcessInfo info);

    List<ProcessInfoDescriptor> getHeaders(ProcessListSupport.Provider provider);

    String getOutlineHeaderName();

}
