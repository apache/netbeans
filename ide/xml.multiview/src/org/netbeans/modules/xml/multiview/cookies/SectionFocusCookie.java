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

package org.netbeans.modules.xml.multiview.cookies;

import org.netbeans.modules.xml.multiview.ui.NodeSectionPanel;

public interface SectionFocusCookie  {
    /**
     * Request to set the focus for the specified section panel.
     *
     * @param NodeSectionPanel panel Section panel that need to be focused on
     * @return boolean return true if the focus was able to be set on the identified Object
     */
    public boolean focusSection(NodeSectionPanel panel);
}
