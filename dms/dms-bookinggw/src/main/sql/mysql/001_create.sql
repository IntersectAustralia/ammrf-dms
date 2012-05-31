    create table ags_users (
        userid bigint not null,
        email varchar(100),
        password varchar(100),
        user_fname varchar(50),
        user_lname varchar(50),
        username varchar(100) not null,
        primary key (userid)
    ) ENGINE=InnoDB;

    create table ez_bookings (
        bookingid bigint not null,
        bookingdate datetime not null,
        comments varchar(255) not null,
        fromtime double precision not null,
        fromminute double precision not null,
        objectid bigint not null,
        totime double precision not null,
        tominute double precision not null,
        userid bigint,
        primary key (bookingid)
    ) ENGINE=InnoDB;

    create table ez_objects_names (
        languageId varchar(255) not null,
        objectId bigint not null,
        objectdescription varchar(255),
        objectname varchar(255),
        primary key (languageId, objectId)
    ) ENGINE=InnoDB;

    create table organisations (
        orgid bigint not null,
        organisation varchar(255),
        primary key (orgid)
    ) ENGINE=InnoDB;

    create table participants (
        regno bigint not null,
        academic integer,
        academics varchar(255),
        comments varchar(255),
        contact integer,
        contacts varchar(255),
        frontdesk datetime,
        headofdept datetime,
        status varchar(255),
        subsmngr datetime,
        supervisors varchar(255),
        timestamp datetime,
        projcode bigint,
        supervisor bigint,
        userid bigint,
        primary key (regno)
    ) ENGINE=InnoDB;

    create table projects (
        projcode bigint not null,
        discipline varchar(255),
        equipment varchar(255),
        finish datetime,
        outline varchar(255),
        start datetime,
        status varchar(255),
        subscode integer,
        timestamp datetime,
        title varchar(255),
        primary key (projcode)
    ) ENGINE=InnoDB;

    create table users (
        userid bigint not null,
        title varchar(255),
        orgid bigint,
        primary key (userid)
    ) ENGINE=InnoDB;

    alter table ez_bookings 
        add index FKA625A38434C2CCE0 (userid), 
        add constraint FKA625A38434C2CCE0 
        foreign key (userid) 
        references ags_users (userid);

    alter table participants 
        add index FK89FFF7A01120F46F (projcode), 
        add constraint FK89FFF7A01120F46F 
        foreign key (projcode) 
        references projects (projcode);

    alter table participants 
        add index FK89FFF7A016DEE22 (supervisor), 
        add constraint FK89FFF7A016DEE22 
        foreign key (supervisor) 
        references ags_users (userid);

    alter table participants 
        add index FK89FFF7A034C2CCE0 (userid), 
        add constraint FK89FFF7A034C2CCE0 
        foreign key (userid) 
        references ags_users (userid);

    alter table users 
        add index FK6A68E0852935CD7 (orgid), 
        add constraint FK6A68E0852935CD7 
        foreign key (orgid) 
        references organisations (orgid);

-- carlosayam / password

insert into ags_users (userid, email, password, user_fname, user_lname, username)
            values (1, 'carlos@intersect.org.au', '5f4dcc3b5aa765d61d8327deb882cf99', 'carlos', 'aya', 'carlosayam');
