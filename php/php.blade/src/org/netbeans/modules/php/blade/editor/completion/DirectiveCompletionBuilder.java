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

import javax.swing.text.Document;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bogdan
 */
public class DirectiveCompletionBuilder {

    public static CompletionItem simpleItem(int startOffset, String directive,
            String description) {

        return BladeCompletionItem.createInlineDirective(directive, startOffset, description);
    }

    public static CompletionItem simpleItem(int startOffset, int carretOffset,
            String prefix, String directive, String endtag,
            String description, Document doc) {

        return BladeCompletionItem.createBlockDirective(
                directive, endtag, startOffset, description);
    }

    public static CompletionItem itemWithArg(int startOffset, int carretOffset,
            String prefix, String directive,
            String description, Document doc) {

        return BladeCompletionItem.createDirectiveWithArg(directive, startOffset, description);
    }

    public static CompletionItem itemWithArg(int startOffset, int carretOffset,
            String prefix, String directive, String endtag,
            String description, Document doc) {

        return BladeCompletionItem.createBlockDirectiveWithArg(
                directive, endtag, startOffset, description);
    }

    public static CompletionItem itemWithArg(int startOffset, int carretOffset,
            String prefix, String directive,
            String description, Document doc,
            FileObject file) {
        return BladeCompletionItem.createDirectiveWithArg(directive, startOffset, description);
    }
}
