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
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Handles Javadoc information.
 * @author Jesse Glick
 */
final class JavadocQuery implements JavadocForBinaryQueryImplementation {
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final AuxiliaryConfiguration aux;
    
    public JavadocQuery(AntProjectHelper helper, PropertyEvaluator eval, AuxiliaryConfiguration aux) {
        this.helper = helper;
        this.eval = eval;
        this.aux = aux;
    }

    public JavadocForBinaryQuery.Result findJavadoc(URL binaryRoot) {
        Element data = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_LASTEST, true);
        if (data != null) {
            for (Element cu : XMLUtil.findSubElements(data)) {
                assert cu.getLocalName().equals("compilation-unit") : cu;
                boolean rightCU = false;
                for (Element builtTo : XMLUtil.findSubElements(cu)) {
                    if (builtTo.getLocalName().equals("built-to")) { // NOI18N
                        String rawtext = XMLUtil.findText(builtTo);
                        assert rawtext != null;
                        String evaltext = eval.evaluate(rawtext);
                        if (evaltext != null) {
                            URL url = evalTextToURL(evaltext);
                            if (url != null) {
                                if (url.equals(binaryRoot)) {
                                    rightCU = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (rightCU) {
                    List<URL> resultURLs = new ArrayList<URL>();
                    for (Element javadocTo : XMLUtil.findSubElements(cu)) {
                        if (javadocTo.getLocalName().equals("javadoc-built-to")) { // NOI18N
                            String rawtext = XMLUtil.findText(javadocTo);
                            assert rawtext != null;
                            String evaltext = eval.evaluate(rawtext);
                            if (evaltext != null) {
                                resultURLs.add(evalTextToURL(evaltext));
                            }
                        }
                    }
                    if (resultURLs.size() == 0) {
                        return null;
                    }
                    return new FixedResult(resultURLs);
                }
            }
        }
        return null;
    }
    
    private URL evalTextToURL(String evaltext) {
        File location = helper.resolveFile(evaltext);
        return FileUtil.urlForArchiveOrDir(location);
    }
    
    private static final class FixedResult implements JavadocForBinaryQuery.Result {
        
        private final List<URL> urls;
        
        public FixedResult(List<URL> urls) {
            this.urls = urls;
        }

        public URL[] getRoots() {
            return urls.toArray(new URL[0]);
        }
        
        public void addChangeListener(ChangeListener l) {}

        public void removeChangeListener(ChangeListener l) {}

    }
    
}
