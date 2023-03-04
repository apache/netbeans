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

package org.netbeans.modules.gradle.spi.actions;

import java.io.PrintWriter;
import org.openide.util.Lookup;

/**
 *
 * @author Laszlo Kishalmi
 */
public interface BeforeReloadActionHook {
    /**
     * This method is called after the build action has been finished but before
     * the project reload (if reload would be enabled by the action reload rules).
     *
     * @param action the name of the action which is to be processed.
     * @param context the action context.
     * @param result the result of the build. 0 when the build is successful.
     * @param out the OutputWriter for the I/O tab of the build,
     *        it can be {@code null} on reload only actions.
     * @return  {@code true} if the project can be reloaded.
     */
    boolean beforeReload(final String action, final Lookup context, int result, PrintWriter out);

}
