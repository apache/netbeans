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
package org.netbeans.modules.gsf.codecoverage.api;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.util.Exceptions;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * This utility class helps with implementations of tasks that providers
 * need, such as project persistence of the enabled- and aggregation- state
 * for a project. (This isn't done automatically by the coverage manager
 * since there may be code coverage frameworks, such as emma for a Java project,
 * where this state should be recorded as the ant attributes directly instead
 * of a separate auxiliary state.
 *
 * @author Tor Norbye
 */
public class CoverageProviderHelper {
    private static final String COVERAGE_NAMESPACE_URI = "http://www.netbeans.org/ns/code-coverage/1"; // NOI18N

    private CoverageProviderHelper() {
        // Utility method class
    }

    public static boolean isEnabled(Project project) {
        AuxiliaryConfiguration config = ProjectUtils.getAuxiliaryConfiguration(project);
        Element configurationFragment = config.getConfigurationFragment("coverage", COVERAGE_NAMESPACE_URI, false); // NOI18N
        if (configurationFragment == null) {
            return false;
        }
        return configurationFragment.getAttribute("enabled").equals("true"); // NOI18N
    }

    public static void setEnabled(Project project, boolean enabled) {
        AuxiliaryConfiguration config = ProjectUtils.getAuxiliaryConfiguration(project);
        if (ProjectManager.getDefault().isValid(project)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                org.w3c.dom.Document document = builder.newDocument();
                Element configurationFragment = document.createElementNS(
                        COVERAGE_NAMESPACE_URI,
                        "coverage"); // NOI18N
                configurationFragment.setAttribute("enabled", enabled ? "true" : "false"); // NOI18N

                config.putConfigurationFragment(
                        configurationFragment, false);
                ProjectManager.getDefault().saveProject(project);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ParserConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (DOMException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    public static boolean isAggregating(Project project) {
        AuxiliaryConfiguration config = ProjectUtils.getAuxiliaryConfiguration(project);
        Element configurationFragment = config.getConfigurationFragment("coverage", COVERAGE_NAMESPACE_URI, false); // NOI18N
        if (configurationFragment == null) {
            return false;
        }
        return configurationFragment.getAttribute("aggregating").equals("true"); // NOI18N
    }

    public static void setAggregating(Project project, boolean aggregating) {
        AuxiliaryConfiguration config = ProjectUtils.getAuxiliaryConfiguration(project);
        if (ProjectManager.getDefault().isValid(project)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                org.w3c.dom.Document document = builder.newDocument();
                Element configurationFragment = document.createElementNS(
                        COVERAGE_NAMESPACE_URI,
                        "coverage"); // NOI18N
                configurationFragment.setAttribute("aggregating", aggregating ? "true" : "false"); // NOI18N

                config.putConfigurationFragment(
                        configurationFragment, false);
                ProjectManager.getDefault().saveProject(project);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ParserConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (DOMException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }
}
