/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.apache.tools.ant.module.bridge;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * What is implemented by bridge.jar.
 * @author Jesse Glick
 */
public interface BridgeInterface {

    /**
     * Actually run a build script.
     * @param buildFile an Ant build script
     * @param targets a list of target names to run, or null to run the default target
     * @param in an input stream for console input
     * @param out an output stream with the ability to have hyperlinks
     * @param err an error stream with the ability to have hyperlinks
     * @param properties any Ant properties to define
     * @param concealedProperties  the names of the properties whose values should not be visible to the user
     * @param verbosity the intended logging level
     * @param displayName a user-presentable name for the session
     * @param interestingOutputCallback will be called if and when some interesting output appears, or input is requested
     * @param handle a progress handle to update if appropriate (switch to sleeping and back to indeterminate)
     * @param io raw I/O handle for more advanced output
     * @return true if the build succeeded, false if it failed for any reason
     */
    boolean run(File buildFile, List<String> targets, InputStream in, OutputWriter out, OutputWriter err, Map<String,String> properties,
            Set<? extends String> concealedProperties, int verbosity, String displayName, Runnable interestingOutputCallback, ProgressHandle handle, InputOutput io);
    
    /**
     * Try to stop a running build.
     * The implementation may wait for a while to stop at a safe point,
     * and/or stop forcibly.
     * @param process the thread which is currently running the build (in which {@link #run} was invoked)
     */
    void stop(Thread process);
    
    /**
     * Get some informational value of the Ant version.
     * @return the version
     */
    String getAntVersion();
    
    /**
     * Check whether Ant 1.6 is loaded.
     * If so, additional abilities may be possible, such as namespace support.
     */
    boolean isAnt16();
    
    /**
     * Get a proxy for IntrospectionHelper, to introspect task + type structure.
     */
    IntrospectionHelperProxy getIntrospectionHelper(Class<?> clazz);
    
    /**
     * See Project.toBoolean.
     */
    boolean toBoolean(String val);
    
    /**
     * Get values of an enumeration class.
     * If it is not actually an enumeration class, return null.
     */
    String[] getEnumeratedValues(Class<?> c);
    
}
