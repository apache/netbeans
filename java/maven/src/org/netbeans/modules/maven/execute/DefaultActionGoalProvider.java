/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.maven.execute;

import java.io.IOException;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Function;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionProfile;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.openide.util.NbBundle;

/**
 * a default implementation of AdditionalM2ActionsProvider, a fallback when nothing is
 * user configured or overriden by a more specialized provider.
 * @author mkleint
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.maven.spi.actions.MavenActionsProvider.class, position=666)
public class DefaultActionGoalProvider extends AbstractMavenActionsProvider {
    
    @StaticResource private static final String MAPPINGS = "org/netbeans/modules/maven/execute/defaultActionMappings.xml";

    public DefaultActionGoalProvider() {
        reader = createI18nReader(NbBundle.getBundle(DefaultActionGoalProvider.class));
    }
    
    public static NetbeansBuildActionXpp3Reader createI18nReader(ResourceBundle bundle) {
        return new NetbeansBuildActionXpp3Reader() {
            @Override
            public ActionToGoalMapping read(Reader reader, boolean strict) throws IOException, XmlPullParserException {
                ActionToGoalMapping agm = super.read(reader, strict);
                return supplyDisplayNames(bundle, agm);
            }
        };
    }
    
    private static String supplyDisplayName(String prefix, String id, String d, Function<String, String> defaultTranslator) {
        if (d == null) {
            try {
                d = defaultTranslator.apply(prefix + id);
            } catch (MissingResourceException ex) {
            }
        } else if (d.startsWith("#")) {
            String key = d.substring(1);
            int sep = key.indexOf('#');
            if (sep == -1) {
                d = defaultTranslator.apply(key);
            } else {
                String bname = key.substring(sep);
                key = key.substring(sep + 1);
                try {
                    d = NbBundle.getBundle(bname).getString(key);
                } catch (MissingResourceException ex) {}
            }
        } else {
            return null;
        }
        return d;
    }
    
    private static ActionToGoalMapping supplyDisplayNames(ResourceBundle bundle, ActionToGoalMapping agm) {
        if (agm.getActions() != null) {
            supplyDisplayNames(bundle, agm.getActions());
        }
        if (agm.getProfiles() != null) {
            for (NetbeansActionProfile nap : agm.getProfiles()) {
                String d = supplyDisplayName("profile.", nap.getId(), nap.getDisplayName(), bundle::getString);
                if (d != null) {
                    nap.setDisplayName(d);
                }
                supplyDisplayNames(bundle, nap.getActions());
            }
        }
        return agm;
    }

    private static void supplyDisplayNames(ResourceBundle bundle, List<NetbeansActionMapping> actions) {
        for (NetbeansActionMapping m : actions) {
            String d = supplyDisplayName("action.", m.getActionName(), m.getDisplayName(), bundle::getString);
            if (d != null) {
                m.setDisplayName(d);
            }
        }
    }

    @Override protected InputStream getActionDefinitionStream() {
        return DefaultActionGoalProvider.class.getClassLoader().getResourceAsStream(MAPPINGS);
    }
}
