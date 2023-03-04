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
package org.netbeans.modules.cordova.platforms.ios;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.XMLPropertyListParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author Jan Becicka
 */
public class PlistTest {

    public PlistTest() {
    }

    @Test
    public void testListing() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, Exception {
        Tabs tabs = new Tabs();
        tabs.update(PlistTest.class.getResourceAsStream("listing.xml"));
        System.out.println(tabs.get("1"));
        System.out.println(tabs.get("2"));
    }
}

class Tabs {

    private HashMap<String, TabDescriptor> map = new HashMap<>();

    public void update(InputStream is) throws Exception {
        NSDictionary root = (NSDictionary) XMLPropertyListParser.parse(is);
        NSDictionary argument = (NSDictionary) root.objectForKey("__argument");
        NSDictionary listing = (NSDictionary) argument.objectForKey("WIRListingKey");
        for (String s : listing.allKeys()) {
            NSDictionary o = (NSDictionary) listing.objectForKey(s);
            NSObject identifier = o.objectForKey("WIRPageIdentifierKey");
            NSObject url = o.objectForKey("WIRURLKey");
            NSObject title = o.objectForKey("WIRTitleKey");
            map.put(s, new TabDescriptor(url.toString(), title.toString(), identifier.toString()));
        }
    }

    public TabDescriptor get(String key) {
        return map.get(key);
    }

    public class TabDescriptor {

        String url;
        String title;
        String identifier;

        public TabDescriptor(String url, String title, String identifier) {
            this.url = url;
            this.title = title;
            this.identifier = identifier;
        }

        public String getUrl() {
            return url;
        }

        public String getTitle() {
            return title;
        }

        public String getIdentifier() {
            return identifier;
        }

        @Override
        public String toString() {
            return "TabDescriptor{" + "url=" + url + ", title=" + title + ", identifier=" + identifier + '}';
        }
    }
}