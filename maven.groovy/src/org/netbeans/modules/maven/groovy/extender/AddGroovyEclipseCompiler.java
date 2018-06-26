/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.groovy.extender;

import java.util.List;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.POMComponentFactory;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.Project;

/**
 * This needs to be here because of issues in eclipse-compiler-plugin.
 *
 * In short if there are no files in java source folder then the groovy source
 * folder cannot be found and thus groovy files can't be executed. There are
 * few possible workarrounds and this class is implementing one of them.
 * <br/><br/>
 *
 * @see <a href="http://groovy.codehaus.org/Groovy-Eclipse+compiler+plugin+for+Maven">Possible workarrounds</a>
 * @see <a href="http://jira.codehaus.org/browse/GRECLIPSE-1221">JIRA GRECLIPSE-1221</a>
 *
 * @author Martin Janicek
 */
public class AddGroovyEclipseCompiler implements ModelOperation<POMModel> {

    @Override
    public void performOperation(POMModel model) {
        POMComponentFactory factory = model.getFactory();
        Project project = model.getProject();

        Build build = project.getBuild();
        if (build == null) {
            build = factory.createBuild();
            project.setBuild(build);
        }

        Plugin groovyEclipseCompiler = factory.createPlugin();
        groovyEclipseCompiler.setGroupId(MavenConstants.GROOVY_ECLIPSE_COMPILER_GROUP_ID);
        groovyEclipseCompiler.setArtifactId(MavenConstants.GROOVY_ECLIPSE_COMPILER_ARTIFACT_ID);
        groovyEclipseCompiler.setVersion(MavenConstants.GROOVY_ECLIPSE_COMPILER_VERSION);
        groovyEclipseCompiler.setExtensions(Boolean.TRUE); // THIS IS THE IMPORTANT PART !

        if (!groovyEclipseCompilerExists(build)) {
            build.addPlugin(groovyEclipseCompiler);
        }
    }

    private boolean groovyEclipseCompilerExists(final Build build) {
        List<Plugin> plugins = build.getPlugins();
        if (plugins != null) {
            for (Plugin plugin : plugins) {
                if (MavenConstants.GROOVY_ECLIPSE_COMPILER_GROUP_ID.equals(plugin.getGroupId()) &&
                    MavenConstants.GROOVY_ECLIPSE_COMPILER_ARTIFACT_ID.equals(plugin.getArtifactId())) {
                    return true;
                }
            }
        }
        return false;
    }
}
