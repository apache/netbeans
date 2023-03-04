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
package org.netbeans.modules.maven.grammar;

import java.io.File;
import java.net.URI;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.xml.api.model.GrammarEnvironment;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * 
 * @author mkleint
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.maven.grammar.GrammarFactory.class)
public class DefaultGrammarFactory extends GrammarFactory {

    @Override
    public GrammarQuery isSupported(GrammarEnvironment env) {
        FileObject fo = env.getFileObject();
        if (fo == null) { //#134797 in abbreviation ui the fileobject can be non existant..
            return null;
        }
        if (fo.getNameExt().equals("settings.xml") && fo.getParent() != null && ".m2".equalsIgnoreCase(fo.getParent().getNameExt())) {//NOI18N
            return new MavenSettingsGrammar(env);
        }
        Project owner = FileOwnerQuery.getOwner(fo);
        if (owner == null) {
            //#107511
            return null;
        }
        if (fo.getNameExt().equals("pom.xml") && owner.getProjectDirectory().equals(fo.getParent())) {//NOI18N
            return new MavenProjectGrammar(env, owner);
        }
        File file = FileUtil.toFile(fo);
        if (owner.getLookup().lookup(NbMavenProject.class) != null) {
            if ("src/main/resources/META-INF/archetype.xml".equals(FileUtil.getRelativePath(owner.getProjectDirectory(), env.getFileObject()))) {//NOI18N
                return new MavenArchetypeGrammar(env);
            }
            String desc = PluginPropertyUtils.getPluginProperty(owner, "org.apache.maven.plugins", "maven-assembly-plugin", "descriptor", null, null);//NOI18N
            if (desc != null) {
                URI uri = FileUtilities.getDirURI(owner.getProjectDirectory(), desc);
                if (uri != null && Utilities.toFile(uri).equals(file)) {
                    return new MavenAssemblyGrammar(env);
                }
            }
            //TODO delete this, descriptor is deprecated
            desc = PluginPropertyUtils.getPluginProperty(owner, "org.codehaus.mojo", "nbm-maven-plugin", "descriptor", null, null);//NOI18N
            //NOI18N
            if (desc == null) {
                desc = PluginPropertyUtils.getPluginProperty(owner, "org.codehaus.mevenide.plugins", "maven-nbm-plugin", "descriptor", null, null);//NOI18N
            }
            if (desc != null) {
                URI uri = FileUtilities.getDirURI(owner.getProjectDirectory(), desc);
                if (uri != null && Utilities.toFile(uri).equals(file)) {
                    return new MavenNbmGrammar(env);
                }
            }
        }
        return null;
    }
}
