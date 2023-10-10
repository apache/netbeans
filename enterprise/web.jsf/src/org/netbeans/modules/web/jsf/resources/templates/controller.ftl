<#--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->
<#if comment>

  TEMPLATE DESCRIPTION:

  This is Java template for 'JSF Pages From Entity Beans' controller class. Templating
  is performed using FreeMaker (http://freemarker.org/) - see its documentation
  for full syntax. Variables available for templating are:

    controllerClassName - controller class name (type: String)
    controllerPackageName - controller package name (type: String)
    entityClassName - entity class name without package (type: String)
    importEntityFullClassName - whether to import entityFullClassName or not
    entityFullClassName - fully qualified entity class name (type: String)
    ejbClassName - EJB class name (type: String)
    importEjbFullClassName - whether to import ejbFullClassName or not
    ejbFullClassName - fully qualified EJB class name (type: String)
    managedBeanName - name of managed bean (type: String)
    keyEmbedded - is entity primary key is an embeddable class (type: Boolean)
    keyType - fully qualified class name of entity primary key
    keyBody - body of Controller.Converter.getKey() method
    keyStringBody - body of Controller.Converter.getStringKey() method
    keyGetter - entity getter method returning primaty key instance
    keySetter - entity setter method to set primary key instance
    embeddedIdFields - contains information about embedded primary IDs
    cdiEnabled - project contains beans.xml, so Named beans can be used
    bundle - name of the variable defined in the JSF config file for the resource bundle (type: String)
    jakartaPersistencePackages - true if jakarta persistence is used, false if not (type: Boolean)
    jakartaJsfPackages - true if jakarta JSF is used, false if not (type: Boolean)

  This template is accessible via top level menu Tools->Templates and can
  be found in category JavaServer Faces->JSF from Entity.

</#if>
package ${controllerPackageName};

<#if importEntityFullClassName?? && importEntityFullClassName == true>
import ${entityFullClassName};
</#if>
import ${controllerPackageName}.util.JsfUtil;
import ${controllerPackageName}.util.PaginationHelper;
<#if importEjbFullClassName?? && importEjbFullClassName == true>
    <#if ejbClassName??>
import ${ejbFullClassName};
    <#elseif jpaControllerClassName??>
import ${jpaControllerFullClassName};
    </#if>
</#if>

import java.io.Serializable;
import java.util.ResourceBundle;
<#if jakartaJsfPackages?? && jakartaJsfPackages==true>
<#if isInjected?? && isInjected==true>
import jakarta.annotation.Resource;
</#if>
<#if ejbClassName??>
import jakarta.ejb.EJB;
</#if>
<#if managedBeanName??>
<#if cdiEnabled?? && cdiEnabled>
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
<#else>
import jakarta.faces.bean.ManagedBean;
import jakarta.faces.bean.SessionScoped;
</#if>
</#if>
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.ListDataModel;
import jakarta.faces.model.SelectItem;
<#else>
<#if isInjected?? && isInjected==true>
import javax.annotation.Resource;
</#if>
<#if ejbClassName??>
import javax.ejb.EJB;
</#if>
<#if managedBeanName??>
<#if cdiEnabled?? && cdiEnabled>
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
<#else>
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
</#if>
</#if>
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
</#if>
<#if jpaControllerClassName??>
<#if jakartaPersistencePackages?? && jakartaPersistencePackages==true >
<#if isInjected?? && isInjected==true >
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.transaction.UserTransaction;
<#else>
import jakarta.persistence.Persistence;
</#if>
<#else>
<#if isInjected?? && isInjected==true >
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.UserTransaction;
<#else>
import javax.persistence.Persistence;
</#if>
</#if>
</#if>


<#if managedBeanName??>
<#if cdiEnabled?? && cdiEnabled>
@Named("${managedBeanName}")
<#else>
@ManagedBean(name="${managedBeanName}")
</#if>
@SessionScoped
</#if>
public class ${controllerClassName} implements Serializable {

<#if isInjected?? && isInjected==true>
    @Resource
    private UserTransaction utx = null;
    @PersistenceUnit<#if persistenceUnitName??>(unitName = "${persistenceUnitName}")</#if>
    private EntityManagerFactory emf = null;
</#if>

    private ${entityClassName} current;
    private DataModel items = null;
<#if ejbClassName??>
    @EJB private ${ejbFullClassName} ejbFacade;
<#elseif jpaControllerClassName??>
    private ${jpaControllerClassName} jpaController = null;
</#if>
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public ${controllerClassName}() {
    }

    public ${entityClassName} getSelected() {
        if (current == null) {
            current = new ${entityClassName}();
<#if keyEmbedded>
            current.${keySetter}(new ${keyType}());
</#if>
            selectedItemIndex = -1;
        }
        return current;
    }

<#if ejbClassName??>
    private ${ejbClassName} getFacade() {
        return ejbFacade;
    }
<#elseif jpaControllerClassName??>
    private ${jpaControllerClassName} getJpaController() {
        if (jpaController == null) {
<#if isInjected?? && isInjected==true>
            jpaController = new ${jpaControllerClassName}(utx, emf);
<#else>
            jpaController = new ${jpaControllerClassName}(Persistence.createEntityManagerFactory(<#if persistenceUnitName??>"${persistenceUnitName}"</#if>));
</#if>
        }
        return jpaController;
    }
</#if>
    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
<#if ejbClassName??>
                    return getFacade().count();
<#elseif jpaControllerClassName??>
                    return getJpaController().get${entityClassName}Count();
</#if>
                }

                @Override
                public DataModel createPageDataModel() {
<#if ejbClassName??>
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem()+getPageSize()}));
<#elseif jpaControllerClassName??>
                     return new ListDataModel(getJpaController().find${entityClassName}Entities(getPageSize(), getPageFirstItem() ));
</#if>
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (${entityClassName})getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new ${entityClassName}();
<#if keyEmbedded>
        current.${keySetter}(new ${keyType}());
</#if>
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
<#list embeddedIdFields as fields>
            current.${keyGetter}().${fields.getEmbeddedSetter()}(current.${fields.getCodeToPopulate()});
</#list>
<#if ejbClassName??>
            getFacade().create(current);
<#elseif jpaControllerClassName??>
            getJpaController().create(current);
</#if>
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("${bundle}").getString("${entityClassName}Created"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("${bundle}").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (${entityClassName})getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
<#list embeddedIdFields as fields>
            current.${keyGetter}().${fields.getEmbeddedSetter()}(current.${fields.getCodeToPopulate()});
</#list>
<#if ejbClassName??>
            getFacade().edit(current);
<#elseif jpaControllerClassName??>
            getJpaController().edit(current);
</#if>
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("${bundle}").getString("${entityClassName}Updated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("${bundle}").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (${entityClassName})getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
<#if ejbClassName??>
            getFacade().remove(current);
<#elseif jpaControllerClassName??>
            getJpaController().destroy(current.${keyGetter}());
</#if>
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("${bundle}").getString("${entityClassName}Deleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("${bundle}").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
<#if ejbClassName??>
        int count = getFacade().count();
<#elseif jpaControllerClassName??>
        int count = getJpaController().get${entityClassName}Count();
</#if>
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count-1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
<#if ejbClassName??>
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex+1}).get(0);
<#elseif jpaControllerClassName??>
            current = getJpaController().find${entityClassName}Entities(1, selectedItemIndex).get(0);
</#if>
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
<#if ejbClassName??>
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
<#elseif jpaControllerClassName??>
        return JsfUtil.getSelectItems(getJpaController().find${entityClassName}Entities(), false);
</#if>
    }

    public SelectItem[] getItemsAvailableSelectOne() {
<#if ejbClassName??>
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
<#elseif jpaControllerClassName??>
        return JsfUtil.getSelectItems(getJpaController().find${entityClassName}Entities(), true);
</#if>
    }

<#if ejbClassName?? && cdiEnabled?? && cdiEnabled>
    public ${entityClassName} get${entityClassName}(${keyType} id) {
        return ejbFacade.find(id);
    }
</#if>

    @FacesConverter(forClass=${entityClassName}.class)
    public static class ${controllerClassName}Converter implements Converter {
<#if keyEmbedded>

        private static final String SEPARATOR = "#";
        private static final String SEPARATOR_ESCAPED = "\\#";
</#if>

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ${controllerClassName} controller = (${controllerClassName})facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "${managedBeanName}");
<#if ejbClassName??>
<#if cdiEnabled?? && cdiEnabled>
            return controller.get${entityClassName}(getKey(value));
<#else>
            return controller.ejbFacade.find(getKey(value));
</#if>
<#elseif jpaControllerClassName??>
            return controller.getJpaController().find${entityClassName}(getKey(value));
</#if>
        }

        ${keyType} getKey(String value) {
            ${keyType} key;
${keyBody}
            return key;
        }

        String getStringKey(${keyType} value) {
            StringBuilder sb = new StringBuilder();
${keyStringBody}
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof ${entityClassName}) {
                ${entityClassName} o = (${entityClassName}) object;
                return getStringKey(o.${keyGetter}());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: "+${entityClassName}.class.getName());
            }
        }

    }

}
