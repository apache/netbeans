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

package org.netbeans.modules.java.hints.jackpot.spi;

import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import com.sun.source.util.TreePath;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spiimpl.hints.HintsInvoker;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**XXX: probably unused
 *
 * @author lahvac
 */
public class HintsRunner {

    @CheckForNull
    public static Map<HintDescription, List<ErrorDescription>> computeErrors(CompilationInfo info, Iterable<? extends HintDescription> hints, AtomicBoolean cancel) {
        return new HintsInvoker(HintsSettings.getSettingsFor(info.getFileObject()), cancel).computeHints(info, new TreePath(info.getCompilationUnit()), hints, new LinkedList<>());
    }

}
