//
// Treasure Data Bulk-Import Tool in Java
//
// Copyright (C) 2012 - 2014 Muga Nishizawa
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package com.treasure_data.td_import;

import com.amazonaws.Protocol;

public interface Constants {
    long MAX_LOG_TIME = 253402300799L;

    String CMD_TABLEIMPORT = "table_import";

    String CMD_TABLEIMPORT_USAGE = "  $ td table:import <db> <table> <files...>\n";

    String CMD_TABLEIMPORT_EXAMPLE =
            "  $ td table:import example_db table1 --apache access.log\n" +
            "  $ td table:import example_db table1 --json -t time - < test.json\n";

    String CMD_TABLEIMPORT_DESC = "  Parse and import files to a table\n";

    String CMD_TABLEIMPORT_OPTIONS =
            "      --format FORMAT              file format (default: apache)\n" +
            "      --apache                     same as --format apache; apache common log format\n" +
            "      --syslog                     same as --format syslog; syslog\n" +
            "      --msgpack                    same as --format msgpack; msgpack stream format\n" +
            "      --json                       same as --format json; LF-separated json format\n" +
            "  -t, --time-key COL_NAME          time key name for json and msgpack format (e.g. 'created_at')\n" +
            "      --auto-create-table          Create table and database if doesn't exist\n";

    String CMD_PREPARE = "prepare";

    String CMD_PREPARE_USAGE = "  $ td import:prepare <files...>\n";

    String CMD_PREPARE_EXAMPLE =
            "  $ td import:prepare logs/*.csv --format csv --columns time,uid,price,count --time-column time -o parts/\n" +
            "  $ td import:prepare logs/*.csv --format csv --columns date_code,uid,price,count --time-value 1394409600,10 -o parts/\n" +
            "  $ td import:prepare mytable --format mysql --db-url jdbc:mysql://localhost/mydb --db-user myuser --db-password mypass\n" +
            "  $ td import:prepare \"s3://<s3_access_key>:<s3_secret_key>@/my_bucket/path/to/*.csv\" --format csv --column-header --time-column date_time -o parts/\n";

    String CMD_PREPARE_DESC = "  Convert files into part file format\n";

    String CMD_PREPARE_OPTIONS =
            "    -f, --format FORMAT              source file format [csv, tsv, json, msgpack, apache, regex, mysql]; default=csv\n" +
            "    -C, --compress TYPE              compressed type [gzip, none, auto]; default=auto detect\n" +
            "    -T, --time-format FORMAT         specifies the strftime format of the time column\n" +
            "                                      The format slightly differs from Ruby's Time#strftime format in that the\n" +
            "                                      '%:z' and '%::z' timezone options are not supported.\n" +
            "    -e, --encoding TYPE              encoding type [UTF-8, etc.]\n" +
            "    -o, --output DIR                 output directory. default directory is 'out'.\n" +
            "    -s, --split-size SIZE_IN_KB      size of each parts (default: 16384)\n" +
            "    -t, --time-column NAME           name of the time column\n" +
            "    --time-value TIME,HOURS          time column's value. If the data doesn't have a time column,\n" +
            "                                     users can auto-generate the time column's value in 2 ways:\n" +
            "                                      * Fixed time value with --time-value TIME:\n" +
            "                                        where TIME is a Unix time in seconds since Epoch. The time\n" +
            "                                        column value is constant and equal to TIME seconds.\n" +
            "                                        E.g. '--time-value 1394409600' assigns the equivalent of\n" +
            "                                        timestamp 2014-03-10T00:00:00 to all records imported.\n" +
            "                                      * Incremental time value with --time-value TIME,HOURS:\n" +
            "                                        where TIME is the Unix time in seconds since Epoch and\n" +
            "                                        HOURS is the maximum range of the timestamps in hours.\n" +
            "                                        This mode can be used to assign incremental timestamps to\n" +
            "                                        subsequent records. Timestamps will be incremented by 1 second\n" +
            "                                        each record. If the number of records causes the timestamp to\n" +
            "                                        overflow the range (timestamp >= TIME + HOURS * 3600), the\n" +
            "                                        next timestamp will restart at TIME and continue from there.\n" +
            "                                        E.g. '--time-value 1394409600,10' will assign timestamp 1394409600\n" +
            "                                        to the first record, timestamp 1394409601 to the second, 1394409602\n" +
            "                                        to the third, and so on until the 36000th record which will have\n" +
            "                                        timestamp 1394445600 (1394409600 + 10 * 3600). The timestamp assigned\n" +
            "                                        to the 36001th record will be 1394409600 again and the timestamp\n" +
            "                                        will restart from there.\n" +
            "    --primary-key NAME:TYPE          pair of name and type of primary key declared in your item table\n" +
            "    --prepare-parallel NUM           prepare in parallel (default: 2; max 96)\n" +
            "    --only-columns NAME,NAME,...     only columns\n" +
            "    --exclude-columns NAME,NAME,...  exclude columns\n" +
            "    --error-records-handling MODE    error records handling mode [skip, abort]; default=skip\n" +
            "    --invalid-columns-handling MODE  invalid columns handling mode [autofix, warn]; default=warn\n" +
            "    --error-records-output DIR       write error records; default directory is 'error-records'.\n" +
            "    --columns NAME,NAME,...          column names (use --column-header instead if the first line has column names)\n" +
            "    --column-types TYPE,TYPE,...     column types [string, int, long, double]\n" +
            "    --column-type NAME:TYPE          column type [string, int, long, double]. A pair of column name and type can be specified like 'age:int'\n" +
            "    -S, --all-string                 disable automatic type conversion\n" +
            "    --empty-as-null-if-numeric       the empty string values are interpreted as null values if columns are numerical types.\n" +
            "\n" +
            "    CSV/TSV specific options:\n" +
            "    --column-header                  first line includes column names\n" +
            "    --delimiter CHAR                 delimiter CHAR; default=\",\" at csv, \"\\t\" at tsv\n" +
            "    --escape CHAR                    escape CHAR; default=\\\n" +
            "    --newline TYPE                   newline [CRLF, LF, CR];  default=CRLF\n" +
            "    --quote CHAR                     quote [DOUBLE, SINGLE, NONE]; if csv format, default=DOUBLE. if tsv format, default=NONE\n" +
            "\n" +
            "    MySQL specific options:\n" +
            "    --db-url URL                     JDBC connection URL\n" +
            "    --db-user NAME                   user name for MySQL account\n" +
            "    --db-password PASSWORD           password for MySQL account\n" +
            "\n" +
            "    REGEX specific options:\n" +
            "    --regex-pattern PATTERN          pattern to parse line. When 'regex' is used as source file format, this option is required\n";

    String CMD_UPLOAD = "upload";

    String CMD_UPLOAD_USAGE =
            "  $ td import:upload <session name> <files...>\n";

    String CMD_UPLOAD_EXAMPLE =
            "  $ td import:upload mysess parts/* --parallel 4\n" +
            "  $ td import:upload mysess parts/*.csv --format csv --columns time,uid,price,count --time-column time -o parts/\n" +
            "  $ td import:upload parts/*.csv --auto-create mydb.mytbl --format csv --columns time,uid,price,count --time-column time -o parts/\n" +
            "  $ td import:upload mysess mytable --format mysql --db-url jdbc:mysql://localhost/mydb --db-user myuser --db-password mypass\n" +
            "  $ td import:upload \"s3://<s3_access_key>:<s3_secret_key>@/my_bucket/path/to/*.csv\" --format csv --column-header --time-column date_time -o parts/\n";


    String CMD_UPLOAD_DESC = "  Upload or re-upload files into a bulk import session";

    String CMD_UPLOAD_OPTIONS =
            "    --retry-count NUM                upload process will automatically retry at specified time; default: 10\n" +
            "    --auto-create DATABASE.TABLE     create automatically bulk import session by specified database and table names\n" +
            "                                     If you use 'auto-create' option, you MUST not specify any session name as first argument.\n" +
            "    --auto-perform                   perform bulk import job automatically\n" +
            "    --auto-commit                    commit bulk import job automatically\n" +
            "    --auto-delete                    delete bulk import session automatically\n" +
            "    --parallel NUM                   upload in parallel (default: 2; max 8)\n" +
            "\n" +
            CMD_PREPARE_OPTIONS;

    String CMD_AUTO = "auto";
    String CMD_AUTO_ENABLE = "td.bulk_import.auto.enable";

    String CMD_AUTO_USAGE =
            "  $ td import:auto <session name> <files...>\n";

    String CMD_AUTO_EXAMPLE =
            "  $ td import:auto mysess parts/* --parallel 4\n" +
            "  $ td import:auto mysess parts/*.csv --format csv --columns time,uid,price,count --time-column time -o parts/\n" +
            "  $ td import:auto parts/*.csv --auto-create mydb.mytbl --format csv --columns time,uid,price,count --time-column time -o parts/\n" +
            "  $ td import:auto mysess mytable --format mysql --db-url jdbc:mysql://localhost/mydb --db-user myuser --db-password mypass\n" +
            "  $ td import:auto \"s3://<s3_access_key>:<s3_secret_key>@/my_bucket/path/to/*.csv\" --format csv --column-header --time-column date_time -o parts/\n";


    String CMD_AUTO_DESC = "  Automatically upload or re-upload files into a bulk import session. "
            + "It's functional equivalent of 'upload' command with 'auto-perform', 'auto-commit' and 'auto-delete' options. "
            + "But it, by default, doesn't provide 'auto-create' option. "
            + "If you want 'auto-create' option, you explicitly must declare it as command options.\n";

    String CMD_AUTO_OPTIONS =
            "    --retry-count NUM                upload process will automatically retry at specified time; default: 10\n" +
            "    --auto-create DATABASE.TABLE     create automatically bulk import session by specified database and table names\n" +
            "                                     If you use 'auto-create' option, you MUST not specify any session name as first argument.\n" +
            "    --parallel NUM                   upload in parallel (default: 2; max 8)\n" +
            "\n" +
            CMD_PREPARE_OPTIONS;

    String STAT_SUCCESS = "SUCCESS";
    String STAT_ERROR = "ERROR";

    ////////////////////////////////////////
    // OPTIONS                            //
    ////////////////////////////////////////

    // help
    String BI_PREPARE_PARTS_HELP = "help";
    String HYPHENHYPHEN = "--";
    String BI_PREPARE_PARTS_HELP_DESC = "show this help message";

    ////////////////////////////////////////
    // TABLE_IMPORT_OPTIONS               //
    ////////////////////////////////////////

    // format
    String TABLE_IMPORT_FORMAT_DESC = "file format (default: apache)"; // default 'apache'
    String TABLE_IMPORT_FORMAT_APACHE = "apache";
    String TABLE_IMPORT_FORMAT_APACHE_DESC = "same as --format apache; apache common log format";
    String TABLE_IMPORT_FORMAT_SYSLOG = "syslog";
    String TABLE_IMPORT_FORMAT_SYSLOG_DESC = "same as --format syslog; syslog";
    String TABLE_IMPORT_FORMAT_MSGPACK = "msgpack";
    String TABLE_IMPORT_FORMAT_MSGPACK_DESC = "same as --format msgpack; msgpack stream format";
    String TABLE_IMPORT_FORMAT_JSON = "json";
    String TABLE_IMPORT_FORMAT_JSON_DESC = "same as --format json; LF-separated json format";
    String TABLE_IMPORT_FORMAT_DEFAULTVALUE = TABLE_IMPORT_FORMAT_APACHE;

    // time-key
    String TABLE_IMPORT_TIME_KEY = "time-key";
    String TABLE_IMPORT_TIME_KEY_DESC = "time key name for json and msgpack format (e.g. 'created_at')";

    // auto-create-table
    String TABLE_IMPORT_AUTO_CREATE_TABLE = "auto-create-table";
    String TABLE_IMPORT_AUTO_CREATE_TABLE_DESC = "Create table and database if doesn't exist";

    ////////////////////////////////////////
    // UPLOAD_PARTS_OPTIONS               //
    ////////////////////////////////////////

    // format
    String BI_UPLOAD_PARTS_FORMAT_DEFAULTVALUE = "msgpack.gz"; // default 'msgpack.gz'

    // auto-craete
    String BI_UPLOAD_PARTS_AUTO_CREATE = "auto-create";
    String BI_UPLOAD_AUTO_CREATE_HYPHEN = HYPHENHYPHEN + BI_UPLOAD_PARTS_AUTO_CREATE;
    String BI_UPLOAD_PARTS_AUTO_CREATE_DESC =
            "create automatically bulk import session by specified database and table names";

    // auto-delete
    String BI_UPLOAD_PARTS_AUTO_DELETE = "auto-delete";
    String BI_UPLOAD_AUTO_DELETE_HYPHEN = HYPHENHYPHEN + BI_UPLOAD_PARTS_AUTO_DELETE;
    String BI_UPLOAD_PARTS_AUTO_DELETE_DESC = "delete bulk import session automatically";

    // auto-perform
    String BI_UPLOAD_PARTS_AUTO_PERFORM = "auto-perform";
    String BI_UPLOAD_AUTO_PERFORM_HYPHEN = HYPHENHYPHEN + BI_UPLOAD_PARTS_AUTO_PERFORM;
    String BI_UPLOAD_PARTS_AUTO_PERFORM_DEFAULTVALUE = "false";
    String BI_UPLOAD_PARTS_AUTO_PERFORM_DESC = "perform bulk import job automatically";

    // auto-commit
    String BI_UPLOAD_PARTS_AUTO_COMMIT = "auto-commit";
    String BI_UPLOAD_AUTO_COMMIT_HYPHEN = HYPHENHYPHEN + BI_UPLOAD_PARTS_AUTO_COMMIT;
    String BI_UPLOAD_PARTS_AUTO_COMMIT_DEFAULTVALUE = "false";
    String BI_UPLOAD_PARTS_AUTO_COMMIT_DESC = "commit bulk import job automatically";

    // parallel NUM
    String BI_UPLOAD_PARTS_PARALLEL = "parallel";
    String BI_UPLOAD_AUTO_PARALLEL_HYPHEN = HYPHENHYPHEN + BI_UPLOAD_PARTS_PARALLEL;
    String BI_UPLOAD_PARTS_PARALLEL_DEFAULTVALUE = "2";
    String BI_UPLOAD_PARTS_PARALLEL_MAX_VALUE = "8";
    String BI_UPLOAD_PARTS_PARALLEL_DESC = "upload in parallel (default: 2; max 8)";

    // retry-count
    String BI_UPLOAD_RETRY_COUNT = "retry-count";
    String BI_UPLOAD_RETRY_COUNT_HYPHEN = HYPHENHYPHEN + BI_UPLOAD_RETRY_COUNT;
    String BI_UPLOAD_RETRY_COUNT_DEFAULTVALUE = "10";
    String BI_UPLOAD_RETRY_COUNT_DESC =
            "upload process will automatically retry at specified time; default: 10";

    // waitSec NUM
    String BI_UPLOAD_PARTS_WAITSEC = "td.bulk_import.upload_parts.waitsec";
    String BI_UPLOAD_PARTS_WAITSEC_DEFAULTVALUE = "1";

    ////////////////////////////////////////
    // PREPARE_PARTS_OPTIONS              //
    ////////////////////////////////////////

    // format [csv, tsv, json, msgpack, apache, regex]; default=auto detect
    String BI_PREPARE_PARTS_FORMAT = "format";
    String BI_PREPARE_FORMAT_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_FORMAT;
    String BI_PREPARE_PARTS_FORMAT_DEFAULTVALUE = "csv"; // default 'csv'
    String BI_PREPARE_PARTS_FORMAT_DESC = "source file format [csv, tsv, json, msgpack]; default=csv";

    // output format [msgpackgz]; default=msgpackgz
    String BI_PREPARE_PARTS_OUTPUTFORMAT = "td.bulk_import.prepare_parts.outputformat";
    String BI_PREPARE_PARTS_OUTPUTFORMAT_DEFAULTVALUE = "msgpackgz";

    // compress [gzip,.., auto]; default=auto detect
    String BI_PREPARE_PARTS_COMPRESSION = "compress";
    String BI_PREPARE_COMPRESSION_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_COMPRESSION;
    String BI_PREPARE_PARTS_COMPRESSION_DEFAULTVALUE = "auto";
    String BI_PREPARE_PARTS_COMPRESSION_DESC = "compressed type [gzip, none]; default=auto detect";

    // parallel
    String BI_PREPARE_PARTS_PARALLEL = "prepare-parallel";
    String BI_PREPARE_PARALLEL_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_PARALLEL;
    String BI_PREPARE_PARTS_PARALLEL_DEFAULTVALUE = "1";
    String BI_PREPARE_PARTS_PARALLEL_DESC = "prepare in parallel (default: 2; max 96)";

    // encoding [UTF-8,...]
    String BI_PREPARE_PARTS_ENCODING = "encoding";
    String BI_PREPARE_ENCODING_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_ENCODING;
    String BI_PREPARE_PARTS_ENCODING_DEFAULTVALUE = "UTF-8";
    String BI_PREPARE_PARTS_ENCODING_DESC = "encoding type [UTF-8, etc.]; default=UTF-8";

    // columns, column-types, column-type
    String BI_PREPARE_PARTS_COLUMNS = "columns";
    String BI_PREPARE_COLUMNS_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_COLUMNS;
    String BI_PREPARE_PARTS_COLUMNS_DESC = "column names (use --column-header instead if the first line has column names)";
    String BI_PREPARE_PARTS_COLUMNTYPES = "column-types";
    String BI_PREPARE_COLUMNTYPES_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_COLUMNTYPES;
    String BI_PREPARE_PARTS_COLUMNTYPES_DESC = "column types [string, int, long, double]";
    String BI_PREPARE_COLUMNTYPE = "column-type";
    String BI_PREPARE_COLUMNTYPE_HYPHEN = HYPHENHYPHEN + BI_PREPARE_COLUMNTYPE;
    String BI_PREPARE_COLUMNTYPE_DESC = "column type [string, int, long, double]. A pair of column name and type can be specified like 'age:int'";

    // all-string
    String BI_PREPARE_ALL_STRING = "all-string";
    String BI_PREPARE_ALL_STRING_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_COLUMNS;
    String BI_PREPARE_ALL_STRING_DESC = "disable automatic type conversion";

    // empty-as-null-if-numeric; default=false
    String BI_PREPARE_PARTS_EMPTYASNULL = "empty-as-null-if-numeric";
    String BI_PREPARE_EMPTYASNULL_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_EMPTYASNULL;
    String BI_PREPARE_PARTS_EMPTYASNULL_DEFAULTVALUE = "false";
    String BI_PREPARE_PARTS_EMPTYASNULL_DESC = "the empty string values are interpreted as null values if columns arenumerical types";

    // exclude-columns, only-columns
    String BI_PREPARE_PARTS_EXCLUDE_COLUMNS = "exclude-columns";
    String BI_PREPARE_EXCLUDE_COLUMNS_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_EXCLUDE_COLUMNS;
    String BI_PREPARE_PARTS_EXCLUDE_COLUMNS_DESC = "exclude columns";
    String BI_PREPARE_PARTS_ONLY_COLUMNS = "only-columns";
    String BI_PREPARE_ONLY_COLUMNS_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_ONLY_COLUMNS;
    String BI_PREPARE_PARTS_ONLY_COLUMNS_DESC = "only columns";

    // primary-key
    String BI_PREPARE_PARTS_PRIMARY_KEY = "primary-key";
    String BI_PREPARE_PARTS_PRIMARY_KEY_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_PRIMARY_KEY;
    String BI_PREPARE_PARTS_PRIMARY_KEY_DESC = "pair of name and type of primary key declared in your item table";

    // time-column NAME; default='time'
    String BI_PREPARE_PARTS_TIMECOLUMN = "time-column";
    String BI_PREPARE_TIMECOLUMN_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_TIMECOLUMN;
    String BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE = "time";
    String BI_PREPARE_PARTS_TIMECOLUMN_DESC = "name of the time column";

    // time-value
    String BI_PREPARE_PARTS_TIMEVALUE = "time-value";
    String BI_PREPARE_TIMEVALUE_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_TIMEVALUE;
    String BI_PREPARE_PARTS_TIMEVALUE_DESC = "long value of the time column";

    // time-format STRF_FORMAT; default=auto detect
    String BI_PREPARE_PARTS_TIMEFORMAT = "time-format";
    String BI_PREPARE_TIMEFORMAT_HYPHEN_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_TIMEFORMAT;
    String BI_PREPARE_PARTS_TIMEFORMAT_DESC =
            "specifies the strftime format of the time column\n" +
            "  The format slightly differs from Ruby's Time#strftime format in that the\n" +
            "  '%:z' and '%::z' timezone options are not supported.";

    // output DIR
    String BI_PREPARE_PARTS_OUTPUTDIR = "output";
    String BI_PREPARE_OUTPUTDIR_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_OUTPUTDIR;
    String BI_PREPARE_PARTS_OUTPUTDIR_DEFAULTVALUE = "out"; // './out/'
    String BI_PREPARE_PARTS_OUTPUTDIR_DESC = "output directory";

    // error handling
    String BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING = "error-records-handling";
    String BI_PREPARE_ERROR_RECORDS_HANDLING_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING;
    String BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING_DEFAULTVALUE= "skip";
    String BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING_DESC = "error records handling mode [skip, abort]; default=skip";

    // invalid columns handling
    String BI_PREPARE_INVALID_COLUMNS_HANDLING = "invalid-columns-handling";
    String BI_PREPARE_INVALID_COLUMNS_HANDLING_HYPHEN = HYPHENHYPHEN + BI_PREPARE_INVALID_COLUMNS_HANDLING;
    String BI_PREPARE_INVALID_COLUMNS_HANDLING_DEFAULTVALUE = "warn";
    String BI_PREPARE_INVALID_COLUMNS_HANDLING_DESC = "invalid columns handling mode [autofix, warn]; default=warn";

    // error records output
    String BI_PREPARE_PARTS_ERROR_RECORDS_OUTPUT = "error-records-output";
    String BI_PREPARE_PARTS_ERROR_RECORDS_OUTPUTDIR_DEFAULTVALUE = "error-records";
    String BI_PREPARE_ERROR_RECORDS_OUTPUT_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_ERROR_RECORDS_OUTPUT;
    String BI_PREPARE_PARTS_ERROR_RECORDS_OUTPUT_DESC = "write error records; default directory is 'error-records'.";

    // dry-run; show samples as JSON and exit
    String BI_PREPARE_PARTS_DRYRUN = "td.bulk_import.prepare_parts.dry-run";
    String BI_PREPARE_PARTS_DRYRUN_DEFAULTVALUE = "false";

    String BI_PREPARE_PARTS_SPLIT_SIZE = "split-size";
    String BI_PREPARE_SPLIT_SIZE_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_SPLIT_SIZE;
    String BI_PREPARE_PARTS_SPLIT_SIZE_DEFAULTVALUE ="16384";
    String BI_PREPARE_PARTS_SPLIT_SIZE_DESC = "size of each parts (default: 16384)";

    ////////////////////////////////////////
    // CSV/TSV PREPARE_PARTS_OPTIONS      //
    ////////////////////////////////////////

    // quote [DOUBLE, SINGLE]; default=DOUBLE
    String BI_PREPARE_PARTS_QUOTE = "quote";
    String BI_PREPARE_QUOTE_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_QUOTE;
    String BI_PREPARE_PARTS_QUOTE_CSV_DEFAULTVALUE = "DOUBLE";
    String BI_PREPARE_PARTS_QUOTE_TSV_DEFAULTVALUE = "NONE";
    String BI_PREPARE_PARTS_QUOTE_DESC = "quote [DOUBLE, SINGLE, NONE]; if csv format, default=DOUBLE. if tsv format, default=NONE";

    // delimiter CHAR; default=',' at 'csv', '\t' at 'tsv'
    String BI_PREPARE_PARTS_DELIMITER = "delimiter";
    String BI_PREPARE_DELIMITER_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_DELIMITER;
    String BI_PREPARE_PARTS_DELIMITER_CSV_DEFAULTVALUE = ",";
    String BI_PREPARE_PARTS_DELIMITER_TSV_DEFAULTVALUE = "\t";
    String BI_PREPARE_PARTS_DELIMITER_DESC = "delimiter CHAR; default=\",\" at csv, \"\\t\" at tsv";

    // escape CHAR; default=N/A
    String BI_PREPARE_PARTS_ESCAPE = "escape";
    String BI_PREPARE_ESCAPE_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_ESCAPE;
    String BI_PREPARE_PARTS_ESCAPE_DESC = "escape CHAR; default=N/A";

    // newline [CRLF, LF, CR]; default=CRLF (or auto detect?)
    String BI_PREPARE_PARTS_NEWLINE = "newline";
    String BI_PREPARE_NEWLINE_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_NEWLINE;
    String BI_PREPARE_PARTS_NEWLINE_DEFAULTVALUE = "CRLF"; // default CRLF
    String BI_PREPARE_PARTS_NEWLINE_DESC = "newline [CRLR, LR, CR];  default=CRLF";

    // column-header; default=true
    String BI_PREPARE_PARTS_COLUMNHEADER = "column-header";
    String BI_PREPARE_COLUMNHEADER_HYPHEN = HYPHENHYPHEN + BI_PREPARE_PARTS_COLUMNHEADER;
    String BI_PREPARE_PARTS_COLUMNHEADER_DEFAULTVALUE = "false";
    String BI_PREPARE_PARTS_COLUMNHEADER_DESC = "first line includes column names";

    // type-conversion-error [skip,null]; default=skip
    String BI_PREPARE_PARTS_TYPE_CONVERSION_ERROR_DEFAULTVALUE = "skip";

    String BI_PREPARE_PARTS_SAMPLE_ROWSIZE = "td.bulk_import.prepare_parts.sample.rowsize";
    String BI_PREPARE_PARTS_SAMPLE_ROWSIZE_DEFAULTVALUE = "30";

    ////////////////////////////////////////
    // MYSQL PREPARE_PARTS_OPTIONS        //
    ////////////////////////////////////////

    String BI_PREPARE_PARTS_MYSQL_JDBCDRIVER_CLASS = "com.mysql.jdbc.Driver";

    // url
    String BI_PREPARE_PARTS_JDBC_CONNECTION_URL = "db-url";
    String BI_PREPARE_PARTS_JDBC_CONNECTION_URL_DESC = "JDBC connection URL";

    // user
    String BI_PREPARE_PARTS_JDBC_USER = "db-user";
    String BI_PREPARE_PARTS_JDBC_USER_DESC = "user name for MySQL account";

    // password
    String BI_PREPARE_PARTS_JDBC_PASSWORD = "db-password";
    String BI_PREPARE_PARTS_JDBC_PASSWORD_DESC = "password for MySQL account";

    ////////////////////////////////////////
    // REGEX PREPARE_PARTS_OPTIONS        //
    ////////////////////////////////////////

    String BI_PREPARE_PARTS_REGEX_PATTERN = "regex-pattern";
    String BI_PREPARE_PARTS_REGEX_PATTERN_DESC = "pattern to parse line. When 'regex' is used as source file format, this option is required";

    ////////////////////////////////////////
    // SOURCE PREPARE_PARTS_OPTIONS       //
    ////////////////////////////////////////

    // S3
    Protocol BI_PREPARE_S3_PROTOCOL = Protocol.HTTPS;
    int BI_PREPARE_S3_MAX_CONNECTIONS = 10; // SDK default: 50
    int BI_PREPARE_S3_MAX_ERRORRETRY = 5; // SDK default: 3
    int BI_PREPARE_S3_SOCKET_TIMEOUT = 8 * 60 * 1000; // SDK default: 50 * 1000
}
