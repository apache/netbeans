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

package test;
    
import javax.ejb.*;
  
/**
 * This is the bean class for the TestEntityBean enterprise bean.
 * Created 4.3.2005 15:31:28
 * @author lm97939
 */
   public abstract class TestEntityBean implements javax.ejb.EntityBean, test.TestEntityLocalBusiness {
         private javax.ejb.EntityContext context;
        
         // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click on the + sign on the left to edit the code.">
         // TODO Consider creating Transfer Object to encapsulate data
         // TODO Review finder methods
         /**
          * @see javax.ejb.EntityBean#setEntityContext(javax.ejb.EntityContext)
          */
          public void setEntityContext (javax.ejb.EntityContext aContext) {
              context = aContext;
          }

         /**
          * @see javax.ejb.EntityBean#ejbActivate()
          */
          public void ejbActivate () {

          }
  
         /**
          * @see javax.ejb.EntityBean#ejbPassivate()
          */
          public void ejbPassivate () {

          }

         /**
          * @see javax.ejb.EntityBean#ejbRemove()
          */
          public void ejbRemove () {

          }
  
         /**
          * @see javax.ejb.EntityBean#unsetEntityContext()
          */
          public void unsetEntityContext () {
              context = null;
          }

         /**
          * @see javax.ejb.EntityBean#ejbLoad()
          */
          public void ejbLoad () {

          }
  
         /**
          * @see javax.ejb.EntityBean#ejbStore()
          */
          public void ejbStore () {

          }
          // </editor-fold>
          
        // <editor-fold desc="CMP fields and relationships.">
           
          public abstract java.lang.String getKey();
          public abstract void setKey(java.lang.String key);
        
         // </editor-fold>
         
         public java.lang.String ejbCreate(java.lang.String key)  throws javax.ejb.CreateException {
             if (key == null) {
                 throw new javax.ejb.CreateException("The field \"key\" must not be null");
             }
                         
             // TODO add additional validation code, throw CreateException if data is not valid
             setKey(key);
              
             return null;
         }
     
         public void ejbPostCreate(java.lang.String key) {
             // TODO populate relationships here if appropriate
             
         }

       public abstract java.lang.String getCmpField();

       public abstract void setCmpField(java.lang.String cmpField);

       public String businessMethod() {
           //TODO implement businessMethod
           return null;
       }
   }
  
