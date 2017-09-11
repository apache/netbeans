/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.implspi;

import java.util.Collection;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.openide.util.Lookup;

/**
 * Factory for {@link SourceEnvironment} instances. The Factory method is given
 * a {@link Source.EnvControl} object so the created SourceEnvironment may
 * manipulate the Source state using privileged APIs.
 * <p/>
 * An instance of this Provider must be registered in the default Lookup. The first
 * instance found will be used.
 * @since 9.2
 */
public interface EnvironmentFactory {
    /**
     * Provides access to context-dependent Lookup, to multi-user environment
     * @return context-depedent Lookup
     */
    public Lookup   getContextLookup();
    
    /**
     * Parovides a class of a predefined Scheduler.
     * The predefined Schedulers are available as final field values.
     *
     * @param schedulerName
     * @return
     */
    @CheckForNull
    public Class<? extends Scheduler> findStandardScheduler(@NonNull String schedulerName);
    
    /**
     * Creates parser for the mime type
     * @param mimeType
     * @return 
     */
    public Parser   findMimeParser(Lookup context, String mimeType);
    
    public Collection<? extends Scheduler> getSchedulers(Lookup context);

    /**
     * Creates an environment for the specified Source.
     * The passed control object may be {@code null}, which means the source cannot
     * be controlled by environment and listening may not be necessary.
     *
     * @param src the Source instance
     * @param control the control object; if null, the source cannot be controlled.
     * @return the SourceEnvironment, must not be null.
     */
    @NonNull
    public SourceEnvironment createEnvironment(@NonNull Source src, @NullAllowed SourceControl control);
    
    
    /**
     * Runs a priority I/O operation. The environment may lock out or suspend 
     * some (background) activities during the priority I/O operation.
     * 
     * @param <T> type of result
     * @param r the code to execute
     * @return computed result
     * @throws Exception propagated from the executed code
     */
    public abstract <T> T runPriorityIO (final Callable<T> r) throws Exception;
}
