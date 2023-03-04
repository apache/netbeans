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
package org.netbeans.modules.php.editor.completion;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.spi.templates.completion.CompletionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@ServiceProvider(service = CompletionProvider.class, path = "Latte/Completion/Controls") //NOI18N
public class LatteControlCompletionProvider implements CompletionProvider {
    private static final Logger LOGGER = Logger.getLogger(LatteControlCompletionProvider.class.getName());
    private static final String CREATE_COMPONENT_PREFIX = "createComponent"; //NOI18N
    private Set<String> result;
    private String controlPrefix;

    @Override
    public Set<String> getItems(FileObject sourceFile, String controlPrefix) {
        result = new HashSet<>();
        if (LatteUtils.isView(sourceFile)) {
            this.controlPrefix = controlPrefix;
            processTemplateFile(sourceFile);
        }
        return result;
    }

    private void processTemplateFile(FileObject templateFile) {
        FileObject presenterFile = LatteUtils.getPresenterFile(templateFile);
        if (presenterFile != null) {
            try {
                parsePresenter(presenterFile);
            } catch (ParseException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }
    }

    private void parsePresenter(FileObject presenterFile) throws ParseException {
        ParserManager.parse(Collections.singleton(Source.create(presenterFile)), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                PHPParseResult parseResult = (PHPParseResult) resultIterator.getParserResult();
                if (parseResult != null) {
                    findControls(parseResult);
                }
            }

        });
    }

    private void findControls(PHPParseResult parseResult) {
        Set<MethodScope> controlFactories = new HashSet<>();
        String realPrefix = StringUtils.hasText(controlPrefix) ? CREATE_COMPONENT_PREFIX + StringUtils.capitalize(controlPrefix) : CREATE_COMPONENT_PREFIX;
        NameKind.Prefix nameKindPrefix = NameKind.prefix(realPrefix);
        Model model = parseResult.getModel(Model.Type.COMMON);
        Collection<? extends ClassScope> declaredClasses = ModelUtils.getDeclaredClasses(model.getFileScope());
        for (ClassScope classScope : declaredClasses) {
            Collection<? extends MethodScope> methods = classScope.getMethods();
            controlFactories = ElementFilter.forName(nameKindPrefix).filter(new HashSet<>(methods));
        }
        for (MethodScope methodScope : controlFactories) {
            String methodName = methodScope.getName();
            String controlName = methodName.substring(CREATE_COMPONENT_PREFIX.length());
            result.add(StringUtils.decapitalize(controlName));
        }
    }

}
