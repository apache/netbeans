<?xml version="1.0" encoding="UTF-8"?>
<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" omit-xml-declaration="yes"/>
    <xsl:param name="cluster.name"/>
    <xsl:param name="filename"/>

    <xsl:template match="filesystem/folder[@name='Templates']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Templates</xsl:attribute>
            <xsl:apply-templates mode="project-wizard"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='OptionsDialog']">
        <xsl:element name="folder">
            <xsl:attribute name="name">OptionsDialog</xsl:attribute>
            <xsl:apply-templates mode="options"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="filesystem/folder[@name='org-netbeans-api-project-libraries']/folder[@name='Libraries']">
        <xsl:element name="folder">
            <xsl:attribute name="name">org-netbeans-api-project-libraries</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name">Libraries</xsl:attribute>
                <xsl:apply-templates mode="libraries"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Services']/folder[@name='MIMEResolver']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Services</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name">MIMEResolver</xsl:attribute>
                <xsl:apply-templates mode="mime-resolvers"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Debugger']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Debugger</xsl:attribute>
                <xsl:apply-templates mode="attach-types"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Servers']/file[attr/@stringvalue='org.netbeans.spi.server.ServerWizardProvider']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Servers</xsl:attribute>
                <xsl:apply-templates mode="common-server-types" select="."/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='J2EE']/folder[@name='DeploymentPlugins']
        /folder/file[attr/@stringvalue='org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Servers</xsl:attribute>
                <xsl:apply-templates mode="j2ee-server-types" select="."/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Servers']/folder[@name='Actions']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Servers</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name">Actions</xsl:attribute>
                <xsl:apply-templates mode="actions"/>
            </xsl:element>
        </xsl:element>
        <xsl:for-each select="file/attr[@name='originalFile']">
            <xsl:call-template name="actions-definition">
                <xsl:with-param name="originalFile" select="."/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="filesystem/folder[@name='Cloud']/file[attr/@stringvalue='org.netbeans.spi.server.ServerWizardProvider']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Cloud</xsl:attribute>
                <xsl:apply-templates mode="common-server-types" select="."/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Menu']/folder[@name='Profile']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Menu</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name">Profile</xsl:attribute>
                <xsl:apply-templates mode="actions" select="attr"/>
                <xsl:apply-templates mode="actions" select="file[attr[@name='ergonomics' and @boolvalue='true']]"/>
            </xsl:element>
        </xsl:element>
        <xsl:for-each select="file/attr[@name='originalFile' and ../attr[@name='ergonomics' and @boolvalue='true']]">
            <xsl:call-template name="actions-definition">
                <xsl:with-param name="originalFile" select="."/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Menu']/folder[@name='File']/folder[@name='Import']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Menu</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name">File</xsl:attribute>
                <xsl:element name="folder">
                    <xsl:attribute name="name">Import</xsl:attribute>
                    <xsl:apply-templates mode="actions" select="attr"/>
                    <xsl:apply-templates mode="actions" select="file[attr[@name='ergonomics' and @boolvalue='true']]"/>
                </xsl:element>
            </xsl:element>
        </xsl:element>
        <xsl:for-each select="file/attr[@name='originalFile' and ../attr[@name='ergonomics' and @boolvalue='true']]">
            <xsl:call-template name="actions-definition">
                <xsl:with-param name="originalFile" select="."/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Services']/folder[@name='AntBasedProjectTypes']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Ergonomics</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name">AntBasedProjectTypes</xsl:attribute>
                <xsl:apply-templates mode="project-types"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="filesystem/folder[@name='Services']/folder[@name='ProjectConvertors']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Services</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name">ProjectConvertors</xsl:attribute>
                <xsl:apply-templates mode="project-convertors"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Loaders']/folder/folder/folder[@name='Factories']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Loaders</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name"><xsl:value-of select="../../@name"/></xsl:attribute>
                <xsl:element name="folder">
                    <xsl:attribute name="name"><xsl:value-of select="../@name"/></xsl:attribute>
                    <xsl:element name="folder">
                        <xsl:attribute name="name">Factories</xsl:attribute>
                        <xsl:element name="file">
                            <xsl:attribute name="name"><xsl:value-of select="$filename"/>-ergonomics.instance</xsl:attribute>
                            <xsl:element name="attr">
                                <xsl:attribute name="name">instanceCreate</xsl:attribute>
                                <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.fod.FodDataObjectFactory.create</xsl:attribute>
                            </xsl:element>
                            <xsl:element name="attr">
                                <xsl:attribute name="name">position</xsl:attribute>
                                <xsl:attribute name="intvalue">
                                    <xsl:choose>
                                        <xsl:when test="file/attr[@name='position' and @intvalue]">
                                            <xsl:value-of select="1000000 + number(file/attr[@name='position']/@intvalue)"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:text>999999</xsl:text>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:attribute>
                            </xsl:element>
                        </xsl:element>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <!-- project wizard -->
    <xsl:template match="file" mode="project-wizard">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:apply-templates mode="project-wizard"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="folder" mode="project-wizard">
        <xsl:element name="folder">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:apply-templates mode="project-wizard"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@name='instantiatingIterator']" mode="project-wizard">
        <xsl:element name="attr">
            <xsl:attribute name="name">instantiatingIterator</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.api.Factory.newProject</xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@name='templateWizardIterator']" mode="project-wizard">
        <xsl:element name="attr">
            <xsl:attribute name="name">instantiatingIterator</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.api.Factory.newProject</xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@name='urlvalue']" mode="project-wizard">
        <xsl:element name="attr">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:call-template name="urlvalue">
                <xsl:with-param name="url" select="@urlvalue"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr" mode="project-wizard">
        <xsl:copy-of select="."/>
    </xsl:template>

    <!-- mime-resolvers -->
    <xsl:template match="file" mode="mime-resolvers">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:call-template name="url">
                <xsl:with-param name="url" select="@url"/>
            </xsl:call-template>
            <xsl:apply-templates mode="mime-resolvers"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr" mode="mime-resolvers">
        <xsl:copy-of select="."/>
    </xsl:template>

    <!-- libraries -->
    <xsl:template match="file" mode="libraries">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:call-template name="url">
                <xsl:with-param name="url" select="@url"/>
            </xsl:call-template>
            <xsl:apply-templates mode="libraries"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr" mode="libraries">
        <xsl:copy-of select="."/>
    </xsl:template>
    
    <!-- options -->
    <xsl:template match="file" mode="options">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:apply-templates mode="options"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="folder" mode="options">
        <xsl:element name="folder">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:apply-templates mode="options-adv"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@name='controller']" mode="options">
        <xsl:element name="attr">
            <xsl:attribute name="name">controller</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.fod.OptionCntrl.basic</xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr" mode="options">
        <xsl:copy-of select="."/>
    </xsl:template>
    <xsl:template match="file" mode="options-adv">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:apply-templates mode="options-adv"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="folder" mode="options-adv">
    </xsl:template>
    <xsl:template match="attr[@name='controller']" mode="options-adv">
        <xsl:element name="attr">
            <xsl:attribute name="name">controller</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.fod.OptionCntrl.advanced</xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr" mode="options-adv">
        <xsl:copy-of select="."/>
    </xsl:template>

    <!-- attach type -->
    <xsl:template match="file" mode="attach-types">
        <xsl:if test="attr[@stringvalue='org.netbeans.spi.debugger.ui.AttachType']">
            <xsl:element name="file">
                <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                <xsl:call-template name="url">
                    <xsl:with-param name="url" select="@url"/>
                </xsl:call-template>
                <xsl:apply-templates mode="attach-types"/>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template match="attr[@name='instanceCreate']" mode="attach-types">
        <xsl:element name="attr">
            <xsl:attribute name="name">instanceCreate</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.debugger.AttachTypeProxy.create</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr" mode="attach-types">
        <xsl:copy-of select="."/>
    </xsl:template>

    <!-- server type -->
    <xsl:template match="file" mode="common-server-types">
        <xsl:if test="attr[@name='displayName']">
            <xsl:element name="file">
                <xsl:attribute name="name">WizardProvider-<xsl:value-of select="@name"/></xsl:attribute>
                <attr name="instanceCreate" methodvalue="org.netbeans.modules.ide.ergonomics.ServerWizardProviderProxy.create"/>
                <attr name="instanceClass" stringvalue="org.netbeans.modules.ide.ergonomics.ServerWizardProviderProxy"/>
                <attr name="instanceOf" stringvalue="org.netbeans.spi.server.ServerWizardProvider"/>
                <attr name="originalDefinition">
                    <xsl:attribute name="stringvalue">
                        <xsl:call-template name="fullpath">
                            <xsl:with-param name="file" select="."/>
                        </xsl:call-template>
                    </xsl:attribute>
                </attr>
                <xsl:apply-templates select="attr[@name='displayName']" mode="j2ee-server-types"/>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template match="file" mode="j2ee-server-types">
        <xsl:if test="attr[@name='displayName']">
            <xsl:element name="file">
                <xsl:attribute name="name">J2eeWizardProvider-<xsl:value-of select="../@name"/>.instance</xsl:attribute>
                <attr name="instanceCreate" methodvalue="org.netbeans.modules.ide.ergonomics.ServerWizardProviderProxy.create"/>
                <attr name="instanceClass" stringvalue="org.netbeans.modules.ide.ergonomics.ServerWizardProviderProxy"/>
                <attr name="instanceOf" stringvalue="org.netbeans.spi.server.ServerWizardProvider"/>
                <attr name="originalDefinition">
                    <xsl:attribute name="stringvalue">
                        <xsl:call-template name="fullpath">
                            <xsl:with-param name="file" select="."/>
                        </xsl:call-template>
                    </xsl:attribute>
                </attr>
                <xsl:apply-templates select="attr[@name='displayName']" mode="j2ee-server-types"/>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template match="attr" mode="j2ee-server-types">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template name="fullpath" mode="j2ee-server-types">
        <xsl:param name="file"/>
        <xsl:if test="$file/../@name">
            <xsl:call-template name="fullpath">
                <xsl:with-param name="file" select="$file/.."/>
            </xsl:call-template>
            <xsl:text>/</xsl:text>
        </xsl:if>
        <xsl:value-of select="$file/@name"/>
    </xsl:template>

    <!-- actions -->
    <xsl:template match="file" mode="actions">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:call-template name="url">
                <xsl:with-param name="url" select="@url"/>
            </xsl:call-template>
            <xsl:apply-templates mode="actions"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr[@name='delegate']" mode="actions">
        <xsl:element name="attr">
            <xsl:attribute name="name">delegate</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.fod.FeatureAction.create</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr" mode="actions">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template name="actions-definition">
        <xsl:param name="originalFile"/>
        <xsl:for-each select=".">
            <xsl:call-template name="actions-definition-impl">
                <xsl:with-param name="path" select="$originalFile/@stringvalue"/>
                <xsl:with-param name="query" select="'filesystem'"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="actions-definition-impl">
        <xsl:param name="path"/>
        <xsl:param name="query"/>
        <xsl:variable name="category" select="substring-before($path,'/')"/>
        <xsl:choose>
            <xsl:when test="$category">
                <xsl:element name="folder">
                    <xsl:attribute name="name">
                        <xsl:value-of select="$category"/>
                    </xsl:attribute>
                    <xsl:call-template name="actions-definition-impl">
                        <xsl:with-param name="path" select="substring-after($path,'/')"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates
                    select="//filesystem/folder[@name='Actions']/descendant::file[@name=$path]"
                    mode="actions"
                />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- project type -->
    <xsl:template match="file" mode="project-types">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:call-template name="url">
                <xsl:with-param name="url" select="@url"/>
            </xsl:call-template>
            <xsl:apply-templates mode="project-types"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr[@name='instanceCreate']" mode="project-types">
        <xsl:element name="attr">
            <xsl:attribute name="name">instanceCreate</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.fod.FeatureProjectFactory.create</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr[@name='instanceClass']" mode="project-types">
        <xsl:element name="attr">
            <xsl:attribute name="name">instanceClass</xsl:attribute>
            <xsl:attribute name="stringvalue">org.netbeans.spi.project.ProjectFactory</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr" mode="project-types">
        <xsl:copy-of select="."/>
    </xsl:template>
    
    <!-- project convertor -->
    <xsl:template match="file" mode="project-convertors">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:call-template name="url">
                <xsl:with-param name="url" select="@url"/>
            </xsl:call-template>
            <xsl:apply-templates mode="project-convertors"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr[@name='delegate']" mode="project-convertors">
        <xsl:element name="attr">
            <xsl:attribute name="name">delegate</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.fod.FeatureProjectConvertor.create</xsl:attribute>
        </xsl:element>
        <xsl:text>
</xsl:text>
        <xsl:element name="attr">
            <xsl:attribute name="name">fod</xsl:attribute>
            <xsl:if test="./@methodvalue">
                <xsl:attribute name="methodvalue"><xsl:value-of select="./@methodvalue"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="./@newvalue">
                <xsl:attribute name="newvalue"><xsl:value-of select="./@newvalue"/></xsl:attribute>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr" mode="project-convertors">
        <xsl:copy-of select="."/>
    </xsl:template>
    
    <!-- convert relative URLs to absolute -->
    <xsl:template name="url">
        <xsl:param name="url"/>

        <xsl:choose>
            <xsl:when test="not($url)"/>
            <xsl:when test="contains($url,':')">
                <xsl:attribute name="url">
                    <xsl:value-of select="$url"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="starts-with($url,'/')">
                <xsl:attribute name="url">
                    <xsl:value-of select="$url"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="prefix" select="substring-before($filename, '.xml')"/>
                <xsl:attribute name="url">
                    <xsl:text>nbresloc:/</xsl:text>
                    <xsl:value-of select="translate($prefix, '.', '/')"/>
                    <xsl:text>/</xsl:text>
                    <xsl:value-of select="$url"/>
                </xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="urlvalue">
        <xsl:param name="url"/>

        <xsl:choose>
            <xsl:when test="not($url)"/>
            <xsl:when test="contains($url,'/')">
                <xsl:attribute name="urlvalue">
                    <xsl:value-of select="$url"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="prefix" select="substring-before($filename, '.xml')"/>
                <xsl:attribute name="urlvalue">
                    <xsl:text>ergoloc:/</xsl:text>
                    <xsl:value-of select="translate($prefix, '.', '/')"/>
                    <xsl:text>/</xsl:text>
                    <xsl:value-of select="$url"/>
                </xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
