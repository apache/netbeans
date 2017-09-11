/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.versioning.core.util;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.netbeans.modules.versioning.core.spi.VCSVisibilityQuery;
import org.netbeans.spi.queries.CollocationQueryImplementation2;

/**
 * Warning: VCS internal use only. Not to be implemented by clients.
 * 
 * Provides implementations for {@link org.netbeans.modules.versioning.spi.VersioningSystem} 
 * and {@link org.netbeans.modules.versioning.fileproxy.spi.VersioningSystem}
 * 
 * @author Tomas Stupka
 */
public abstract class VCSSystemProvider {

    /**
     * Add a listener to changes in registered versioning systems
     * @param l 
     */
    public abstract void addChangeListener(ChangeListener l);
    
    /**
     * Stop listening to changes in registered versioning systems
     * @param l 
     */
    public abstract void removeChangeListener(ChangeListener l);
    
    /**
     * Provides all registered versioning systems 
     * 
     * @return a collections of all registered versioning systems 
     */
    public abstract Collection<VersioningSystem> getVersioningSystems();
    
    /**
     * Provides abstraction either over a {@link org.netbeans.modules.versioning.fileproxy.spi.VersioningSystem}
     * or a {@link org.netbeans.modules.versioning.spi.VersioningSystem}
     * @param <S> 
     */
    public interface VersioningSystem<S> {
        
        S getDelegate();
        
        public String getDisplayName();
        
        public String getMenuLabel();
        
        public boolean isLocalHistory();
        
        public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file);

        public VCSAnnotator getVCSAnnotator();

        public VCSInterceptor getVCSInterceptor();

        public VCSHistoryProvider getVCSHistoryProvider();
        
        public void getOriginalFile(VCSFileProxy workingCopy, VCSFileProxy originalFile);

        public CollocationQueryImplementation2 getCollocationQueryImplementation();

        public VCSVisibilityQuery getVisibilityQuery();

        public void addPropertyCL(PropertyChangeListener listener);

        public void removePropertyCL(PropertyChangeListener listener);

        public boolean isExcluded(VCSFileProxy file);
        
        public boolean accept(VCSContext ctx);
        
        public boolean isMetadataFile(VCSFileProxy file);
        
    }
}
