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
package org.netbeans.modules.php.dbgp.packets;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.UnsufficientValueException;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.w3c.dom.Node;

/**
 *
 * @author Ondrej Brejla
 */
public class RequestedUrlEvalResponse extends EvalResponse {
    private static final Logger LOGGER = Logger.getLogger(DbgpStream.class.getName());

    public RequestedUrlEvalResponse(Node node) {
        super(node);
    }

    @Override
    @NbBundle.Messages("LBL_PhpRequestedUrls=PHP Requested Urls")
    public void process(DebugSession session, DbgpCommand command) {
        Property property = getProperty();
        if (property != null) {
            InputOutput io = IOProvider.getDefault().getIO(Bundle.LBL_PhpRequestedUrls(), false);
            try {
                io.getOut().println(property.getStringValue(), new OutputListenerImpl());
            } catch (UnsufficientValueException | IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            } finally {
                io.getOut().close();
            }
        }
    }

    private static class OutputListenerImpl implements OutputListener {

        @Override
        public void outputLineAction(OutputEvent ev) {
            try {
                HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(ev.getLine()));
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }

    }

}
