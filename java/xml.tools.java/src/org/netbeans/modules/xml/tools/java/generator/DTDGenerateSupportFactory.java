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
package org.netbeans.modules.xml.tools.java.generator;

import org.openide.nodes.Node;
import org.openide.loaders.DataObject;

import org.netbeans.modules.xml.DTDDataObject;
import org.netbeans.modules.xml.cookies.CookieFactory;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class DTDGenerateSupportFactory extends CookieFactory {
    /** */
    private final DTDDataObject dataObject;
    /** */
    private static final Class[] SUPPORTED_COOKIES = new Class[] {
        SAXGeneratorSupport.class,
        GenerateDOMScannerSupport.class,
    };


    /** Create new DTDGenerateSupportFactory. */
    public DTDGenerateSupportFactory (DTDDataObject dataObject) {
        this.dataObject = dataObject;
    }


    /**
     */
    protected Class[] supportedCookies () {
        return SUPPORTED_COOKIES;
    }

    /**
     */
    public Node.Cookie createCookie (Class clazz) {
        
        
        if ( clazz.isAssignableFrom (SAXGeneratorSupport.class) ) {
            return new SAXGeneratorSupport (this.dataObject);
        } else 
        if ( clazz.isAssignableFrom (GenerateDOMScannerSupport.class) ) {
            return new GenerateDOMScannerSupport (this.dataObject);
        }
        
        return null;
    }


    //
    // class Creator
    //

    public static final class Creator implements DTDDataObject.DTDCookieFactoryCreator {

        /**
         */
        public CookieFactory createCookieFactory (DataObject obj) {
            return new DTDGenerateSupportFactory ((DTDDataObject) obj);
        }

    } // end: class Creator

}
