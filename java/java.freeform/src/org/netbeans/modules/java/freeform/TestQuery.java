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

package org.netbeans.modules.java.freeform;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Reports location of unit tests.
 * Rather than associating each test root to each source root, the project may
 * have any number of source and test roots, and each source root is associated
 * with all test roots, and each test root is associated with all source roots.
 * This is not as precise as it could be but in practice it is unlikely to matter.
 * Also all package roots within one compilation unit are treated interchangeably.
 * @see "#47835"
 * @author Jesse Glick
 */
final class TestQuery implements MultipleRootsUnitTestForSourceQueryImplementation {
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final AuxiliaryConfiguration aux;
    
    public TestQuery(AntProjectHelper helper, PropertyEvaluator eval, AuxiliaryConfiguration aux) {
        this.helper = helper;
        this.eval = eval;
        this.aux = aux;
    }

    public URL[] findUnitTests(FileObject source) {
        URL[][] data = findSourcesAndTests();
        URL sourceURL = source.toURL();
        if (Arrays.asList(data[0]).contains(sourceURL)) {
            return data[1];
        } else {
            return null;
        }
    }

    public URL[] findSources(FileObject unitTest) {
        URL[][] data = findSourcesAndTests();
        URL testURL = unitTest.toURL();
        if (Arrays.asList(data[1]).contains(testURL)) {
            return data[0];
        } else {
            return null;
        }
    }
    
    /**
     * Look for all source roots and test source roots in the project.
     * @return two-element array: first source roots, then test source roots
     */
    private URL[][] findSourcesAndTests() {
        List<URL> sources = new ArrayList<URL>();
        List<URL> tests = new ArrayList<URL>();
        Element data = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_LASTEST, true);
        if (data != null) {
            for (Element cu : XMLUtil.findSubElements(data)) {
                assert cu.getLocalName().equals("compilation-unit") : cu;
                boolean isTests = XMLUtil.findElement(cu, "unit-tests", JavaProjectNature.NS_JAVA_LASTEST) != null; // NOI18N
                for (Element pr : XMLUtil.findSubElements(cu)) {
                    if (pr.getLocalName().equals("package-root")) { // NOI18N
                        String rawtext = XMLUtil.findText(pr);
                        assert rawtext != null;
                        String evaltext = eval.evaluate(rawtext);
                        if (evaltext != null) {
                            (isTests ? tests : sources).add(evalTextToURL(evaltext));
                        }
                    }
                }
            }
        }
        return new URL[][] {
            sources.toArray(new URL[0]),
            tests.toArray(new URL[0]),
        };
    }

    private URL evalTextToURL(String evaltext) {
        File location = helper.resolveFile(evaltext);
        return FileUtil.urlForArchiveOrDir(location);
    }
    
}
