/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hudson.impl;

import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.api.ui.FailureDataDisplayer;
import org.netbeans.modules.hudson.api.ui.FailureDataDisplayer.Case;
import org.netbeans.modules.hudson.api.ui.FailureDataDisplayer.Suite;
import org.netbeans.modules.hudson.spi.BuilderConnector;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author jhavlin
 */
@NbBundle.Messages({
    "no_test_result=No test result found for this build."})
public class HudsonFailureDataProvider extends BuilderConnector.FailureDataProvider {

    private static final Logger LOG = Logger.getLogger(
            HudsonFailureDataProvider.class.getName());

    @Override
    public void showFailures(final HudsonJobBuild build,
            final FailureDataDisplayer displayer) {
        new RequestProcessor(build.getUrl() + "failures").post( // NOI18N
                new Runnable() {
            @Override
            public void run() {
                showBuildFailures(build.getJob(), build.getUrl(), displayer);
            }
        });
    }

    @Override
    public void showFailures(final HudsonMavenModuleBuild moduleBuild,
            final FailureDataDisplayer displayer) {
        new RequestProcessor(moduleBuild.getUrl() + "failures").post( // NOI18N
                new Runnable() {
            @Override
            public void run() {
                showBuildFailures(moduleBuild.getBuild().getJob(),
                        moduleBuild.getUrl(), displayer);
            }
        });
    }

    private void showBuildFailures(HudsonJob job, String url,
            FailureDataDisplayer displayer) {

        try {
            XMLReader parser = XMLUtil.createXMLReader();
            parser.setContentHandler(new ContentHandler(displayer));
            // XXX could use ?tree (would be faster) if there were an alternate object for failed tests only
            String u = url + "testReport/api/xml?xpath=//suite[case/errorStackTrace]&wrapper=failures"; //NOI18N
            InputSource source = new InputSource(new ConnectionBuilder().job(job).url(u).connection().getInputStream());
            source.setSystemId(u);
            displayer.open();
            parser.parse(source);
        } catch (FileNotFoundException x) {
            Toolkit.getDefaultToolkit().beep();
            StatusDisplayer.getDefault().setStatusText(Bundle.no_test_result());
        } catch (Exception x) {
            Toolkit.getDefaultToolkit().beep();
            LOG.log(Level.INFO, null, x);
        }
    }

    private class ContentHandler extends DefaultHandler {

        private StringBuilder buf;
        private final FailureDataDisplayer displayer;

        public ContentHandler(FailureDataDisplayer displayer) {
            this.displayer = displayer;
        }

        long parseDuration(String d) {
            if (d == null) {
                return 0;
            }
            try {
                return (long) (1000 * Float.parseFloat(d));
            } catch (NumberFormatException x) {
                return 0;
            }
        }

        Suite suite = null;
        Case caze = null;

        public @Override
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.matches("errorStackTrace|stdout|stderr|name|className")) { //NOI18N
                buf = new StringBuilder();
            } else if (qName.equals("suite")) { //NOI18N
                suite = new Suite();
                caze = null;
            } else if (qName.equals("case") && suite != null) { //NOI18N
                caze = new Case();
            }
        }

        public @Override
        void characters(char[] ch, int start, int length) throws SAXException {
            if (buf != null) {
                buf.append(ch, start, length);
            }
        }

        public @Override
        void endElement(String uri, String localName, String qName) throws SAXException {
            if (suite == null) {
                return;
            }
            String text = buf != null && buf.length() > 0 ? buf.toString() : null;
            buf = null;
            if (caze == null) { // suite level
                if (qName.equals("stdout")) { // NOI18N
                    suite.setStdout(text);
                } else if (qName.equals("stderr")) { // NOI18N
                    suite.setStderr(text);
                } else if (qName.equals("name")) { // NOI18N
                    suite.setName(text);
                } else if (qName.equals("duration")) { // NOI18N
                    suite.setDuration(parseDuration(text));
                }
            } else { // case level
                if (qName.equals("errorStackTrace")) { // NOI18N
                    caze.setErrorStackTrace(text);
                } else if (qName.equals("name")) { // NOI18N
                    caze.setName(text);
                } else if (qName.equals("className")) { // NOI18N
                    caze.setClassName(text);
                } else if (qName.equals("duration")) { // NOI18N
                    caze.setDuration(parseDuration(text));
                }
            }
            if (qName.equals("case")) { //NOI18N
                suite.addCase(caze);
                caze = null;
            } else if (qName.equals("suite")) { // NOI18N
                try {
                    displayer.showSuite(suite);
                } catch (Exception x) {
                    LOG.log(Level.FINE, null, x);
                }
                suite = null;
            }
        }

        @Override
        public void endDocument() throws SAXException {
            displayer.close();
        }
    }
}
