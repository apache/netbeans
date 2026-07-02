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
package org.netbeans.modules.php.blade.syntax.php;

import org.netbeans.modules.php.blade.syntax.annotation.PhpKeyword;
import org.netbeans.modules.php.blade.syntax.annotation.PhpKeywordRegister;

/**
 *
 * @author bogdan
 */
@PhpKeywordRegister({
    //conditionals
    @PhpKeyword(name = "empty", parenExpr = true),
    @PhpKeyword(name = "isset", parenExpr = true),
    @PhpKeyword(name = "class"),
    @PhpKeyword(name = "echo"),
})
public class PhpKeywordList {

    public PhpKeyword[] getKeywords() {
        PhpKeywordRegister register = this.getClass().getAnnotation(PhpKeywordRegister.class);
        return register.value();
    }
}
