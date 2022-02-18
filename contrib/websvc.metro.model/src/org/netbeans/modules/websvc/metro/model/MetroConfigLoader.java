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

package org.netbeans.modules.websvc.metro.model;

import com.sun.xml.ws.runtime.config.MetroConfig;
import com.sun.xml.ws.runtime.config.ObjectFactory;
import com.sun.xml.ws.runtime.config.TubeFactoryConfig;
import com.sun.xml.ws.runtime.config.TubelineDefinition;
import com.sun.xml.ws.runtime.config.TubelineMapping;
import com.sun.xml.ws.runtime.config.Tubelines;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.wsitconf.api.WSITConfigProvider;
import org.netbeans.modules.websvc.wsitconf.spi.WsitProvider;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author snajper
 */
public class MetroConfigLoader {

    public static final String CFG_FILE_NAME="metro.xml";
    public static final String DEFAULT_TUBELINE_NAME="default";

    private JAXBContext jaxbContext = null;
    private ObjectFactory objFactory = null;

    public MetroConfigLoader() {
        try {
            jaxbContext = JAXBContext.newInstance(MetroConfig.class.getPackage().getName());
            objFactory = new ObjectFactory();
        } catch (JAXBException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public boolean isMetroConfigSupported(Project project) {
        if (project == null) return false;
        WsitProvider wsitProvider = project.getLookup().lookup(WsitProvider.class);
        if ((wsitProvider == null) || (!wsitProvider.isWsitSupported())) return false;

        // metro.xml with tubelines is supported starting from Metro 2.0, which includes JAX-WS 2.2
        WSStackVersion version = WSITConfigProvider.getDefault().getHighestWSStackVersion(project);
        if (version == null || version.compareTo(WSStackVersion.valueOf(2, 2, 0, 0)) < 0) return false;

        return true;
    }
    
    public MetroConfig createFreshMetroConfig() {
        MetroConfig metroConfig = objFactory.createMetroConfig();
        metroConfig.setVersion("1.0");        
        return metroConfig;
    }

    public TubelineDefinition createDefaultTubeline(MetroConfig cfg) {
        TubelineDefinition tDef = getDefaultTubeline(cfg);
        if (tDef == null) {
            Tubelines tubelines = cfg.getTubelines();
            if (tubelines == null) {
                tubelines = objFactory.createTubelines();
                cfg.setTubelines(tubelines);
            }
            tDef = objFactory.createTubelineDefinition();
            tDef.setName(DEFAULT_TUBELINE_NAME);
            tubelines.getTubelineDefinitions().add(tDef);
            tubelines.setDefault("#" + tDef.getName());
        }
        return tDef;
    }

    public TubelineDefinition createTubeline(MetroConfig cfg, String endpointRef, String tName) {
        TubelineDefinition tDef = getTubeline(cfg, endpointRef);
        if (tDef == null) {
            Tubelines tubelines = cfg.getTubelines();
            if (tubelines == null) {
                tubelines = objFactory.createTubelines();
                cfg.setTubelines(tubelines);
            }
            tDef = objFactory.createTubelineDefinition();
            tDef.setName(tName);
            tubelines.getTubelineDefinitions().add(tDef);

            List<TubelineMapping> mappings = tubelines.getTubelineMappings();            

            TubelineMapping mapping = objFactory.createTubelineMapping();
            mapping.setEndpointRef(endpointRef);
            mapping.setTubelineRef("#" + tName);

            mappings.add(mapping);
        }
        return tDef;
    }

    public void removeTubelineReference(MetroConfig cfg, String endpointRef) {
        TubelineMapping mapping = getTubelineMapping(cfg, endpointRef);
        if (mapping != null) {
            String tRef = mapping.getTubelineRef();
            Tubelines tubelines = cfg.getTubelines();
            tubelines.getTubelineMappings().remove(mapping);
            if (!isTubelineReferenced(tubelines, tRef)) {
                removeTubeline(tubelines, tRef);
            }
        }
        clearTubelines(cfg);
    }

    public void removeDefaultTubeline(MetroConfig cfg) {
        Tubelines tubelines = cfg.getTubelines();
        TubelineDefinition tDef = getDefaultTubeline(cfg);
        if (tDef != null) {
            tubelines.getTubelineDefinitions().remove(tDef);
        }

        tubelines.setDefault(null);
        clearTubelines(cfg);
    }

    private void clearTubelines(MetroConfig cfg) {
        Tubelines tubelines = cfg.getTubelines();
        List<TubelineDefinition> tDefs = tubelines.getTubelineDefinitions();
        if ((tDefs == null) || (tDefs.isEmpty())) {
            cfg.setTubelines(null);
        }
    }

    private void removeTubeline(Tubelines tubelines, String tubelineRef) {
        TubelineDefinition tDef = getTubeline(tubelines, tubelineRef);
        if (tDef != null) {
            tubelines.getTubelineDefinitions().remove(tDef);
        }
    }

    private boolean isTubelineReferenced(Tubelines tubelines, String tubelineRef) {
        List<TubelineMapping> mappings = tubelines.getTubelineMappings();
        for (TubelineMapping mapping : mappings) {
            if (tubelineRef.equals(mapping.getTubelineRef())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads the default definition from metro-default.xml file from the metro jars.
     * @param project
     * @return
     */
    public MetroConfig loadDefaultMetroConfig(Project project) {
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
        if ( classPath == null ){
	    return null;
	}
        FileObject defFO = classPath.findResource("META-INF/metro-default.xml"); // NOI18N
        try {
            if (defFO != null) {
                return loadMetroConfig(defFO.getURL());
            }
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    public MetroConfig loadMetroConfig(Project project) {
        FileObject cfgFolder = getConfigFolder(project, false);
        if ((cfgFolder != null) && (cfgFolder.isValid())) {
            try {
                FileObject cfgFile = cfgFolder.getFileObject(CFG_FILE_NAME);
                return (cfgFile != null) ? loadMetroConfig(cfgFile.getURL()) : null;
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    private MetroConfig loadMetroConfig(@NonNull URL resourceUrl) {
        MetroConfig result = null;
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            final JAXBElement<MetroConfig> configElement = unmarshaller.unmarshal(XMLInputFactory.newInstance().createXMLStreamReader(resourceUrl.openStream()), MetroConfig.class);
            result = configElement.getValue();
        } catch (Exception e) {
            Logger.global.warning(String.format("Unable to unmarshall metro config file from location: '%s'", resourceUrl.toString()));
        }
        return result;
    }

    public void saveMetroConfig(@NonNull MetroConfig metroConfig, Project project) {
        FileObject cfgFolder = getConfigFolder(project, false);
        if ((cfgFolder != null) && (cfgFolder.isValid())) {
            File outFile = null;
            FileObject cfgFile = cfgFolder.getFileObject(CFG_FILE_NAME);
            if (metroConfig == null) { //delete the file and return
                if ((cfgFile != null) && (cfgFile.isValid())) {
                    outFile = FileUtil.toFile(cfgFile);
                    outFile.delete();
                }
                return;
            }
            if ((cfgFile != null) && (cfgFile.isValid())) {
                outFile = FileUtil.toFile(cfgFile);
            } else {
                outFile = new File(FileUtil.toFile(cfgFolder), CFG_FILE_NAME);
            }
            saveMetroConfig(metroConfig, outFile);
        }
    }

    private void saveMetroConfig(@NonNull MetroConfig metroConfig, @NonNull File cfgFile) {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(metroConfig, cfgFile);
        } catch (Exception e) {
            Logger.global.warning(String.format("Unable to marshall metro config to file: '%s'", cfgFile.toString()));
        }
    }

    public List<TubeFactoryConfig> createTubeFactoryConfigList(List<String> classList) {
        List<TubeFactoryConfig> tubeFacList = new ArrayList<TubeFactoryConfig>();
        for (String clsName : classList) {
            TubeFactoryConfig cfg = objFactory.createTubeFactoryConfig();
            cfg.setClassName(clsName);
            tubeFacList.add(cfg);
        }
        return tubeFacList;
    }
            
    public TubelineDefinition getDefaultTubeline(MetroConfig cfg) {
        return getDefaultTubeline(cfg.getTubelines());
    }

    private TubelineDefinition getDefaultTubeline(Tubelines tubelines) {
        if (tubelines == null) return null;
        return getTubeline(tubelines, tubelines.getDefault());
    }

    private TubelineDefinition getTubeline(Tubelines tubelines, String tubeRef) {
        if (tubeRef == null) return null;
        List<TubelineDefinition> tubelineDefs = tubelines.getTubelineDefinitions();
        for (TubelineDefinition tubelineDef : tubelineDefs) {
            if (tubeRef.equals("#" + tubelineDef.getName())) {
                return tubelineDef;
            }
        }
        return null;
    }

    public TubelineDefinition getTubeline(MetroConfig cfg, String endpointRef) {
        Tubelines tubelines = cfg.getTubelines();
        TubelineMapping mapping = getTubelineMapping(cfg, endpointRef);
        if (mapping != null) {
            return getTubeline(tubelines, mapping.getTubelineRef());
        }
        return null;
    }

    public static TubelineMapping getTubelineMapping(MetroConfig cfg, String endpointRef) {
        Tubelines tubelines = cfg.getTubelines();
        if (tubelines != null) {
            List<TubelineMapping> mappings = tubelines.getTubelineMappings();
            for (TubelineMapping mapping : mappings) {
                if (endpointRef.equals(mapping.getEndpointRef())) {
                    return mapping;
                }
            }
        }
        return null;
    }

    private FileObject getConfigFolder(Project project, boolean client) {
        WsitProvider wsitProvider = project.getLookup().lookup(WsitProvider.class);
        return (wsitProvider != null) ? wsitProvider.getConfigFilesFolder(client) : null;
    }

}
