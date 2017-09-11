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
package org.netbeans.modules.xml.retriever.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.netbeans.modules.xml.retriever.*;

/**
 * @author girix
 */
public class FileResourceRetriever implements ResourceRetriever{
    
    public boolean accept(String baseAddr, String currentAddr) throws URISyntaxException {
        
        URI currURI = new URI(currentAddr);
        if( (currURI.isAbsolute()) && (currURI.getScheme().equalsIgnoreCase("file"))) //NOI18N
            return true;
        if(!currURI.isAbsolute() && (baseAddr == null))
            return true;
        if(baseAddr != null){
            if(!currURI.isAbsolute()){
                URI baseURI = new URI(baseAddr);
                if(baseURI.getScheme().equalsIgnoreCase("file")) //NOI18N
                    return true;
            }
        }
        return false;
    }
    
    long streamLength = 0;

    public HashMap<String, InputStream> retrieveDocument(String baseAddress, String documentAddress) throws IOException,URISyntaxException{
        String address = getEffectiveAddress(baseAddress, documentAddress);

        if (address == null) {
            return new HashMap<String, InputStream>();
        }
        URI currURI = new URI(address);
        HashMap<String, InputStream> result = null;
        File curFile = new File(currURI);
    
        if(curFile.isFile()){
            InputStream is = new FileInputStream(curFile);
            result = new HashMap<String, InputStream>();
            result.put(curFile.toURI().toString(), is);
            streamLength = curFile.length();
            return result;
        }else{
            //file not found in the system
            throw new IOException("File not found: "+curFile.toString()); //NOI18N
        }
    }
    
    public long getStreamLength() {
        return streamLength;
    }
    
    public String getEffectiveAddress(String baseAddress, String documentAddress) throws IOException, URISyntaxException {
//System.out.println();
//System.out.println("baseAddress: " + baseAddress);
//System.out.println("documentAddress: " + documentAddress);
        URI currURI = new URI(documentAddress);
        if(currURI.isAbsolute()){
            //abs file URI
            return currURI.toString();
        }else{
            //relative URI
            if(baseAddress != null){
                URI baseURI = new URI(baseAddress);
                return (baseURI.resolve(currURI)).toString();
            }else{
                //neither the current URI nor the base URI are absoulte. So, can not resolve this
                //path
//System.out.println("    return null");
                return null;
            }
        }
    }
}
