/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
     
        assertPropertyDeclaration("@image: conic-gradient(from 40deg, white, black)");
        assertPropertyDeclaration("@image: conic-gradient(red 36deg, orange 36deg 170deg, yellow 170deg)");
        assertPropertyDeclaration("@image: conic-gradient(var(--color) var(--angle))");
        assertPropertyDeclaration("@image: conic-gradient(at 60px 20%, coral, orange, coral)");
        assertPropertyDeclaration("@image: conic-gradient(from 45deg in oklch, white, black, white)");
    }
    
}
