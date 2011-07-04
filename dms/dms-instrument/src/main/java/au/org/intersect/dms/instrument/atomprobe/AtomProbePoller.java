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

package au.org.intersect.dms.instrument.atomprobe;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import au.org.intersect.dms.core.service.AtomProbeService;

/**
 * Polls atom probe DB for new experiments
 */
public class AtomProbePoller extends SimpleJdbcDaoSupport
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomProbePoller.class);
    
    @Autowired
    private AtomProbeService atomProbeService;

    /**
     * Stock server of the instrument.
     */
    private Long instrumentId;
    private Long machineId;

    @Required
    public void setInstrument(Long instrumentId)
    {
        this.instrumentId = instrumentId;
    }

    @Required
    public void setMachineId(Long machineId)
    {
        this.machineId = machineId;
    }

    public List<Experiment> getExperiments()
    {
        SimpleJdbcTemplate jdbcTemplate = getSimpleJdbcTemplate();
        StringBuilder query = new StringBuilder();
        long lastProcessedExperiment = getLastProcessedExperiment();
        query.append("select exp.ExperimentID as id, exp.name as name, op.UserName as username")
                .append(" from experiments as exp inner join operators as op on exp.OperatorID = op.OperatorID")
                .append(" where exp.ExperimentID > ? and exp.MachineID = ?");
        List<Experiment> experiments = jdbcTemplate.query(query.toString(), new RowMapper<Experiment>()
        {
            @Override
            public Experiment mapRow(ResultSet rs, int rowNum) throws SQLException
            {
                Experiment experiment = new Experiment();
                experiment.setId(rs.getLong("id"));
                experiment.setFileName(rs.getString("name"));
                experiment.setUsername(rs.getString("username"));
                return experiment;
            }
        }, lastProcessedExperiment, machineId);
        return experiments;
    }
    
    private long getLastProcessedExperiment()
    {
        long lastProcessedExperiment = atomProbeService.getLastProcessedExperiment(instrumentId);
        LOGGER.info("Requesting experiments to ingest greater then {}", lastProcessedExperiment);
        return lastProcessedExperiment;
    }
}
