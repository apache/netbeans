/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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

    private HashMap<String, TabDescriptor> map = new HashMap();

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