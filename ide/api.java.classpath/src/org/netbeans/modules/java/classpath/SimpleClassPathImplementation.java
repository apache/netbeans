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
package org.netbeans.modules.java.classpath;

import java.util.List;
import java.beans.PropertyChangeListener;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;


public class SimpleClassPathImplementation implements ClassPathImplementation {

    List<? extends PathResourceImplementation> entries;

    public SimpleClassPathImplementation() {
        this(new ArrayList<PathResourceImplementation>());
    }

    public SimpleClassPathImplementation(List<? extends PathResourceImplementation> entries) {
        this.entries = entries;
    }
    
    public List <? extends PathResourceImplementation> getResources() {
        return Collections.unmodifiableList(entries);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // XXX TBD
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        // XXX TBD
    }
    
    public String toString () {
        StringBuilder builder = new StringBuilder ("SimpleClassPathImplementation["); //NOI18N
        for (PathResourceImplementation impl : this.entries) {
            URL[] roots = impl.getRoots();
            for (URL root : roots) {
                builder.append (root.toExternalForm());
                builder.append (", ");  //NOI18N
            }
        }
        builder.append ("]");   //NOI18N
        return builder.toString ();
    }    
}
