
CREATE TABLE ags_users (
    userid integer NOT NULL IDENTITY,
    username varchar(100) NOT NULL,
    password varchar(100),
    user_fname varchar(50),
    user_lname varchar(50),
    address varchar(50),
    zipcode varchar(20),
    city varchar(50),
    phonenumber varchar(25),
    cellphonenumber varchar(25),
    fax varchar(25),
    email varchar(100),
    active_status integer,
    "language" varchar(5),
    personal_code_number varchar(12),
    compid integer,
    country varchar(50)
);

CREATE TABLE ez_bookings (
    bookingid integer NOT NULL IDENTITY,
    userid integer,
    objectid integer,
    bookingdate timestamp,
    fromtime double precision,
    totime double precision,
    comments varchar(255),
    nopersons integer,
    fromminute double precision,
    tominute double precision,
    runningbookingid integer,
    userdefinedfield varchar(255),
    realuserid integer,
    registereddate timestamp,
    lastediteddate timestamp,
    invoiced integer
);

CREATE TABLE projects (
    projcode integer NOT NULL IDENTITY,
    subscode integer,
    title varchar(150) NOT NULL,
    outline longvarchar,
    discipline varchar(16) NOT NULL,
    start date NOT NULL,
    finish date NOT NULL,
    equipment longvarchar,
    status character(3) NOT NULL,
    timestamp timestamp
);

CREATE TABLE participants (
    regno integer NOT NULL IDENTITY,
    userid integer NOT NULL,
    projcode integer NOT NULL,
    status character(3) NOT NULL,
    supervisor integer,
    academic integer,
    academics longvarchar,
    contact integer,
    contacts longvarchar,
    supervisors varchar(100),
    comments longvarchar,
    subsmngr timestamp,
    headofdept timestamp,
    frontdesk timestamp,
    timestamp timestamp
);

CREATE TABLE ez_objects_names (
    objectid integer NOT NULL,
    languageid varchar(10) NOT NULL,
    objectname varchar(100),
    objectdescription longvarchar
);

CREATE TABLE users (
    userid integer NOT NULL IDENTITY,
    title varchar(16),
    unistatus varchar(16),
    orgid integer NOT NULL,
    department varchar(100),
    deptunit varchar(100),
    bcode varchar(10),
    usydno varchar(16),
    scardno varchar(16),
    afterhours boolean DEFAULT false,
    safetynotes timestamp,
    unikey varchar(20),
    sessionkey varchar(64),
    lastauth timestamp,
    lastaccessed timestamp,
    timestamp timestamp,
    scarddate date,
    category2 varchar(100),
    category3 varchar(100),
    cardarea varchar(40)
);

CREATE TABLE organisations (
    orgid integer NOT NULL IDENTITY,
    organisation varchar(100) NOT NULL,
    nano boolean NOT NULL,
    type integer,
    fixrate integer,
    discount integer,
    enrolform boolean
);
