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
package org.netbeans.modules.web.clientproject.api.network.ui;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class NetworkErrorPanelTest {

    @Test
    public void testJoinRequestsSingle() {
        List<String> requests = Arrays.asList("www.oracle.com");
        Assert.assertEquals("www.oracle.com", NetworkErrorPanel.joinRequests(requests));
    }

    @Test
    public void testJoinRequestsMulti() {
        List<String> requests = Arrays.asList(
                "www.oracle.com",
                "MyJsLibrary-0.1");
        Assert.assertEquals("www.oracle.com<br>MyJsLibrary-0.1", NetworkErrorPanel.joinRequests(requests));
    }

    @Test
    public void testDecorateRequestPlain() {
        String request = "www.oracle.com";
        Assert.assertEquals("www.oracle.com", NetworkErrorPanel.decorateRequest(request));
    }

    @Test
    public void testDecorateRequestPlainLong() {
        String request = "www.oracle.com www.oracle.com www.oracle.com www.oracle.com www.oracle.com www.oracle.com www.oracle.com www.oracle.com ";
        Assert.assertEquals("www.oracle.com www.oracle.com www.oracle.com www.oracle.com www.oracle.com www.o...", NetworkErrorPanel.decorateRequest(request));
    }

    @Test
    public void testDecorateRequestHttp() {
        String request = "http://www.oracle.com";
        Assert.assertEquals("<a href=\"http://www.oracle.com\">http://www.oracle.com</a>", NetworkErrorPanel.decorateRequest(request));
    }

    @Test
    public void testDecorateRequestHttpLong() {
        String request = "http://www.oracle.com/www.oracle.com/www.oracle.com/www.oracle.com/www.oracle.com/";
        Assert.assertEquals("<a href=\"http://www.oracle.com/www.oracle.com/www.oracle.com/www.oracle.com/www.oracle.com/\">http://www.oracle.com/www.oracle.com/www.oracle.com/www.oracle.com/www.oracle.co...</a>", NetworkErrorPanel.decorateRequest(request));
    }

    @Test
    public void testDecorateRequestHttps() {
        String request = "https://www.oracle.com";
        Assert.assertEquals("<a href=\"https://www.oracle.com\">https://www.oracle.com</a>", NetworkErrorPanel.decorateRequest(request));
    }

    @Test
    public void testDecorateRequestFtp() {
        String request = "ftp://www.oracle.com";
        Assert.assertEquals("ftp://www.oracle.com", NetworkErrorPanel.decorateRequest(request));
    }

}
