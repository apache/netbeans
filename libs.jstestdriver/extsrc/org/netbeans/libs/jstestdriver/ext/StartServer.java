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
package org.netbeans.libs.jstestdriver.ext;

import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.browser.DocType;
import com.google.jstestdriver.config.DefaultConfiguration;
import com.google.jstestdriver.embedded.JsTestDriverBuilder;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.runner.RunnerMode;
import java.io.File;
import java.util.concurrent.Semaphore;

/**
 *
 */
public class StartServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        assert args.length == 2;
        int port = Integer.parseInt(args[0]);
        boolean strict = Boolean.parseBoolean(args[1]);
        new StartServer().startServer(port, strict);
    }
    
    private Semaphore s;
    
    public void startServer(int port, boolean strict) {
        System.out.println("Starting js-test-driver server on port "+port);
        JsTestDriver serverRunner = new JsTestDriverBuilder()
                .setDefaultConfiguration(new ServerConfiguration(strict))
                .raiseExceptionOnTestFailure(false)
                .addServerListener(new MyListener())
                .setRunnerMode(RunnerMode.QUIET)
                .setPort(port).build();
        serverRunner.startServer();

        // wait until server is stopped:
        s = new Semaphore(0);
        try {
            s.acquire();
        } catch (InterruptedException ex) {
            // ok; server was stopped
        }
    }
    
    static class ServerConfiguration extends DefaultConfiguration {

        private boolean strictMode;

        public ServerConfiguration(boolean strictMode) {
            super(new BasePaths(new File(".")));
            this.strictMode = strictMode;
        }

        @Override
        public DocType getDocType() {
            return strictMode ? DocType.STRICT : DocType.QUIRKS;
        }
    }
    
    class MyListener implements com.google.jstestdriver.hooks.ServerListener {

        public MyListener() {
        }

        @Override
        public void serverStarted() {
            System.out.println("msg.server.started");
        }

        @Override
        public void serverStopped() {
            System.out.println("Starting stopped");
            s.release();
        }

        @Override
        public void browserCaptured(com.google.jstestdriver.BrowserInfo bi) {
            System.out.println("New browser captured:\n"+bi);
        }

        @Override
        public void browserPanicked(com.google.jstestdriver.BrowserInfo bi) {
            System.out.println("Browser panicked:\n"+bi);
        }
    }

}
