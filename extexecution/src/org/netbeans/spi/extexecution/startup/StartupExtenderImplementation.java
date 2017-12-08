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
package org.netbeans.spi.extexecution.startup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.openide.util.Lookup;

/**
 * Defines the interface for plugins providing additional arguments for process
 * startup. Typically the server plugin implementor or project will query
 * the arguments via API counterpart {@link StartupExtender}. Of course it is
 * not mandatory to use such arguments and there is no way to force it.
 *
 * @author Petr Hejl
 * @since 1.30
 * @see StartupExtender
 * @see Registration
 */
public interface StartupExtenderImplementation {

    /**
     * Returns the list of arguments to pass to the process for the given
     * start mode.
     *
     * @param context the lookup providing the contract between client
     *             and provider (see {@link StartupExtender#getExtenders(Lookup context, StartupExtender.StartMode mode)}
     *             for details)
     * @param mode the startup mode the client is going to use
     * @return the list of arguments to pass to the process
     */
    @NonNull
    List<String> getArguments(@NonNull Lookup context, @NonNull StartupExtender.StartMode mode);

    /**
     * Annotation used to properly register the SPI implementations.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Registration {

        /**
         * The human readable description of the implementation. May be
         * a bundle key. For example this might be "JRebel", "Profiler" etc.
         *
         * @return human readable description of the implementation
         */
        String displayName();

        /**
         * Modes to which the provider will respond.
         *
         * @return modes to which the provider will respond
         */
        StartupExtender.StartMode[] startMode();

        /**
         * Position of the provider in the list of providers.
         *
         * @return position of the provider in the list of providers
         */
        int position() default Integer.MAX_VALUE;

    }
}
