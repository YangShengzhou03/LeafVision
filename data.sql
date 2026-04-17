-- =============================================
-- LeafVision 角色权限系统数据库初始化脚本
-- =============================================

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    role_id BIGINT,
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 权限表
CREATE TABLE IF NOT EXISTS permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    permission_name VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_path VARCHAR(255),
    parent_id BIGINT DEFAULT 0,
    description VARCHAR(255),
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_permission_code (permission_code),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 审计日志表
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    operation VARCHAR(50) NOT NULL,
    module VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id VARCHAR(100),
    target_name VARCHAR(255),
    detail TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_module (module),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 初始化角色数据
-- =============================================
INSERT INTO role (role_code, role_name, description) VALUES
('ADMIN', '系统管理员', '拥有系统所有权限，可以管理用户、角色、权限和系统配置'),
('OPERATOR', '运维人员', '可以管理服务器、容器、服务，查看监控数据和告警信息'),
('VIEWER', '访客', '只能查看监控数据和告警信息，无管理权限');

-- =============================================
-- 初始化权限数据（菜单权限）
-- =============================================
INSERT INTO permission (permission_code, permission_name, resource_type, resource_path, parent_id, sort_order) VALUES
('MONITOR', '监控中心', 'menu', '/monitor', 0, 1),
('SYSTEM', '系统管理', 'menu', '/system', 0, 2),
('DASHBOARD', '监控总览', 'menu', '/monitor/dashboard', 1, 1),
('REALTIME', '实时监控', 'menu', '/monitor/realtime', 1, 2),
('SERVERS', '主机管理', 'menu', '/monitor/servers', 1, 3),
('CONTAINERS', '容器管理', 'menu', '/monitor/containers', 1, 4),
('SERVICES', '服务管理', 'menu', '/monitor/services', 1, 5),
('METRICS', '指标检索', 'menu', '/monitor/metrics', 1, 6),
('LOGS', '日志查询', 'menu', '/monitor/logs', 1, 7),
('TRACES', '链路追踪', 'menu', '/monitor/traces', 1, 8),
('ALERTS', '告警列表', 'menu', '/monitor/alerts', 1, 9),
('ALERT_RULES', '告警规则', 'menu', '/monitor/alert-rules', 1, 10),
('USERS', '用户管理', 'menu', '/monitor/users', 2, 1),
('ROLES', '角色管理', 'menu', '/monitor/roles', 2, 2),
('PERMISSIONS', '权限配置', 'menu', '/monitor/permissions', 2, 3),
('AUDIT_LOGS', '审计日志', 'menu', '/monitor/audit-logs', 2, 4),
('SETTINGS', '系统设置', 'menu', '/monitor/settings', 2, 5);

-- =============================================
-- 初始化角色权限关联
-- =============================================

INSERT INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permission;

INSERT INTO role_permission (role_id, permission_id)
SELECT 2, id FROM permission 
WHERE permission_code IN ('MONITOR', 'DASHBOARD', 'REALTIME', 'SERVERS', 'CONTAINERS', 
                           'SERVICES', 'METRICS', 'LOGS', 'TRACES', 'ALERTS', 
                           'ALERT_RULES', 'SETTINGS');

INSERT INTO role_permission (role_id, permission_id)
SELECT 3, id FROM permission 
WHERE permission_code IN ('MONITOR', 'DASHBOARD', 'REALTIME', 'METRICS', 
                           'LOGS', 'TRACES', 'ALERTS');

-- =============================================
-- 初始化默认管理员账号
-- =============================================
INSERT INTO user (username, password, name, email, role_id, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'YangSZ03@foxmail.com', 1, 1);

-- =============================================
-- 创建视图：用户权限视图
-- =============================================
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

-- =============================================
-- 权限管理示例
-- =============================================

-- 给运维人员添加用户管理权限
-- INSERT INTO role_permission (role_id, permission_id)
-- SELECT 2, id FROM permission WHERE permission_code = 'USERS';

-- 移除访客的实时监控权限
-- DELETE FROM role_permission 
-- WHERE role_id = 3 AND permission_id = (SELECT id FROM permission WHERE permission_code = 'REALTIME');

-- 添加新角色
-- INSERT INTO role (role_code, role_name, description) VALUES ('DEVELOPER', '开发人员', '可以查看监控数据和日志');

-- 给新角色分配权限
-- INSERT INTO role_permission (role_id, permission_id)
-- SELECT (SELECT id FROM role WHERE role_code = 'DEVELOPER'), id 
-- FROM permission 
-- WHERE permission_code IN ('DASHBOARD', 'METRICS', 'LOGS', 'TRACES');
