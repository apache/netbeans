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
package org.netbeans.spi.sendopts;

import org.netbeans.api.sendopts.CommandException;

/** Method in this interface is invoked to finish processing 
 * of options defined by subclass of this interface. <code>public</code>
 * fields annotated by {@link Arg} have been initialized before 
 * the {@link #process(org.netbeans.spi.sendopts.Env)} method is invoked.
 * The subclass needs to have public default constructor.
 * <p class="nonnormative">
 * For headless NetBeans applications: command line argument processing is the
 * last step in the boot sequence; services and classes offered by the platform
 * have been initialized at this point. Application code in {@link #process(org.netbeans.spi.sendopts.Env)} can safely
 * use the platform.
 * </p>
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since 2.20
 */
public interface ArgsProcessor {
    /** 
     * Called after all fields annotated with {@code @}{@link Arg}
     * are filled with appropriate values.
     * 
     * @param env environment for the execution - current working directory,
     *   stdin, stdout, stderr
     * @throws CommandException if the parameters don't make sense, throw an
     *   exception
     */
    public void process(Env env) throws CommandException;
}
