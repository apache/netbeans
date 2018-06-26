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
package test.expand;
    
import javax.ejb.*;
  
/**
 * This is the bean class for the ExpandTest002Bean enterprise bean.
 * Created 4.3.2005 10:42:53
 * @author lm97939
 */
public class ExpandTest002Bean implements javax.ejb.SessionBean, test.expand.ExpandTest002RemoteBusiness {
    private javax.ejb.SessionContext context;

    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click the + sign on the left to edit the code.">
    // TODO Add code to acquire and use other enterprise resources (DataSource, JMS, enterprise bean, Web services)
    // TODO Add business methods
    /**
    * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
    */
    public void setSessionContext (javax.ejb.SessionContext aContext) {
    context = aContext;
    }

    /**
    * @see javax.ejb.SessionBean#ejbActivate()
    */
    public void ejbActivate () {

    }

    /**
    * @see javax.ejb.SessionBean#ejbPassivate()
    */
    public void ejbPassivate () {

    }

    /**
    * @see javax.ejb.SessionBean#ejbRemove()
    */
    public void ejbRemove () {

    }
    // </editor-fold>
    
    /**
    * See section 7.10.3 of the EJB 2.0 specification
    * See section 7.11.3 of the EJB 2.1 specification
    */
    public void ejbCreate () {
        // TODO implement ejbCreate if necessary, acquire resources
        // This method has access to the JNDI context so resource aquisition
        // spanning all methods can be performed here such as home interfaces
        // and data sources. 
    }
 
    

    // Enter business methods below. (Right-click in editor and choose 
    // Enterprise JavaBeans (EJB) > Add Business Method)

    public String testMethod() {
        //TODO implement testMethod
        return null;
    }
    

    
}
  
