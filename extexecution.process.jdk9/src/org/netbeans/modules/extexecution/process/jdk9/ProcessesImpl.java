/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.extexecution.process.jdk9;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.netbeans.spi.extexecution.base.ProcessesImplementation;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author phejl
 */
@ServiceProvider(service=ProcessesImplementation.class, position = 1000)
public class ProcessesImpl implements ProcessesImplementation {

    private static final Logger LOGGER = Logger.getLogger(ProcessesImpl.class.getName());

    private static final boolean ENABLED;

    private static final Method PROCESS_TO_HANDLE;

    private static final Method PROCESS_HANDLE_DESCENDANTS;

    private static final Method PROCESS_HANDLE_DESTROY;

    static {
        Method toHandle = null;
        Method descendants = null;
        Method destroy = null;
        try {
            toHandle = Process.class.getDeclaredMethod("toHandle", new Class[]{}); // NOI18N
            if (toHandle != null) {
                Class processHandle = Class.forName("java.lang.ProcessHandle"); // NOI18N
                descendants = processHandle.getDeclaredMethod("descendants", new Class[]{}); // NOI18N
                destroy = processHandle.getDeclaredMethod("destroy", new Class[]{}); // NOI18N
            }
        } catch (NoClassDefFoundError | Exception ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }

        ENABLED = toHandle != null && descendants != null && destroy != null;
        PROCESS_TO_HANDLE = toHandle;
        PROCESS_HANDLE_DESCENDANTS = descendants;
        PROCESS_HANDLE_DESTROY = destroy;
    }

    @Override
    public void killTree(Process process, Map<String, String> environment) {
        if (!ENABLED) {
            throw new UnsupportedOperationException("The JDK 9 way of killing process tree is not supported"); // NOI18N
        }

        try {
            Object handle = PROCESS_TO_HANDLE.invoke(process, (Object[]) null);
            try (Stream s = (Stream) PROCESS_HANDLE_DESCENDANTS.invoke(handle, (Object[]) null)) {
                destroy(handle);
                s.forEach(ch ->  destroy(ch));
            }
        } catch (IllegalAccessException | IllegalArgumentException |InvocationTargetException ex) {
            throw new UnsupportedOperationException("The JDK 9 way of killing process tree has failed", ex); // NOI18N
        }
    }

    private static void destroy(Object handle) {
        try {
            PROCESS_HANDLE_DESTROY.invoke(handle, (Object[]) null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

}
