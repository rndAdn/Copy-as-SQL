\set ON_ERROR_STOP on

BEGIN;

CREATE SCHEMA playground;

CREATE TABLE playground.scalar_types (
    id integer PRIMARY KEY,
    label text NOT NULL,
    smallint_value smallint,
    integer_value integer,
    bigint_value bigint,
    numeric_value numeric(30, 10),
    real_value real,
    double_value double precision,
    money_value money,
    boolean_value boolean,
    char_value char(5),
    varchar_value varchar(100),
    text_value text,
    uuid_value uuid,
    bytea_value bytea,
    json_value json,
    jsonb_value jsonb
);

INSERT INTO playground.scalar_types VALUES
    (1, 'typical', 42, 123456, 9007199254740993, 12345678901234567890.1234567890,
     1.25, 3.141592653589793, 19.99, true, 'abc', 'Bonjour',
     'Une chaîne simple', '123e4567-e89b-12d3-a456-426614174000', decode('00ff275c0a', 'hex'),
     '{"name":"O''Reilly","active":true}', '{"tags":["été","日本語"],"value":null}'),
    (2, 'escaping', -32768, -2147483648, -9223372036854775808, -0.0000000001,
     -1.5, -1e100, -12.34, false, ' x ', 'O''Reilly',
     E'O''Reilly\nDeuxième ligne\tC:\\temp\\fichier.sql — café 日本語 🚀',
     'ffffffff-ffff-ffff-ffff-ffffffffffff', decode('deadbeef', 'hex'),
     '{"quote":"''","backslash":"\\"}', '{"nested":{"array":[1,true,null,"O''Reilly"]}}'),
    (3, 'empty-not-null', 0, 0, 0, 0, 0, 0, 0, false, '', '', '',
     '00000000-0000-0000-0000-000000000000', decode('', 'hex'), 'null', '{}'),
    (4, 'sql-null', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
     NULL, NULL, NULL, NULL),
    (5, 'boundaries-and-literal-null', 32767, 2147483647, 9223372036854775807,
     99999999999999999999.9999999999, 'Infinity', 'NaN', 0, true, 'NULL', 'NULL', 'NULL',
     'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee', decode('00', 'hex'), '[]', '"NULL"');

CREATE TABLE playground.temporal_types (
    id integer PRIMARY KEY,
    label text NOT NULL,
    date_value date,
    time_value time(6),
    timetz_value time(6) with time zone,
    timestamp_value timestamp(6),
    timestamptz_value timestamp(6) with time zone,
    interval_value interval
);

INSERT INTO playground.temporal_types VALUES
    (1, 'typical', '2024-02-29', '12:34:56.123456', '12:34:56.123456+02',
     '2024-02-29 12:34:56.123456', '2024-02-29 12:34:56.123456+02', '1 year 2 mons 3 days 04:05:06.123456'),
    (2, 'offset-and-negative', '1970-01-01', '00:00:00', '23:59:59.999999-05:30',
     '1999-12-31 23:59:59.999999', '2024-12-31 23:59:59.999999-05:30', '-3 days -04:05:06'),
    (3, 'infinity', 'infinity', '24:00:00', '00:00:00+00', '-infinity', 'infinity', '0'),
    (4, 'sql-null', NULL, NULL, NULL, NULL, NULL, NULL);

CREATE TYPE playground.mood AS ENUM ('happy', 'neutral', 'it''s complicated');

CREATE TABLE playground.postgres_types (
    id integer PRIMARY KEY,
    label text NOT NULL,
    enum_value playground.mood,
    integer_array integer[],
    text_array text[],
    matrix_value integer[][],
    inet_value inet,
    cidr_value cidr,
    macaddr_value macaddr,
    macaddr8_value macaddr8,
    bit_value bit(8),
    varbit_value bit varying(16),
    int4range_value int4range,
    int8range_value int8range,
    numrange_value numrange,
    daterange_value daterange,
    tsrange_value tsrange,
    tstzrange_value tstzrange,
    multirange_value int4multirange,
    point_value point,
    line_value line,
    lseg_value lseg,
    box_value box,
    path_value path,
    polygon_value polygon,
    circle_value circle,
    xml_value xml,
    tsvector_value tsvector,
    tsquery_value tsquery
);

INSERT INTO playground.postgres_types VALUES
    (1, 'typical', 'happy', ARRAY[1, 2, 3], ARRAY['été', 'O''Reilly', '日本語'], ARRAY[[1, 2], [3, 4]],
     '192.168.1.42', '192.168.1.0/24', '08:00:2b:01:02:03', '08:00:2b:ff:fe:01:02:03', B'10101010', B'101',
     '[1,10)', '[1,9223372036854775807)', '[1.25,9.99]', '[2024-01-01,2025-01-01)',
     '["2024-01-01 00:00:00","2024-02-01 00:00:00")',
     '["2024-01-01 00:00:00+00","2024-02-01 00:00:00+00")', '{[1,3),[7,10)}',
     '(1.5,-2.5)', '{1,-1,0}', '[(0,0),(1,1)]', '(3,4),(0,0)', '[(0,0),(1,1),(2,0)]',
     '((0,0),(2,0),(1,2))', '<(1,2),3>', '<root name="O''Reilly">été &amp; SQL</root>',
     to_tsvector('simple', 'hello SQL world'), to_tsquery('simple', 'hello & sql')),
    (2, 'empty-and-special', 'it''s complicated', ARRAY[1, NULL, -2], ARRAY['', NULL, 'NULL', 'a,b', '"quoted"', E'C:\\temp'],
     ARRAY[[NULL, 0], [-1, 2]], '2001:db8::42', '2001:db8::/32', '00:00:00:00:00:00', '00:00:00:00:00:00:00:00',
     B'00000000', B'', 'empty', '(,)', '(,)', 'empty', 'empty', 'empty', '{}',
     '(0,0)', '{0,1,0}', '[(0,0),(0,0)]', '(0,0),(0,0)', '((0,0),(1,0),(0,1))',
     '((0,0),(1,0),(0,1))', '<(0,0),0>', '<empty/>', ''::tsvector, ''::tsquery);

INSERT INTO playground.postgres_types (id, label, integer_array, text_array, matrix_value)
VALUES (3, 'empty-arrays', ARRAY[]::integer[], ARRAY[]::text[], ARRAY[]::integer[][]);
INSERT INTO playground.postgres_types (id, label) VALUES (4, 'sql-null');

CREATE TABLE playground."Odd Table" (
    "Id" integer PRIMARY KEY,
    "select" text,
    "with space" text,
    "a""quote" text
);
INSERT INTO playground."Odd Table" VALUES
    (1, 'O''Reilly', 'été 日本語 🚀', 'double " quote'),
    (2, '', 'NULL', NULL);

-- Empty destination for testing copied INSERT statements without primary-key conflicts.
CREATE TABLE playground.insert_target (LIKE playground.scalar_types INCLUDING ALL);

-- Fail initialization if the distinctions most useful to copy-as-SQL testing are lost.
DO $$
BEGIN
    IF (SELECT count(*) FROM playground.scalar_types) <> 5
        OR NOT EXISTS (SELECT FROM playground.scalar_types WHERE id = 2 AND position(chr(10) IN text_value) > 0
                       AND position(chr(92) IN text_value) > 0)
        OR NOT EXISTS (SELECT FROM playground.scalar_types WHERE id = 3 AND text_value = '' AND json_value::text = 'null')
        OR NOT EXISTS (SELECT FROM playground.scalar_types WHERE id = 4 AND text_value IS NULL AND json_value IS NULL)
        OR NOT EXISTS (SELECT FROM playground.scalar_types WHERE id = 5 AND text_value = 'NULL'
                       AND bigint_value = 9223372036854775807)
        OR NOT EXISTS (SELECT FROM playground.postgres_types WHERE id = 2 AND integer_array[2] IS NULL)
        OR NOT EXISTS (SELECT FROM playground.postgres_types WHERE id = 3 AND cardinality(integer_array) = 0)
        OR NOT EXISTS (SELECT FROM playground.postgres_types WHERE id = 4 AND integer_array IS NULL)
    THEN
        RAISE EXCEPTION 'Copy-as-SQL fixture invariants failed';
    END IF;
END
$$;

COMMIT;
