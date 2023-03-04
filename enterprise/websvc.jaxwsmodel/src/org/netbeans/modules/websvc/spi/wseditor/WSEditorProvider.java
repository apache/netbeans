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

/*
 * WSEditorProvider.java
 *
 * Created on March 9, 2006, 2:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.spi.wseditor;

import org.netbeans.modules.websvc.api.wseditor.WSEditor;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Roderico Cruz
 */
public interface WSEditorProvider {

    /** This is used to determine if this editor should be displayed.
     *
     * @param node node for which WS editor should be enabled or not.
     * @return
     */
    boolean enable(Node node);
    
    /** Create an instance of the editor component
     * 
     * @param lookupContext lookup context, e.g. node lookup
     * @return
     */
     WSEditor createWSEditor(Lookup lookupContext);
}
