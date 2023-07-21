<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:boomi="http://boomi.com/custom-function">
    <!--searching SB string which indicates that PO number is found .-->
    <xsl:output method="text"  indent="yes" />
    
    <xsl:template match="@*|node()">
            <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <!-- SB123456 last found stands-->
    <xsl:template match=".[matches(.,'^SB\d{6}$')]">
        <xsl:variable name="PONumber" select="boomi:set-ddp('DDP_PO_LOCAL', current())"/>
        <xsl:value-of select="$PONumber" />
    </xsl:template>
    
</xsl:stylesheet>
