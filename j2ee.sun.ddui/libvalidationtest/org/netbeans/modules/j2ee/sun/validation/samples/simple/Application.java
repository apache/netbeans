/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.sun.validation.samples.simple;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collection;

import org.netbeans.modules.j2ee.sun.validation.samples.simple.beans.*;
import org.netbeans.modules.j2ee.sun.validation.util.BundleReader;
import org.netbeans.modules.j2ee.sun.validation.util.Display;
import org.netbeans.modules.j2ee.sun.validation.util.Utils;
import org.netbeans.modules.j2ee.sun.validation.ValidationManager;
import org.netbeans.modules.j2ee.sun.validation.ValidationManagerFactory;



/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class Application {
    
    /** Creates a new instance of sample */
    public Application() {
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RootElement rootElement =  null;
        String fileBeingValidated = "org/netbeans/modules/" +           //NOI18N
            "j2ee/sun/validation/samples/simple/simple.xml";            //NOI18N

        String validationFile = "org/netbeans/modules/" +               //NOI18N
            "j2ee/sun/validation/samples/simple/validation.xml";        //NOI18N

///    String validationFile = "C:/testframe/tests/org/netbeans/" +     //NOI18N
///        "modules/j2ee/sun/validation/samples/simple/validation.xml"; //NOI18N


//      You should set impl.file to fully qualified file name of the 
//      impl(implementation) file
///        String implFile = "com.sun.enterprise.tools." +              //NOI18N
///            "common.XYZImpl";                                        //NOI18N
///     System.setProperty("impl.file", implFile);                      //NOI18N


//      You can set constraints.file to either absolute or relative path of the
//      Constraints file
///     String cosntriantsFile = "com/sun/enterprise/tools/" +          //NOI18N
///         "common/testXYZ.xml";                                       //NOI18N
///     String cosntriantsFile = "C:/testframe/src/java/com/sun/" +     //NOI18N
///         "enterprise/tools/XYZ/testXYZ.xml";                         //NOI18N
///     System.setProperty("constraints.file", cosntriantsFile);        //NOI18N


        //Create an InpurtStream object
        Utils utils = new Utils();
        InputStream inputStream = utils.getInputStream(fileBeingValidated);

        //Create graph
        if(inputStream != null) {
            try {
                rootElement = RootElement.createGraph(inputStream);
            } catch(Exception e) {
               System.out.println(e.getMessage());
            }
        } else {
            String format = 
                BundleReader.getValue("MSG_Unable_to_use_file");        //NOI18N
            Object[] arguments = new Object[]{fileBeingValidated};
            System.out.println(MessageFormat.format(format, arguments));
        }

        if(rootElement != null){
            ValidationManagerFactory validationManagerFactory = 
                new ValidationManagerFactory();

            //you can pass either absolute or relative path of the validation
            //file to getValidationManager()
            ValidationManager validationManager = 
                validationManagerFactory.getValidationManager(validationFile);

            Collection failures =
                validationManager.validate(rootElement);

            Display display = new Display();
            display.text(failures);
            display.gui(failures);
        }
    }
}
