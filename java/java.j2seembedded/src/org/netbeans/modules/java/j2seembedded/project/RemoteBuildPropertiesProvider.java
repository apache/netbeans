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

package org.netbeans.modules.java.j2seembedded.project;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seembedded.platform.ConnectionMethod;
import org.netbeans.modules.java.j2seembedded.platform.RemotePlatform;
import org.netbeans.modules.java.j2seproject.api.J2SEBuildPropertiesProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
@ProjectServiceProvider(service=J2SEBuildPropertiesProvider.class, projectType="org-netbeans-modules-java-j2seproject")
public class RemoteBuildPropertiesProvider implements J2SEBuildPropertiesProvider {
    
    private static final String PROP_PASSWD = "remote.platform.password"; //NOI18N
    private static final String PROP_PASSPHRASE = "remote.platform.passphrase"; //NOI18N
    private static final String PROP_OS_ARCH_RP = "remote.platform.rp.target"; //NOI18N
    private static final String PROP_FILENAME_RP = "remote.platform.rp.filename"; //NOI18N
    private static final String PROP_JAVA_SPEC_VER = "remote.platform.java.spec.ver"; //NOI18N
    
    private final Project prj;

    public RemoteBuildPropertiesProvider(@NonNull final Project prj) {
        Parameters.notNull("prj", prj); //NOI18N
        this.prj = prj;
    }

    @NonNull
    @Override
    @SuppressWarnings("fallthrough")
    public Map<String, String> createAdditionalProperties(
            @NonNull final String command,
            @NonNull final Lookup context) {
        Parameters.notNull("command", command); //NOI18N
        Parameters.notNull("context", context); //NOI18N
        switch (command) {
            case ActionProvider.COMMAND_RUN:
            case ActionProvider.COMMAND_DEBUG:            
            case ActionProvider.COMMAND_PROFILE:
                final RemotePlatform rp = Utilities.getRemotePlatform(prj);
                if (rp != null) {
                    final ConnectionMethod.Authentification auth = rp.getConnectionMethod().getAuthentification();                    
                    Map<String,String> res = new HashMap<>();
                    String target = Utilities.getTargetOSForRP(
                        rp.getSystemProperties().get("os.name"),    //NOI18N
                        rp.getSystemProperties().get("os.arch"),    //NOI18N
                        rp.getSystemProperties().get("sun.arch.abi"),   //NOI18N
                        rp.getSystemProperties().get(("java.vm.name"))); //NOI18N
                    res.put(PROP_JAVA_SPEC_VER, rp.getSystemProperties().get("java.specification.version").replace(".","")); //NOI18N
                    res.put(PROP_OS_ARCH_RP, target);
                    res.put(PROP_FILENAME_RP, target.replace("-", "").replace("15","")); //NOI18N
                    switch (auth.getKind()) {
                        case PASSWORD:
                            res.put(
                                PROP_PASSWD,
                                ((ConnectionMethod.Authentification.Password)auth).getPassword());
                            break;
                        case KEY:
                            res.put(
                                PROP_PASSPHRASE,
                                ((ConnectionMethod.Authentification.Key)auth).getPassPhrase());
                            break;
                        default:
                            throw new IllegalStateException(auth.getKind().name());
                    }
                    return Collections.unmodifiableMap(res);
                }
            default:
                return Collections.<String,String>emptyMap();
        }
    }

    @NonNull
    @Override
    @SuppressWarnings("fallthrough")
    public Set<String> createConcealedProperties(@NonNull final String command, @NonNull final Lookup context) {
        Parameters.notNull("command", command); //NOI18N
        Parameters.notNull("context", context); //NOI18N
        switch (command) {
            case ActionProvider.COMMAND_RUN:
            case ActionProvider.COMMAND_DEBUG:
            case ActionProvider.COMMAND_PROFILE:
                final RemotePlatform rp = Utilities.getRemotePlatform(prj);
                if (rp != null) {
                final ConnectionMethod.Authentification.Kind kind = rp.getConnectionMethod().getAuthentification().getKind();
                    switch (kind) {
                        case PASSWORD:
                            return Collections.singleton(PROP_PASSWD);
                        case KEY:
                            return Collections.singleton(PROP_PASSPHRASE);
                        default:
                            throw new IllegalStateException(kind.name());
                    }
                }
            default:
                return Collections.<String>emptySet();
        }
    }
}
