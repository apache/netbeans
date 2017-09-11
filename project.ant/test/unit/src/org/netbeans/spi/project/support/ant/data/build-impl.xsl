<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:test="urn:test:shared"
                exclude-result-prefixes="xalan p test">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">
    
<xsl:variable name="name" select="/p:project/p:configuration/test:data/test:name"/>
<xsl:variable name="displayname" select="/p:project/p:configuration/test:data/test:display-name"/>
<project name="{$name}.impl" default="all" basedir="..">

    <description><xsl:value-of select="$displayname"/></description>

    <target name="all"/>
    
    <xsl:if test="/p:project/p:configuration/test:data/test:shared-stuff">
        <target name="x" description="shared-stuff was defined"/>
    </xsl:if>

</project>

    </xsl:template>
    
</xsl:stylesheet>
