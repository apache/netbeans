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
