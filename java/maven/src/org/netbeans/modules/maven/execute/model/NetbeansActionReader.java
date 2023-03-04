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
