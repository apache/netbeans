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
package org.netbeans.modules.css.refactoring.api;

import org.netbeans.modules.csl.api.OffsetRange;

public interface Entry {

    /**
     * quite similar to isValidInSourceDocument() but here we do not use the
     * adjusted start offset to check if can be translated to the source
     * but rather use the real node start offset.
     * In case of virtually generated class or selector the isVirtual
     * is always true since the dot or has doesn't exist in the css source code
     *
     */
    public boolean isVirtual();

    public boolean isValidInSourceDocument();

    /**
     * 
     * @return a line offset of the document start offset in the underlying document.
     * The -1 value denotes that there has been a problem getting the line.
     */
    public int getLineOffset();

    public CharSequence getText();

    public CharSequence getLineText();

    public String getName();

    public OffsetRange getDocumentRange();

    public OffsetRange getRange();

    public OffsetRange getBodyRange();

    public OffsetRange getDocumentBodyRange();

}
