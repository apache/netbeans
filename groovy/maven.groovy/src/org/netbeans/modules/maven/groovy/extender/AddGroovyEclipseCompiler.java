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
