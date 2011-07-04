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

package au.org.intersect.dms.catalogue.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.xml.transform.StringSource;

import au.org.intersect.dms.catalogue.MetadataXmlConverter;
import au.org.intersect.dms.catalogue.MetadataXmlConverter.ConversionParams;
import au.org.intersect.dms.catalogue.MetadataXmlConverter.Format;
import au.org.intersect.dms.integration.search.Document;
import au.org.intersect.dms.integration.search.Field;
import au.org.intersect.dms.integration.search.FieldType;

/**
 * Indexes Datasets in the Solr
 */
public class DatasetIndexer
{
    private static final String INDEX_EXCEPTION_MESSAGE = "Exception during indexing";

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetIndexer.class);
    
    private static final String SPACE = " ";
    
    @Autowired
    private MetadataXmlConverter metadataXmlConverter;

    @Qualifier("searchUnmarshaller")
    @Autowired
    private Unmarshaller unmarshaller;
    
    public void indexDatasets(Collection<DbDataset> datasets)
    {
        List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
        for (DbDataset dataset : datasets)
        {
            LOGGER.debug("Indexing dataset with ID {}", dataset.getId());
            SolrInputDocument sid = new SolrInputDocument();
            addBasicFields(dataset, sid);
            StringBuilder summary = new StringBuilder();
            summary.append(dataset.getCreationDate()).append(SPACE).append(dataset.getId()).append(SPACE)
                    .append(dataset.getOwner()).append(SPACE).append(dataset.getUrl());

            StringBuilder metadata = new StringBuilder();
            for (DbMetadataItem metadataItem : dataset.getMetadata())
            {
                if (metadataXmlConverter.isConvertionSupported(metadataItem.getMetadataSchema(), Format.INDEX))
                {
                    ConversionParams conversionParams = new ConversionParams();
                    conversionParams.schema(metadataItem.getMetadataSchema()).desinationFormat(Format.INDEX)
                            .metadata(metadataItem.getMetadata());

                    String text = metadataXmlConverter.convert(conversionParams);
                    List<Field> fields = extractFields(text);
                    for (Field field : fields)
                    {
                        LOGGER.debug("Adding index field <{}> with value <{}>", field.getName(), field.getValue());
                        sid.addField(field.getName(), field.getValue());
                        if (FieldType.TEXT == field.getType())
                        {
                            metadata.append(field.getValue()).append(" ");
                        }
                    }
                }
            }

            if (metadata.length() > 0)
            {
                sid.addField("dataset.metadata_t", metadata.toString());
                summary.append(SPACE).append(metadata);
            }

            // Add summary field to allow searching documents for objects of this type
            sid.addField("dataset_solrsummary_t", summary);

            documents.add(sid);
        }

        try
        {
            SolrServer solrServer = DbDataset.solrServer();
            solrServer.add(documents);
            solrServer.commit();
        }
        catch (SolrServerException e)
        {
            LOGGER.error(INDEX_EXCEPTION_MESSAGE, e);
        }
        catch (IOException e)
        {
            LOGGER.error(INDEX_EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Adds basic dataset's fields into solr index
     * 
     * @param dataset input dataset
     * @param solrInputDocument solr document to add fields
     */
    private void addBasicFields(DbDataset dataset, SolrInputDocument solrInputDocument)
    {
        solrInputDocument.addField("id", "dataset_" + dataset.getId());
        solrInputDocument.addField("dataset.creationdate_dt", dataset.getCreationDate());
        solrInputDocument.addField("dataset.id_l", dataset.getId());
        solrInputDocument.addField("dataset.owner_s", dataset.getOwner());
        solrInputDocument.addField("dataset.project_l", dataset.getProjectCode()); //TODO check if null is OK
        solrInputDocument.addField("dataset.url_s", dataset.getUrl());
    }


    private List<Field> extractFields(String xml)
    {
        LOGGER.trace("Parsing xml\n{}", xml);
        List<Field> fields = new LinkedList<Field>();
        try
        {
            Document document = (Document) unmarshaller.unmarshal(new StringSource(xml));
            fields = document.getField();
        }
        catch (XmlMappingException e)
        {
            LOGGER.error(INDEX_EXCEPTION_MESSAGE, e);
        }
        catch (IOException e)
        {
            LOGGER.error(INDEX_EXCEPTION_MESSAGE, e);
        }
        return fields;
    }
}
