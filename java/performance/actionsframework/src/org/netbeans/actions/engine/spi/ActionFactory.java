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
 * ActionFactory.java
 *
 * Created on January 24, 2004, 1:28 AM
 */

package org.netbeans.actions.engine.spi;

import java.util.Map;
import javax.swing.Action;

/** Creates actions on demand only when they need to be invoked.
 *
 * @author  tim
 */
public interface ActionFactory {

    //XXX get rid of the containerCtx param, it's not used

    /** Construct an invokable Swing action.  The action should only be
     * constructed when a user-gesture to invoke has been made and needs to
     * be fulfilled */
    public Action getAction (String action, String containerCtx, Map context);

}
