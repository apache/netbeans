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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.javadoc.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/* Base class providing search for JDK1.2/1.3 documentation
 * Jdk12SearchType.java
 *
 * @author Petr Hrebejk, Petr Suchomel
 */
@ServiceProvider(service=JavadocSearchType.class, position=100)
public final class Jdk12SearchType_japan extends Jdk12SearchType {

    private String  japanEncoding;

    /** generated Serialized Version UID */
    private static final long serialVersionUID =-2453877778724454324L;
    
    private static final String JDK12_ALLCLASSES_JA = "\u3059\u3079\u3066\u306e\u30af\u30e9\u30b9"; // NOI18N

    /** Getter for property encoding.
     * @return Value of property encoding.
    */
    public java.lang.String getJapanEncoding() {
        return ( japanEncoding != null ) ? japanEncoding : "JISAutoDetect";    //NOI18N
    }
    
    /** Setter for property encoding.
     * @param encoding New value of property encoding.
    */
    public void setJapanEncoding(java.lang.String japanEncoding) {
        String old = this.japanEncoding;
        this.japanEncoding = japanEncoding;
//        firePropertyChange("japanEncoding", old, japanEncoding);   //NOI18N
    }    
        
    /** Returns Java doc search thread for doument
     * @param toFind String to find
     * @param fo File object containing index-files
     * @param diiConsumer consumer for parse events
     * @return IndexSearchThread
     * @see IndexSearchThread
     */    
    @Override
    public IndexSearchThread getSearchThread(String toFind, URL fo, IndexSearchThread.DocIndexItemConsumer diiConsumer) {
        //here you can send one more parameter .. getJapanEncoding
        return new SearchThreadJdk12_japan ( toFind, fo, diiConsumer, isCaseSensitive(), getJapanEncoding() );
    }    

    @Override
    public boolean accepts(URL root, String encoding) {
        if (encoding == null) {
            return false;
        }
        encoding = encoding.toLowerCase();
        
        // if Japanese encoding, return true quickly
        if ("iso-2022-jp".equals(encoding) // NOI18N
                || "sjis".equals(encoding) // NOI18N
                || "euc-jp".equals(encoding) ) { // NOI18N
            
            setJapanEncoding(encoding);
            return true;
        }
        
        if ("utf-8".equals(encoding)) { // NOI18N
            try {
                InputStream is = URLUtils.open(root, "allclasses-frame.html"); // NOI18N
                if (is == null) {
                    return false;
                }
                boolean jazip = false;
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(is, encoding));
                    String line;
                    while ((line = r.readLine()) != null) {
                        if (line.contains(JDK12_ALLCLASSES_JA)) {
                            jazip = true;
                        }
                        if (line.toLowerCase().contains("</title>")) { // NOI18N
                            break;
                        }
                    }
                } finally {
                    is.close();
                }
                if (jazip) {
                    setJapanEncoding(encoding);
                }
                return jazip;
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        return false;
    }

}
