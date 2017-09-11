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
package org.netbeans.spi.java.classpath;

import java.beans.PropertyChangeListener;
import java.net.URL;

/**
 * SPI interface for one classpath entry.
 * @see ClassPathImplementation
 * @since org.netbeans.api.java/1 1.4
 */
public interface PathResourceImplementation {

    public static final String PROP_ROOTS = "roots";    //NOI18N

    /** Roots of the class path entry.
     *  In the case of simple resource it returns array containing just one URL.
     *  In the case of composite resource it returns array containing one or more URL.
     * @return array of URL, never returns null.
     */
    public URL[] getRoots();

    /**
     * Returns ClassPathImplementation representing the content of the PathResourceImplementation.
     * If the PathResourceImplementation represents leaf resource, it returns null.
     * The ClassPathImplementation is live and can be used for path resource content
     * modification.
     * <p><strong>Semi-deprecated.</strong> There was never a real reason for this method to exist.
     * If implementing <code>PathResourceImplementation</code> you can simply return null;
     * it is unlikely anyone will call this method anyway.
     * @return classpath handle in case of composite resource; null for leaf resource
     */
    public ClassPathImplementation getContent();

    /**
     * Adds property change listener.
     * The listener is notified when the roots of the entry are changed.
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes property change listener.
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

}
