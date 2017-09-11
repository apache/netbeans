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
 *	TestEncoding - Make sure we correctly deal with I18N encoding stuff.
 *
 */

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import org.netbeans.modules.schema2beans.*;
import forteddl.*;


public class TestEncoding extends BaseTest {
    public static void main(String[] argv) {
        BaseTest o = new TestEncoding();
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
        Forteddl app;

        out("creating the bean graph");
        app = Forteddl.createGraph(new FileInputStream(getFullDocumentName()));
	
        //	Check that we can read the graph an it is complete
        out("bean graph created");
	
        out(app);
        
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

