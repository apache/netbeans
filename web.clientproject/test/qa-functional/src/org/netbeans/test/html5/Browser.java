/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
