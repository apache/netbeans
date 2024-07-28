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
package org.netbeans.modules.php.blade.syntax;

import org.netbeans.modules.php.blade.syntax.annotation.Directive;

/**
 * 
 *
 * @author bhaidu
 */
public class BladeDirectivesUtils {

    public static String[] directiveStart2EndPair(String directive){
        if (directive.equals("@section")){
            return new String[]{"@endsection", "@show", "@stop", "@append"};
        }
        DirectivesList listClass = new DirectivesList();
        for (Directive directiveEl :  listClass.getDirectives()){
            if (!directiveEl.name().equals(directive)){
                continue;
            }
            if (directiveEl.endtag().isEmpty()){
                return null;
            }
            return new String[]{directiveEl.endtag()};
        }
        return null;
    }
    
    public static String[] directiveEnd2StartPair(String directive){
        //still easier with switch
        switch (directive) {
            case "@endif":
                return new String[]{"@if", "@hasSection", "@sectionMissing"};
            case "@endsection":
            case "@stop":
            case "@append":
            case "@show":
                return new String[]{"@section"};
        }
        DirectivesList listClass = new DirectivesList();
        for (Directive directiveEl :  listClass.getDirectives()){
            if (directiveEl.endtag().isEmpty()){
                continue;
            }
            if (directiveEl.endtag().equals(directive)){
                return new String[]{directiveEl.name()};
            }
        }

        return null;
    }
}
