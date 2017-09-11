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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.api.execute;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * Context provider for maven executors and checkers. Never to be implemented by
 * client code.
 * @author Milos Kleint
 */
public interface RunConfig {
    
    /**
     * directory where the maven build execution happens.
     * @return
     */
    File getExecutionDirectory();

    void setExecutionDirectory(File directory);

    RunConfig getPreExecution();

    void setPreExecution(RunConfig config);

    RunConfig.ReactorStyle getReactorStyle();

//    void setPreExecution(RunConfig config);

    /**
     * project that is being used for execution, can be null.
     * @return 
     */
    Project getProject();

    /**
     * the maven project instance loaded with the context of execution,
     * with execution's profiles enabled and execution properties injected.
     * Can differd from the MavenProject returned from within the Project instance.
     * All Maven model checks shall be done against this instance.
     * @return
     */
    MavenProject getMavenProject();

    /**
     * goals to be executed.
     * @return a list of goals to run
     */
    List<String> getGoals();

    String getExecutionName();
    
    String getTaskDisplayName();

    String getActionName();
    
    /**
     * Properties to be used in execution.
     * @return a read-only copy of the current properties (possibly inherited from the parent)
     */
    @NonNull Map<? extends String,? extends String> getProperties();

    void setProperty(@NonNull String key, @NullAllowed String value);
    
    void addProperties(@NonNull Map<String, String> properties);  
    
    void setInternalProperty(@NonNull String key, @NullAllowed Object value);
    
    @NonNull Map<? extends String, ? extends Object> getInternalProperties();
    
    boolean isShowDebug();
    
    boolean isShowError();
    
    Boolean isOffline();
    
    void setOffline(Boolean bool);
    
    boolean isRecursive();
    
    boolean isUpdateSnapshots();

    List<String> getActivatedProfiles();
    
    void setActivatedProfiles(List<String> profiles);
    
    boolean isInteractive();

    FileObject getSelectedFileObject();

    public enum ReactorStyle {
        NONE,
        /**
         * am, --also-make
         * If project list is specified, also build projects required by the list
         */
        ALSO_MAKE,
        /**
         * -amd,--also-make-dependents
         * If project list is specified, also build projects that depend on projects on the list
         */
        ALSO_MAKE_DEPENDENTS
    }
    
}
