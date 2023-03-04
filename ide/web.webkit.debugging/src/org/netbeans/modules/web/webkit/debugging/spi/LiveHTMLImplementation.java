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

package org.netbeans.modules.web.webkit.debugging.spi;

import java.net.URL;

/**
 * A way to plug Live HTML implementation into debugging protocol handler.
 */
public interface LiveHTMLImplementation {

    /**
     * If Live HTML is enabled for the given URL connection then any new version of 
     * document should be recorded using this method.
     * 
     * @param connectionURL URL connection
     * @param timeStamp a timestamp, eg. System.currentTimeMillis();
     * @param content HTML document image before call described by the given callStack was executed
     * @param callStack JSON array (serialized into String) containing individual call frames;
     *   single callframe has following attributes: lineNumber(type:Number), 
     *   columnNumber(type:Number), function(type:String), script(type:String)
     */
    void storeDocumentVersionBeforeChange(URL connectionURL, long timeStamp, String content, String callStack);

    /**
     * This method follows {@link #storeDocumentVersionBeforeChange} and sends version of document after code
     * change happened in the document.
     */
    void storeDocumentVersionAfterChange(URL connectionURL, long timeStamp, String content);
    
    /**
     * If Live HTML is enabled for the given URL connection then any data received from
     * server should be recoded using this method.
     * 
     * @param connectionURL URL connection
     * @param timeStamp a timestamp, eg. System.currentTimeMillis();
     * @param data data serialized into String; can be anything
     */
    void storeDataEvent(URL connectionURL, long timeStamp, String data, String requestURL, String mime);

}
