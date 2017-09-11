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
