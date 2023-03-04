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

package org.netbeans.modules.web.jsf.api.facesmodel;

/**
 * The "supported-locale" element allows authors to declare
 * which locales are supported in this application instance.
 * @author Petr Pisl
 */
public interface SupportedLocale extends JSFConfigComponent {

    /**
     * It must be specified as :language:[_:country:[_:variant:]]
     * without the colons, for example "ja_JP_SJIS".  The
     * separators between the segments may be '-' or '_'.
     * @return value of the supported locale
     */
    String getLocale();

    /**
     * It must be specified as :language:[_:country:[_:variant:]]
     * without the colons, for example "ja_JP_SJIS".  The
     * separators between the segments may be '-' or '_'.
     * @param locale new value for the supported locale
     */
    void setLocale(String locale);
}
