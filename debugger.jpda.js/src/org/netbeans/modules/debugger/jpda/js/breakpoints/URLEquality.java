/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.js.breakpoints;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/** Equality for URLs that can handle canonical paths on local file URLs.
 *
 */
final class URLEquality {
    private final String protocol;
    private final String host;
    private final int port;
    private final String path;
    private final int hash;

    public URLEquality(URL url) {
        protocol = url.getProtocol().toLowerCase();
        String h = url.getHost();
        if (h != null) {
            h = h.toLowerCase();
        }
        host = h;
        port = url.getPort();
        path = url.getPath();
        int last = url.getPath().lastIndexOf("/");
        hash = protocol.hashCode() + host.hashCode() + port + url.getPath().substring(last + 1).hashCode();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof URLEquality)) {
            return false;
        }
        URLEquality ue = (URLEquality) obj;
        if (ue.hash != hash) {
            return false;
        }
        if (
            protocol.equals(ue.protocol) && 
            Objects.equals(host, ue.host) &&
            port == ue.port
        ) {
            if (Objects.equals(path, ue.path)) {
                return true;
            }
            if ("file".equals(protocol) && path != null && ue.path != null) { // NOI18N
                try {
                    File fThis = new File(path);
                    File fObj = new File(ue.path);
                    if (fThis.getCanonicalPath().equals(fObj.getCanonicalPath())) {
                        return true;
                    }
                } catch (IOException ex) {
                    // go on
                }
            }
        }
        return false;
    }
    
}
