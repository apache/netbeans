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
 * DDTableModelEditor.java
 */
package org.netbeans.modules.j2ee.sun.ide.editors.ui;

/**
 * This interface represents a row editor. The editor can at least operate
 * on the data provided by model returning this interface.
 *
 * @author  cwebster
 * @version 1.0
 */

import javax.swing.*;

public interface DDTableModelEditor {

    /**
     * Return value from the editor. This method will not return null. The
     * following pre-condition must always hold setValue(oref);
     * oref != getValue().
     * @return object representing the newly edited value. 
     */
    public Object getValue();
    
    /**
     * set the displayed value. This method must handle oref == null. If the 
     * oref cannot be displayed by this editor, the display value is 
     * unspecified.
     */
    public void setValue(java.lang.Object oref);
    
    /**
     * provide display instance.
     * @return panel suitable for row editing
     */
    public JPanel getPanel();
    
}

