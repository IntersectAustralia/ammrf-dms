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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.xml.sax.InputSource;

import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.util.DateFormatter;

/**
 * Dataset.
 * 
 * @version $Rev: 5 $
 * 
 */
@RooJavaBean
@RooToString
@JsonIgnoreProperties("schemas")
public class Dataset
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Dataset.class);

    private String id;
    private String url;
    private String owner;
    private String creationDate;
    private boolean visible;
    private List<MetadataItem> metadata;
    private Long projectCode;

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = DateFormatter.formatDate2EST(creationDate);
    }
    
    public String getEncodedURL() throws UnsupportedEncodingException 
    {
        return URLEncoder.encode(url, "UTF-8");
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public void setMetadata(MetadataSchema schema, String metadata)
    {
        for (MetadataItem metadataItem : this.metadata)
        {
            if (schema == metadataItem.getSchema())
            {
                metadataItem.setMetadata(metadata);
                return;
            }
        }
        throw new IllegalArgumentException("No metadata for schema " + schema + " found in this dataset (ID:" + id
                + ").");
    }

    public String getMetadata(MetadataSchema schema)
    {
        if (this.metadata != null)
        {
            for (MetadataItem metadataItem : this.metadata)
            {
                if (schema == metadataItem.getSchema())
                {
                    return metadataItem.getMetadata();
                }
            }
        }
        throw new IllegalArgumentException("No metadata for schema " + schema + " found in this dataset (ID:" + id
                + ").");
    }

    public Set<MetadataSchema> getSchemas()
    {
        Set<MetadataSchema> datasetSchemas = new HashSet<MetadataSchema>();
        for (MetadataItem metadataItem : metadata)
        {
            datasetSchemas.add(metadataItem.getSchema());
        }
        return datasetSchemas;
    }

    public String getProjectTitle()
    {
        String projectTitle = "";
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceContext()
        {

            @Override
            public Iterator getPrefixes(String namespaceURI)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getPrefix(String namespaceURI)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getNamespaceURI(String prefix)
            {
                if ("rif-cs".equals(prefix))
                {
                    return "http://ands.org.au/standards/rif-cs/registryObjects";
                }
                else
                {
                    return XMLConstants.NULL_NS_URI;
                }
            }
        });

        String xpathExpression = "//rif-cs:collection/rif-cs:name[@type='primary']/rif-cs:namePart/text()";
        try
        {
            String rifCS = getMetadata(MetadataSchema.RIF_CS);
            InputStream is = new ByteArrayInputStream(rifCS.getBytes());
            InputSource inputSource = new InputSource(is);
            projectTitle = xpath.evaluate(xpathExpression, inputSource);
            LOGGER.debug("Project title for dataset ID: {} is <{}>", id, projectTitle);
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error("Couldn't extract project title from RIF-CS. Dataset ID <{}>, exception:\n{}", id, e);
        }
        catch (IllegalArgumentException e)
        {
            if (this.metadata != null)
            {
                LOGGER.error("Couldn't extract project title Dataset ID <{}> as it does't have RIF-CS", id);
            }
        }
        return projectTitle;
    }

}
