/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

/*
 * ResourceRetriever.java
 *
 * Created on January 9, 2006, 2:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.netbeans.modules.xml.retriever.*;

/**
 *
 * @author girix
 */
public interface ResourceRetriever {
    /**
     * This method will be called by the factory class. Impl should decide if it supports the protocol
     * @param baseAddr originating documents address
     * @param currentAddr address of the document that needs to be fetched - as mentioned in the base document.
     * @return if impl supports then true else false
     */
    public boolean accept(String baseAddr, String currentAddr) throws URISyntaxException;
    
    /**
     * Given the base doc address and the current address (that could be either relative or absoulte), determine the final address to fetch doc and get the stream of that doc.
     * The method may throw {@link ResourceRedirectException} if the Retriever resolves the resource
     * to a different URL incompatible with the Retriever instance.
     * 
     * @param baseAddress address of the base document where the link was found
     * @param documentAddress current document address as mentioned in the base doc
     * @return Hash map has the "key" as the final address from where the file was fetched and "value" has the input sream of the file.
     */
    public HashMap<String,InputStream> retrieveDocument(
            String baseAddress, String documentAddress) throws IOException,URISyntaxException;
    
    
     /**
     * Given the base doc address and the current address (that could be either relative or absoulte), determine the final address to fetch doc and get the stream of that doc.
     * @param baseAddress address of the base document where the link was found
     * @param documentAddress current document address as mentioned in the base doc
     * @return Hash map has the "key" as the final address from where the file was fetched and "value" has the input sream of the file.
     */
    public String getEffectiveAddress(
            String baseAddress, String documentAddress) throws IOException,URISyntaxException;
    
    
    /**
     * Must be called after retrieveDocument() method. 
     * This returns the number of chars in the stream. 
     * Useful for URL retriever where its better to know the length upfront.
     */
    public long getStreamLength();
}
