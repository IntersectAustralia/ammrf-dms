--
-- PostgreSQL database dump
-- 
-- Triggers have been removed (as we only care for reading data and don't need to enforce consistency, etc)
--

SET client_encoding = 'SQL_ASCII';
SET check_function_bodies = false;

SET SESSION AUTHORIZATION 'admin';

SET search_path = public, pg_catalog;

CREATE SEQUENCE ags_users_userid_seq;

CREATE TABLE ags_users (
    userid integer DEFAULT nextval('"ags_users_userid_seq"'::text) NOT NULL,
    username character varying(100) NOT NULL,
    "password" character varying(100),
    user_fname character varying(50),
    user_lname character varying(50),
    address character varying(50),
    zipcode character varying(20),
    city character varying(50),
    phonenumber character varying(25),
    cellphonenumber character varying(25),
    fax character varying(25),
    email character varying(100),
    active_status integer,
    "language" character varying(5),
    personal_code_number character varying(12),
    compid integer,
    country character varying(50)
);

ALTER TABLE ONLY ags_users
    ADD CONSTRAINT pk_ags_users PRIMARY KEY (userid);

CREATE TABLE users (
    userid integer NOT NULL,
    title character varying(16),
    unistatus character varying(16),
    orgid integer NOT NULL,
    department character varying(100),
    deptunit character varying(100),
    bcode character varying(10),
    usydno character varying(16),
    scardno character varying(16),
    afterhours boolean DEFAULT false,
    safetynotes timestamp with time zone,
    unikey character varying(20),
    sessionkey character varying(64),
    lastauth timestamp with time zone,
    lastaccessed timestamp with time zone,
    "timestamp" timestamp with time zone DEFAULT ('now'::text)::timestamp(6) with time zone,
    scarddate date,
    category2 character varying(100),
    category3 character varying(100),
    cardarea character varying(40)
);


CREATE TABLE organisations (
    orgid integer DEFAULT nextval('"organisations_orgid_seq"'::text) NOT NULL,
    organisation character varying(100) NOT NULL,
    nano boolean NOT NULL,
    "type" integer,
    fixrate integer,
    discount integer,
    enrolform boolean
);

ALTER TABLE ONLY organisations
    ADD CONSTRAINT pk_organisations PRIMARY KEY (orgid);


CREATE TABLE ez_objects_names (
    objectid integer NOT NULL,
    languageid character varying(10) NOT NULL,
    objectname character varying(100),
    objectdescription text
);

ALTER TABLE ONLY ez_objects_names
    ADD CONSTRAINT pk_ez_objects_names PRIMARY KEY (objectid, languageid);

CREATE TABLE projects (
    projcode integer DEFAULT nextval('"projects_projcode_seq"'::text) NOT NULL,
    subscode integer,
    title character varying(150) NOT NULL,
    outline text,
    discipline character varying(16) NOT NULL,
    "start" date NOT NULL,
    finish date NOT NULL,
    equipment text,
    status character(3) NOT NULL,
    "timestamp" timestamp with time zone DEFAULT ('now'::text)::timestamp(6) with time zone
);

ALTER TABLE ONLY projects
    ADD CONSTRAINT pk_projects PRIMARY KEY (projcode);


CREATE TABLE participants (
    regno integer DEFAULT nextval('"participants_regno_seq"'::text) NOT NULL,
    userid integer NOT NULL,
    projcode integer NOT NULL,
    status character(3) NOT NULL,
    supervisor integer,
    academic integer,
    academics text,
    contact integer,
    contacts text,
    supervisors character varying(100),
    comments text,
    subsmngr timestamp with time zone,
    headofdept timestamp with time zone,
    frontdesk timestamp with time zone,
    "timestamp" timestamp with time zone DEFAULT ('now'::text)::timestamp(6) with time zone
);

ALTER TABLE ONLY participants
    ADD CONSTRAINT pk_participants PRIMARY KEY (regno);


CREATE TABLE ez_bookings (
    bookingid integer NOT NULL,
    userid integer,
    objectid integer,
    bookingdate timestamp with time zone,
    fromtime double precision,
    totime double precision,
    comments character varying(255),
    nopersons integer,
    fromminute double precision,
    tominute double precision,
    runningbookingid integer,
    userdefinedfield character varying(255),
    realuserid integer,
    registereddate timestamp with time zone,
    lastediteddate timestamp with time zone,
    invoiced integer
);


ALTER TABLE ONLY ez_bookings
    ADD CONSTRAINT pk_ez_bookings PRIMARY KEY (bookingid);

--
-- TOC entry 4 (OID 687686)
-- Name: ags_users; Type: ACL; Schema: public; Owner: admin
--

REVOKE ALL ON TABLE ags_users FROM PUBLIC;
GRANT ALL ON TABLE ags_users TO rwuser;
GRANT SELECT ON TABLE ags_users TO rouser;

REVOKE ALL ON TABLE users FROM PUBLIC;
GRANT ALL ON TABLE users TO rwuser;
GRANT SELECT ON TABLE users TO rouser;

REVOKE ALL ON TABLE organisations FROM PUBLIC;
GRANT ALL ON TABLE organisations TO rwuser;
GRANT SELECT ON TABLE organisations TO rouser;

REVOKE ALL ON TABLE ez_objects_names FROM PUBLIC;
GRANT ALL ON TABLE ez_objects_names TO rwuser;
GRANT SELECT ON TABLE ez_objects_names TO rouser;

REVOKE ALL ON TABLE projects FROM PUBLIC;
GRANT ALL ON TABLE projects TO rwuser;
GRANT SELECT ON TABLE projects TO rouser;

REVOKE ALL ON TABLE participants FROM PUBLIC;
GRANT ALL ON TABLE participants TO rwuser;
GRANT SELECT ON TABLE participants TO rouser;

REVOKE ALL ON TABLE ez_bookings FROM PUBLIC;
GRANT ALL ON TABLE ez_bookings TO rwuser;
GRANT SELECT ON TABLE ez_bookings TO rouser;


SET SESSION AUTHORIZATION 'admin';

