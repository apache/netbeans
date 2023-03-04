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
package org.netbeans.modules.form;

import java.awt.datatransfer.Transferable;

/**
 * Provider of <code>NewComponentDrop</code>s. The provider should
 * be registered in lookup (ideally via <code>META-INF/service</code> directory).
 *
 * @author Jan Stola
 */
public interface NewComponentDropProvider {
 
    /**
     * Processes given <code>transferable</code> and returns the corresponding
     * <code>NewComponentDrop</code>.
     *
     * @param formModel corresponding form model.
     * @param transferable description of transferred data.
     * @return <code>NewComponentDrop</code> that corresponds to given
     * <code>transferable</code> or <code>null</code> if this provider
     * don't understand to or don't want to process this data transfer.
     */
    NewComponentDrop processTransferable(FormModel formModel, Transferable transferable);
    
}
