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

package org.netbeans.modules.java.j2seplatform.libraries;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.Lookup;

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
        return libs.toArray(new LibraryImplementation[libs.size()]);
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
