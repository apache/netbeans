/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
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
package org.netbeans.modules.htmlui;

import java.util.HashMap;
import javax.swing.Action;
import org.netbeans.api.htmlui.OpenHTMLRegistration;
import org.netbeans.modules.htmlui.Pages;
import org.openide.awt.ActionID;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author jtulach
 */
public class OpenHTMLRegistrationTest {
    
    public OpenHTMLRegistrationTest() {
    }

    @ActionID(category = "Test", id="html.test")
    @OpenHTMLRegistration(displayName = "Open me!",
        iconBase = "x.png",
        url = "empty.html",
        techIds = { "uno", "duo", "tre" }
    )
    public static void main() {
    }
    
    @Test public void verifyRegistered() {
        final String path = "Actions/Test/html-test.instance";
        final FileObject fo = FileUtil.getConfigFile(path);
        assertNotNull(fo, "Registration found");
        Action a = FileUtil.getConfigObject(path, Action.class);
        assertNotNull(a, "Action found");
        assertEquals(a.getValue(Action.NAME), "Open me!");
        
        assertEquals(fo.getAttribute("class"), OpenHTMLRegistrationTest.class.getCanonicalName(), "Fully qualified name");
        assertEquals(fo.getAttribute("method"), "main");
        
        class FOMap extends HashMap<String,Object> {

            @Override
            public Object get(Object key) {
                return fo.getAttribute(key.toString());
            }
        }

        Pages.R r = new Pages.R(new FOMap());
        Object[] arr = r.getTechIds();
        assertEquals(arr.length, 3, "Three different ids");
        assertEquals(arr[0], "uno");
        assertEquals(arr[1], "duo");
        assertEquals(arr[2], "tre");
    }
}
