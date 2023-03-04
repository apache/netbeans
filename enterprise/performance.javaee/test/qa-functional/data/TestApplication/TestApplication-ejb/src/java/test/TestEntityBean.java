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
  
