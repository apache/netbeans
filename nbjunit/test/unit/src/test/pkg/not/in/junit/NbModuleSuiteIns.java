/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
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

package test.pkg.not.in.junit;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.netbeans.insane.scanner.ObjectMap;
import org.netbeans.insane.scanner.Visitor;

public class NbModuleSuiteIns extends TestCase implements Visitor {
    private static Logger LOG = Logger.getLogger("test.logger");

    public NbModuleSuiteIns(String t) {
        super(t);
    }

    public void testOne() {
        try {
            Class<?> access = Class.forName("org.netbeans.insane.model.Support");
            System.setProperty("ins.one", "OK");
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Error loading class", ex);
        }
    }

    public void testFS() {
        try {
            ClassLoader l = NbModuleSuiteIns.class.getClassLoader();
            Class<?> access = l.loadClass("org.openide.filesystems.FileSystem");
            System.setProperty("ins.fs", "OK");
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Error loading class", ex);
        }
    }

    public void testWindowSystem() {
        try {
            ClassLoader l = NbModuleSuiteIns.class.getClassLoader();
            Class<?> access = l.loadClass("org.netbeans.api.java.platform.JavaPlatform");
            System.setProperty("ins.java", "OK");
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Error loading class", ex);
        }
    }

    public void testSecond() {
        System.setProperty("ins.two", "OK");
    }

    public void testThree() {
        System.setProperty("ins.three", "OK");
    }

    public void visitClass(Class cls) {
    }

    public void visitObject(ObjectMap map, Object object) {
    }

    public void visitObjectReference(ObjectMap map, Object from, Object to, Field ref) {
    }

    public void visitArrayReference(ObjectMap map, Object from, Object to, int index) {
    }

    public void visitStaticReference(ObjectMap map, Object to, Field ref) {
    }
}
