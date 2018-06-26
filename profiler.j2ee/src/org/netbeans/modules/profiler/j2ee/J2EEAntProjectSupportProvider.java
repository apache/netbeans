/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.j2ee;

import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.nbimpl.project.AntProjectSupportProvider;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jiri Sedlacek
 */
@ProjectServiceProvider(service=org.netbeans.modules.profiler.nbimpl.project.AntProjectSupportProvider.class, 
                        projectTypes={
                            @ProjectType(id="org-netbeans-modules-j2ee-ejbjarproject"),  // NOI18N
                            @ProjectType(id="org-netbeans-modules-j2ee-earproject"),  // NOI18N
                            @ProjectType(id="org-netbeans-modules-web-project") // NOI18N
                        }
)
public final class J2EEAntProjectSupportProvider extends AntProjectSupportProvider.Abstract {
    
    public J2EEAntProjectSupportProvider(Project project) {
        super(project);
    }
    
    @Override
    public void configurePropertiesForProfiling(final Map<String, String> props, final FileObject profiledClassFile) {
//        Project project = getProject();
//        initAntPlatform(project, props);

        // set forceRestart
        props.put("profiler.j2ee.serverForceRestart", "true"); // NOI18N

        // set timeout
        props.put("profiler.j2ee.serverStartupTimeout", "300000"); // NOI18N

        // set agent id
        props.put("profiler.j2ee.agentID", "-Dnbprofiler.agentid=" + // NOI18N
                  J2EEProjectProfilingSupportProvider.generateAgentID());

//        // redirect profiler.info.jvmargs to profiler.info.jvmargs.extra
//        String jvmArgs = props.get("profiler.info.jvmargs"); // NOI18N
//
//        if ((jvmArgs != null) && (jvmArgs.trim().length() > 0)) {
//            props.put("profiler.info.jvmargs.extra", jvmArgs);
//        }
//
//        // fix agent startup arguments
//        JavaPlatform javaPlatform = getJavaPlatformFromAntName(project, props);
//        props.put("profiler.platform.java", javaPlatform.getPlatformId()); // set the used platform ant property
//
//        String javaVersion = javaPlatform.getPlatformJDKVersion();
//        String localPlatform = IntegrationUtils.getLocalPlatform(javaPlatform.getPlatformArchitecture());
//
//        if (javaVersion.equals(CommonConstants.JDK_15_STRING)) {
//            // JDK 1.5 used
//            props.put("profiler.info.jvmargs.agent", // NOI18N
//                              IntegrationUtils.getProfilerAgentCommandLineArgs(localPlatform, IntegrationUtils.PLATFORM_JAVA_50,
//                                                                               false,
//                                                                               ProfilerIDESettings.getInstance().getPortNo()));
//        } else {
//            // JDK 1.6 or later used
//            props.put("profiler.info.jvmargs.agent", // NOI18N
//                              IntegrationUtils.getProfilerAgentCommandLineArgs(localPlatform, IntegrationUtils.PLATFORM_JAVA_60,
//                                                                               false,
//                                                                               ProfilerIDESettings.getInstance().getPortNo()));
//        }
//
//        generateAgentPort(); // sets lastAgentPort
//
//        String loadGenPath = LoadGenPanel.hasInstance() ? LoadGenPanel.instance().getSelectedScript() : null;
//        if (loadGenPath != null) {
//            props.put("profiler.loadgen.path", loadGenPath); // TODO factor out "profiler.loadgen.path" to a constant
//        }
//
//        if (profiledClassFile == null) {
//            return;
//        }
//
//        if (WebProjectUtils.isJSP(profiledClassFile)) {
//            props.put("client.urlPart", WebProjectUtils.getJSPFileContext(project, profiledClassFile, false)); // NOI18N
//        } else if (WebProjectUtils.isHttpServlet(profiledClassFile)) {
//            String servletAddress = null;
//            Collection<Document> ddos = WebProjectUtils.getDeploymentDescriptorDocuments(project, true);
//
//            for (Document dd : ddos) {
//                String mapping = WebProjectUtils.getServletMapping(profiledClassFile, dd);
//
//                if ((mapping != null) && (mapping.length() > 0)) {
//                    servletAddress = mapping;
//
//                    break;
//                }
//            }
//
//            if (servletAddress != null) {
//                ServletUriPanel uriPanel = new ServletUriPanel(servletAddress);
//                DialogDescriptor desc = new DialogDescriptor(uriPanel, Bundle.TTL_setServletExecutionUri(),
//                                                             true, // NOI18N
//                                                             new Object[] {
//                                                                 DialogDescriptor.OK_OPTION,
//                                                                 new javax.swing.JButton(Bundle.J2EEProjectTypeProfiler_SkipButtonName()) {
//                        public java.awt.Dimension getPreferredSize() {
//                            return new java.awt.Dimension(super.getPreferredSize().width + 16, super.getPreferredSize().height);
//                        }
//                    }
//                                                             }, DialogDescriptor.OK_OPTION, DialogDescriptor.BOTTOM_ALIGN, null,
//                                                             null);
//                Object res = DialogDisplayer.getDefault().notify(desc);
//
//                if (res.equals(NotifyDescriptor.YES_OPTION)) {
//                    servletAddress = uriPanel.getServletUri();
//                }
//
//                props.put("client.urlPart", servletAddress); // NOI18N
//            }
//        }
//        // FIXME - method should receive the JavaProfilerSource as the parameter
//        JavaProfilerSource src = JavaProfilerSource.createFrom(profiledClassFile);
//        if (src != null) {
//            String profiledClass = src.getTopLevelClass().getQualifiedName();
//            props.put("profile.class", profiledClass); //NOI18N
//            // include it in javac.includes so that the compile-single picks it up
//            final String clazz = FileUtil.getRelativePath(ProjectUtilities.getRootOf(
//                    ProjectUtilities.getSourceRoots(project),profiledClassFile), 
//                    profiledClassFile);
//            props.put("javac.includes", clazz); //NOI18N
//        }
    }
    
//    private void initAntPlatform(Project project, Map<String, String> props) {
//        String javaPlatformAntName = props.get("profiler.info.javaPlatform"); // NOI18N
//
//        if (javaPlatformAntName == null) {
//            ProjectProfilingSupport pps = ProjectProfilingSupport.get(project);
//            JavaPlatform platform = pps.getProjectJavaPlatform();
//            String platformId;
//            
//            if (platform == null) {
//                platformId = JavaPlatform.getDefaultPlatform().getPlatformId(); // no Platform sepcified; use the IDE default JVM platform
//            } else {
//                platformId = platform.getPlatformId();
//            }
//
//            props.put("profiler.info.javaPlatform", platformId); // set the used platform ant property
//        }
//    }
    
}
