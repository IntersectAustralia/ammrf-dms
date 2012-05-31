/**
 * Project: Platforms for Collaboration at the AMMRF
 *
 * Copyright (c) Intersect Pty Ltd, 2011
 *
 * @see http://www.ammrf.org.au
 * @see http://www.intersect.org.au
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * This program contains open source third party libraries from a number of
 * sources, please read the THIRD_PARTY.txt file for more details.
 */

package au.org.intersect.dms.catalogue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import au.org.intersect.dms.core.catalogue.MetadataSchema;

/**
 * Converts Xml into different formats
 * 
 * @version $Rev: 5 $
 * 
 */
public class MetadataXmlConverter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataXmlConverter.class);
    
    /**
     * Conversion destination formats
     */
    public static enum Format
    {
        HTML, INDEX
    }

    /**
     * Parameters for conversion process. Implemented through Builder pattern.
     */
    public static class ConversionParams
    {
        private MetadataSchema schema;
        private Format destinationFormat;
        private String metadata;
        private boolean isEditMode;
        private Map<String, Object> extraParams = new HashMap<String, Object>();

        public ConversionParams schema(MetadataSchema schema)
        {
            this.schema = schema;
            return this;
        }

        public ConversionParams desinationFormat(Format destinationFormat)
        {
            this.destinationFormat = destinationFormat;
            return this;
        }

        public ConversionParams metadata(String metadata)
        {
            this.metadata = metadata;
            return this;
        }

        public ConversionParams edit(boolean isEditMode)
        {
            this.isEditMode = isEditMode;
            return this;
        }

        public ConversionParams extraParam(String name, Object value)
        {
            extraParams.put(name, value);
            return this;
        }
    }

    @Autowired
    private ResourceLoader resourceLoader;

    private Map<MetadataSchema, Map<Format, String>> mapping = new HashMap<MetadataSchema, Map<Format, String>>();

    @Required
    public void setMapping(Map<MetadataSchema, Map<Format, String>> mapping)
    {
        this.mapping = mapping;
    }

    /**
     * Converts metadata of supported schemas into supported formats
     * 
     * @param params
     *            conversion parameters
     * @return
     * @throws IOException
     * @throws TransformerException
     */
    public String convert(ConversionParams params) throws IOException, TransformerException
    {
        String xslt = getXslt(params.schema, params.destinationFormat);
        if (xslt == null)
        {
            throw new IllegalArgumentException("Mapping from " + params.schema + " to " + params.destinationFormat
                    + " is not supported.");
        }

        TransformerFactory xsltFactory = TransformerFactory.newInstance();
        final Resource xsltResource = resourceLoader.getResource(xslt);
        Transformer transformer = xsltFactory.newTransformer(new StreamSource(xsltResource
                .getInputStream()));
        transformer.setParameter("editMode", params.isEditMode);
        final String baseURI = xsltResource.getURI().toString();
        transformer.setURIResolver(new URIResolver()
        {
            
            @Override
            public Source resolve(String href, String base) throws TransformerException
            {
                StringBuilder resolvedPath = new StringBuilder();
                resolvedPath.append(baseURI.substring(0, baseURI.lastIndexOf("/") + 1)).append(href);
                LOGGER.debug("Relative path [{}] was resolved to [{}]", href, resolvedPath);
                return new StreamSource(resolvedPath.toString());
            }
        });
        if (params.extraParams != null)
        {
            for (Entry<String, Object> parameter : params.extraParams.entrySet())
            {
                transformer.setParameter(parameter.getKey(), parameter.getValue());
            }
        }

        StringResult result = new StringResult();
        transformer.transform(new StringSource(params.metadata), result);

        return result.toString();
    }

    /**
     * Returns true if conversion for schema and destination format is supported and false otherwise
     * 
     * @param schema
     * @param destinationFormat
     * @return
     */
    public boolean isConvertionSupported(MetadataSchema schema, Format destinationFormat)
    {
        String xslt = getXslt(schema, destinationFormat);
        return xslt != null ? true : false;
    }

    private String getXslt(MetadataSchema schema, Format destinationFormat)
    {
        Map<Format, String> xslt = mapping.get(schema);
        if (xslt != null)
        {
            return xslt.get(destinationFormat);
        }
        else
        {
            return null;
        }
    }
}
