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
package org.netbeans.modules.web.webkit.debugging.spi;

import java.net.URL;
import org.netbeans.modules.web.webkit.debugging.api.TransportStateException;

/**
 * Transport of {@link Command}s or {@link Response}s between IDE and WebKit browser.
 */
public interface TransportImplementation {

    // requestChildNodes was introduced in 
    // http://trac.webkit.org/changeset/93396/trunk/Source/WebCore/inspector/Inspector.json
    public static final String VERSION_UNKNOWN_BEFORE_requestChildNodes = "version without requestChildNodes";
    public static final String VERSION_1 = "version 1.0";
    
    /**
     * Activate transport.
     */
    boolean attach();
    
    /**
     * Deactivate transport.
     */
    boolean detach();

    /**
     * Send command to WebKit.
     * 
     * @throws TransportStateException when the transport is not in a state
     * that allows execution of the given command.
     */
    void sendCommand(Command command) throws TransportStateException;
    
    /**
     * Register callback for receiving responses from WebKit.
     */
    void registerResponseCallback(ResponseCallback callback);

    /**
     * Descriptive name of the established transport. For example URL being debugged.
     */
    String getConnectionName();
    
    /**
     * URL being debugged.
     */
    URL getConnectionURL();
    
    /**
     * Returns version of the protocol supported on browser side. See constants
     * above.
     */
    String getVersion();
    
}
