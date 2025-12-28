/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.j2seplatform.libraries;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.spi.project.libraries.LibraryImplementation;

/**
 * Implementation of library provider for unit testing.
 *
 * @author  David Konecny
 */
public class TestLibraryProviderImpl implements org.netbeans.spi.project.libraries.LibraryProvider {

    private List<LibraryImplementation> libs = new ArrayList<LibraryImplementation>();
    private PropertyChangeSupport support;
    
    private static final TestLibraryProviderImpl DEFAULT = new TestLibraryProviderImpl();
    
    public static TestLibraryProviderImpl getDefault() {
        assert DEFAULT != null;
        return DEFAULT;
    }
    
    private TestLibraryProviderImpl() {
        support = new PropertyChangeSupport(this);
    }
    
    public void addLibrary(LibraryImplementation library) throws IOException {
        libs.add(library);
        fireChange();
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    public LibraryImplementation[] getLibraries() {
        return libs.toArray(new LibraryImplementation[0]);
    }
    
    public void init() throws IOException {
    }
    
    public void removeLibrary(LibraryImplementation library) throws IOException {
        boolean res = libs.remove(library);
        assert res : "Removing library which is not in this provider "+library;
        fireChange();
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private void fireChange() {
        support.firePropertyChange(PROP_LIBRARIES, null, null);
    }
    
}
