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

import java.net.MalformedURLException;
import java.net.URL;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/* Base class providing search for JDK1.2/1.3 documentation
 * @author Petr Hrebejk, Petr Suchomel
 */
// no position since it must be the last service
@ServiceProvider(service=JavadocSearchType.class)
public class Jdk12SearchType extends JavadocSearchType {

    private boolean caseSensitive = true;

    /** generated Serialized Version UID */
    private static final long serialVersionUID = -2453877778724454324L;
    
    /** Getter for property caseSensitive.
     * @return Value of property caseSensitive.
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    /** Setter for property caseSensitive.
     * @param caseSensitive New value of property caseSensitive.
    */
    public void setCaseSensitive(boolean caseSensitive) {
        boolean oldVal = this.caseSensitive;
        this.caseSensitive = caseSensitive;
//        this.firePropertyChange("caseSensitive", oldVal ? Boolean.TRUE : Boolean.FALSE, caseSensitive ? Boolean.TRUE : Boolean.FALSE);   //NOI18N
    }

    public @Override URL getDocFileObject(URL apidocRoot) {
        URL u = URLUtils.findOpenable(apidocRoot, "index-files/index-1.html"); // NOI18N
        try {
            return u != null ? new URL(apidocRoot, "index-files/") : URLUtils.findOpenable(apidocRoot, "index-all.html"); // NOI18N
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }    
    
    /** Returns Java doc search thread for doument
     * @param toFind String to find
     * @param fo File object containing index-files
     * @param diiConsumer consumer for parse events
     * @return IndexSearchThread
     * @see IndexSearchThread
     */    
    public @Override IndexSearchThread getSearchThread(String toFind, URL fo, IndexSearchThread.DocIndexItemConsumer diiConsumer) {
        return new SearchThreadJdk12 ( toFind, fo, diiConsumer, isCaseSensitive() );
    }


    public @Override boolean accepts(URL apidocRoot, String encoding) {
        //XXX returns always true, must be the last JavadocType
        return true;
    }
    
}
