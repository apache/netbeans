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

package org.netbeans.spi.editor.mimelookup;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.util.Lookup;

/**
 * Provides a <code>Lookup</code> for the specific <code>MimePath</code>.
 *
 * <p>The implementations of this interface should be registered among the services
 * in the default lookup using {@link org.openide.util.lookup.ServiceProvider}.
 *
 *  @author Miloslav Metelka, Vita Stejskal
 */
public interface MimeDataProvider {

    /**
     * Retrieves a <code>Lookup</code> for the given <code>MimePath</code>.
     * 
     * <p>The <code>Lookup</code> returned by this method should hold a reference
     * to the <code>MimePath</code> it was created for.
     *
     * <p>The implementors should consider caching of the <code>Lookup</code> instances
     * returned by this method for performance reasons. The <code>MimePath</code>
     * object can be used as a stable key for such a cache, because it implements
     * its <code>equals</code> and <code>hashCode</code> method in the suitable way.
     *
     * @param mimePath The mime path to get the <code>Lookup</code> for. The mime
     * path passed in can't be <code>null</code>, but it can be the 
     * {@link MimePath#EMPTY} mime path.
     *
     * @return The <code>Lookup</code> for the given <code>MimePath</code> or
     * <code>null</code> if there is no lookup available for this mime path.
     */
    public Lookup getLookup(MimePath mimePath);

}
