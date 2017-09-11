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
package org.netbeans.modules.css.editor.module.main;

/**
 *
 * @author mfukala@netbeans.org
 */
public class ImageValuesModuleTest extends CssModuleTestBase {

    public ImageValuesModuleTest(String testName) {
        super(testName);
    }
    
    public void testProperties() {
        assertPropertyDeclaration("image-orientation: 90deg");
        
        assertPropertyDeclaration("image-resolution: 300dpi");
        assertPropertyDeclaration("image-resolution: from-image");
        assertPropertyDeclaration("image-resolution: 300dpi from-image");
        assertPropertyDeclaration("image-resolution: from-image 300dpi");
        
        assertPropertyDeclaration("background-image:url(picture.png)");
        assertPropertyDeclaration("background: linear-gradient(white, gray);");

        assertPropertyDeclaration("@radial-gradient: radial-gradient(circle, #006, #00a 90%, #0000af 100%, white 100%)");
        
        assertPropertyDeclaration("@image: linear-gradient(yellow, blue);");
        assertPropertyDeclaration("@image: linear-gradient(top, yellow 0%, blue 100%);");
        assertPropertyDeclaration("@image: linear-gradient(-45deg, blue, yellow);");
        assertPropertyDeclaration("@image: radial-gradient(50% 50%, farthest-corner, yellow, green);");
        assertPropertyDeclaration("@radial-gradient: radial-gradient(yellow, green);");
        assertPropertyDeclaration("@radial-gradient: radial-gradient(center, ellipse cover, yellow 0%, green 100%);");
        assertPropertyDeclaration("@radial-gradient: radial-gradient(50% 50%, farthest-corner, yellow, green);");
//        assertPropertyDeclaration("@radial-gradient: radial-gradient(bottom left, farthest-side, red, yellow 50px, green);");
        assertPropertyDeclaration("@radial-gradient: radial-gradient(20px 30px, 20px 20px, red, yellow, green);");
        
        assertPropertyDeclaration("@repeating-radial-gradient:repeating-radial-gradient(20px 30px, circle contain, red, yellow, green 100%, yellow 150%, red 200%)");
        assertPropertyDeclaration("@repeating-radial-gradient:repeating-radial-gradient(red, blue 20px, red 40px)");
     
        
        
        
    }
    
}
