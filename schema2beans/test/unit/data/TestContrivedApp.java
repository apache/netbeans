/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
/*
 *	TestBook - test the basic features.
 *
 *	The following test assumes that we know the content of the
 *	graph as we get elements, add and change them. Therefore, the TestBook.xml
 *	file and this java test should be kept in sync.
 *
 * 	Test the following:
 *
 *	single String: get/set/remove/set/get
 *	boolean (from true): get/set true/get/set false/get/set true/get
 *	boolean (from false): get/set false/get/set true/get/set false/get
 *	String[]: get/set (null & !null)/add/remove
 *	Bean: remove/set(null)/create bean and graph of beans/set/add
 *
 */

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import org.netbeans.modules.schema2beans.*;
import application.*;


public class TestContrivedApp extends BaseTest {
    public static void main(String[] argv) {
        BaseTest o = new TestContrivedApp();
        if (argv.length > 0)
            o.setDocumentDir(argv[0]);
        try {
            o.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
    
    public void run() throws Exception {
        Application app;

        out("creating the bean graph");

        app = Application.createGraph(new FileInputStream(getFullDocumentName()));
	
        //	Check that we can read the graph and it is complete
        out("bean graph created");
	
        out(app);
        Module module = app.getModule();
        out("module.myaltDd = "+module.getMyAltDd());
        out("Make sure XML metacharacters get escapped");
        module.setMyAltDd("Foo & Co");
        out(app);
        out("Make sure alt-dd is still there with no whitespace");
        module.setMyAltDd("");
        out(app);
        out("alt-dd goes away now");
        module.setMyAltDd(null);
        out(app);
        module.setAlternateNameEjb("blue");
        out(app);
        //Module optionalModule = new Module();
        //optionalModule.setAlternateNameEjb("optional module");
        //app.setModule2(optionalModule);
        //out(app);
        
        return;
    }
	
    void parse(BaseBean bean, String parse) {
        out("Parsing " + parse);
        DDParser p = new DDParser(bean, parse);
        while (p.hasNext()) {
            Object o = p.next();
            if (o != null) {
                if (o instanceof BaseBean)
                    this.out(((BaseBean)o).dumpBeanNode());
                else
                    this.out(o.toString());
            }
            else
                this.out("null");
        }
    }
    
    void printChoiceProperties(BaseProperty[] bps) {
        if (bps == null)
            err("got null instead a BaseProperty[] instance");
        else {
            for (int l=0; l<bps.length; l++)
                check(bps[l].isChoiceProperty(), bps[l].getDtdName());
        }
    }
}

