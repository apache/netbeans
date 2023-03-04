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
package org.netbeans.test.html5;

import java.io.IOException;

/**
 *
 * @author Vladimir Riha
 */
public class Browser {

    private Platform currentOS;
    public String name;

    enum Platform {

        WINDOWS,
        LINUX,
        OTHER
    }

    public Browser() {
        if (!GeneralHTMLProject.inEmbeddedBrowser) {
            if (GeneralHTMLProject.currentBrowser.indexOf("Chrome") > -1) {
                this.name = "chrome";
            } else if (GeneralHTMLProject.currentBrowser.indexOf("Chromium") > -1) {
                if (this.getCurrentOS() == Platform.LINUX) {
                    this.name = "chromium-browser";
                } else {
                    this.name = "chrome";
                }
            } else {
                this.name = GeneralHTMLProject.currentBrowser.toLowerCase();
            }
        }
    }

    /**
     * Returns current operating system
     *
     * @return
     */
    private Platform getCurrentOS() {
        if (this.currentOS == null) {
            if (System.getProperty("os.name").startsWith("Windows")) {
                this.currentOS = Platform.WINDOWS;
            } else if (System.getProperty("os.name").startsWith("Linux")) {
                this.currentOS = Platform.LINUX;
            } else {
                this.currentOS = Platform.OTHER;
            }
        }
        return this.currentOS;
    }

    /**
     * Closes browser window. If project is running in embedded browser,
     * parameter pageTitle is value of &lt;title&gt;. For external browser, this
     * parameter will be ignored
     *
     * @param pageTitle
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    public void closeBrowser(String pageTitle) throws IOException, UnsupportedOperationException {
        if (GeneralHTMLProject.currentBrowser.indexOf("Embedded") < 0) {
            this.closeExternalBrowser();
        } else {
            (new EmbeddedBrowserOperator(pageTitle)).close();
        }
    }

    /**
     * Kills all processes that are named like current browser.
     * @throws IOException
     * @throws UnsupportedOperationException 
     */
    private void closeExternalBrowser() throws IOException, UnsupportedOperationException {
        switch (this.getCurrentOS()) {
            case WINDOWS:
                Runtime.getRuntime().exec("taskkill /IM " + this.name + ".exe");
                break;
            case LINUX:
                Runtime.getRuntime().exec("killall " + this.name);
                break;
            default:
                throw new UnsupportedOperationException("Action not available for platform " + this.currentOS);
        }
    }
}
