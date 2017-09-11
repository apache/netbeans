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

package org.netbeans.modules.profiler.nbimpl;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.tools.ant.types.LogLevel;
import org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher;


/**
 * Ant task to start the NetBeans profiler profile action.
 * <p/>
 * Will put the profiler into listening mode, placing the port number into the "profiler.port" property.
 * The target app then should be started through the profiler agent passing it this port number.
 *
 * @author Tomas Hurka
 * @author Ian Formanek
 */
public final class NBProfileDirectTask extends Task {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    /**
     * Enumerated attribute with the values "asis", "add" and "remove".
     */
    public static class YesNoAuto extends EnumeratedAttribute {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public String[] getValues() {
            return new String[] { "yes", "true", "no", "false", "auto" }; //NOI18N
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final int INTERACTIVE_AUTO = 0;
    private static final int INTERACTIVE_YES = 1;
    private static final int INTERACTIVE_NO = 2;
    private static final String DEFAULT_AGENT_JVMARGS_PROPERTY = "profiler.info.jvmargs.agent"; // NOI18N
    private static final String DEFAULT_JVM_PROPERTY = "profiler.info.jvm"; // NOI18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    /**
     * Explicit classpath of the profiled process.
     */
    private Path classpath = null;
    private Path rootsPath = null;
    private String jvmArgsPrefix = ""; // NOI18N
    private String jvmArgsProperty = DEFAULT_AGENT_JVMARGS_PROPERTY;
    private String jvmProperty = DEFAULT_JVM_PROPERTY;
    private String mainClass = null;
    private int interactive = INTERACTIVE_AUTO;
    
    private AtomicBoolean connectionCancel = new AtomicBoolean();

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    public void setInteractive(NBProfileDirectTask.YesNoAuto arg) {
        String value = arg.getValue();

        if (value.equals("auto")) { //NOI18N
            interactive = INTERACTIVE_AUTO;
        } else if (value.equals("yes") || value.equals("true")) { // NOI18N
            interactive = INTERACTIVE_YES;
        } else if (value.equals("no") || value.equals("false")) { // NOI18N
            interactive = INTERACTIVE_NO;
        }
    }

    public void setJvmArgsPrefix(String value) {
        jvmArgsPrefix = value;
    }

    public void setJvmArgsProperty(String value) {
        jvmArgsProperty = value;
    }

    public void setJvmProperty(String value) {
        jvmProperty = value;
    }

    // -- Properties -------------------------------------------------------------------------------------------------------
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    /**
     * "classpath" subelements, only one is allowed
     *
     * @param path the classpath
     */
    public void addClasspath(final Path path) {
        if (classpath != null) {
            throw new BuildException("Only one classpath subelement is supported"); //NOI18N
        }

        classpath = path;
    }

    /**
     * "classpath" subelements, only one is allowed
     *
     * @param path the classpath
     */
    public void addRootspath(final Path path) {
        if (rootsPath != null) {
            throw new BuildException("Only one classpath subelement is supported"); //NOI18N
        }

        rootsPath = path;
    }

    // -- Main methods -----------------------------------------------------------------------------------------------------
    public void execute() throws BuildException {
        ProfilerLauncher.Session s = ProfilerLauncher.getLastSession();
        if (s != null && s.isConfigured()) {
            Map<String, String> props = s.getProperties();
            if (props != null) {
                for(Map.Entry<String, String> e : props.entrySet()) {
                    getProject().setProperty(e.getKey(), e.getValue());
                }
                getProject().setProperty("profiler.jvmargs", "-J-Dprofiler.pre72=true"); // NOI18N
                
                getProject().addBuildListener(new BuildEndListener(connectionCancel));
                
                if (!NetBeansProfiler.getDefaultNB().startEx(s.getProfilingSettings(), s.getSessionSettings(), connectionCancel)) {
                    throw new BuildException("User abort"); // NOI18N
                }
            }
        } else {
            throw new BuildException("User abort");// NOI18N
        }
    }
}
