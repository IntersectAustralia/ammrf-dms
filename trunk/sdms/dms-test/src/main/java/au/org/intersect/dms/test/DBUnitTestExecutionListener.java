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

package au.org.intersect.dms.test;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.util.StringUtils;

/**
 * A Spring @{TestExecutionListener} used to integrate the functionality of DBUnit.
 * 
 */
public class DBUnitTestExecutionListener extends TransactionalTestExecutionListener
{

    private static final String PATH_SEPARATOR = "/";

    private static final Logger LOGGER = LoggerFactory.getLogger(DBUnitTestExecutionListener.class);

    private static final String DEFAULT_DATASOURCE_NAME = "dataSource";
    private static final String[] TABLE_TYPES = {"TABLE", "ALIAS"};

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception
    {

        super.beforeTestMethod(testContext);

        DataSource dataSource = getDataSource(testContext);
        Connection conn = DataSourceUtils.getConnection(dataSource);
        IDatabaseConnection dbUnitConn = getDBUnitConnection(conn);
        try
        {
            IDataSet[] dataSets = getDataSets(testContext);
            for (IDataSet dataSet : dataSets)
            {
                DatabaseOperation.CLEAN_INSERT.execute(dbUnitConn, dataSet);
                LOGGER.info("Performed CLEAN_INSERT of IDataSet.");
            }
        }
        finally
        {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private DataSource getDataSource(TestContext context) throws Exception
    {
        DataSource dataSource;
        Map<String, DataSource> beans = context.getApplicationContext().getBeansOfType(DataSource.class);
        if (beans.size() > 1)
        {
            dataSource = (DataSource) beans.get(DEFAULT_DATASOURCE_NAME);
            if (dataSource == null)
            {
                throw new NoSuchBeanDefinitionException("Unable to locate default data source.");
            }
        }
        else
        {
            dataSource = (DataSource) beans.values().iterator().next();
        }
        return dataSource;
    }

    private IDatabaseConnection getDBUnitConnection(Connection c) throws DatabaseUnitException
    {
        IDatabaseConnection conn = new DatabaseConnection(c);
        DatabaseConfig config = conn.getConfig();
        // config.setProperty("http://www.dbunit.org/features/qualifiedTableNames", true);
        config.setProperty("http://www.dbunit.org/properties/tableType", TABLE_TYPES);
        config.setProperty("http://www.dbunit.org/features/caseSensitiveTableNames", true);
        return conn;
    }

    private IDataSet[] getDataSets(TestContext context) throws Exception
    {
        String[] dataFiles = getDataLocations(context);
        IDataSet[] dataSets = new IDataSet[dataFiles.length];
        for (int i = 0; i < dataFiles.length; i++)
        {
            Resource resource = new ClassPathResource(dataFiles[i]);
            Class<?> clazz = getDataSetType(context);
            Constructor<?> con = clazz.getConstructor(InputStream.class);
            dataSets[i] = (IDataSet) con.newInstance(resource.getInputStream());
        }
        return dataSets;
    }

    protected Class<?> getDataSetType(TestContext context)
    {
        Class<?> testClass = context.getTestClass();
        DBUnitConfiguration config = testClass.getAnnotation(DBUnitConfiguration.class);
        return config.type();
    }

    private String[] getDataLocations(TestContext context)
    {
        Class<?> testClass = context.getTestClass();
        DBUnitConfiguration config = testClass.getAnnotation(DBUnitConfiguration.class);
        if (config == null)
        {
            throw new IllegalStateException(
                    "Test class '" + testClass + "' is missing @DBUnitConfiguration annotation."
            );
        }
        if (config.locations().length == 0)
        {
            // no locations, let's try with the name of the test
            String tempDsRes = context.getTestInstance().getClass().getName();
            tempDsRes = StringUtils.replace(tempDsRes, ".", PATH_SEPARATOR);
            tempDsRes = PATH_SEPARATOR + tempDsRes + "-dataset.xml";
            if (getClass().getResource(tempDsRes) != null)
            {
                LOGGER.info("detected default dataset: {}", tempDsRes);
                return new String[] {tempDsRes};
            }
            else
            {
                LOGGER.info("no default dataset");
                return config.locations();
            }
        }
        else
        {
            String[] locs = config.locations();
            String tempDsRes = context.getTestInstance().getClass().getPackage().getName();
            tempDsRes = PATH_SEPARATOR + StringUtils.replace(tempDsRes, ".", PATH_SEPARATOR);
            for (int i = 0; i < locs.length; i++)
            {
                String location = locs[i];
                if (!location.startsWith(PATH_SEPARATOR))
                {
                    locs[i] = tempDsRes + PATH_SEPARATOR + location;
                }
            }
            return locs;
        }
    }
}
