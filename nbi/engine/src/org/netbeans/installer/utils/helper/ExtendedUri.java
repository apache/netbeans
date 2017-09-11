/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.helper;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Kirill Sorokin
 */
public class ExtendedUri {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private URI remote;
    private List<URI> alternates;
    private URI local;
    private long size;
    private String md5;
    
    public ExtendedUri(
            final URI remote, 
            final long size, 
            final String md5) {
        this.remote = remote;
        this.size   = size;
        this.md5    = md5;
        
        this.alternates = new LinkedList<URI>();
    }
    
    public ExtendedUri(
            final URI remote, 
            final List<URI> alternates, 
            final long size, 
            final String md5) {
        this(remote, size, md5);
        
        this.alternates.addAll(alternates);
    }
    
    public ExtendedUri(
            final URI remote, 
            final URI local, 
            final long size, 
            final String md5) {
        this(remote, size, md5);
        
        this.local  = local;
    }
    
    public ExtendedUri(
            final URI remote, 
            final List<URI> alternates, 
            final URI local, 
            final long size, 
            final String md5) {
        this(remote, alternates, size, md5);
        
        this.local  = local;
    }
    
    public URI getRemote() {
        return remote;
    }
    
    public void setRemote(final URI remote) {
        this.remote = remote;
    }
    
    public List<URI> getAlternates() {
        return new LinkedList<URI>(alternates);
    }
    
    public URI getLocal() {
        return local;
    }
    
    public void setLocal(final URI local) {
        this.local = local;
    }
    
    public long getSize() {
        return size;
    }
    
    public String getMd5() {
        return md5;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String RESOURCE_SCHEME = 
            "resource"; // NOI18N
    
    public static final String HTTP_SCHEME = 
            "http"; // NOI18N
    
    public static final String FILE_SCHEME = 
            "file"; // NOI18N
}
