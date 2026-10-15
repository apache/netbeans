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

package org.netbeans.modules.maven.grammar.spi;

import java.util.Enumeration;
import java.util.List;
import org.jdom2.Element;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;

/**
 *
 * @author dafe
 */
public interface GrammarExtensionProvider {

    @NonNull List<GrammarResult> getDynamicCompletion(String path, HintContext hintCtx, Element parent);

    Enumeration<GrammarResult> getDynamicValueCompletion(String path, HintContext virtualTextCtx, Element el);

}
