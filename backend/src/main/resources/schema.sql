CREATE TABLE IF NOT EXISTS servers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    ip VARCHAR(50) NOT NULL,
    port INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20),
    cpu_usage DOUBLE,
    memory_usage DOUBLE,
    version VARCHAR(50),
    uptime VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fingerprint VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    level VARCHAR(20),
    server VARCHAR(255),
    value VARCHAR(100),
    summary TEXT,
    description TEXT,
    starts_at TIMESTAMP,
    ends_at TIMESTAMP,
    updated_at TIMESTAMP,
    status VARCHAR(20),
    source VARCHAR(50),
    server_id BIGINT
);

CREATE TABLE IF NOT EXISTS alert_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    expr VARCHAR(1000) NOT NULL,
    duration VARCHAR(50),
    level VARCHAR(20),
    enabled BOOLEAN DEFAULT TRUE,
    summary TEXT,
    description TEXT,
    server_id BIGINT,
    group_name VARCHAR(255),
    source VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_servers_type ON servers(type);
CREATE INDEX IF NOT EXISTS idx_servers_status ON servers(status);
CREATE INDEX IF NOT EXISTS idx_alerts_status ON alerts(status);
CREATE INDEX IF NOT EXISTS idx_alerts_server_id ON alerts(server_id);
CREATE INDEX IF NOT EXISTS idx_alert_rules_server_id ON alert_rules(server_id);
CREATE INDEX IF NOT EXISTS idx_alert_rules_enabled ON alert_rules(enabled);
