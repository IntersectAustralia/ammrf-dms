CREATE TABLE recordstatus (
  RecordStatusID int(10) unsigned NOT NULL auto_increment,
  RecordStatus tinyint(1) NOT NULL default '0',
  Description char(10) default NULL,
  PRIMARY KEY  (RecordStatusID)
);

CREATE TABLE apertures (
  ApertureID int(10) unsigned NOT NULL auto_increment,
  Name varchar(30) NOT NULL default '',
  ApDate datetime default NULL,
  OpeningRadiusX double default NULL,
  OpeningRadiusY double default NULL,
  ThroatThickness double default NULL,
  ToatlHeight double default NULL,
  TipAngle double default NULL,
  BaseAngle double default NULL,
  Comments varchar(255) default NULL,
  CT_ProcessTypeID int(10) unsigned default NULL,
  CT_MaterialTypeID int(10) unsigned default NULL,
  CT_ConditionTypeID int(10) unsigned default NULL,
  RecordStatusID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (ApertureID),
  KEY RecordStatusID (RecordStatusID),
  CONSTRAINT apertures_ibfk_4 FOREIGN KEY (RecordStatusID) REFERENCES recordstatus (RecordStatusID)
);


CREATE TABLE configuration (
  ConfigurationID int(10) unsigned NOT NULL auto_increment,
  RequireLogin tinyint(1) default NULL,
  RequirePassword tinyint(1) default NULL,
  RecordStatusID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (ConfigurationID),
  CONSTRAINT configuration_ibfk_1 FOREIGN KEY (RecordStatusID) REFERENCES recordstatus (RecordStatusID)
);

CREATE TABLE customers (
  CustomerID int(10) unsigned NOT NULL auto_increment,
  CustomerName char(50) NOT NULL default '',
  RecordStatusID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (CustomerID),
  CONSTRAINT customers_ibfk_1 FOREIGN KEY (RecordStatusID) REFERENCES recordstatus (RecordStatusID)
);

CREATE TABLE operators (
  OperatorID int(10) unsigned NOT NULL auto_increment,
  Last varchar(50) NOT NULL default '',
  First varchar(30) NOT NULL default '',
  UserName varchar(10) default NULL,
  UserPassword varchar(10) default NULL,
  Admin tinyint(1) NOT NULL default '0',
  CanUseScreenDesigner tinyint(1) NOT NULL default '0',
  CanAddTableInfo tinyint(1) NOT NULL default '0',
  CustomerID int(10) unsigned NOT NULL default '0',
  RecordStatusID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (OperatorID),
  KEY CustomerID (CustomerID),
  CONSTRAINT operators_ibfk_1 FOREIGN KEY (RecordStatusID) REFERENCES recordstatus (RecordStatusID)
--  CONSTRAINT operators_ibfk_2 FOREIGN KEY (CustomerID) REFERENCES customers (CustomerID)
);

CREATE TABLE currentmachineoper (
  MachineName char(30) NOT NULL default '',
  OperatorID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (MachineName),
  KEY OperatorID (OperatorID),
  CONSTRAINT currentmachineoper_ibfk_1 FOREIGN KEY (OperatorID) REFERENCES operators (OperatorID)
);

CREATE TABLE custom_fields (
  CustomFieldID int(10) unsigned NOT NULL auto_increment,
  ScreenID int(10) unsigned NOT NULL default '0',
  OrderNumber int(10) unsigned NOT NULL default '0',
  Enabled int(10) unsigned NOT NULL default '0',
  TableName varchar(30) NOT NULL default '',
  FieldName varchar(30) NOT NULL default '',
  FieldType int(10) unsigned NOT NULL default '0',
  Fld_Label varchar(30) NOT NULL default '',
  Fld_Size int(10) unsigned NOT NULL default '0',
  Fld_Rexp varchar(100) NOT NULL default '',
  PRIMARY KEY  (CustomFieldID)
);

CREATE TABLE custom_table_types (
  CustomTableTypeID int(10) unsigned NOT NULL auto_increment,
  CoreTable tinyint(1) NOT NULL default '0',
  TypeName varchar(25) NOT NULL default '',
  Description varchar(100) default NULL,
  CreatedByID int(10) unsigned default NULL,
  CreatedByDATE datetime default NULL,
  DisabledByID int(10) unsigned default NULL,
  DisabledByDATE datetime default NULL,
  AllowExpansion tinyint(1) NOT NULL default '0',
  RecordStatusID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (CustomTableTypeID),
  KEY CreatedByID (CreatedByID),
  CONSTRAINT custom_table_types_ibfk_1 FOREIGN KEY (CreatedByID) REFERENCES operators (OperatorID),
  CONSTRAINT custom_table_types_ibfk_2 FOREIGN KEY (RecordStatusID) REFERENCES recordstatus (RecordStatusID)
);

CREATE TABLE custom_tables (
  CustomTableID int(10) unsigned NOT NULL auto_increment,
  CustomTableTypeID int(10) unsigned NOT NULL default '0',
  DisplayName varchar(25) NOT NULL default '',
  Description varchar(100) default NULL,
  NewDefault tinyint(4) default NULL,
  CoreTableItem tinyint(1) NOT NULL default '0',
  RecordStatusID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (CustomTableID),
  KEY CustomTableTypeID (CustomTableTypeID),
  CONSTRAINT custom_tables_ibfk_1 FOREIGN KEY (CustomTableTypeID) REFERENCES custom_table_types (CustomTableTypeID),
  CONSTRAINT custom_tables_ibfk_2 FOREIGN KEY (RecordStatusID) REFERENCES recordstatus (RecordStatusID)
);

CREATE TABLE carousels (
  CarouselID int(10) unsigned NOT NULL auto_increment,
  Name varchar(30) NOT NULL default '',
  CT_CarouselTypeID int(10) unsigned NOT NULL default '0',
  RecordStatusID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (CarouselID),
  KEY CT_CarouselTypeID (CT_CarouselTypeID),
  CONSTRAINT carousels_ibfk_1 FOREIGN KEY (CT_CarouselTypeID) REFERENCES custom_tables (CustomTableID),
  CONSTRAINT carousels_ibfk_2 FOREIGN KEY (RecordStatusID) REFERENCES recordstatus (RecordStatusID)
);

CREATE TABLE dbversion (
  DBVersionID int(10) unsigned NOT NULL auto_increment,
  Version int(11) NOT NULL default '0',
  Release int(11) NOT NULL default '0',
  Build int(11) NOT NULL default '0',
  PRIMARY KEY  (DBVersionID)
);

CREATE TABLE machines (
  MachineID int(10) unsigned NOT NULL auto_increment,
  Name varchar(25) NOT NULL default '',
  DisplayName varchar(25) NOT NULL default '',
  ImagoDesignation varchar(50) default NULL,
  AcquisitionPath varchar(255) default NULL,
  BackUpPath varchar(255) default NULL,
  RecordStatusID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (MachineID),
  CONSTRAINT machines_ibfk_1 FOREIGN KEY (RecordStatusID) REFERENCES recordstatus (RecordStatusID)
);

CREATE TABLE experiments (
  ExperimentID int(10) unsigned NOT NULL auto_increment,
  Name varchar(30) default NULL,
  ExperimentDATE datetime default NULL,
  EndDATE datetime default NULL,
  Comment varchar(255) default NULL,
  GoodHits bigint(20) default NULL,
  Temp double default NULL,
  Vacuum double default NULL,
  StopVoltage double default NULL,
  FlightPathLength double default NULL,
  PulseFrequency double default NULL,
  PulseFraction double default NULL,
  PerCentGoodHits double default NULL,
  LaserPower double default NULL,
  ReasonForStop text,
  ActualRunParamID int(10) unsigned default NULL,
  DataPath varchar(255) default NULL,
  ArchiveLocation varchar(255) default NULL,
  ArchiveDATE datetime default NULL,
  SpecimenID int(10) unsigned default NULL,
  ApertureID int(10) unsigned default NULL,
  OperatorID int(10) unsigned NOT NULL default '0',
  MachineID int(10) unsigned NOT NULL default '0',
  CT_ResultTypeID int(10) unsigned default NULL,
  RecordStatusID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (ExperimentID),
  KEY SpecimenID (SpecimenID),
  KEY ApertureID (ApertureID),
  KEY MachineID (MachineID),
  KEY CT_ResultTypeID (CT_ResultTypeID),
  KEY ExperimentDATE (ExperimentDATE),
--  CONSTRAINT experiments_ibfk_1 FOREIGN KEY (OperatorID) REFERENCES operators (OperatorID),
--  CONSTRAINT experiments_ibfk_2 FOREIGN KEY (MachineID) REFERENCES machines (MachineID),
--  CONSTRAINT experiments_ibfk_3 FOREIGN KEY (CT_ResultTypeID) REFERENCES custom_tables (CustomTableID),
  CONSTRAINT experiments_ibfk_4 FOREIGN KEY (RecordStatusID) REFERENCES recordstatus (RecordStatusID)
);

CREATE TABLE projects (
  ProjectID int(10) unsigned NOT NULL auto_increment,
  Name varchar(30) NOT NULL default '',
  ContactName varchar(255) default NULL,
  StackDescription varchar(255) default NULL,
  Division varchar(255) default NULL,
  DATEStarted datetime default NULL,
  DATEFirstStarted datetime default NULL,
  DATEFirstGoodData datetime default NULL,
  DATEArrived datetime default NULL,
  DATECouponsOut datetime default NULL,
  DATEEnd datetime default NULL,
  DATEReport datetime default NULL,
  Comment varchar(255) default NULL,
  InHouseProject tinyint(1) NOT NULL default '0',
  CustomerID int(10) unsigned NOT NULL default '0',
  CT_ProjectTypeID int(10) unsigned NOT NULL default '0',
  CT_PriorityCodeID int(10) unsigned NOT NULL default '0',
  CT_SpecimenTypeID int(10) unsigned NOT NULL default '0',
  RecordStatusID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (ProjectID),
  KEY CT_ProjectTypeID (CT_ProjectTypeID),
  KEY CT_PriorityCodeID (CT_PriorityCodeID),
  KEY CT_SpecimenTypeID (CT_SpecimenTypeID),
  CONSTRAINT projects_ibfk_1 FOREIGN KEY (CustomerID) REFERENCES customers (CustomerID),
  CONSTRAINT projects_ibfk_2 FOREIGN KEY (RecordStatusID) REFERENCES recordstatus (RecordStatusID),
  CONSTRAINT projects_ibfk_3 FOREIGN KEY (CT_PriorityCodeID) REFERENCES custom_tables (CustomTableID),
  CONSTRAINT projects_ibfk_4 FOREIGN KEY (CT_SpecimenTypeID) REFERENCES custom_tables (CustomTableID)
);

CREATE TABLE pucks (
  PuckID int(10) unsigned NOT NULL auto_increment,
  Type char(1) NOT NULL default '',
  Name varchar(10) NOT NULL default '',
  CustomerID int(10) unsigned NOT NULL default '0',
  RecordStatusID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (PuckID),
  UNIQUE KEY Name (Name),
  KEY Type (Type),
  CONSTRAINT pucks_ibfk_1 FOREIGN KEY (CustomerID) REFERENCES customers (CustomerID),
  CONSTRAINT pucks_ibfk_2 FOREIGN KEY (RecordStatusID) REFERENCES recordstatus (RecordStatusID)
);

CREATE TABLE runparams (
  RunParamID int(10) unsigned NOT NULL auto_increment,
  RPType int(11) NOT NULL default '0',
  Name varchar(50) default NULL,
  Fim varchar(25) default NULL,
  Temp double default NULL,
  Hz double default NULL,
  MaxEvap double default NULL,
  MaxPF double default NULL,
  PW varchar(25) default NULL,
  PID varchar(25) default NULL,
  VStart double default NULL,
  FOV varchar(25) default NULL,
  RecordStatusID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (RunParamID),
  KEY RPType (RPType),
  CONSTRAINT runparams_ibfk_1 FOREIGN KEY (RecordStatusID) REFERENCES recordstatus (RecordStatusID)
);

CREATE TABLE slots (
  SlotID int(10) unsigned NOT NULL auto_increment,
  Name varchar(30) NOT NULL default '',
  CarouselID int(10) unsigned NOT NULL default '0',
  RecordStatusID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (SlotID),
  KEY CarouselID (CarouselID),
  CONSTRAINT slots_ibfk_1 FOREIGN KEY (CarouselID) REFERENCES carousels (CarouselID),
  CONSTRAINT slots_ibfk_2 FOREIGN KEY (RecordStatusID) REFERENCES recordstatus (RecordStatusID)
);

CREATE TABLE specimens (
  SpecimenID int(10) unsigned NOT NULL auto_increment,
  Name varchar(30) NOT NULL default '',
  Comment varchar(255) default NULL,
  ParentID int(10) unsigned default NULL,
  DATEOfSpecimen datetime default NULL,
  TipRadius double default NULL,
  TipLength double default NULL,
  ShankAngle double default NULL,
  SpecimenColumn double default NULL,
  SpecimenRow double default NULL,
  Pitch double default NULL,
  OriginalSpecimenID int(10) unsigned default NULL,
  RecommendedRunParamID int(10) unsigned default NULL,
  ProjectID int(10) unsigned NOT NULL default '0',
  CustomerID int(10) unsigned NOT NULL default '0',
  CT_ProcessTypeID int(10) unsigned default NULL,
  CT_ConditionTypeID int(10) unsigned default NULL,
  CT_MaterialTypeID int(10) unsigned default NULL,
  CT_PostMortemTypeID int(10) unsigned default NULL,
  RecordStatusID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (SpecimenID),
  KEY Name_2 (Name),
  KEY OriginalSpecimenID (OriginalSpecimenID),
  CONSTRAINT specimens_ibfk_1 FOREIGN KEY (CustomerID) REFERENCES customers (CustomerID),
  CONSTRAINT specimens_ibfk_2 FOREIGN KEY (RecordStatusID) REFERENCES recordstatus (RecordStatusID)
);

CREATE TABLE tracking_locationacceptrules (
  LocationAcceptID int(10) unsigned NOT NULL auto_increment,
  ContainType int(10) unsigned NOT NULL default '0',
  PhysicalID int(10) unsigned NOT NULL default '0',
  AcceptsType int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (LocationAcceptID),
  KEY ContainType (ContainType),
  KEY PhysicalID (PhysicalID),
  KEY AcceptsType (AcceptsType)
);

CREATE TABLE tracking_physical_locations (
  TrackingPhysicalLocationID int(10) unsigned NOT NULL auto_increment,
  Description varchar(100) default NULL,
  ParentID int(10) unsigned default NULL,
  LocationType int(11) NOT NULL default '0',
  RecordID int(10) unsigned NOT NULL default '0',
  RecordStatusID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (TrackingPhysicalLocationID),
  CONSTRAINT tracking_physical_locations_ibfk_1 FOREIGN KEY (RecordStatusID) REFERENCES recordstatus (RecordStatusID)
);

CREATE TABLE tracking_transactions (
  TrackingTransactionID int(10) unsigned NOT NULL auto_increment,
  TransactionDATE datetime NOT NULL default '0000-00-00 00:00:00',
  Comment varchar(100) default NULL,
  OperatorID int(10) unsigned NOT NULL default '0',
  TrackingTypeID int(10) unsigned NOT NULL default '0',
  RecordID int(10) unsigned NOT NULL default '0',
  CurrentLocType int(10) unsigned NOT NULL default '0',
  CurrentLocID int(10) unsigned NOT NULL default '0',
  RecordStatusID int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (TrackingTransactionID),
  KEY RecordID (RecordID),
  KEY TrackingTypeID (TrackingTypeID),
  KEY TransactionDATE (TransactionDATE),
  KEY CurrentLocType (CurrentLocType),
  KEY CurrentLocID (CurrentLocID),
  CONSTRAINT tracking_transactions_ibfk_1 FOREIGN KEY (OperatorID) REFERENCES operators (OperatorID),
  CONSTRAINT tracking_transactions_ibfk_2 FOREIGN KEY (RecordStatusID) REFERENCES recordstatus (RecordStatusID)
);
