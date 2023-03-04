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

package org.netbeans.editor;

/** Process the batches of the text in the document. This interface
* can be passed to the BaseDocument.processText() and this method
* then calls the processTextBatch() to process the text batches.
*
* @author Miloslav Metelka
* @version 1.00
*/


public interface TextBatchProcessor {

    /** Process one batch of the text.
    * @doc document to work with
    * @startPos starting position of the batch
    * @endPos ending position of the batch
    * @lastBatch whether this batch is the last one in the text area that
    *   is searched.
    * @return non-negative number to stop the batch processing. The returned
    *   value is remembered and returned from BaseDocument.processText().
    *   Negative value means to continue with the next batch.
    */
    public int processTextBatch(BaseDocument doc, int startPos, int endPos,
                                boolean lastBatch);

}
