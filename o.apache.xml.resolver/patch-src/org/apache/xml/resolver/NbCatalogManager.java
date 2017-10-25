/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.xml.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.xml.resolver.helpers.BootstrapResolver;
import org.apache.xml.resolver.helpers.Debug;

public class NbCatalogManager
  extends CatalogManager
{
  private static String pFiles = "xml.catalog.files";
  private static String pVerbosity = "xml.catalog.verbosity";
  private static String pPrefer = "xml.catalog.prefer";
  private static String pStatic = "xml.catalog.staticCatalog";
  private static String pAllowPI = "xml.catalog.allowPI";
  private static String pClassname = "xml.catalog.className";
  private static String pIgnoreMissing = "xml.catalog.ignoreMissing";
  private static NbCatalogManager staticManager = new NbCatalogManager();
  private BootstrapResolver bResolver = new BootstrapResolver();
  private boolean ignoreMissingProperties = (System.getProperty(pIgnoreMissing) != null) || (System.getProperty(pFiles) != null);
  private ResourceBundle resources;
  private String propertyFile = "CatalogManager.properties";
  private URL propertyFileURI = null;
  private String defaultCatalogFiles = "./xcatalog";
  private String catalogFiles = null;
  private boolean fromPropertiesFile = false;
  private int defaultVerbosity = 1;
  private Integer verbosity = null;
  private boolean defaultPreferPublic = true;
  private Boolean preferPublic = null;
  private boolean defaultUseStaticCatalog = true;
  private Boolean useStaticCatalog = null;
  private static Catalog staticCatalog = null;
  private boolean defaultOasisXMLCatalogPI = true;
  private Boolean oasisXMLCatalogPI = null;
  private boolean defaultRelativeCatalogs = true;
  private Boolean relativeCatalogs = null;
  private String catalogClassName = null;
  public Debug debug = null;
  
  public NbCatalogManager()
  {
    this.debug = new Debug();
  }
  
  public NbCatalogManager(String propertyFile)
  {
    this.propertyFile = propertyFile;
    
    this.debug = new Debug();
  }
  
  public void setBootstrapResolver(BootstrapResolver resolver)
  {
    this.bResolver = resolver;
  }
  
  public BootstrapResolver getBootstrapResolver()
  {
    return this.bResolver;
  }
  
  private synchronized void readProperties()
  {
    if (this.propertyFile == null) {
      return;
    }
    try
    {
      this.propertyFileURI = NbCatalogManager.class.getResource("/" + this.propertyFile);
      InputStream in = NbCatalogManager.class.getResourceAsStream("/" + this.propertyFile);
      if (in == null)
      {
        if (!this.ignoreMissingProperties)
        {
          this.debug.message(2, "Cannot find " + this.propertyFile);
          
          this.ignoreMissingProperties = true;
        }
        return;
      }
      this.resources = new PropertyResourceBundle(in);
    }
    catch (MissingResourceException mre)
    {
      if (!this.ignoreMissingProperties) {
        System.err.println("Cannot read " + this.propertyFile);
      }
    }
    catch (IOException e)
    {
      if (!this.ignoreMissingProperties) {
        System.err.println("Failure trying to read " + this.propertyFile);
      }
    }
    if (this.verbosity == null) {
      try
      {
        String verbStr = this.resources.getString("verbosity");
        int verb = Integer.parseInt(verbStr.trim());
        this.debug.setDebug(verb);
        this.verbosity = new Integer(verb);
      }
      catch (Exception e) {}
    }
  }
  
  public static CatalogManager getStaticManager()
  {
    return staticManager;
  }
  
  public boolean getIgnoreMissingProperties()
  {
    return this.ignoreMissingProperties;
  }
  
  public void setIgnoreMissingProperties(boolean ignore)
  {
    this.ignoreMissingProperties = ignore;
  }
  
  /**
   * @deprecated
   */
  public void ignoreMissingProperties(boolean ignore)
  {
    setIgnoreMissingProperties(ignore);
  }
  
  private int queryVerbosity()
  {
    String defaultVerbStr = Integer.toString(this.defaultVerbosity);
    
    String verbStr = System.getProperty(pVerbosity);
    if (verbStr == null)
    {
      if (this.resources == null) {
        readProperties();
      }
      if (this.resources != null) {
        try
        {
          verbStr = this.resources.getString("verbosity");
        }
        catch (MissingResourceException e)
        {
          verbStr = defaultVerbStr;
        }
      } else {
        verbStr = defaultVerbStr;
      }
    }
    int verb = this.defaultVerbosity;
    try
    {
      verb = Integer.parseInt(verbStr.trim());
    }
    catch (Exception e)
    {
      System.err.println("Cannot parse verbosity: \"" + verbStr + "\"");
    }
    if (this.verbosity == null)
    {
      this.debug.setDebug(verb);
      this.verbosity = new Integer(verb);
    }
    return verb;
  }
  
  public int getVerbosity()
  {
    if (this.verbosity == null) {
      this.verbosity = new Integer(queryVerbosity());
    }
    return this.verbosity.intValue();
  }
  
  public void setVerbosity(int verbosity)
  {
    this.verbosity = new Integer(verbosity);
    this.debug.setDebug(verbosity);
  }
  
  /**
   * @deprecated
   */
  public int verbosity()
  {
    return getVerbosity();
  }
  
  private boolean queryRelativeCatalogs()
  {
    if (this.resources == null) {
      readProperties();
    }
    if (this.resources == null) {
      return this.defaultRelativeCatalogs;
    }
    try
    {
      String allow = this.resources.getString("relative-catalogs");
      return (allow.equalsIgnoreCase("true")) || (allow.equalsIgnoreCase("yes")) || (allow.equalsIgnoreCase("1"));
    }
    catch (MissingResourceException e) {}
    return this.defaultRelativeCatalogs;
  }
  
  public boolean getRelativeCatalogs()
  {
    if (this.relativeCatalogs == null) {
      this.relativeCatalogs = new Boolean(queryRelativeCatalogs());
    }
    return this.relativeCatalogs.booleanValue();
  }
  
  public void setRelativeCatalogs(boolean relative)
  {
    this.relativeCatalogs = new Boolean(relative);
  }
  
  /**
   * @deprecated
   */
  public boolean relativeCatalogs()
  {
    return getRelativeCatalogs();
  }
  
  private String queryCatalogFiles()
  {
    String catalogList = System.getProperty(pFiles);
    this.fromPropertiesFile = false;
    if (catalogList == null)
    {
      if (this.resources == null) {
        readProperties();
      }
      if (this.resources != null) {
        try
        {
          catalogList = this.resources.getString("catalogs");
          this.fromPropertiesFile = true;
        }
        catch (MissingResourceException e)
        {
          System.err.println(this.propertyFile + ": catalogs not found.");
          catalogList = null;
        }
      }
    }
    if (catalogList == null) {
      catalogList = this.defaultCatalogFiles;
    }
    return catalogList;
  }
  
  public Vector getCatalogFiles()
  {
    if (this.catalogFiles == null) {
      this.catalogFiles = queryCatalogFiles();
    }
    StringTokenizer files = new StringTokenizer(this.catalogFiles, ";");
    Vector catalogs = new Vector();
    while (files.hasMoreTokens())
    {
      String catalogFile = files.nextToken();
      URL absURI = null;
      if ((this.fromPropertiesFile) && (!relativeCatalogs())) {
        try
        {
          absURI = new URL(this.propertyFileURI, catalogFile);
          catalogFile = absURI.toString();
        }
        catch (MalformedURLException mue)
        {
          absURI = null;
        }
      }
      catalogs.add(catalogFile);
    }
    return catalogs;
  }
  
  public void setCatalogFiles(String fileList)
  {
    this.catalogFiles = fileList;
    this.fromPropertiesFile = false;
  }
  
  /**
   * @deprecated
   */
  public Vector catalogFiles()
  {
    return getCatalogFiles();
  }
  
  private boolean queryPreferPublic()
  {
    String prefer = System.getProperty(pPrefer);
    if (prefer == null)
    {
      if (this.resources == null) {
        readProperties();
      }
      if (this.resources == null) {
        return this.defaultPreferPublic;
      }
      try
      {
        prefer = this.resources.getString("prefer");
      }
      catch (MissingResourceException e)
      {
        return this.defaultPreferPublic;
      }
    }
    if (prefer == null) {
      return this.defaultPreferPublic;
    }
    return prefer.equalsIgnoreCase("public");
  }
  
  public boolean getPreferPublic()
  {
    if (this.preferPublic == null) {
      this.preferPublic = new Boolean(queryPreferPublic());
    }
    return this.preferPublic.booleanValue();
  }
  
  public void setPreferPublic(boolean preferPublic)
  {
    this.preferPublic = new Boolean(preferPublic);
  }
  
  /**
   * @deprecated
   */
  public boolean preferPublic()
  {
    return getPreferPublic();
  }
  
  private boolean queryUseStaticCatalog()
  {
    String staticCatalog = System.getProperty(pStatic);
    if (staticCatalog == null)
    {
      if (this.resources == null) {
        readProperties();
      }
      if (this.resources == null) {
        return this.defaultUseStaticCatalog;
      }
      try
      {
        staticCatalog = this.resources.getString("static-catalog");
      }
      catch (MissingResourceException e)
      {
        return this.defaultUseStaticCatalog;
      }
    }
    if (staticCatalog == null) {
      return this.defaultUseStaticCatalog;
    }
    return (staticCatalog.equalsIgnoreCase("true")) || (staticCatalog.equalsIgnoreCase("yes")) || (staticCatalog.equalsIgnoreCase("1"));
  }
  
  public boolean getUseStaticCatalog()
  {
    if (this.useStaticCatalog == null) {
      this.useStaticCatalog = new Boolean(queryUseStaticCatalog());
    }
    return this.useStaticCatalog.booleanValue();
  }
  
  public void setUseStaticCatalog(boolean useStatic)
  {
    this.useStaticCatalog = new Boolean(useStatic);
  }
  
  /**
   * @deprecated
   */
  public boolean staticCatalog()
  {
    return getUseStaticCatalog();
  }
  
  public Catalog getPrivateCatalog()
  {
    Catalog catalog = staticCatalog;
    if (this.useStaticCatalog == null) {
      this.useStaticCatalog = new Boolean(getUseStaticCatalog());
    }
    if ((catalog == null) || (!this.useStaticCatalog.booleanValue()))
    {
      try
      {
        String catalogClassName = getCatalogClassName();
        if (catalogClassName == null) {
          catalog = new Catalog();
        } else {
          try
          {
            catalog = (Catalog)Class.forName(catalogClassName).newInstance();
          }
          catch (ClassNotFoundException cnfe)
          {
            this.debug.message(1, "Catalog class named '" + catalogClassName + "' could not be found. Using default.");
            
            catalog = new Catalog();
          }
          catch (ClassCastException cnfe)
          {
            this.debug.message(1, "Class named '" + catalogClassName + "' is not a Catalog. Using default.");
            
            catalog = new Catalog();
          }
        }
        catalog.setCatalogManager(this);
        catalog.setupReaders();
        catalog.loadSystemCatalogs();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      if (this.useStaticCatalog.booleanValue()) {
        staticCatalog = catalog;
      }
    }
    return catalog;
  }
  
  public Catalog getCatalog()
  {
    Catalog catalog = staticCatalog;
    if (this.useStaticCatalog == null) {
      this.useStaticCatalog = new Boolean(getUseStaticCatalog());
    }
    if ((catalog == null) || (!this.useStaticCatalog.booleanValue()))
    {
      catalog = getPrivateCatalog();
      if (this.useStaticCatalog.booleanValue()) {
        staticCatalog = catalog;
      }
    }
    return catalog;
  }
  
  public boolean queryAllowOasisXMLCatalogPI()
  {
    String allow = System.getProperty(pAllowPI);
    if (allow == null)
    {
      if (this.resources == null) {
        readProperties();
      }
      if (this.resources == null) {
        return this.defaultOasisXMLCatalogPI;
      }
      try
      {
        allow = this.resources.getString("allow-oasis-xml-catalog-pi");
      }
      catch (MissingResourceException e)
      {
        return this.defaultOasisXMLCatalogPI;
      }
    }
    if (allow == null) {
      return this.defaultOasisXMLCatalogPI;
    }
    return (allow.equalsIgnoreCase("true")) || (allow.equalsIgnoreCase("yes")) || (allow.equalsIgnoreCase("1"));
  }
  
  public boolean getAllowOasisXMLCatalogPI()
  {
    if (this.oasisXMLCatalogPI == null) {
      this.oasisXMLCatalogPI = new Boolean(queryAllowOasisXMLCatalogPI());
    }
    return this.oasisXMLCatalogPI.booleanValue();
  }
  
  public void setAllowOasisXMLCatalogPI(boolean allowPI)
  {
    this.oasisXMLCatalogPI = new Boolean(allowPI);
  }
  
  /**
   * @deprecated
   */
  public boolean allowOasisXMLCatalogPI()
  {
    return getAllowOasisXMLCatalogPI();
  }
  
  public String queryCatalogClassName()
  {
    String className = System.getProperty(pClassname);
    if (className == null)
    {
      if (this.resources == null) {
        readProperties();
      }
      if (this.resources == null) {
        return null;
      }
      try
      {
        return this.resources.getString("catalog-class-name");
      }
      catch (MissingResourceException e)
      {
        return null;
      }
    }
    return className;
  }
  
  public String getCatalogClassName()
  {
    if (this.catalogClassName == null) {
      this.catalogClassName = queryCatalogClassName();
    }
    return this.catalogClassName;
  }
  
  public void setCatalogClassName(String className)
  {
    this.catalogClassName = className;
  }
  
  /**
   * @deprecated
   */
  public String catalogClassName()
  {
    return getCatalogClassName();
  }
}
