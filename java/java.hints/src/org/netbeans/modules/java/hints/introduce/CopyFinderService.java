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
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.util.TreePath;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.api.java.source.matching.Pattern;
import org.openide.util.lookup.ServiceProvider;

/**
 * Used exclusively by SourceUtils
 * @author Jan Becicka
 */
@ServiceProvider(service=CopyFinderService.class)
public class CopyFinderService {
    public static Set<TreePath> computeDuplicates(CompilationInfo info, TreePath searchingFor, TreePath scope, AtomicBoolean cancel) {
        Set<TreePath> result = new HashSet<TreePath>();
        
        for (Occurrence od : Matcher.create(info).setCancel(cancel).setSearchRoot(scope).match(Pattern.createSimplePattern(searchingFor))) {
            result.add(od.getOccurrenceRoot());
        }

        return result;
    }
}
