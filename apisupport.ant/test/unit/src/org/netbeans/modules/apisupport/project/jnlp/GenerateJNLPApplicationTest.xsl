<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>

    <xsl:param name="file"/>
    
    <xsl:template match="module_version">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <file crc="12345">
                <xsl:attribute name="name">
                    <xsl:value-of select="$file"/>
                </xsl:attribute>    
            </file>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
