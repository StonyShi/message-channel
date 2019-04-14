CREATE TABLE IF NOT EXISTS worker_register
(
    ID          INT AUTO_INCREMENT PRIMARY KEY,
    host        VARCHAR(255),
    port        INT,
    create_time BIGINT,
    update_time BIGINT,
    UNIQUE KEY idx_host_port (host, port)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


CREATE TABLE IF NOT EXISTS worker_info
(
    ID                           INT AUTO_INCREMENT PRIMARY KEY,
    machine_id                   VARCHAR(255),
    server_name                  VARCHAR(255),
    server_port                  INT,
    connection_count             BIGINT,
    connection_error_count       BIGINT,
    max_connection_count         BIGINT,
    request_total_count          BIGINT,
    request_total_bytes          BIGINT,
    response_total_count         BIGINT,
    response_total_succeed_count BIGINT,
    response_total_failed_count  BIGINT,
    response_total_time_ms       BIGINT,
    response_total_bytes         BIGINT,
    subscriber_count             BIGINT,
    subscriber_message_count     BIGINT,
    create_time                  BIGINT,
    update_time                  BIGINT,
    UNIQUE KEY idx_server_addr(server_name, server_port)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


CREATE TABLE IF NOT EXISTS worker_info_history
(
    ID                           INT AUTO_INCREMENT PRIMARY KEY,
    machine_id                   VARCHAR(255),
    server_name                  VARCHAR(255),
    server_port                  INT,
    connection_count             BIGINT,
    connection_error_count       BIGINT,
    max_connection_count         BIGINT,
    request_total_count          BIGINT,
    request_total_bytes          BIGINT,
    response_total_count         BIGINT,
    response_total_succeed_count BIGINT,
    response_total_failed_count  BIGINT,
    response_total_time_ms       BIGINT,
    response_total_bytes         BIGINT,
    subscriber_count             BIGINT,
    subscriber_message_count     BIGINT,
    create_time                  BIGINT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
    PARTITION BY RANGE (`create_time`) (
        PARTITION p_20200101 VALUES LESS THAN (1577836800000),
        PARTITION p_20210101 VALUES LESS THAN (1609459200000),
        PARTITION p_20220101 VALUES LESS THAN (1640995200000),
        PARTITION p_20230101 VALUES LESS THAN (1672531200000),
        PARTITION p_20240101 VALUES LESS THAN (1704067200000),
        PARTITION p_max VALUES LESS THAN MAXVALUE
        );

ALTER TABLE worker_info_history ADD INDEX idx_s_address (server_name,server_port);

