<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/TR/xhtml1/strict">

    <xsl:strip-space elements="doc chapter section"/>
    <xsl:output
        method="text"
        indent="no"
        encoding="iso-8859-1"
    />

    <xsl:template match="doc">
        TITLE: <xsl:value-of select="title"/>
        =====
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="doc/title">
        # <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="chapter/title">
        * <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="section/title">
        - <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="para">
        <xsl:apply-templates/>
        -----
    </xsl:template>

    <xsl:template match="note">
        [NOTE: <xsl:apply-templates/>]
    </xsl:template>

    <xsl:template match="emph">
        (<xsl:apply-templates/>)
    </xsl:template>

</xsl:stylesheet>
