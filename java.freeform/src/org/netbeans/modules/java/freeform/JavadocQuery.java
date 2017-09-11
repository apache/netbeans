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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
            return urls.toArray(new URL[urls.size()]);
        }
        
        public void addChangeListener(ChangeListener l) {}

        public void removeChangeListener(ChangeListener l) {}

    }
    
}
