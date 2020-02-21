/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.discovery.api.ApplicableImpl;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectImpl;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.ProviderPropertyType;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

/**
 *
 */
public class AnalyzeMakeLog extends BaseProvider {
    public static final String MAKE_LOG_PROVIDER_ID = "make-log"; // NOI18N
    private final Map<String,ProviderProperty> myProperties = new LinkedHashMap<String,ProviderProperty>();
    private final ProviderProperty<String> MAKE_LOG_PROPERTY;
    private final ProviderProperty<FileSystem> LOG_FILESYSTEM_PROPERTY;
    
    public AnalyzeMakeLog() {
        myProperties.clear();
        MAKE_LOG_PROPERTY = new ProviderProperty<String>(){
            private String myPath;
            @Override
            public String getName() {
                return i18n("Make_Log_File_Name"); // NOI18N
            }
            @Override
            public String getDescription() {
                return i18n("Make_Log_File_Description"); // NOI18N
            }
            @Override
            public String getValue() {
                return myPath;
            }
            @Override
            public void setValue(String value) {
                myPath = value;
            }
            @Override
            public ProviderPropertyType getPropertyType() {
                return ProviderPropertyType.MakeLogPropertyType;
            }
        };
        myProperties.put(MAKE_LOG_PROPERTY.getPropertyType().key(), MAKE_LOG_PROPERTY);

        LOG_FILESYSTEM_PROPERTY = new ProviderProperty<FileSystem>(){
            private FileSystem fs;
            @Override
            public String getName() {
                return ""; // NOI18N
            }
            @Override
            public String getDescription() {
                return ""; // NOI18N
            }
            @Override
            public FileSystem getValue() {
                return fs;
            }
            @Override
            public void setValue(FileSystem value) {
                fs = value;
            }
            @Override
            public ProviderPropertyType<FileSystem> getPropertyType() {
                return ProviderPropertyType.LogFileSystemPropertyType;
            }
        };
        myProperties.put(LOG_FILESYSTEM_PROPERTY.getPropertyType().key(), LOG_FILESYSTEM_PROPERTY);
        
        myProperties.put(RESTRICT_SOURCE_ROOT_PROPERTY.getPropertyType().key(), RESTRICT_SOURCE_ROOT_PROPERTY);
        myProperties.put(RESTRICT_COMPILE_ROOT_PROPERTY.getPropertyType().key(), RESTRICT_COMPILE_ROOT_PROPERTY);
    }

    @Override
    public String getID() {
        return MAKE_LOG_PROVIDER_ID; // NOI18N
    }
    
    @Override
    public String getName() {
        return i18n("Make_Log_Provider_Name"); // NOI18N
    }
    
    @Override
    public String getDescription() {
        return i18n("Make_Log_Provider_Description"); // NOI18N
    }
    
    @Override
    public List<String> getPropertyKeys() {
        return new ArrayList<String>(myProperties.keySet());
    }
    
    @Override
    public ProviderProperty getProperty(String key) {
        return myProperties.get(key);
    }

    @Override
    public boolean isApplicable(ProjectProxy project) {
//        if (detectMakeLog(project) != null){
        String o = RESTRICT_COMPILE_ROOT_PROPERTY.getValue();
        if (o == null || o.isEmpty()) {
            RESTRICT_COMPILE_ROOT_PROPERTY.setValue(project.getSourceRoot());
            return true;
        }
        return false;
    }
    
    private FileObject getLog(String set) {
        FileSystem fs = LOG_FILESYSTEM_PROPERTY.getValue();
        if (fs == null) {
            fs = CndFileSystemProvider.getLocalFileSystem();
        }
        FSPath log = new FSPath(fs, set);
        FileObject fo = log.getFileObject();
        if (fo != null && fo.isValid() && fo.isData() && fo.canRead()) {
            return fo;
        }
        return null;
    }
    
    @Override
    public DiscoveryExtensionInterface.Applicable canAnalyze(ProjectProxy project, Interrupter interrupter) {
        init(project);
        String set = MAKE_LOG_PROPERTY.getValue();
        if (set == null || set.length() == 0 || getLog(set) == null) {
            return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(AnalyzeMakeLog.class, "NotFoundMakeLog")));
        }
        return new ApplicableImpl(true, null, null, 80, false, null, null, null, null);
    }

    @Override
    protected List<SourceFileProperties> getSourceFileProperties(String logFileName, Map<String,SourceFileProperties> map, ProjectProxy project, Set<String> dlls,
            List<String> buildArtifacts, Map<ItemProperties.LanguageKind,Map<String,Integer>> buildTools, CompileLineStorage storage){
        String root = RESTRICT_COMPILE_ROOT_PROPERTY.getValue();
        if (root == null) {
            root = ""; //NOI18N
        }
        List<SourceFileProperties> res = runLogReader(getLog(logFileName), root, progress, project, buildArtifacts, buildTools, storage);
        progress = null;
        return res;

    }
    
    private List<SourceFileProperties> runLogReader(FileObject logFileObject, String root, Progress progress, ProjectProxy project,
            List<String> buildArtifacts, Map<ItemProperties.LanguageKind,Map<String,Integer>> buildTools, CompileLineStorage storage){
        FileSystem fileSystem = getFileSystem(project);
        MakeLogReader reader = new MakeLogReader(logFileObject, root, project, getRelocatablePathMapper(), fileSystem);
        List<SourceFileProperties> list = reader.getResults(progress, getStopInterrupter(), storage);
        buildArtifacts.addAll(reader.getArtifacts(progress, getStopInterrupter(), storage));
        buildTools.putAll(reader.getTools(progress, getStopInterrupter(), storage));
        return list;
    }

    private Progress progress;

    @Override
    public List<Configuration> analyze(final ProjectProxy project, Progress progress, Interrupter interrupter) {
        resetStopInterrupter(interrupter);
        List<Configuration> confs = new ArrayList<Configuration>();
        init(project);
        this.progress = progress;
        if (!getStopInterrupter().cancelled()){
            Configuration conf = new Configuration(){
                private List<SourceFileProperties> myFileProperties;
                private List<String> myBuildArtifacts;
                private Map<ItemProperties.LanguageKind,Map<String,Integer>> buildTools;
                private List<String> myIncludedFiles = new ArrayList<String>();;
                @Override
                public List<ProjectProperties> getProjectConfiguration() {
                    return ProjectImpl.divideByLanguage(getSourcesConfiguration(), project);
                }
                
                @Override
                public List<String> getDependencies() {
                    return null;
                }

                @Override
                public List<String> getBuildArtifacts() {
                    if (myBuildArtifacts == null){
                        process();
                    }
                    return myBuildArtifacts;
                }

                @Override
                public Map<ItemProperties.LanguageKind, Map<String, Integer>> getBuildTools() {
                    if (buildTools == null){
                        process();
                    }
                    return buildTools;
                }

                @Override
                public List<SourceFileProperties> getSourcesConfiguration() {
                    if (myFileProperties == null){
                        process();
                    }
                    return myFileProperties;
                }
                
                @Override
                public List<String> getIncludedFiles(){
                    return myIncludedFiles;
                }

                private void process() {
                    String set = MAKE_LOG_PROPERTY.getValue();
                    if (set != null && set.length() > 0) {
                        myBuildArtifacts = Collections.synchronizedList(new ArrayList<String>());
                        buildTools = new ConcurrentHashMap<ItemProperties.LanguageKind, Map<String, Integer>>();
                        myFileProperties = getSourceFileProperties(new String[]{set},null, project, null, myBuildArtifacts, buildTools, new CompileLineStorage());
                        store(project);
                    }
                }
            };
            confs.add(conf);
        }
        return confs;
    }
    
    private static String i18n(String id) {
        return NbBundle.getMessage(AnalyzeMakeLog.class,id);
    }
}
