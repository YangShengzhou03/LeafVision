DROP DATABASE IF EXISTS leaf_vision;
CREATE DATABASE leaf_vision DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE leaf_vision;

CREATE TABLE IF NOT EXISTS servers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '服务器名称',
    ip VARCHAR(50) NOT NULL COMMENT '服务器IP地址',
    port INT NOT NULL COMMENT '服务端口',
    type VARCHAR(50) NOT NULL COMMENT '服务器类型：prometheus-master/prometheus-node/alertmanager/docker',
    status VARCHAR(20) DEFAULT 'unknown' COMMENT '状态：online/offline/unknown',
    docker_port INT DEFAULT 2375 COMMENT 'Docker API端口',
    cpu_usage DOUBLE COMMENT 'CPU使用率(%)',
    memory_usage DOUBLE COMMENT '内存使用率(%)',
    version VARCHAR(50) COMMENT '版本号',
    uptime VARCHAR(50) COMMENT '运行时长',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_sync_at TIMESTAMP COMMENT '最后同步时间',
    UNIQUE KEY uk_ip_port (ip, port),
    INDEX idx_status (status),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务器信息表';

CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    fingerprint VARCHAR(100) UNIQUE COMMENT '告警指纹，用于去重',
    name VARCHAR(255) NOT NULL COMMENT '告警名称',
    severity VARCHAR(20) COMMENT '严重程度：critical/warning/info',
    instance VARCHAR(100) COMMENT '实例地址',
    value VARCHAR(100) COMMENT '触发值',
    summary VARCHAR(500) COMMENT '告警摘要',
    description TEXT COMMENT '详细描述',
    starts_at TIMESTAMP COMMENT '告警开始时间',
    ends_at TIMESTAMP COMMENT '告警结束时间',
    updated_at TIMESTAMP COMMENT '更新时间',
    status VARCHAR(20) DEFAULT 'firing' COMMENT '状态：firing/resolved',
    source VARCHAR(50) COMMENT '来源：prometheus/alertmanager',
    server_id BIGINT COMMENT '关联服务器ID',
    fired_at VARCHAR(50) COMMENT '触发时间字符串',
    duration VARCHAR(50) COMMENT '持续时间',
    INDEX idx_status (status),
    INDEX idx_severity (severity),
    INDEX idx_starts_at (starts_at),
    INDEX idx_fingerprint (fingerprint)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警事件表';

CREATE TABLE IF NOT EXISTS alert_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(255) NOT NULL COMMENT '规则名称',
    expr VARCHAR(500) NOT NULL COMMENT 'PromQL表达式',
    duration VARCHAR(50) COMMENT '持续时间',
    severity VARCHAR(20) COMMENT '严重程度：critical/warning/info',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
    summary VARCHAR(500) COMMENT '告警摘要模板',
    description TEXT COMMENT '详细描述模板',
    server_id BIGINT COMMENT '关联服务器ID',
    group_name VARCHAR(100) COMMENT '规则分组名称',
    source VARCHAR(50) COMMENT '来源：prometheus/manual',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_enabled (enabled),
    INDEX idx_severity (severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则表';

CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    name VARCHAR(100) COMMENT '姓名',
    email VARCHAR(100) COMMENT '邮箱地址',
    phone VARCHAR(20) COMMENT '手机号码',
    role_id BIGINT COMMENT '角色ID',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_at TIMESTAMP COMMENT '最后登录时间',
    INDEX idx_username (username),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

CREATE TABLE IF NOT EXISTS role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色信息表';

CREATE TABLE IF NOT EXISTS permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    resource_type VARCHAR(50) COMMENT '资源类型：menu/button/api',
    resource_path VARCHAR(255) COMMENT '资源路径',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    description VARCHAR(255) COMMENT '权限描述',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_permission_code (permission_code),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限信息表';

CREATE TABLE IF NOT EXISTS role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型：LOGIN/CREATE/UPDATE/DELETE等',
    module VARCHAR(50) NOT NULL COMMENT '模块名称',
    target_type VARCHAR(50) COMMENT '目标类型',
    target_id VARCHAR(100) COMMENT '目标ID',
    target_name VARCHAR(255) COMMENT '目标名称',
    detail TEXT COMMENT '操作详情',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    status TINYINT DEFAULT 1 COMMENT '状态：0-失败 1-成功',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_module (module),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

CREATE TABLE IF NOT EXISTS settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    setting_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    setting_value TEXT COMMENT '配置值',
    setting_type VARCHAR(20) DEFAULT 'string' COMMENT '值类型：string/number/boolean/json',
    description VARCHAR(255) COMMENT '配置描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置表';

CREATE TABLE IF NOT EXISTS logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    server_id BIGINT COMMENT '服务器ID',
    level VARCHAR(20) NOT NULL COMMENT '日志级别：DEBUG/INFO/WARN/ERROR',
    source VARCHAR(100) COMMENT '日志来源',
    message TEXT COMMENT '日志内容',
    time VARCHAR(50) COMMENT '日志时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_server_id (server_id),
    INDEX idx_level (level),
    INDEX idx_time (time),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日志数据表';

CREATE TABLE IF NOT EXISTS traces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    trace_id VARCHAR(100) NOT NULL COMMENT '追踪ID',
    service VARCHAR(100) COMMENT '服务名称',
    operation VARCHAR(255) COMMENT '操作名称',
    duration BIGINT COMMENT '耗时(毫秒)',
    span_count INT DEFAULT 1 COMMENT 'Span数量',
    status VARCHAR(20) DEFAULT 'ok' COMMENT '状态：ok/error',
    start_time VARCHAR(50) COMMENT '开始时间',
    server_id BIGINT COMMENT '服务器ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_trace_id (trace_id),
    INDEX idx_service (service),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链路追踪表';

CREATE TABLE IF NOT EXISTS password_reset_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    token VARCHAR(100) NOT NULL UNIQUE COMMENT '重置令牌',
    expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
    used TINYINT DEFAULT 0 COMMENT '是否已使用：0-未使用 1-已使用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_token (token),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='密码重置令牌表';

CREATE TABLE IF NOT EXISTS server_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '分组名称',
    description VARCHAR(255) COMMENT '分组描述',
    parent_id BIGINT DEFAULT 0 COMMENT '父分组ID',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务器分组表';

CREATE TABLE IF NOT EXISTS server_group_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    server_id BIGINT NOT NULL COMMENT '服务器ID',
    group_id BIGINT NOT NULL COMMENT '分组ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_server_group (server_id, group_id),
    INDEX idx_server_id (server_id),
    INDEX idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务器分组关联表';

INSERT INTO role (role_code, role_name, description) VALUES
('ADMIN', '系统管理员', '拥有系统所有权限，可以管理用户、角色、权限和系统配置'),
('OPERATOR', '运维人员', '可以管理服务器、容器、服务，查看监控数据和告警信息'),
('VIEWER', '访客', '只能查看监控数据和告警信息，无管理权限');

INSERT INTO permission (permission_code, permission_name, resource_type, resource_path, parent_id, sort_order) VALUES
('MONITOR', '监控中心', 'menu', '/monitor', 0, 1),
('SYSTEM', '系统管理', 'menu', '/system', 0, 2),
('DASHBOARD', '监控总览', 'menu', '/monitor/dashboard', 1, 1),
('REALTIME', '实时监控', 'menu', '/monitor/realtime', 1, 2),
('SERVERS', '主机管理', 'menu', '/monitor/servers', 1, 3),
('SERVER_GROUPS', '服务器分组', 'menu', '/monitor/server-groups', 1, 4),
('CONTAINERS', '容器管理', 'menu', '/monitor/containers', 1, 5),
('SERVICES', '服务管理', 'menu', '/monitor/services', 1, 6),
('METRICS', '指标检索', 'menu', '/monitor/metrics', 1, 7),
('LOGS', '日志查询', 'menu', '/monitor/logs', 1, 8),
('TRACES', '链路追踪', 'menu', '/monitor/traces', 1, 9),
('ALERTS', '告警列表', 'menu', '/monitor/alerts', 1, 10),
('ALERT_RULES', '告警规则', 'menu', '/monitor/alert-rules', 1, 11),
('USERS', '用户管理', 'menu', '/monitor/users', 2, 1),
('ROLES', '角色管理', 'menu', '/monitor/roles', 2, 2),
('PERMISSIONS', '权限配置', 'menu', '/monitor/permissions', 2, 3),
('AUDIT_LOGS', '审计日志', 'menu', '/monitor/audit-logs', 2, 4),
('SETTINGS', '系统设置', 'menu', '/monitor/settings', 2, 5);

INSERT INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permission;

INSERT INTO role_permission (role_id, permission_id)
SELECT 2, id FROM permission 
WHERE permission_code IN ('MONITOR', 'DASHBOARD', 'REALTIME', 'SERVERS', 'SERVER_GROUPS', 'CONTAINERS', 
                           'SERVICES', 'METRICS', 'LOGS', 'TRACES', 'ALERTS', 
                           'ALERT_RULES', 'SETTINGS');

INSERT INTO role_permission (role_id, permission_id)
SELECT 3, id FROM permission 
WHERE permission_code IN ('MONITOR', 'DASHBOARD', 'REALTIME', 'METRICS', 
                           'LOGS', 'TRACES', 'ALERTS');

INSERT INTO user (username, password, name, email, role_id, status) VALUES
('admin', '123456', '系统管理员', 'YangSZ03@foxmail.com', 1, 1);

CREATE OR REPLACE VIEW v_user_permission AS
SELECT 
    u.id AS user_id,
    u.username,
    u.name,
    r.role_code,
    r.role_name,
    p.permission_code,
    p.permission_name,
    p.resource_type,
    p.resource_path
FROM user u
LEFT JOIN role r ON u.role_id = r.id
LEFT JOIN role_permission rp ON r.id = rp.role_id
LEFT JOIN permission p ON rp.permission_id = p.id
WHERE u.status = 1 AND r.status = 1 AND p.status = 1;
