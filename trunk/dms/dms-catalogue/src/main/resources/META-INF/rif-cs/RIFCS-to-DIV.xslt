<xsl:stylesheet version="1.0"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:o="http://ands.org.au/standards/rif-cs/registryObjects">

<xsl:output method="html" />
<!-- non-empty editMode will trigger textarea generation for fields -->
<xsl:param name="editMode" />
<xsl:param name="owner" />
<xsl:param name="date" />
<xsl:template match="/">
	<style>
#content-container
{
	padding: 2px 18px 8px 8px; /* top right bottom left */	
}

.research_title {
color:#774E26;
}
	
.recordTable
{
	background: transparent;
	border: none;
}
.recordTable td
{
	padding: 4px;
	border: none;
	vertical-align: top;
}

.recordTable > .recordFields td
{
	border-top: 1px solid #dddddd;
	border-bottom: 1px solid #dddddd;
}

.recordTable > .recordFields > tr > td:first-child
{
	padding-top: 6px;
	text-align: right;
	font-size: 0.8em;
	font-style: italic;
	font-weight: bold;
	white-space: nowrap;
}

thead td
{
	font-family: arial, sans-serif;
	font-size: 1.3em;
	font-weight: bold;
	font-style: normal;
	padding: 4px;
	border: none;
	border-bottom: 1px solid #dddddd;
}

thead th
{
	text-align: left;
	padding: 4px;
	border: none;
	border-bottom: 1px solid #dddddd;
}

tbody td
{
	border-left: 1px solid #dddddd;
}

tbody th
{
	border-left: 1px solid #dddddd;
}

img
{
	border: 0px;
}

h1 {
color:#283F09;
font-family:arial,sans-serif;
font-size:1.4em;
font-weight:bold;
margin:0.2em 0;
}

	</style>
    
<div id="content-container">
<!-- BEGIN CONTENT -->
<table summary="Registry Object" class="recordTable">
	<tbody class="recordFields">
		<tr>
			<td>Owner:
			</td>
			<td><xsl:value-of select="$owner"/>
			</td>
		</tr>
		<tr>
			<td>Ingested:
			</td>
			<td><xsl:value-of select="$date"/>
			</td>
		</tr>

<!-- NAMES-->
		<xsl:call-template name="field">
			<xsl:with-param name="label">Collection</xsl:with-param>
			<xsl:with-param name="variableName">md_title</xsl:with-param>
			<xsl:with-param name="fieldType">textarea</xsl:with-param>
			<xsl:with-param name="content"><xsl:value-of select="//o:registryObjects/o:registryObject/o:collection/o:name[@type='primary']/o:namePart/text()"/></xsl:with-param>
		</xsl:call-template>
		<xsl:call-template name="field">
			<xsl:with-param name="label">Type</xsl:with-param>
			<xsl:with-param name="content"><xsl:value-of select="//o:registryObjects/o:registryObject/o:collection/@type"/></xsl:with-param>
		</xsl:call-template>
		<xsl:call-template name="field">
			<xsl:with-param name="label">About</xsl:with-param>
			<xsl:with-param name="variableName">md_description</xsl:with-param>
			<xsl:with-param name="fieldType">textarea</xsl:with-param>
			<xsl:with-param name="content"><xsl:value-of select="//o:registryObjects/o:registryObject/o:collection/o:description[@type='about']/text()"/></xsl:with-param>
		</xsl:call-template>
		<xsl:call-template name="field">
			<xsl:with-param name="label">Url</xsl:with-param>
			<xsl:with-param name="content"><xsl:value-of select="//o:registryObjects/o:registryObject/o:collection/o:location/o:address/o:electronic[@type='url']/*/text()"/></xsl:with-param>
		</xsl:call-template>
		<xsl:call-template name="field">
			<xsl:with-param name="label">Address</xsl:with-param>
			<xsl:with-param name="variableName">md_address</xsl:with-param>
			<xsl:with-param name="fieldType">textarea</xsl:with-param>
			<xsl:with-param name="content"><xsl:value-of select="//o:registryObjects/o:registryObject/o:collection/o:location/o:address/o:physical/*/text()"/></xsl:with-param>
		</xsl:call-template>
		<xsl:call-template name="field">
			<xsl:with-param name="label">Rights</xsl:with-param>
			<xsl:with-param name="variableName">md_rights</xsl:with-param>
			<xsl:with-param name="fieldType">textarea</xsl:with-param>
			<xsl:with-param name="content"><xsl:value-of select="//o:registryObjects/o:registryObject/o:collection/o:description[@type='rights']/text()"/></xsl:with-param>
		</xsl:call-template>
	</tbody>
</table>
</div>

</xsl:template>

<!-- 
	label : always provided
	variableName : if provided and editMode, display form field; if not provided, variable is display only
	content : the value of the variable
	fieldType : either textarea or input (not required if variableName is not present)
 -->
<xsl:template name="field">
	<xsl:param name="variableName"/>
	<xsl:param name="label"/>
	<xsl:param name="content"/>
	<xsl:param name="fieldType"/>
		<tr>
			<td><xsl:value-of select="$label"/></td>
			<td>
			<xsl:choose>
			<xsl:when test="$editMode and $variableName">
				<xsl:choose>
					<!-- textarea name=$variableName. $content /textarea. -->
					<xsl:when test="$fieldType='textarea'">
						<xsl:element name="textarea">
							<xsl:attribute name="name">
								<xsl:value-of select="$variableName"/>
							</xsl:attribute>
						<xsl:value-of select="$content"/>
						</xsl:element>
					</xsl:when>
					<xsl:otherwise>
						<!-- input type=text name=$variableName value=$content /. -->
						<xsl:element name="input">
							<xsl:attribute name="name">
								<xsl:value-of select="$variableName"/>
							</xsl:attribute>
							<xsl:attribute name="type">text</xsl:attribute>
							<xsl:attribute name="value">
								<xsl:value-of select="$content"/>
							</xsl:attribute>
						</xsl:element>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$content"/>
			</xsl:otherwise>
			</xsl:choose>
			</td>
		</tr>
</xsl:template>

<xsl:template match="text()|@*">
</xsl:template>

<!--
<xsl:template match="*">
	<xsl:apply-templates/>
</xsl:template>
-->

</xsl:stylesheet>
