/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.black.kotlin.diagnostics.netbeans.bracesmatcher;

import org.netbeans.api.editor.mimelookup.MimeRegistration; 
import org.netbeans.spi.editor.bracesmatching.BracesMatcher; 
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory; 
import org.netbeans.spi.editor.bracesmatching.MatcherContext; 
import org.netbeans.spi.editor.bracesmatching.support.BracesMatcherSupport; 

@MimeRegistration(mimeType="text/x-kt",service=BracesMatcherFactory.class) 
public class KotlinBracesMatcherFactory implements BracesMatcherFactory { 
    
    @Override 
    public BracesMatcher createMatcher(MatcherContext context) { 
        return BracesMatcherSupport.defaultMatcher(context, -1, -1); 
    } 
}