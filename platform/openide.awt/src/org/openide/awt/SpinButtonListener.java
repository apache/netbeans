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


/**
* Listener for SpinButton component.
* @deprecated Obsoleted by <code>javax.swing.JSpinner</code> in JDK 1.4
* @author Jan Jancura
* @version 0.10 Nov 17, 1997
*/
@Deprecated
public interface SpinButtonListener {
    /**
    * Is invoked when button up / left is clicked.
    */
    public void moveUp();

    /**
    * Is invoked when button down / right is clicked.
    */
    public void moveDown();

    /**
    * Is invoked when button up is clicked.
    */
    public void changeValue();
}
