CREATE TABLE SPRZET (
  SPRZET_ID INT PRIMARY KEY,
  OPIS varchar(80),
  TYP VARCHAR(50) NOT NULL,
  CZY_WLACZONY int check (CZY_WLACZONY in (0,1))
);

INSERT INTO SPRZET VALUES (1, 'gniazdko 1', 'gniazdko', 1);
INSERT INTO SPRZET VALUES (2, 'gniazdko 2', 'gniazdko', 1);

DROP TABLE POMIAR_GNIAZDKO;

CREATE TABLE POMIAR_GNIAZDKO (
  POMIAR_ID INT PRIMARY KEY,
  SPRZET_ID INT NOT NULL,
  MOMENT_POMIARU TIMESTAMP,
  POMIAR_NAPIECIE DECIMAL(8,2),
  POMIAR_PRAD DECIMAL(8,2),
  POMIAR_MOC DECIMAL(8,2),
  CONSTRAINT FK_SPRZET
    FOREIGN KEY (SPRZET_ID) REFERENCES SPRZET(SPRZET_ID)
  );

INSERT INTO POMIAR_GNIAZDKO VALUES (1, 1, CURRENT_TIMESTAMP, 150.00, 31.00, 7000.00);
INSERT INTO POMIAR_GNIAZDKO VALUES (2, 1, CURRENT_TIMESTAMP, 152.00, 30.00, 7002.00);
INSERT INTO POMIAR_GNIAZDKO VALUES (3, 2, CURRENT_TIMESTAMP, 155.00, 25.00, 5002.00);
INSERT INTO POMIAR_GNIAZDKO VALUES (4, 2, CURRENT_TIMESTAMP, 156.00, 10.00, 4002.00);

DROP TABLE POMIAR_TEMPERATURA;

CREATE TABLE POMIAR_TEMPERATURA (
  POMIAR_ID INT PRIMARY KEY,
  SPRZET_ID INT NOT NULL,
  MOMENT_POMIARU TIMESTAMP NOT NULL,
  POMIAR_TEMP decimal(8,2),
  CONSTRAINT FK_SPRZET1
    FOREIGN KEY (SPRZET_ID) REFERENCES SPRZET(SPRZET_ID)
  );
  
INSERT INTO POMIAR_TEMPERATURA VALUES (1, 2, CURRENT_TIMESTAMP, -10);
INSERT INTO POMIAR_TEMPERATURA VALUES (2, 2, CURRENT_TIMESTAMP, 10);
INSERT INTO POMIAR_TEMPERATURA VALUES (3, 1, CURRENT_TIMESTAMP, -10);
INSERT INTO POMIAR_TEMPERATURA VALUES (4, 1, CURRENT_TIMESTAMP, -1);
INSERT INTO POMIAR_TEMPERATURA VALUES (5, 1, CURRENT_TIMESTAMP, 5);

CREATE TABLE POMIARY_TEMP (
  SPRZET_ID INT PRIMARY KEY,
  OPIS varchar(80),
  CZY_WLACZONY int,
  POMIAR_TEMP decimal(8,2)
);

CREATE VIEW OSTATNI_POMIAR_TEMPERATURA(SPRZET_ID, POMIAR_TEMP, MOMENT_POMIARU) AS
(SELECT POM1.SPRZET_ID, POM1.POMIAR_TEMP, POM1.MOMENT_POMIARU FROM pomiar_temperatura POM1
WHERE POM1.MOMENT_POMIARU = (SELECT MAX(POM2.MOMENT_POMIARU) FROM pomiar_temperatura POM2
                                WHERE POM1.SPRZET_ID = POM2.SPRZET_ID));

CREATE VIEW OSTATNI_POMIAR_TEMPERATURA(SPRZET_ID, OPIS, CZY_WLACZONY, POMIAR_TEMP) AS
(SELECT POM1.SPRZET_ID, S.OPIS, S.CZY_WLACZONY, POM1.POMIAR_TEMP FROM pomiar_temperatura POM1, sprzet S
WHERE POM1.MOMENT_POMIARU = (SELECT MAX(POM2.MOMENT_POMIARU) FROM pomiar_temperatura POM2
                                WHERE POM1.SPRZET_ID = POM2.SPRZET_ID)
AND POM1.SPRZET_ID = S.SPRZET_ID);