<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:i="http://www.acmm.sydney.edu.au/schemata/atomprobe">

<xsl:output indent="yes" encoding="UTF-8"/>
<xsl:variable name="mapping" select="document('atom_probe-mapping.xml')/mapping"/>

<xsl:template match="/">
<document>
<xsl:for-each select="//i:atomProbe/i:*">
		<xsl:call-template name="section">
			<xsl:with-param name="section"><xsl:value-of select="node()"/></xsl:with-param>
		</xsl:call-template>
	</xsl:for-each>
</document>
</xsl:template>

<xsl:template name="section">
	<xsl:param name="section" />
	<xsl:for-each select="./i:property">
		<xsl:call-template name="property">
			<xsl:with-param name="property"><xsl:value-of select="node()"/></xsl:with-param>
		</xsl:call-template>
	</xsl:for-each>
	
</xsl:template>

<xsl:template name="property">
	<xsl:param name="property" />
	<xsl:variable name="property-name"><xsl:value-of select="name(..)"/>/<xsl:value-of select="@name"/></xsl:variable>
	<xsl:variable name="property-mapping" select="$mapping/property[@xml-field=concat($property-name, '')]"/>
	<xsl:if test="$property-mapping/@xml-field != ''">
		<field name="{$property-mapping/@index-field}" value="{@value}" type="{$property-mapping/@type}"/>
	</xsl:if>
</xsl:template>

</xsl:stylesheet>
