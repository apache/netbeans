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
package org.netbeans.modules.php.blade.editor.completion;

/**
 *
 * @author bogdan
 */
public class BladeCompletionBuilder {

    //----------- Factory methods --------------
    public static BladeCompletionItem createTag(String name, int substitutionOffset) {
        return new BladeCompletionItem.BladeTag(name, substitutionOffset);
    }

    public static BladeCompletionItem createViewPath(String name,
            int substitutionOffset, boolean isFolder, String path) {
        return new BladeCompletionItem.ViewPath(name, substitutionOffset, isFolder, path);
    }

    public static BladeCompletionItem createInlineDirective(String directive,
            int substitutionOffset, String description) {
        return new BladeCompletionItem.InlineDirective(directive, substitutionOffset, description);
    }

    public static BladeCompletionItem createDirectiveWithArg(String directive,
            int substitutionOffset, String description) {
        return new BladeCompletionItem.DirectiveWithArg(directive, substitutionOffset, description);
    }

    public static BladeCompletionItem createBlockDirective(String directive,
            String endTag, int substitutionOffset, String description) {
        return new BladeCompletionItem.BlockDirective(directive, endTag, substitutionOffset, description);
    }

    public static BladeCompletionItem createBlockDirectiveWithArg(String directive,
            String endTag, int substitutionOffset, String description) {
        return new BladeCompletionItem.BlockDirectiveWithArg(directive, endTag, substitutionOffset, description);
    }
}
