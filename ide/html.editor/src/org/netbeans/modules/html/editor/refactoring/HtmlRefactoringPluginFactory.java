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
package org.netbeans.modules.html.editor.refactoring;

import org.netbeans.modules.css.refactoring.api.CssRefactoringInfo;
import org.netbeans.modules.html.editor.refactoring.api.ExtractInlinedStyleRefactoring;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class, position = 120)
public class HtmlRefactoringPluginFactory implements RefactoringPluginFactory {

    @Override
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
	if (refactoring instanceof RenameRefactoring) {
            if (refactoring.getRefactoringSource().lookup(CssRefactoringInfo.class) != null) {
                return new CssRenameRefactoringPlugin((RenameRefactoring)refactoring);
            } else if (null != refactoring.getRefactoringSource().lookup(FileObject.class)) {
		return new HtmlRenameRefactoringPlugin((RenameRefactoring)refactoring);
	    }
	} else if(refactoring instanceof ExtractInlinedStyleRefactoring) {
            return new ExtractInlinedStyleRefactoringPlugin((ExtractInlinedStyleRefactoring)refactoring);
        } else if (refactoring instanceof WhereUsedQuery && refactoring.getRefactoringSource().lookup(CssRefactoringInfo.class) != null) {
            return new CssWhereUsedQueryPlugin((WhereUsedQuery)refactoring);
        }

	return null;

    }
}
