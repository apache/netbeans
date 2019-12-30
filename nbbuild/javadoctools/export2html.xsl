<?xml version="1.0" encoding="UTF-8" ?>
<!--

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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:import href="jsonhelp.xsl" />
    <xsl:output method="html"/>
    <xsl:param name="date" />
    <xsl:param name="download" select="'true'"/>
    <xsl:param name="maturity" />
    <xsl:param name="version" />
    <xsl:param name="releaseinfo" />
    
    <!-- unique key over all groups of apis -->
    <xsl:key match="//api[@type='export']" name="apiGroups" use="@group" />
    <!-- unique key over all names of apis -->
    <xsl:key match="//api" name="apiNames" use="@name" />
    <xsl:template match="/apis" >
        <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
        <html>
            <head>
                <!-- projects.netbeans.org -->
                <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
                <xsl:element name="title">
                    <xsl:call-template name="apachenetbeanstext" >
                        <xsl:with-param name="maturity">
                            <xsl:value-of select="$maturity"/>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:text>API List</xsl:text>
                </xsl:element>
                <link rel="stylesheet" href="netbeans.css" type="text/css"/>
                <link rel="icon" type="image/png" sizes="32x32" href="//netbeans.apache.org/favicon-32x32.png" /> 
                <link rel="icon" type="image/png" sizes="16x16" href="//netbeans.apache.org/favicon-16x16.png" />
          

            </head>

            <body>

                <center>
                    <xsl:element name="h1">
                        <xsl:call-template name="apachenetbeanstext" >
                            <xsl:with-param name="maturity">
                                <xsl:value-of select="$maturity"/>
                            </xsl:with-param>
                        </xsl:call-template>
                        <xsl:text>API List</xsl:text>
                    </xsl:element>
                    <xsl:element name="h3">
                        <xsl:call-template name="apachenetbeansversion" >
                            <xsl:with-param name="maturity">
                                <xsl:value-of select="$maturity"/>                
                            </xsl:with-param>
                            <xsl:with-param name="version">
                                <xsl:value-of select="$version"/>                
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:element>
                    <xsl:if test="$date" >
                        <xsl:value-of select="$date" />
                        <p/>
                    </xsl:if>
                </center>

                This document provides a list of <em>NetBeans APIs</em> with a short description
                of what they are used for, and a table describing different types of interfaces
                (see <a href="http://wiki.netbeans.org/API_Design">What is
                    an API?</a> to understand why we list DTDs, file formats, etc.) and with
                a stability category (<span style="background:#ffffff">stable and official</span>,
                <span style="background:#ddcc80">under development</span>,
                <span style="background:#afafaf;text-decoration:line-through">deprecated</span>,
                <span style="background:#e0c0c0">friend or private</span>;
                see <a href="http://wiki.netbeans.org/API_Stability">API
                    stability</a> for more info).
                The aim is to provide as detailed a definition of NetBeans module 
                external interfaces as possible and give other developers a chance to decide
                whether they want to depend on a particular API or not.
                <p/>
                Some of these APIs are part of the 
                <a href="http://platform.netbeans.org/">NetBeans Platform</a>
                (for example lookup,
                loaders, utilities, nodes, explorer, window systems, multiview, etc.), some of them
                are specific to 
                <a href="http://www.netbeans.org/products/ide/index.html">NetBeans IDE</a> 
                (projects, javacore, diff, etc.) and some
                are not included in the release at all and are just provided for download
                (usually via autoupdate). Basically when building an application based on
                <em>NetBeans</em> one is free to choose the set of modules and their APIs 
                to satisfy one's needs.
                <p>
                    This is a list of APIs for 
                    <xsl:call-template name="apachenetbeansversion" >
                        <xsl:with-param name="maturity">
                            <xsl:value-of select="$maturity"/>                
                        </xsl:with-param>
                        <xsl:with-param name="version">
                            <xsl:value-of select="$version"/>                
                        </xsl:with-param>
                    </xsl:call-template>, if you want to see
                    a list of APIs at for a particular version, you may want to go to:
                </p>
                <ul>
                    <xsl:variable name="currentversion" select="document($releaseinfo)/*/@position"/>
                    <xsl:for-each select="document($releaseinfo)//release">
                        <xsl:sort data-type="number" select="@position" order="descending" />
                        <li>
                            <xsl:choose>
                                <xsl:when test="$currentversion = @position">
                                    This is were your are - Javadoc as released for
                                    <xsl:call-template name="apachenetbeansversion" >
                                        <xsl:with-param name="maturity">
                                            <xsl:value-of select="@tlp"/>
                                        </xsl:with-param>
                                        <xsl:with-param name="version">
                                            <xsl:value-of select="@version"/>                
                                        </xsl:with-param>
                                    </xsl:call-template>             
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:element name="a">
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="@apidocurl"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="target">_top</xsl:attribute>
                                        <xsl:value-of select="@version"/>
                                    </xsl:element>                 
                                    - Javadoc as released for 
                                    <xsl:call-template name="apachenetbeansversion" >
                                        <xsl:with-param name="maturity">
                                            <xsl:value-of select="@tlp"/>
                                        </xsl:with-param>
                                        <xsl:with-param name="version">
                                            <xsl:value-of select="@version"/>                
                                        </xsl:with-param>
                                    </xsl:call-template>          
                                </xsl:otherwise>
                            </xsl:choose>
                    
                        </li>
                    </xsl:for-each>
                </ul>
                <p>
                    This is a list of apidocs of NetBeans of Oracle era.
                </p>
                <ul>
                    <li>
                        <a href="http://bits.netbeans.org/8.2/javadoc/" target="_top">8.2</a> - Javadoc as released for NetBeans IDE 8.2</li>
                    <li>
                        <a href="http://bits.netbeans.org/8.1/javadoc/" target="_top">8.1</a> - Javadoc as released for NetBeans IDE 8.1</li>
                    <li>
                        <a href="http://bits.netbeans.org/8.0.1/javadoc/" target="_top">8.0.1</a> - Javadoc as released for NetBeans IDE 8.0.1</li>
                    <li>
                        <a href="http://bits.netbeans.org/8.0/javadoc/" target="_top">8.0</a> - Javadoc as released for NetBeans IDE 8.0</li>
                    <li>
                        <a href="http://bits.netbeans.org/7.4/javadoc/" target="_top">7.4</a> - Javadoc as released for NetBeans IDE 7.4</li>
                    <li>
                        <a href="http://bits.netbeans.org/7.3.1/javadoc/" target="_top">7.3.1</a> - Javadoc as released for NetBeans IDE 7.3.1</li>
                    <li>
                        <a href="http://bits.netbeans.org/7.3/javadoc/" target="_top">7.3</a> - Javadoc as released for NetBeans IDE 7.3</li>
                    <li>
                        <a href="http://bits.netbeans.org/7.2.1/javadoc/" target="_top">7.2.1</a> - Javadoc as released for NetBeans IDE 7.2.1</li>
                    <li>
                        <a href="http://bits.netbeans.org/7.2/javadoc/" target="_top">7.2</a> - Javadoc as released for NetBeans IDE 7.2</li>
                    <li>
                        <a href="http://bits.netbeans.org/7.1.2/javadoc/" target="_top">7.1.2</a> - Javadoc as released for NetBeans IDE 7.1.2</li>
                    <li>
                        <a href="http://bits.netbeans.org/7.1.1/javadoc/" target="_top">7.1.1</a> - Javadoc as released for NetBeans IDE 7.1.1</li>
                    <li>
                        <a href="http://bits.netbeans.org/7.1/javadoc/" target="_top">7.1</a> - Javadoc as released for NetBeans IDE 7.1</li>
                    <li>
                        <a href="http://bits.netbeans.org/7.0.1/javadoc/" target="_top">7.0.1</a> - Javadoc as released for NetBeans IDE 7.0.1</li>
                    <li>
                        <a href="http://bits.netbeans.org/7.0/javadoc/" target="_top">7.0</a> - Javadoc as released for NetBeans IDE 7.0</li>
                    <li>
                        <a href="http://bits.netbeans.org/6.9.1/javadoc/" target="_top">6.9.1</a> - Javadoc as released for NetBeans IDE 6.9.1</li>
                    <li>
                        <a href="http://bits.netbeans.org/6.9/javadoc/" target="_top">6.9</a> - Javadoc as released for NetBeans IDE 6.9</li>
                    <li>
                        <a href="http://bits.netbeans.org/6.8/javadoc/" target="_top">6.8</a> - Javadoc as released for NetBeans IDE 6.8</li>
                    <li>
                        <a href="http://bits.netbeans.org/6.7/javadoc/" target="_top">6.7</a> - Javadoc as released for NetBeans IDE 6.7</li>
                    <li>
                        <a href="http://bits.netbeans.org/6.5/javadoc/" target="_top">6.5</a> - Javadoc as released for NetBeans IDE 6.5</li>
                    <li>
                        <a href="http://bits.netbeans.org/6.1/javadoc/" target="_top">6.1</a> - Javadoc as released for NetBeans IDE 6.1</li>
                    <li>
                        <a href="http://bits.netbeans.org/6.0/javadoc/" target="_top">6.0</a> - Javadoc as released for NetBeans IDE 6.0</li>
                    <li>
                        <a href="http://www.netbeans.org/download/5_5_1/javadoc/" target="_top">5.5.1</a> - Javadoc as released for NetBeans IDE 5.5.1</li>
                    <li>
                        <a href="http://www.netbeans.org/download/5_5/javadoc/" target="_top">5.5</a> - Javadoc as released for NetBeans IDE 5.5</li>
                    <li>
                        <a href="http://www.netbeans.org/download/5_0/javadoc/" target="_top">5.0</a> - Javadoc as released for NetBeans IDE 5.0</li>
                </ul>
                <p/>
                To get the API of your module listed here, see the documentation for the 
                Javadoc building
                <a href="http://wiki.netbeans.org/APIDevelopment">infrastructure</a>.
        
                <h4>Additional Sources of Information</h4>
        
                <ul>
                    <li>
                        <a href="apichanges.html">Changes since previous release</a>
                    </li>
                    <li>
                        <a href="usecases.html">How to use certain NetBeans APIs</a>
                    </li>
                    <li>
                        <a href="allclasses-frame.html">Index of all NetBeans API classes</a>
                    </li>
                    <li>
                        <a href="layers.html">Extracted List of Layer APIs</a>
                    </li>
                    <li>
                        <a href="properties.html">Extracted List of Property APIs</a>
                    </li>
                    <li>
                        <a href="branding.html">Extracted List of Branding APIs</a>
                    </li>
                </ul>

                <h4>FAQ and Mailing List</h4>

                <p>Can't find what you're looking for? Try the <a href="https://netbeans.apache.org" target="_top">Apache NetBeans website</a>.</p>

                <hr/>
                <xsl:call-template name="list-modules" />
                <hr/>
                <xsl:apply-templates />
        
            </body>
        </html>
       
    </xsl:template>
    
    <xsl:template name="list-modules">
        <h2>Content</h2>
        <ul>
            <xsl:for-each select="/apis/module" >
                <xsl:sort select="@name" />
                <xsl:choose>
                    <xsl:when test="api" >
                        <li>
                            <span>
                                <xsl:attribute name="style">
                                    <xsl:choose>
                                        <xsl:when test="descendant::api[@category='stable' and @group='java']">background:#ffffff</xsl:when>
                                        <xsl:when test="descendant::api[@category='official' and @group='java']">background:#ffffff</xsl:when>
                                        <xsl:when test="descendant::api[@category='devel' and @group='java']">background:#ddcc80</xsl:when>
                                        <xsl:when test="descendant::api[@category='deprecated' and @group='java']">text-decoration: line-through</xsl:when>
                                        <xsl:otherwise>background:#e0c0c0</xsl:otherwise>
                                    </xsl:choose>
                                </xsl:attribute>
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="substring-before(@target,'/')" />/overview-summary.html</xsl:attribute>
                                    <xsl:attribute name="target">classFrame</xsl:attribute>
                                    <xsl:value-of select="@name"/>
                                </a> -
                                <!-- XXX the following is crap; e.g. messes up descs of Dialogs API, I/O API, ... -->
                                <!-- Should use e.g.:
                                <answer id="arch-what">
                                    <span class="summary">This API does such-and-such.</span>
                                    It also does some other less important stuff.
                                </answer>
                                -->
                                <xsl:comment>Begin of first sentenece</xsl:comment>
                                <xsl:apply-templates mode="first-sentence" select="description" />
                                <xsl:comment>End of first sentenece</xsl:comment>.
                            </span>
                        </li>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- will be covered later -->
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            <xsl:for-each select="/apis/module" >
                <xsl:sort select="api" order="descending" />
                <xsl:sort select="@name" />
                <xsl:choose>
                    <xsl:when test="api" >
                        <!-- covered before -->
                    </xsl:when>
                    <xsl:otherwise>
                        <li>
                            <xsl:variable name="where" select="substring-before(@target, '/')"/>
                            <b>
                                <a href="{$where}/overview-summary.html">
                                    <xsl:value-of select="$where"/>
                                </a>
                            </b>
                            - no API description provided
                            (see <a href="http://wiki.netbeans.org/APIDevelopment">how to do it</a>)
                        </li>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </ul>
    </xsl:template>

    <xsl:template match="module">
        <xsl:variable name="interfaces" select="descendant::api[@type='export' and generate-id() = generate-id(key('apiNames', @name))]" />
        <xsl:variable name="module.name" select="@name" />
        <xsl:variable name="arch.stylesheet" select="@stylesheet" />
        <xsl:variable name="arch.overviewlink" select="@overviewlink" />
        <xsl:variable name="arch.footer" select="@footer" />
        <xsl:variable name="arch.target" select="@target" />

        <xsl:if test="$interfaces">
            <h3>
                <a name="def-api-{$module.name}">
                    <xsl:value-of select="$module.name"/>
                </a>
            </h3>

                
            <a>
                <xsl:attribute name="href">
                    <xsl:call-template name="filedirapi" >
                        <xsl:with-param name="arch.target" select="$arch.target" />
                    </xsl:call-template>
                    <xsl:text>/index.html</xsl:text>
                </xsl:attribute>
                <xsl:text>javadoc</xsl:text>
            </a>
            <xsl:if test="$download = 'true'"> | <a>
                    <xsl:attribute name="href">
                        <xsl:call-template name="filedirapi" >
                            <xsl:with-param name="arch.target" select="$arch.target" />
                        </xsl:call-template>
                        <xsl:text>.zip</xsl:text>
                    </xsl:attribute>
                    <xsl:text>download</xsl:text>
                </a>
            </xsl:if>
            | <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="$arch.target" />
                </xsl:attribute>
                <xsl:text>architecture</xsl:text>
            </a> 
            <xsl:if test="//module[@name=$module.name]/arch-usecases" >
                | <a>
                    <xsl:attribute name="href">
                        <xsl:text>usecases.html#usecase-</xsl:text>
                        <xsl:value-of select="$module.name" />
                    </xsl:attribute>
                    <xsl:text>usecases</xsl:text>
                </a>
            </xsl:if>
            <p/>

            <div>
                <xsl:apply-templates select="description"/>
            </div>

            <xsl:if test="deploy-dependencies">
                <div>
                    <p>
                        <b>Usage:</b>
                    </p>
                    <xsl:apply-templates select="deploy-dependencies"/>
                </div>
            </xsl:if>

            <p/>
            <table cellpadding="1" cellspacing="0" border="0" class="tablebg" width="100%">
                <tr>
                    <td>
                        <table border="0" cellpadding="3" cellspacing="1" width="100%">
                            <!--                    <tr><td COLSPAN="5" class="tablecbg" ALIGN="CENTER"><font CLASS="titlectable">Do not duplicate any files</font></td></tr> -->
                            <tr class="tablersh">
                                <td align="CENTER" width="30%">
                                    <span class="titlectable">Interface Name</span>
                                </td>
                                <td align="CENTER" width="15%">
                                    <span class="titlectable">Stability Classification</span>
                                </td>
                                <td align="CENTER" >
                                    <span class="titlectable">Specified in What Document?</span>
                                </td>
                            </tr>

                            <xsl:for-each select="$interfaces">
                                <xsl:if test="@group='java'" >
                                    <xsl:call-template name="api" >
                                        <xsl:with-param name="arch.target" select="$arch.target" />
                                    </xsl:call-template>
                                </xsl:if>
                            </xsl:for-each>

                            <xsl:for-each select="//api[generate-id() = generate-id(key('apiGroups', @group))]">
                                <xsl:variable name="grp" select="@group" />
                                <xsl:if test="$grp!='java'" >
                                    <xsl:variable name="apis" select="/apis" />
                                    <xsl:variable name="module" select="$apis/module[@name=$module.name]" />

                                    <xsl:variable name="allOfTheGroup" select="$module/api[@group=$grp]" />
                                    <xsl:if test="$allOfTheGroup">
                                        <tr class="tabler">
                                            <td>Set of <xsl:value-of select="$grp"/> APIs</td>
                                            <td>Individual</td>
                                            <td>
                                                <a href="{$arch.target}#group-{$grp}">table with definitions</a>
                                            </td>
                                        </tr>
                                    </xsl:if>
                                </xsl:if>
                            </xsl:for-each>

                        </table>
                    </td>
                </tr>
            </table>
        </xsl:if>


        <P/>

    </xsl:template>

    <xsl:template name="api">
        <xsl:param name="arch.target" />
        <xsl:variable name="name" select="@name" />
        <xsl:variable name="type" select="@type" />
        <xsl:variable name="category" select="@category" />
        <xsl:variable name="url" select="@url" />
        <xsl:variable name="description" select="node()" />

        <tr class="tabler">
            <td>
                <a>
                    <xsl:attribute name="href" >
                        <xsl:value-of select="$arch.target" />
                        <xsl:text>#java-</xsl:text>
                        <xsl:value-of select="$name"/>
                    </xsl:attribute>
                    <xsl:value-of select="$name" />
                </a>
            </td>
            <!--
            <td>
                <xsl:choose>
                    <xsl:when test="$type='import'">Imported</xsl:when>
                    <xsl:when test="$type='export'">Exported</xsl:when>
                    <xsl:otherwise>WARNING: <xsl:value-of select="$type" /></xsl:otherwise>
                </xsl:choose>
            </td> -->
            <td> <!-- stability category -->
                <a>
                    <xsl:attribute name="href">
                        <xsl:text>http://wiki.netbeans.org/API_Stability#</xsl:text>
                        <xsl:value-of select="$category" />
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="$category='official'">Official</xsl:when>
                        <xsl:when test="$category='stable'">Stable</xsl:when>
                        <xsl:when test="$category='devel'">Devel</xsl:when>
                        <xsl:when test="$category='third'">Third_Party</xsl:when>
                        <xsl:when test="$category='standard'">Standard</xsl:when>
                        <xsl:when test="$category='friend'">Friend</xsl:when>
                        <xsl:when test="$category='private'">Private</xsl:when>
                        <xsl:when test="$category='deprecated'">Deprecated</xsl:when>
                        <xsl:otherwise>
                            <xsl:message>
                                WARNING: <xsl:value-of select="$category"/>
                            </xsl:message>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>  
            </td>

            <td> <!-- url -->
                <a href="{$url}">
                    <xsl:value-of select="$url"/>
                </a>
                <xsl:if test="$description" >
                    <p>
                        <xsl:apply-templates select="$description" />
                    </p>
                </xsl:if>
            </td>
        </tr>

    </xsl:template>

    <xsl:template match="api-ref">
        <!-- simply bold the name, it link will likely be visible bellow -->
        <b>
            <xsl:value-of select="@name" />
        </b>
    </xsl:template>

    <!-- extracts first part before slash from LoadersAPI/bleble.html -->

    <xsl:template name="filedirapi" >
        <xsl:param name="arch.target" />
    
        <xsl:if test="substring-before($arch.target,'/')">
            <xsl:value-of select="substring-before($arch.target,'/')" />
        </xsl:if>
    </xsl:template>


    <!-- Format random HTML elements as is: -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Gets the first sentence with HTML tags -->
    
    <xsl:template mode="first-sentence" match="api-ref">
        <b>
            <xsl:value-of select="@name" />
        </b>
        <xsl:text> </xsl:text>
    </xsl:template>

    <xsl:template mode="first-sentence" match="node()">
        <xsl:choose>
            <xsl:when test="count(child::*) = 0" >
                <xsl:variable name="first-sentence" select="substring-before(normalize-space(), '. ')" />
                <xsl:variable name="first-dot" select="substring-before(normalize-space(), '.')" />
                <xsl:choose>
                    <xsl:when test="$first-sentence" >
                        <xsl:value-of select="$first-sentence" />
                        <!-- this trick starts comment which disables output produces after 
                        Which means comments out everything after the .
                        -->
                        <xsl:text disable-output-escaping="yes">&lt;!--</xsl:text>
                    </xsl:when>
                    <xsl:when test="$first-dot" >
                        <xsl:value-of select="$first-dot" />
                        <!-- this trick starts comment which disables output produces after 
                        Which means comments out everything after the .
                        -->
                        <xsl:text disable-output-escaping="yes">&lt;!--</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="." />
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:apply-templates mode="first-sentence" select="child::*"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates mode="first-sentence" select="node()"/>
            </xsl:otherwise>
        </xsl:choose>
        
    </xsl:template>
    
</xsl:stylesheet>