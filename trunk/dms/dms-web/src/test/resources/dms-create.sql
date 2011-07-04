
    create table atom_probe_job_settings (
        id bigint not null auto_increment,
        last_processed_experiment bigint,
        version integer,
        instrument bigint,
        primary key (id)
    );

    create table atom_probe_user_matching (
        id bigint not null auto_increment,
        atom_probe_username varchar(255),
        booking_system_username varchar(255),
        version integer,
        instrument bigint,
        primary key (id)
    );

    create table dataset (
        id bigint not null auto_increment,
        creation_date datetime,
        owner varchar(255),
        url varchar(255) not null unique,
        version integer,
        primary key (id)
    );

    create table dms_user (
        id bigint not null auto_increment,
        username varchar(255),
        version integer,
        primary key (id)
    );

    create table job (
        id bigint not null auto_increment,
        copy_started_time_stamp bigint,
        created_time_stamp bigint not null,
        current_bytes bigint not null,
        current_number_of_directories integer not null,
        current_number_of_files integer not null,
        destination varchar(255),
        destination_dir varchar(255),
        finished_time_stamp bigint,
        source varchar(255),
        status varchar(255),
        total_bytes bigint not null,
        total_number_of_directories integer not null,
        total_number_of_files integer not null,
        type varchar(255),
        update_time_stamp bigint,
        version integer,
        dms_user bigint not null,
        primary key (id)
    );

    create table job_detail_metadata (
        id bigint not null auto_increment,
        metadata longtext,
        metadata_schema varchar(255),
        url varchar(255),
        version integer,
        job bigint not null,
        primary key (id)
    );

    create table job_from (
        id bigint not null auto_increment,
        source_dir varchar(255),
        version integer,
        job bigint not null,
        primary key (id)
    );

    create table metadata_item (
        id bigint not null auto_increment,
        metadata longtext,
        metadata_schema varchar(255),
        url varchar(255),
        version integer,
        dataset bigint not null,
        primary key (id)
    );

    create table stock_server (
        id bigint not null auto_increment,
        credentials_option varchar(255),
        description varchar(255),
        instrument_id bigint,
        instrument_profile varchar(255),
        password varchar(255),
        protocol varchar(255),
        server varchar(255),
        type varchar(255),
        username varchar(255),
        version integer,
        primary key (id)
    );
    
INSERT INTO stock_server (id,credentials_option,description,instrument_id,instrument_profile,password,protocol,server,type,username,version) VALUES (1,'FIXED','ACMM FTP',null,null,'password','ftp','localhost','REPOSITORY','ftpuser',0);
INSERT INTO stock_server (id,credentials_option,description,instrument_id,instrument_profile,password,protocol,server,type,username,version) VALUES (2,'NONE','Atom Probe',3,'ATOM_PROBE',null,'local','atomProbe','INSTRUMENT',null,0);