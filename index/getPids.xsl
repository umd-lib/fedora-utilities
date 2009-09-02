<?xml version="1.0"?>

<xsl:stylesheet version="1.1" exclude-result-prefixes="search"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:search="http://www.fedora.info/definitions/1/0/types/">

   <xsl:output method="text"/>

   <xsl:variable name="newline">
      <xsl:text>&#10;</xsl:text>
   </xsl:variable>

   <xsl:template match="/">
      <xsl:for-each select="search:result/search:resultList/search:objectFields/search:pid">
         <xsl:value-of select="concat(.,$newline)"/>
      </xsl:for-each>
   </xsl:template>

</xsl:stylesheet>
