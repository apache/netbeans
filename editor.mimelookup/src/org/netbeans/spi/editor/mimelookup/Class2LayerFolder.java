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

/**
 * Provides further clasification of objects registered for a mime type. This
 * interface is a hook into the implementation of the default
 * <code>MimeDataProvider</code> that can be used to further specify where to
 * look for instances of a certain class and how to create them.
 *
 * <p>The implementations of this interface should be registered among the services
 * in the default lookup using {@link org.openide.util.lookup.ServiceProvider}.
 *
 * <p>The default <code>MimeDataProvider</code> allowes to register instances
 * in a hierarchy of folders on the system filesystem (modules XML layers). The
 * hierarchy starts with the Editors/ folder and then contains subfolders for
 * each mime type that has some registered objects. So, for example there might
 * be settings registered for the mime type identified by the following mime
 * path 'text/x-jsp/text/x-java' and they would be located in the folder
 * 'Editors/text/x-jsp/text/x-java' on the system filesystem. For more details
 * on the implementation of the default <code>MimeDataProvider</code> please see
 * the <a href="package-summary.html#defaultMimeDataProvider"/>SPI package</a> description.
 *
 * <p>The implementations of this interface are used for determining the structure
 * under the folder belonging to a given mime type. This interface allows to 
 * tell the default <code>MimeDataProvider</code> that instances of a certain
 * class are registered in a specific subfolder rather then under the folder
 * belonging to the mime type. When looking up instances of such a class the
 * default <code>MimeDataProvider</code> will not look in the mime type's folder,
 * but in its subfolder, which name it will obtain by calling the implementation
 * of this interface.
 *
 * <p>Therefore, for example instances of the <code>FolderManager</code> class
 * can be registered in the 'Editors/text/x-java/foldManager' folder and they
 * will be properly retrieved when calling
 * <code>MimeLookup.getLookup(MimePath.get("text/x-java")).lookup(FolderManager.class);</code>.
 * @param T type of instance which will be created
 *
 * @author Miloslav Metelka, Martin Roskanin, Vita Stejskal
 * @deprecated Use {@link MimeLocation} instead
 */
@Deprecated
public interface Class2LayerFolder<T> {
    
    /**
     * Gets the class of the instances that are registered under the special
     * subfolder.
     *
     * @return The class which this object provides an additional information for.
     */
    Class<T> getClazz();
    
    /**
     * Gets the name of the subfolder where the instances are registered. The
     * subfolder should be located be located under the folder belonging to
     * the appropriate mime type, i.e.
     * <code>Editors/text/x-java/&lt;desired-layer-subfolder-name&gt;</code>.
     *
     * @return The mime type subfolder name.
     */
    String getLayerFolderName();
    
    /**
     * Gets the <code>InstanceProvider</code> that should be used for creating
     * the registered instances. This method can return <code>null</code> if
     * there is no speacial <code>InstanceProvider</code> needed and the instances
     * can be created in the standard way.
     *
     * @return The <code>InstanceProvider</code> capable of createing instances
     * of the {@link #getClazz()} class. Can return <code>null</code>.
     */
    InstanceProvider<T> getInstanceProvider();
    
}
