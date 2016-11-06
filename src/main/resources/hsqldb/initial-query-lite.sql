

DROP TABLE IF EXISTS person ;
DROP TABLE IF EXISTS people ;



CREATE TABLE person (
firstName VARCHAR(20),
lastName VARCHAR(20),
school VARCHAR(20),
rollNumber int);

INSERT INTO person VALUES ('Henry','Donald','Little Garden',1234901);
INSERT INTO person VALUES ('Eric','Osborne','Little Garden',1234991);

CREATE TABLE people  (
    person_id PRIMARY KEY,
    first_name VARCHAR(20),
    last_name VARCHAR(20)
);
