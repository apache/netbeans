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
 * MenuFactory.java
 *
 * Created on January 24, 2004, 1:06 AM
 */

package org.netbeans.actions.engine.spi;

import java.util.Map;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

/** Factory for producing real menus given a container context name.  It
 * will look up the contents on the ActionProvider, and produce a menu
 * from the result.
 *
 * @author  Tim Boudreau
 */
public interface MenuFactory {
    public JMenu createMenu (String containerContext);
    public void update (String containerContext);
}
