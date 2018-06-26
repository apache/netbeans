/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.env;

import java.io.File;
import java.io.IOException;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.util.EditableProperties;
import org.w3c.dom.Element;

/**
 */
public abstract class CommonProjectHelper {
    public static final Object PRIVATE_PROPERTIES_PATH = new Object();
    public static final Object PROJECT_PROPERTIES_PATH = new Object();

    public abstract EditableProperties getProperties(Object path);

    public abstract void putProperties(Object path, EditableProperties privateProps);

    public abstract SharabilityQueryImplementation2 createSharabilityQuery2(Values evaluator, String[] toArray, String[] string);

    public abstract Values getStandardPropertyEvaluator();

    public abstract Object getXmlSavedHook();

    public abstract File resolveFile(String licensePath);

    public abstract FileObject getProjectDirectory();

    public abstract FileObject resolveFileObject(String sourceFolder);

    public abstract void notifyDeleted();

    public abstract AuxiliaryConfiguration createAuxiliaryConfiguration();

    public abstract void registerCallback(Callback l);

    public abstract Element getPrimaryConfigurationData(boolean b);

    public abstract Object createCacheDirectoryProvider();

    public abstract Object createAuxiliaryProperties();

    public abstract void putPrimaryConfigurationData(Element data, boolean b);

    public interface Callback {
        public void projectXmlSaved() throws IOException;
        public void configurationXmlChanged();
        public void propertiesChanged();
    }
}
