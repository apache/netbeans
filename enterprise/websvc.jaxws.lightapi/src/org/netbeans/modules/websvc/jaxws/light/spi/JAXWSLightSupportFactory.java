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

package org.netbeans.modules.websvc.jaxws.light.spi;

import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.JAXWSLightSupportAccessor;

/**
 * Most general way to create {@link JAXWSLightSupport} instances.
 * You are not permitted to create them directly; instead you implement
 * {@link JAXWSLightSupportImpl} and use this factory.
 *
 * @author Milan Kuchtiak
 */
public final class JAXWSLightSupportFactory {

    private JAXWSLightSupportFactory() {
    }
    /** Create JAXWSLightSupport object from spi object.
     *
     * @param spiJAXWSSupport spi object for JAXWSSupport
     * @return JAXWSLightSupport object
     */
    public static JAXWSLightSupport createJAXWSSupport(JAXWSLightSupportImpl spiJAXWSSupport) {
        return JAXWSLightSupportAccessor.getDefault().createJAXWSSupport(spiJAXWSSupport);
    }

}
