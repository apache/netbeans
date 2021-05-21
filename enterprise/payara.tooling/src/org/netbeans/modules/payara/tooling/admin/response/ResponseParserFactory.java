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
package org.netbeans.modules.payara.tooling.admin.response;

import org.netbeans.modules.payara.tooling.PayaraIdeException;



/**
 * Factory that returns appropriate response parser implementation
 * based on content type of the response.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ResponseParserFactory {

    private static RestXMLResponseParser xmlParser;

    private static RestJSONResponseParser jsonParser;

    public static synchronized RestResponseParser getRestParser(ResponseContentType contentType) {
        switch (contentType) {
            case APPLICATION_XML:
                if (xmlParser == null) {
                    xmlParser = new RestXMLResponseParser();
                }
                return  xmlParser;
            case APPLICATION_JSON:
                if (jsonParser == null) {
                    jsonParser = new RestJSONResponseParser();
                }
                return jsonParser;
            case TEXT_PLAIN:
                return null;
            default: throw new PayaraIdeException("Not supported content type. Cannot create response parser!");
        }
    }

}
