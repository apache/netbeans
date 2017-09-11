/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
