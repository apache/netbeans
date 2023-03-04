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
public interface BeforeBuildActionHook {
    /**
     * This method is called before action execution.
     * An enriched context can be provided by this method which is going to be used further down
     * in the execution chain.
     *
     * @param action the name of the action which is to be processed.
     * @param context the action context.
     * @param out messages written to this stream is going to be displayed on
     *        the IO tab of the build.
     * @return  the same or an enriched context.
     */
    Lookup beforeAction(final String action, final Lookup context, final PrintWriter out);

}
