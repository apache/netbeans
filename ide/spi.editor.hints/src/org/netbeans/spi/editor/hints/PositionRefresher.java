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

package org.netbeans.spi.editor.hints;

import java.util.List;
import java.util.Map;
import javax.swing.text.Document;

/**
 * Refresher invoked upon mouse click or Alt-Enter. Clients should register
 * its implementations inside layer for appropriate mimetype and provide all
 * {@link ErrorDescription}s for given line (specified by position).
 *
 * Contract is that fixes will be already computed in the time of the retrieval.
 *
 * @author Max Sauer
 * @since 1.8.1
 */
public interface PositionRefresher {

    /**
     * @param context current caret position inside document
     * @param doc current document
     * @return map of layer name to {@link ErrorDescription}s for current line
     */
    public Map<String, List<ErrorDescription>> getErrorDescriptionsAt(Context context, Document doc);

}
