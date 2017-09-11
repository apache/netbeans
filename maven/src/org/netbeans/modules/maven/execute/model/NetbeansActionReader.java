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
package org.netbeans.modules.maven.execute.model;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class NetbeansActionReader {
    protected abstract String getRawMappingsAsString();
    protected abstract Reader performDynamicSubstitutions(Map<String,String> replaceMap, String in) throws IOException;
    
    
    public final NetbeansActionMapping getMappingForAction(
            NetbeansBuildActionXpp3Reader reader, Logger LOG,
            String actionName, boolean[] hasInnerProfiles,
            Project project, String profile, Map<String, String> map
    ) {
        NetbeansActionMapping action = null;
        try {
            final String raw = getRawMappingsAsString();
            if (raw == null) {
                return null;
            }
            // just a converter for the To-Object reader..
            Reader read = performDynamicSubstitutions(map, raw);
            // basically doing a copy here..
            ActionToGoalMapping mapping = reader.read(read);
            List<NetbeansActionMapping> actions;
            if (profile == null) {
                actions = mapping.getActions();
            } else {
                actions = Collections.emptyList();
                for (NetbeansActionProfile p : mapping.getProfiles()) {
                    if (profile.equals(p.getId())) {
                        actions = p.getActions();
                        break;
                    }
                }
            }
            if (hasInnerProfiles != null) {
                hasInnerProfiles[0] = !mapping.getProfiles().isEmpty();
            }
            Iterator<NetbeansActionMapping> it = actions.iterator();
            NbMavenProject mp = project.getLookup().lookup(NbMavenProject.class);
            String prjPack = mp.getPackagingType();
            while (it.hasNext()) {
                NetbeansActionMapping elem = it.next();
                if (actionName.equals(elem.getActionName())
                        && (elem.getPackagings().isEmpty()
                        || elem.getPackagings().contains(prjPack.trim())
                        || elem.getPackagings().contains("*"))) {//NOI18N
                    action = elem;
                    break;
                }
            }
        } catch (XmlPullParserException ex) {
            LOG.log(Level.INFO, "Parsing action mapping", ex);
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Parsing action mapping", ex);
        }
        return action;

    }
    
}
