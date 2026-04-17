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

public class Images4Test extends CssModuleTestBase {

    public Images4Test(String testName) {
        super(testName);
    }

    public void testImageSetProperty() {
        assertPropertyDeclaration("background-image: image-set(\"picture.png\" 1x)");
        assertPropertyDeclaration("background-image: image-set(\"picture.png\" type(\"image/avif\"), \"foo.jpg\" type(\"image/jpeg\"))");
        assertPropertyDeclaration("background-image: image-set( linear-gradient(cornflowerblue, white) 1x, url(\"detailed-gradient.png\") 3x );");
    }

}
