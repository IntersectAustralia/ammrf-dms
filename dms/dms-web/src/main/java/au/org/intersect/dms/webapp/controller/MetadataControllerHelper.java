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

package au.org.intersect.dms.webapp.controller;

import java.util.Map;

import au.org.ands.standards.rif_cs.registryobjects.Collection;
import au.org.ands.standards.rif_cs.registryobjects.IdentifierType;
import au.org.ands.standards.rif_cs.registryobjects.NameType;
import au.org.ands.standards.rif_cs.registryobjects.ObjectFactory;
import au.org.ands.standards.rif_cs.registryobjects.Party;
import au.org.ands.standards.rif_cs.registryobjects.RelatedObjectType;
import au.org.ands.standards.rif_cs.registryobjects.NameType.NamePart;
import au.org.ands.standards.rif_cs.registryobjects.RegistryObjects.RegistryObject;
import au.org.ands.standards.rif_cs.registryobjects.RegistryObjects.RegistryObject.OriginatingSource;
import au.org.ands.standards.rif_cs.registryobjects.RelatedObjectType.Relation;

import au.org.intersect.dms.bookinggw.BookingGatewayInterface;
import au.org.intersect.dms.bookinggw.UserDetails;

/**
 * RifCS helper class for Metadata Controller
 * @author carlos
 *
 */
public class MetadataControllerHelper
{
    
    private ObjectFactory factory = new ObjectFactory();
    private BookingGatewayInterface bookingGateway;

    public MetadataControllerHelper(BookingGatewayInterface bookingGateway)
    {
        this.bookingGateway = bookingGateway;
    }

    public void linkOwner(RegistryObject party, RegistryObject collection)
    {
        Collection rawColl = collection.getCollection();
        RelatedObjectType relObj = factory.createRelatedObjectType();
        relObj.setKey(party.getKey());
        Relation rel = factory.createRelatedObjectTypeRelation();
        rel.setType("isOwnedBy");
        relObj.getRelation().add(rel);
        rawColl.getRelatedObject().add(relObj);
    }

    public RegistryObject returnOrCreateOwner(OriginatingSource origSource, Map<String, RegistryObject> owners,
            String owner)
    {
        if (owners.containsKey(owner))
        {
            return owners.get(owner);
        }
        RegistryObject resp = createOwner(origSource, owner); 
        owners.put(owner, resp);
        return resp;
    }

    private RegistryObject createOwner(OriginatingSource origSource, String owner)
    {
        RegistryObject resp = factory.createRegistryObjectsRegistryObject();
        Party party = factory.createParty();
        UserDetails userDetails = bookingGateway.getUserDetails(owner);
        party.setType("person");
        party.getIdentifier().add(createIdentifier(userDetails.getEmail()));
        party.getName().add(createName(userDetails.getFirstName(), userDetails.getLastName()));
        resp.setParty(party);
        resp.setKey(userDetails.getEmail());
        resp.setOriginatingSource(origSource);
        return resp;        
    }
    
    
    private NameType createName(String firstName, String lastName)
    {
        NameType resp = factory.createNameType();
        resp.setType("primary");
        resp.getNamePart().add(createNamePart("given", firstName));
        resp.getNamePart().add(createNamePart("family", lastName));
        return resp;
    }

    private NamePart createNamePart(String type, String string)
    {
        NamePart resp = factory.createNameTypeNamePart();
        resp.setType(type);
        resp.setValue(string);
        return resp;
    }

    private IdentifierType createIdentifier(String string)
    {
        IdentifierType resp = factory.createIdentifierType();
        resp.setType("local");
        resp.setValue(string);
        return resp;
    }


}
