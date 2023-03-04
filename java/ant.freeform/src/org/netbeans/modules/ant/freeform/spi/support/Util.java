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

package org.netbeans.modules.ant.freeform.spi.support;

import java.io.File;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.FreeformProjectType;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

/**
 * Miscellaneous helper methods.
 * @author Jesse Glick, David Konecny
 */
public class Util {
    
    private Util() {}
    
    /**
     * Finds AuxiliaryConfiguration for the given project helper. The method
     * finds project associated with the helper and searches 
     * AuxiliaryConfiguration in project's lookup.
     *
     * @param helper instance of project's AntProjectHelper
     * @return project's AuxiliaryConfiguration
     */
    public static AuxiliaryConfiguration getAuxiliaryConfiguration(AntProjectHelper helper) {
        try {
            Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
            AuxiliaryConfiguration aux = p.getLookup().lookup(AuxiliaryConfiguration.class);
            assert aux != null;
            return aux;
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }

    /** 
     * Relativize given file against the original project and if needed use 
     * ${project.dir} property as base. If file cannot be relativized
     * the absolute filepath is returned.
     * @param projectBase original project base folder
     * @param freeformBase Freeform project base folder
     * @param location location to relativize
     * @return text suitable for storage in project.xml representing given location
     */
    public static String relativizeLocation(File projectBase, File freeformBase, File location) {
        if (CollocationQuery.areCollocated(projectBase, location)) {
            if (projectBase.equals(freeformBase)) {
                return PropertyUtils.relativizeFile(projectBase, location);
            } else if (projectBase.equals(location) && ProjectConstants.PROJECT_LOCATION_PREFIX.endsWith("/")) { // NOI18N
                return ProjectConstants.PROJECT_LOCATION_PREFIX.substring(0, ProjectConstants.PROJECT_LOCATION_PREFIX.length() - 1);
            } else {
                return ProjectConstants.PROJECT_LOCATION_PREFIX + PropertyUtils.relativizeFile(projectBase, location);
            }
        } else {
            return location.getAbsolutePath();
        }
    }

    /**
     * Resolve given string value (e.g. "${project.dir}/lib/lib1.jar")
     * to a File.
     * @param evaluator evaluator to use for properties resolving
     * @param freeformProjectBase freeform project base folder
     * @param val string to be resolved as file
     * @return resolved File or null if file could not be resolved
     */
    public static File resolveFile(PropertyEvaluator evaluator, File freeformProjectBase, String val) {
        String location = evaluator.evaluate(val);
        if (location == null) {
            return null;
        }
        return PropertyUtils.resolveFile(freeformProjectBase, location);
    }

    /**
     * Returns location of original project base folder. The location can be dirrerent
     * from NetBeans metadata project folder.
     * @param helper AntProjectHelper associated with the project
     * @param evaluator PropertyEvaluator associated with the project
     * @return location of original project base folder
     */
    public static File getProjectLocation(AntProjectHelper helper, PropertyEvaluator evaluator) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        String loc = evaluator.getProperty(ProjectConstants.PROP_PROJECT_LOCATION);
        if (loc != null) {
            return helper.resolveFile(loc);
        } else {
            return FileUtil.toFile(helper.getProjectDirectory());
        }
    }

    /**Get the "default" (user-specified) ant script for the given freeform project.
     * Please note that this method may return <code>null</code> if there is no such script.
     *
     * WARNING: This method is there only for a limited set of usecases like the profiler plugin.
     * It should not be used by the freeform project natures.
     *
     * @param prj the freeform project
     * @return the "default" ant script or <code>null</code> if there is no such a script
     * @throws IllegalArgumentException if the passed project is not a freeform project.
     */
    public static FileObject getDefaultAntScript(Project prj) throws IllegalArgumentException {
        ProjectAccessor accessor = prj.getLookup().lookup(ProjectAccessor.class);
        
        if (accessor == null) {
            throw new IllegalArgumentException("Only FreeformProjects are supported.");
        }
        
        return FreeformProjectGenerator.getAntScript(accessor.getHelper(), accessor.getEvaluator());
    }
    
    /**
     * Namespace of data used in {@link #getPrimaryConfigurationData} and {@link #putPrimaryConfigurationData}.
     * @since org.netbeans.modules.ant.freeform/1 1.15
     */
    public static final String NAMESPACE = "http://www.netbeans.org/ns/freeform-project/2"; // NOI18N

    /**
     * Replacement for {@link AntProjectHelper#getPrimaryConfigurationData}
     * taking into account the /1 -> /2 upgrade.
     * @param helper a project helper
     * @return data in {@link #NAMESPACE}, converting /1 data if needed
     * @since org.netbeans.modules.ant.freeform/1 1.15
     */
    public static Element getPrimaryConfigurationData(final AntProjectHelper helper) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Element>() {
            public Element run() {
                AuxiliaryConfiguration ac = helper.createAuxiliaryConfiguration();
                Element data = ac.getConfigurationFragment(FreeformProjectType.NAME_SHARED, NAMESPACE, true);
                if (data != null) {
                    return data;
                } else {
                    return XMLUtil.translateXML(helper.getPrimaryConfigurationData(true), NAMESPACE);
                }
            }
        });
    }

    /**
     * Replacement for {@link AntProjectHelper#putPrimaryConfigurationData}
     * taking into account the /1 -> /2 upgrade.
     * Always pass the /2 data, which will be converted to /1 where legal.
     * @param helper a project helper
     * @param data data in {@link #NAMESPACE}
     * @throws IllegalArgumentException if the incoming data is not in {@link #NAMESPACE}
     * @since org.netbeans.modules.ant.freeform/1 1.15
     */
    public static void putPrimaryConfigurationData(final AntProjectHelper helper, final Element data) {
        if (!data.getNamespaceURI().equals(FreeformProjectType.NS_GENERAL)) {
            throw new IllegalArgumentException("Bad namespace"); // NOI18N
        }
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                Element dataAs1 = XMLUtil.translateXML(data, FreeformProjectType.NS_GENERAL_1);
                if (SCHEMA_1 == null) {
                    try {
                        SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                        SCHEMA_1 = f.newSchema(FreeformProject.class.getResource("resources/freeform-project-general.xsd")); // NOI18N
                        SCHEMA_2 = f.newSchema(FreeformProject.class.getResource("resources/freeform-project-general-2.xsd")); // NOI18N
                    } catch (SAXException e) {
                        Exceptions.printStackTrace(e); // cf. #169994
                        putPrimaryConfigurationDataAs1(helper, dataAs1);
                    }
                }
                try {
                    XMLUtil.validate(dataAs1, SCHEMA_1);
                    putPrimaryConfigurationDataAs1(helper, dataAs1);
                } catch (SAXException x1) {
                    try {
                        XMLUtil.validate(data, SCHEMA_2);
                        putPrimaryConfigurationDataAs2(helper, data);
                    } catch (SAXException x2) {
                        assert false : x2.getMessage() + "; rejected content: " + format(data);
                        putPrimaryConfigurationDataAs1(helper, dataAs1);
                    }
                }
                return null;
            }
        });
    }
    private static Schema SCHEMA_1, SCHEMA_2;
    private static void putPrimaryConfigurationDataAs1(AntProjectHelper helper, Element data) {
        helper.createAuxiliaryConfiguration().removeConfigurationFragment(FreeformProjectType.NAME_SHARED, NAMESPACE, true);
        helper.putPrimaryConfigurationData(data, true);
    }
    private static void putPrimaryConfigurationDataAs2(AntProjectHelper helper, Element data) {
        Document doc = data.getOwnerDocument();
        Element dummy1 = doc.createElementNS(FreeformProjectType.NS_GENERAL_1, FreeformProjectType.NAME_SHARED);
        // Make sure it is not invalid.
        dummy1.appendChild(doc.createElementNS(FreeformProjectType.NS_GENERAL_1, "name")). // NOI18N
                appendChild(doc.createTextNode(XMLUtil.findText(XMLUtil.findElement(data, "name", NAMESPACE)))); // NOI18N
        helper.putPrimaryConfigurationData(dummy1, true);
        helper.createAuxiliaryConfiguration().putConfigurationFragment(data, true);
    }
    private static String format(Element data) {
        LSSerializer ser = ((DOMImplementationLS) data.getOwnerDocument().getImplementation().getFeature("LS", "3.0")).createLSSerializer();
        try {
            ser.getDomConfig().setParameter("format-pretty-print", true);
            ser.getDomConfig().setParameter("xml-declaration", false);
        } catch (DOMException ignore) {}
        return ser.writeToString(data);
    }

}
