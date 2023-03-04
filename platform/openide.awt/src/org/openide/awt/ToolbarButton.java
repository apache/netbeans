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
package org.openide.awt;

import java.awt.event.MouseEvent;

/**
 * An implementation of a toolbar button.
 * @deprecated This class was a workaround for JDK 1.2 era Windows Look and
 * feel issues.  All implementation code has been removed.  It is here only
 * for backward compatibility.
 */
@Deprecated
public class ToolbarButton extends javax.swing.JButton {
    /** generated Serialized Version UID */
    private static final long serialVersionUID = 6564434578524381134L;

    public ToolbarButton() {
    }

    public ToolbarButton(javax.swing.Icon icon) {
        super(icon);
    }

    /** just made public */
    public @Override void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);
    }

}
