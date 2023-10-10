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

package org.netbeans.modules.maven.execute.model.io.jdom;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.netbeans.junit.NbTestCase;

public class NetbeansBuildActionJDOMWriterTest extends NbTestCase {

    public NetbeansBuildActionJDOMWriterTest(String name) {
        super(name);
    }

    public void testInsertAtPreferredLocation() throws Exception {
        NetbeansBuildActionJDOMWriter writer = new NetbeansBuildActionJDOMWriter();
        XMLOutputter xmlout = new XMLOutputter();
        xmlout.setFormat(Format.getRawFormat().setLineSeparator("\n"));
        Element p = new Element("p");
        NetbeansBuildActionJDOMWriter.Counter c = writer.new Counter(1);
        writer.insertAtPreferredLocation(p, new Element("one"), c);
        assertEquals("<p>\n    <one />\n</p>", xmlout.outputString(p));
        c.increaseCount();
        writer.insertAtPreferredLocation(p, new Element("two"), c);
        assertEquals("<p>\n    <one />\n    <two />\n</p>", xmlout.outputString(p));
        c = writer.new Counter(1);
        writer.insertAtPreferredLocation(p, new Element("zero"), c);
        assertEquals("<p>\n    <zero />\n    <one />\n    <two />\n</p>", xmlout.outputString(p));
        c.increaseCount();
        writer.insertAtPreferredLocation(p, new Element("hemi"), c);
        assertEquals("<p>\n    <zero />\n    <hemi />\n    <one />\n    <two />\n</p>", xmlout.outputString(p));
        c.increaseCount();
        c.increaseCount();
        writer.insertAtPreferredLocation(p, new Element("sesqui"), c);
        assertEquals("<p>\n    <zero />\n    <hemi />\n    <one />\n    <sesqui />\n    <two />\n</p>", xmlout.outputString(p));
        c.increaseCount();
        c.increaseCount();
        writer.insertAtPreferredLocation(p, new Element("ultimate"), c);
        assertEquals("<p>\n    <zero />\n    <hemi />\n    <one />\n    <sesqui />\n    <two />\n    <ultimate />\n</p>", xmlout.outputString(p));
        c = writer.new Counter(1);
        writer.insertAtPreferredLocation(p, new Element("initial"), c);
        assertEquals("<p>\n    <initial />\n    <zero />\n    <hemi />\n    <one />\n    <sesqui />\n    <two />\n    <ultimate />\n</p>", xmlout.outputString(p));
        // XXX indentation still not right; better to black-box test write(ActionToGoalMapping...)
    }

}
