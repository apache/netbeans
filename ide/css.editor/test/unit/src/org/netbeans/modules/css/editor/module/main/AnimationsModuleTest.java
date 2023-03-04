/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
public class AnimationsModuleTest extends CssModuleTestBase {

    public AnimationsModuleTest(String name) {
        super(name);
    }

    public void testAnimationName() {
        assertPropertyValues("animation-name", "none", "myanim", "myanim1,myanim2");
        assertPropertyValues("@animation-arg", "none", "myanim", "myanim1,myanim2");
    }
    
    public void testAnimation() {
        assertPropertyValues("animation", "none", "myanim", "myanim1,myanim2");
        assertPropertyValues("animation", "linear");
    }
    
    public void testAnimation_timing_function() {
        assertPropertyValues("animation-timing-function", "linear");
        assertPropertyValues("animation-timing-function", "ease");
        assertPropertyValues("animation-timing-function", "cubic-bezier(1,2,3,4)");
        
//        assertPropertyValues("animation", "cubic-bezier(1,2,3,4)");
    }
    
    public void testAnimation_duration() {
        assertPropertyValues("animation-duration", "10ms");
        assertPropertyValues("animation-duration", "10ms, 20ms");
        
        assertPropertyValues("animation", "10ms, 20ms");
        
    }
    
}
