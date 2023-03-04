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
