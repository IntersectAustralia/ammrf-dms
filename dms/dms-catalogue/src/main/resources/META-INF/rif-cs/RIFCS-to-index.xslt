<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:o="http://ands.org.au/standards/rif-cs/registryObjects">
    
<xsl:output indent="yes" encoding="UTF-8"/>
<xsl:variable name="mapping" select="document('rif_cs-mapping.xml')/mapping"/>

<xsl:template match="/o:registryObjects/o:registryObject">
<document>
	<xsl:call-template name="field">
			<xsl:with-param name="name">originatingSource</xsl:with-param>
			<xsl:with-param name="value" select="//o:originatingSource/text()" />
	</xsl:call-template>
	<xsl:call-template name="field">
			<xsl:with-param name="name">name</xsl:with-param>
			<xsl:with-param name="value" select="//o:collection/o:name[@type='primary']/o:namePart[@type='full']/text()" />
	</xsl:call-template>
	<xsl:call-template name="field">
			<xsl:with-param name="name">description</xsl:with-param>
			<xsl:with-param name="value" select="//o:collection/o:description[@type='about']/text()" />
	</xsl:call-template>
	<xsl:call-template name="field">
			<xsl:with-param name="name">url</xsl:with-param>
			<xsl:with-param name="value" select="//o:collection/o:location/o:address/o:electronic[@type='url']/o:value/text()" />
	</xsl:call-template>
	<xsl:call-template name="field">
			<xsl:with-param name="name">physicalAddress</xsl:with-param>
			<xsl:with-param name="value" select="//o:collection/o:location/o:address/o:physical/o:addressPart[@type='text']/text()" />
	</xsl:call-template>
</document>
</xsl:template>

<xsl:template name="field">
	<xsl:param name="name" />
	<xsl:param name="value" />
	<field name="{$mapping/property[@xml-field=concat($name, '')]/@index-field}" value="{$value}" type="{$mapping/property[@xml-field=concat($name, '')]/@type}"/>
</xsl:template>

</xsl:stylesheet>
