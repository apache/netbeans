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

/**
 * A way to plug Live HTML implementation into debugging protocol handler.
 */
public interface LiveHTMLImplementation {

    /**
     * If Live HTML is enabled for the given URL connection then any new version of 
     * document should be recorded using this method.
     * 
     * @param connectionURL URL connection
     * @param timeStamp a timestamp, eg. System.currentTimeMillis();
     * @param content HTML document image before call described by the given callStack was executed
     * @param callStack JSON array (serialized into String) containing individual call frames;
     *   single callframe has following attributes: lineNumber(type:Number), 
     *   columnNumber(type:Number), function(type:String), script(type:String)
     */
    void storeDocumentVersionBeforeChange(URL connectionURL, long timeStamp, String content, String callStack);

    /**
     * This method follows {@link #storeDocumentVersionBeforeChange} and sends version of document after code
     * change happened in the document.
     */
    void storeDocumentVersionAfterChange(URL connectionURL, long timeStamp, String content);
    
    /**
     * If Live HTML is enabled for the given URL connection then any data received from
     * server should be recoded using this method.
     * 
     * @param connectionURL URL connection
     * @param timeStamp a timestamp, eg. System.currentTimeMillis();
     * @param data data serialized into String; can be anything
     */
    void storeDataEvent(URL connectionURL, long timeStamp, String data, String requestURL, String mime);

}
