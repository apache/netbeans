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
package org.openide.windows;

import java.util.Set;


/**
 * Represents window group. The notion of window group is defined by
 * <a href="https://netbeans.apache.org/projects/ui/ws/ws_spec#s37">Window system UI specification document</a>.
 * The concept of window group explains <a href="https://netbeans.apache.org/projects/platform/core/windowsystem/changes#s23">API changes document</a>.
 *
 * <p>
 * <b><font color="red"><em>Importatnt note: Do not provide implementation of this interface unless you are window system provider!</em></font></b>
 *
 * @author  Peter Zavadsky
 * @since 4.13
 */
public interface TopComponentGroup {
    /** Opens all TopComponent's belonging to this group which have opening flag
     * switched on. */
    public void open();

    /** Closes all TopComponent's belonging to this group which have closing flag
     * switched on. */
    public void close();

    //    /** Set of TopComponentS belonging to this group. */
    //    public Set getTopComponents(); // TEMP
}
