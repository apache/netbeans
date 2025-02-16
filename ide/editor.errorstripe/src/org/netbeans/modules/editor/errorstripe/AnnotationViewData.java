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

package org.netbeans.modules.editor.errorstripe;

import java.util.Map;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;

/**
 *
 * @author Jan Lahoda
 */
interface AnnotationViewData {

    void register(BaseDocument document);

    void unregister();

    Mark getMainMarkForBlock(int startLine, int endLine);

    int findNextUsedLine(int from);

    Status computeTotalStatus();
    
    UpToDateStatus computeTotalStatusType();
    
    Stats computeAnnotationStatistics();

    void clear();
    
    record Stats(Map<String, Integer> err_histogram, int errors, Map<String, Integer> war_histogram, int warnings) {}
}
