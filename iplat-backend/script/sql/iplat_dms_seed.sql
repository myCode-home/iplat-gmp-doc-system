-- ----------------------------
-- GMP 文件管理（DMS）种子数据：字典 / 参数 / 审批流定义 / 演示业务数据
-- 幂等：先删除同 id/编码 数据再插入
-- ----------------------------

-- 字典类型
delete from sys_dict_type where dict_type = 'dms_template_type';
insert into sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, remark) values (9001, '000000', '模板类型', 'dms_template_type', 103, 1, sysdate(), 'DMS 演示字典');
delete from sys_dict_type where dict_type = 'dms_transact_type';
insert into sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, remark) values (9002, '000000', '办理类型', 'dms_transact_type', 103, 1, sysdate(), 'DMS 演示字典');
delete from sys_dict_type where dict_type = 'dms_track_state';
insert into sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, remark) values (9003, '000000', '追踪状态', 'dms_track_state', 103, 1, sysdate(), 'DMS 演示字典');
delete from sys_dict_type where dict_type = 'dms_edit_mode';
insert into sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, remark) values (9004, '000000', '编制方式', 'dms_edit_mode', 103, 1, sysdate(), 'DMS 演示字典');
delete from sys_dict_type where dict_type = 'wf_task_status';
insert into sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, remark) values (9005, '000000', '任务状态', 'wf_task_status', 103, 1, sysdate(), 'DMS 演示字典');

-- 字典数据
delete from sys_dict_data where dict_type = 'dms_template_type' and dict_value = 'SOP';
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark) values (9101, '000000', 1, '规程模板', 'SOP', 'dms_template_type', '', 'primary', 'N', 103, 1, sysdate(), '');
delete from sys_dict_data where dict_type = 'dms_template_type' and dict_value = 'REC';
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark) values (9102, '000000', 2, '记录模板', 'REC', 'dms_template_type', '', 'success', 'N', 103, 1, sysdate(), '');
delete from sys_dict_data where dict_type = 'dms_template_type' and dict_value = 'VAL';
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark) values (9103, '000000', 3, '验证模板', 'VAL', 'dms_template_type', '', 'info', 'N', 103, 1, sysdate(), '');
delete from sys_dict_data where dict_type = 'dms_template_type' and dict_value = 'DOC';
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark) values (9104, '000000', 4, '文档模板', 'DOC', 'dms_template_type', '', 'warning', 'N', 103, 1, sysdate(), '');
delete from sys_dict_data where dict_type = 'dms_transact_type' and dict_value = 'add';
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark) values (9111, '000000', 1, '新增', 'add', 'dms_transact_type', '', 'primary', 'N', 103, 1, sysdate(), '');
delete from sys_dict_data where dict_type = 'dms_transact_type' and dict_value = 'revise';
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark) values (9112, '000000', 2, '修订', 'revise', 'dms_transact_type', '', 'warning', 'N', 103, 1, sysdate(), '');
delete from sys_dict_data where dict_type = 'dms_transact_type' and dict_value = 'copy';
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark) values (9113, '000000', 3, '复制', 'copy', 'dms_transact_type', '', 'info', 'N', 103, 1, sysdate(), '');
delete from sys_dict_data where dict_type = 'dms_track_state' and dict_value = 'edit';
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark) values (9121, '000000', 1, '编制中', 'edit', 'dms_track_state', '', 'info', 'N', 103, 1, sysdate(), '');
delete from sys_dict_data where dict_type = 'dms_track_state' and dict_value = 'approving';
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark) values (9122, '000000', 2, '审批中', 'approving', 'dms_track_state', '', 'warning', 'N', 103, 1, sysdate(), '');
delete from sys_dict_data where dict_type = 'dms_track_state' and dict_value = 'approved';
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark) values (9123, '000000', 3, '已审批', 'approved', 'dms_track_state', '', 'success', 'N', 103, 1, sysdate(), '');
delete from sys_dict_data where dict_type = 'dms_track_state' and dict_value = 'retreat';
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark) values (9124, '000000', 4, '已退回', 'retreat', 'dms_track_state', '', 'danger', 'N', 103, 1, sysdate(), '');
delete from sys_dict_data where dict_type = 'dms_track_state' and dict_value = 'failure';
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark) values (9125, '000000', 5, '已失效', 'failure', 'dms_track_state', '', 'danger', 'N', 103, 1, sysdate(), '');
delete from sys_dict_data where dict_type = 'dms_edit_mode' and dict_value = 'online';
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark) values (9131, '000000', 1, '在线编制', 'online', 'dms_edit_mode', '', 'primary', 'N', 103, 1, sysdate(), '');
delete from sys_dict_data where dict_type = 'dms_edit_mode' and dict_value = 'offline';
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark) values (9132, '000000', 2, '离线上传', 'offline', 'dms_edit_mode', '', 'info', 'N', 103, 1, sysdate(), '');
delete from sys_dict_data where dict_type = 'wf_task_status' and dict_value = 'waiting';
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark) values (9141, '000000', 1, '待办', 'waiting', 'wf_task_status', '', 'warning', 'N', 103, 1, sysdate(), '');
delete from sys_dict_data where dict_type = 'wf_task_status' and dict_value = 'finish';
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark) values (9142, '000000', 2, '已办', 'finish', 'wf_task_status', '', 'success', 'N', 103, 1, sysdate(), '');

-- 系统参数
delete from sys_config where config_key = 'dms_isProduceDoc';
insert into sys_config (config_id, tenant_id, config_name, config_key, config_value, config_type, create_dept, create_by, create_time, remark) values (901, '000000', 'DMS-文档编制-是否启用自动创建', 'dms_isProduceDoc', 'false', 'Y', 103, 1, sysdate(), 'DMS 演示参数');
delete from sys_config where config_key = 'dms_isBatchapplyForm';
insert into sys_config (config_id, tenant_id, config_name, config_key, config_value, config_type, create_dept, create_by, create_time, remark) values (902, '000000', 'DMS-文档编制-是否启用批量申请单', 'dms_isBatchapplyForm', 'false', 'Y', 103, 1, sysdate(), 'DMS 演示参数');
delete from sys_config where config_key = 'dms_wayOfPrint';
insert into sys_config (config_id, tenant_id, config_name, config_key, config_value, config_type, create_dept, create_by, create_time, remark) values (903, '000000', 'DMS-打印方式', 'dms_wayOfPrint', 'browser', 'Y', 103, 1, sysdate(), 'DMS 演示参数');

-- 通用审批流定义（warm-flow）
delete from flow_skip where definition_id = 9001;
delete from flow_node where definition_id = 9001;
delete from flow_definition where id = 9001;
insert into flow_definition (id, flow_code, flow_name, category, version, is_publish, form_custom, activity_status, create_time, del_flag, tenant_id) values (9001, 'dms_common_approval', '通用审批流程', '103', '1', 1, 'N', 1, sysdate(), '0', '000000');
insert into flow_node (id, node_type, definition_id, node_code, node_name, permission_flag, node_ratio, any_node_skip, form_custom, version, create_time, del_flag, tenant_id) values (9101, 0, 9001, 'start', '开始', null, 1, '1', 'N', '1', sysdate(), '0', '000000');
insert into flow_node (id, node_type, definition_id, node_code, node_name, permission_flag, node_ratio, any_node_skip, form_custom, version, create_time, del_flag, tenant_id) values (9102, 1, 9001, 'apply', '申请人', '${initiator}', 1, '1', 'N', '1', sysdate(), '0', '000000');
insert into flow_node (id, node_type, definition_id, node_code, node_name, permission_flag, node_ratio, any_node_skip, form_custom, version, create_time, del_flag, tenant_id) values (9103, 1, 9001, 'approve', '审批', '1', 1, '1', 'N', '1', sysdate(), '0', '000000');
insert into flow_node (id, node_type, definition_id, node_code, node_name, permission_flag, node_ratio, any_node_skip, form_custom, version, create_time, del_flag, tenant_id) values (9104, 2, 9001, 'end', '结束', null, 1, '1', 'N', '1', sysdate(), '0', '000000');
insert into flow_skip (id, definition_id, now_node_code, now_node_type, next_node_code, next_node_type, skip_name, skip_type, create_time, del_flag, tenant_id) values (9201, 9001, 'start', 0, 'apply', 1, '提交', 'PASS', sysdate(), '0', '000000');
insert into flow_skip (id, definition_id, now_node_code, now_node_type, next_node_code, next_node_type, skip_name, skip_type, create_time, del_flag, tenant_id) values (9202, 9001, 'apply', 1, 'approve', 1, '同意', 'PASS', sysdate(), '0', '000000');
insert into flow_skip (id, definition_id, now_node_code, now_node_type, next_node_code, next_node_type, skip_name, skip_type, create_time, del_flag, tenant_id) values (9203, 9001, 'approve', 1, 'apply', 1, '退回', 'REJECT', sysdate(), '0', '000000');
insert into flow_skip (id, definition_id, now_node_code, now_node_type, next_node_code, next_node_type, skip_name, skip_type, create_time, del_flag, tenant_id) values (9204, 9001, 'approve', 1, 'end', 2, '同意', 'PASS', sysdate(), '0', '000000');

-- 文件类型树
delete from dms_document_type where id = '900001';
insert into dms_document_type (id, parent_id, sort_code, sort_name, status, remark, tenant_id, create_dept, create_by, create_time) values ('900001', 'root', 'SOP', '标准操作规程', 'active', '演示分类', '000000', 103, 1, sysdate());
delete from dms_document_type where id = '900002';
insert into dms_document_type (id, parent_id, sort_code, sort_name, status, remark, tenant_id, create_dept, create_by, create_time) values ('900002', 'root', 'REC', '记录表格', 'active', '演示分类', '000000', 103, 1, sysdate());
delete from dms_document_type where id = '900003';
insert into dms_document_type (id, parent_id, sort_code, sort_name, status, remark, tenant_id, create_dept, create_by, create_time) values ('900003', 'root', 'TMPL', '模板文件', 'active', '演示分类', '000000', 103, 1, sysdate());
delete from dms_document_type where id = '900004';
insert into dms_document_type (id, parent_id, sort_code, sort_name, status, remark, tenant_id, create_dept, create_by, create_time) values ('900004', 'root', 'VAL', '验证文件', 'failure', '演示分类', '000000', 103, 1, sysdate());

-- 文件类型属性
delete from dms_document_type_attribute where id = 9301;
insert into dms_document_type_attribute (id, sort_id, attribute_code, attribute_name, if_required, value_type, applicable_mark, status, sort, tenant_id, create_dept, create_by, create_time) values (9301, '900001', 'docNo', '文件编号', 'true', 'input', 'documentFlow', 'active', 1, '000000', 103, 1, sysdate());
delete from dms_document_type_attribute where id = 9302;
insert into dms_document_type_attribute (id, sort_id, attribute_code, attribute_name, if_required, value_type, applicable_mark, status, sort, tenant_id, create_dept, create_by, create_time) values (9302, '900001', 'effectiveDate', '生效日期', 'false', 'date', 'documentFlow', 'active', 2, '000000', 103, 1, sysdate());
delete from dms_document_type_attribute where id = 9303;
insert into dms_document_type_attribute (id, sort_id, attribute_code, attribute_name, if_required, value_type, applicable_mark, status, sort, tenant_id, create_dept, create_by, create_time) values (9303, '900001', 'storageCondition', '贮存条件', 'false', 'textarea', 'printApply', 'active', 3, '000000', 103, 1, sysdate());
delete from dms_document_type_attribute where id = 9304;
insert into dms_document_type_attribute (id, sort_id, attribute_code, attribute_name, if_required, value_type, applicable_mark, status, sort, tenant_id, create_dept, create_by, create_time) values (9304, '900002', 'batchNo', '批号', 'true', 'input', 'documentFlow', 'active', 1, '000000', 103, 1, sysdate());

-- 文件库演示数据
delete from dms_document_lib where id = 1001;
insert into dms_document_lib (id, dl_code, dl_name, dl_type, dl_version, dl_level, doc_source, doc_operation_source, use_state, dl_dept_id, dl_dept_name, issue_dept_id, issue_dept_name, drafter_id, draft_nick_name, status, active_status, tenant_id, create_dept, create_by, create_time) values (1001, 'GMP-SOP-001', '生产设备清洁标准操作规程', '900001', 'V1.0', 'A', 'new', 'fromInput', 'active', 103, '生产部', 103, '生产部', 1, 'admin', 'active', 'active', '000000', 103, 1, sysdate());
delete from dms_document_lib where id = 1002;
insert into dms_document_lib (id, dl_code, dl_name, dl_type, dl_version, dl_level, doc_source, doc_operation_source, use_state, dl_dept_id, dl_dept_name, issue_dept_id, issue_dept_name, drafter_id, draft_nick_name, status, active_status, tenant_id, create_dept, create_by, create_time) values (1002, 'GMP-SOP-002', '洁净区环境监测管理规程', '900001', 'V2.0', 'A', 'new', 'fromInput', 'active', 103, '质量部', 103, '质量部', 1, 'admin', 'active', 'active', '000000', 103, 1, sysdate());
delete from dms_document_lib where id = 1003;
insert into dms_document_lib (id, dl_code, dl_name, dl_type, dl_version, dl_level, doc_source, doc_operation_source, use_state, dl_dept_id, dl_dept_name, issue_dept_id, issue_dept_name, drafter_id, draft_nick_name, status, active_status, tenant_id, create_dept, create_by, create_time) values (1003, 'GMP-SOP-003', '物料入库验收操作规程', '900001', 'V1.0', 'B', 'revise', 'manualRevision', 'active', 103, 'QA部', 103, 'QA部', 1, 'admin', 'active', 'active', '000000', 103, 1, sysdate());
delete from dms_document_lib where id = 1004;
insert into dms_document_lib (id, dl_code, dl_name, dl_type, dl_version, dl_level, doc_source, doc_operation_source, use_state, dl_dept_id, dl_dept_name, issue_dept_id, issue_dept_name, drafter_id, draft_nick_name, status, active_status, tenant_id, create_dept, create_by, create_time) values (1004, 'GMP-SOP-004', '产品放行管理规程', '900004', 'V1.0', 'A', 'new', 'autoCreate', 'active', 103, 'QC部', 103, 'QC部', 1, 'admin', 'active', 'active', '000000', 103, 1, sysdate());
delete from dms_document_lib where id = 1005;
insert into dms_document_lib (id, dl_code, dl_name, dl_type, dl_version, dl_level, doc_source, doc_operation_source, use_state, dl_dept_id, dl_dept_name, issue_dept_id, issue_dept_name, drafter_id, draft_nick_name, status, active_status, tenant_id, create_dept, create_by, create_time) values (1005, 'GMP-SOP-005', '偏差处理标准操作规程', '900001', 'V2.0', 'B', 'new', 'fromInput', 'pending', 103, '设备部', 103, '设备部', 1, 'admin', 'active', 'active', '000000', 103, 1, sysdate());
delete from dms_document_lib where id = 1006;
insert into dms_document_lib (id, dl_code, dl_name, dl_type, dl_version, dl_level, doc_source, doc_operation_source, use_state, dl_dept_id, dl_dept_name, issue_dept_id, issue_dept_name, drafter_id, draft_nick_name, status, active_status, tenant_id, create_dept, create_by, create_time) values (1006, 'GMP-SOP-006', '变更控制管理规程', '900001', 'V1.0', 'A', 'new', 'fromInput', 'pending', 103, '注册部', 103, '注册部', 1, 'admin', 'active', 'active', '000000', 103, 1, sysdate());
delete from dms_document_lib where id = 1007;
insert into dms_document_lib (id, dl_code, dl_name, dl_type, dl_version, dl_level, doc_source, doc_operation_source, use_state, dl_dept_id, dl_dept_name, issue_dept_id, issue_dept_name, drafter_id, draft_nick_name, status, active_status, tenant_id, create_dept, create_by, create_time) values (1007, 'GMP-SOP-007', '设备维护保养操作规程', '900001', 'V1.0', 'A', 'revise', 'manualRevision', 'invalid', 103, '生产部', 103, '生产部', 1, 'admin', 'active', 'active', '000000', 103, 1, sysdate());
delete from dms_document_lib where id = 1008;
insert into dms_document_lib (id, dl_code, dl_name, dl_type, dl_version, dl_level, doc_source, doc_operation_source, use_state, dl_dept_id, dl_dept_name, issue_dept_id, issue_dept_name, drafter_id, draft_nick_name, status, active_status, tenant_id, create_dept, create_by, create_time) values (1008, 'GMP-REC-001', '产品批生产记录模板', '900002', 'V1.0', 'A', 'new', 'fromInput', 'active', 103, '生产部', 103, '生产部', 1, 'admin', 'active', 'active', '000000', 103, 1, sysdate());

-- 文件替代/关联关系
delete from dms_document_lib_replace where id = 9401;
insert into dms_document_lib_replace (id, lib_id, replace_lib_id, tenant_id, create_dept, create_by, create_time) values (9401, 1003, 1007, '000000', 103, 1, sysdate());
delete from dms_document_lib_relate where id = 9411;
insert into dms_document_lib_relate (id, lib_id, relate_lib_id, link_type, tenant_id, create_dept, create_by, create_time) values (9411, 1001, 1002, '2', '000000', 103, 1, sysdate());
delete from dms_document_lib_relate where id = 9412;
insert into dms_document_lib_relate (id, lib_id, relate_lib_id, link_type, tenant_id, create_dept, create_by, create_time) values (9412, 1001, 1004, '1', '000000', 103, 1, sysdate());

-- 记录库演示数据
delete from dms_document_record_lib where id = 1101;
insert into dms_document_record_lib (id, dlr_code, dlr_name, dlr_version, dlr_active_time, document_lib_id, document_code, document_type, edit_way, status, active_status, dl_dept_name, tenant_id, create_dept, create_by, create_time) values (1101, 'REC-001', '设备清洁记录', 'V1.0', sysdate(), 1001, 'GMP-SOP-001', '900002', 'online', 'active', 'active', '生产部', '000000', 103, 1, sysdate());
delete from dms_document_record_lib where id = 1102;
insert into dms_document_record_lib (id, dlr_code, dlr_name, dlr_version, dlr_active_time, document_lib_id, document_code, document_type, edit_way, status, active_status, dl_dept_name, tenant_id, create_dept, create_by, create_time) values (1102, 'REC-002', '洁净区温湿度监测记录', 'V1.0', sysdate(), 1002, 'GMP-SOP-002', '900002', 'online', 'active', 'active', '生产部', '000000', 103, 1, sysdate());
delete from dms_document_record_lib where id = 1103;
insert into dms_document_record_lib (id, dlr_code, dlr_name, dlr_version, dlr_active_time, document_lib_id, document_code, document_type, edit_way, status, active_status, dl_dept_name, tenant_id, create_dept, create_by, create_time) values (1103, 'REC-003', '物料验收记录', 'V1.0', sysdate(), 1001, 'GMP-SOP-001', '900002', 'online', 'active', 'active', '生产部', '000000', 103, 1, sysdate());
delete from dms_document_record_lib where id = 1104;
insert into dms_document_record_lib (id, dlr_code, dlr_name, dlr_version, dlr_active_time, document_lib_id, document_code, document_type, edit_way, status, active_status, dl_dept_name, tenant_id, create_dept, create_by, create_time) values (1104, 'REC-004', '偏差处理记录', 'V1.0', sysdate(), 1003, 'GMP-SOP-003', '900002', 'online', 'active', 'active', '生产部', '000000', 103, 1, sysdate());
delete from dms_document_record_lib where id = 1105;
insert into dms_document_record_lib (id, dlr_code, dlr_name, dlr_version, dlr_active_time, document_lib_id, document_code, document_type, edit_way, status, active_status, dl_dept_name, tenant_id, create_dept, create_by, create_time) values (1105, 'REC-005', '设备维护保养记录', 'V1.0', sysdate(), 1007, 'GMP-SOP-007', '900002', 'online', 'active', 'deactiving', '生产部', '000000', 103, 1, sysdate());

-- 模板库演示数据
delete from dms_tmpl_lib where id = 1201;
insert into dms_tmpl_lib (id, tmpl_code, tmpl_name, tmpl_category, tmpl_version, tmpl_purpose, tmpl_dept, tmpl_dept_name, status, active_status, tenant_id, create_dept, create_by, create_time) values (1201, 'TMPL-001', '标准操作规程模板', 'SOP', 'V1.0', 'document', 103, '研发部门', 'active', 'active', '000000', 103, 1, sysdate());
delete from dms_tmpl_lib where id = 1202;
insert into dms_tmpl_lib (id, tmpl_code, tmpl_name, tmpl_category, tmpl_version, tmpl_purpose, tmpl_dept, tmpl_dept_name, status, active_status, tenant_id, create_dept, create_by, create_time) values (1202, 'TMPL-002', '记录表格模板', 'REC', 'V1.0', 'record', 103, '研发部门', 'active', 'active', '000000', 103, 1, sysdate());
delete from dms_tmpl_lib where id = 1203;
insert into dms_tmpl_lib (id, tmpl_code, tmpl_name, tmpl_category, tmpl_version, tmpl_purpose, tmpl_dept, tmpl_dept_name, status, active_status, tenant_id, create_dept, create_by, create_time) values (1203, 'TMPL-003', '验证方案模板', 'VAL', 'V1.0', 'document', 103, '研发部门', 'active', 'active', '000000', 103, 1, sysdate());
delete from dms_tmpl_lib where id = 1204;
insert into dms_tmpl_lib (id, tmpl_code, tmpl_name, tmpl_category, tmpl_version, tmpl_purpose, tmpl_dept, tmpl_dept_name, status, active_status, tenant_id, create_dept, create_by, create_time) values (1204, 'TMPL-004', '批生产记录模板', 'REC', 'V2.0', 'record', 103, '研发部门', 'active', 'active', '000000', 103, 1, sysdate());

-- 模板编制演示数据
delete from dms_tmpl_preparation where id = 1301;
insert into dms_tmpl_preparation (id, tmpl_code, tmpl_name, tmpl_category, tmpl_version, tmpl_purpose, tmpl_dept, tmpl_dept_name, status, active_status, tenant_id, create_dept, create_by, create_time) values (1301, 'TMPL-005', '偏差处理模板', 'SOP', 'V1.0', 'document', 103, '研发部门', 'edit', 'active', '000000', 103, 1, sysdate());
delete from dms_tmpl_preparation where id = 1302;
insert into dms_tmpl_preparation (id, tmpl_code, tmpl_name, tmpl_category, tmpl_version, tmpl_purpose, tmpl_dept, tmpl_dept_name, status, active_status, tenant_id, create_dept, create_by, create_time) values (1302, 'TMPL-006', '变更控制模板', 'SOP', 'V1.0', 'document', 103, '研发部门', 'approving', 'active', '000000', 103, 1, sysdate());
delete from dms_tmpl_preparation where id = 1303;
insert into dms_tmpl_preparation (id, tmpl_code, tmpl_name, tmpl_category, tmpl_version, tmpl_purpose, tmpl_dept, tmpl_dept_name, status, active_status, tenant_id, create_dept, create_by, create_time) values (1303, 'TMPL-007', '培训记录模板', 'REC', 'V1.0', 'record', 103, '研发部门', 'approved', 'active', '000000', 103, 1, sysdate());

-- 文档编制演示数据
delete from dms_doc_create where id = 1401;
insert into dms_doc_create (id, document_code, document_name, document_type, document_version, document_level, document_dept_id, document_dept_code, document_dept_name, edit_way, operate_source, order_number, status, train_status, drafter_id, drafter_name, tenant_id, create_dept, create_by, create_time) values (1401, 'DOC-101', '年度培训计划管理规程', '900001', 'V1.0', 'A', 103, '103', '研发部门', 'online', 'fromInput', 'DOC-1401', 'approved', 'needTrain', 1, 'admin', '000000', 103, 1, sysdate());
delete from dms_doc_create where id = 1402;
insert into dms_doc_create (id, document_code, document_name, document_type, document_version, document_level, document_dept_id, document_dept_code, document_dept_name, edit_way, operate_source, order_number, status, train_status, drafter_id, drafter_name, tenant_id, create_dept, create_by, create_time) values (1402, 'DOC-102', '数据完整性管理规程', '900001', 'V1.0', 'A', 103, '103', '研发部门', 'online', 'fromInput', 'DOC-1402', 'approved', 'trained', 1, 'admin', '000000', 103, 1, sysdate());
delete from dms_doc_create where id = 1403;
insert into dms_doc_create (id, document_code, document_name, document_type, document_version, document_level, document_dept_id, document_dept_code, document_dept_name, edit_way, operate_source, order_number, status, train_status, drafter_id, drafter_name, tenant_id, create_dept, create_by, create_time) values (1403, 'DOC-103', '供应商审计管理规程', '900001', 'V1.0', 'A', 103, '103', '研发部门', 'offline', 'fromInput', 'DOC-1403', 'approved', 'noNeedTrain', 1, 'admin', '000000', 103, 1, sysdate());
delete from dms_doc_create where id = 1404;
insert into dms_doc_create (id, document_code, document_name, document_type, document_version, document_level, document_dept_id, document_dept_code, document_dept_name, edit_way, operate_source, order_number, status, train_status, drafter_id, drafter_name, tenant_id, create_dept, create_by, create_time) values (1404, 'DOC-104', '留样管理操作规程', '900001', 'V1.0', 'A', 103, '103', '研发部门', 'online', 'fromInput', 'DOC-1404', 'edit', null, 1, 'admin', '000000', 103, 1, sysdate());
delete from dms_doc_create where id = 1405;
insert into dms_doc_create (id, document_code, document_name, document_type, document_version, document_level, document_dept_id, document_dept_code, document_dept_name, edit_way, operate_source, order_number, status, train_status, drafter_id, drafter_name, tenant_id, create_dept, create_by, create_time) values (1405, 'DOC-105', '返工管理规程', '900001', 'V1.0', 'A', 103, '103', '研发部门', 'online', 'manualRevision', 'DOC-1405', 'approving', null, 1, 'admin', '000000', 103, 1, sysdate());

-- 打印申请演示数据
delete from dms_print_apply where id = 1501;
insert into dms_print_apply (id, apply_code, apply_reason, doc_or_record_type, doc_or_record_id, doc_code, doc_name, doc_version, doc_status, print_type, copies, pages, print_source, status, tenant_id, create_dept, create_by, create_time) values (1501, 'PA-001', '文件受控打印申请', 'document', 1001, 'GMP-SOP-001', '生产设备清洁标准操作规程', 'V1.0', 'active', 'docPrint', 2, 12, 'add', 'approved', '000000', 103, 1, sysdate());
delete from dms_print_apply where id = 1502;
insert into dms_print_apply (id, apply_code, apply_reason, doc_or_record_type, doc_or_record_id, doc_code, doc_name, doc_version, doc_status, print_type, copies, pages, print_source, status, tenant_id, create_dept, create_by, create_time) values (1502, 'PA-002', '文件受控打印申请', 'document', 1001, 'GMP-SOP-002', '洁净区环境监测管理规程', 'V1.0', 'active', 'docPrint', 2, 12, 'add', 'approved', '000000', 103, 1, sysdate());
delete from dms_print_apply where id = 1503;
insert into dms_print_apply (id, apply_code, apply_reason, doc_or_record_type, doc_or_record_id, doc_code, doc_name, doc_version, doc_status, print_type, copies, pages, print_source, status, tenant_id, create_dept, create_by, create_time) values (1503, 'PA-003', '文件受控打印申请', 'document', 1001, 'GMP-SOP-003', '物料入库验收操作规程', 'V1.0', 'active', 'docPrint', 2, 12, 'add', 'approving', '000000', 103, 1, sysdate());
delete from dms_print_apply where id = 1504;
insert into dms_print_apply (id, apply_code, apply_reason, doc_or_record_type, doc_or_record_id, doc_code, doc_name, doc_version, doc_status, print_type, copies, pages, print_source, status, tenant_id, create_dept, create_by, create_time) values (1504, 'PA-004', '文件受控打印申请', 'document', 1001, 'GMP-SOP-004', '产品放行管理规程', 'V1.0', 'active', 'docPrint', 2, 12, 'add', 'edit', '000000', 103, 1, sysdate());

-- 打印明细演示数据
delete from dms_print_doc_info where id = 1601;
insert into dms_print_doc_info (id, print_apply_id, apply_code, doc_or_record_type, doc_or_record_id, doc_code, doc_name, doc_version, doc_type, doc_status, copies, pages, print_type, state, print_time, remark, tenant_id, create_dept, create_by, create_time) values (1601, 1501, 'PA-001', 'document', 1001, 'GMP-SOP-001', '生产设备清洁标准操作规程', 'V1.0', '900001', 'active', 2, 12, 'docPrint', 'printed', sysdate(), '演示数据', '000000', 103, 1, sysdate());
delete from dms_print_doc_info where id = 1602;
insert into dms_print_doc_info (id, print_apply_id, apply_code, doc_or_record_type, doc_or_record_id, doc_code, doc_name, doc_version, doc_type, doc_status, copies, pages, print_type, state, print_time, remark, tenant_id, create_dept, create_by, create_time) values (1602, 1501, 'PA-001', 'document', 1001, 'GMP-SOP-001', '生产设备清洁标准操作规程', 'V1.0', '900001', 'active', 2, 12, 'docPrint', 'printed', sysdate(), '演示数据', '000000', 103, 1, sysdate());
delete from dms_print_doc_info where id = 1603;
insert into dms_print_doc_info (id, print_apply_id, apply_code, doc_or_record_type, doc_or_record_id, doc_code, doc_name, doc_version, doc_type, doc_status, copies, pages, print_type, state, print_time, remark, tenant_id, create_dept, create_by, create_time) values (1603, 1502, 'PA-002', 'document', 1001, 'GMP-SOP-002', '洁净区环境监测管理规程', 'V1.0', '900001', 'active', 2, 12, 'docPrint', 'received', sysdate(), '演示数据', '000000', 103, 1, sysdate());
delete from dms_print_doc_info where id = 1604;
insert into dms_print_doc_info (id, print_apply_id, apply_code, doc_or_record_type, doc_or_record_id, doc_code, doc_name, doc_version, doc_type, doc_status, copies, pages, print_type, state, print_time, remark, tenant_id, create_dept, create_by, create_time) values (1604, 1502, 'PA-002', 'document', 1001, 'GMP-SOP-002', '洁净区环境监测管理规程', 'V1.0', '900001', 'active', 2, 12, 'docPrint', 'returned', sysdate(), '演示数据', '000000', 103, 1, sysdate());
delete from dms_print_doc_info where id = 1605;
insert into dms_print_doc_info (id, print_apply_id, apply_code, doc_or_record_type, doc_or_record_id, doc_code, doc_name, doc_version, doc_type, doc_status, copies, pages, print_type, state, print_time, remark, tenant_id, create_dept, create_by, create_time) values (1605, 1502, 'PA-002', 'document', 1001, 'GMP-SOP-002', '洁净区环境监测管理规程', 'V1.0', '900001', 'active', 2, 12, 'docPrint', 'closed', sysdate(), '演示数据', '000000', 103, 1, sysdate());
delete from dms_print_doc_info where id = 1606;
insert into dms_print_doc_info (id, print_apply_id, apply_code, doc_or_record_type, doc_or_record_id, doc_code, doc_name, doc_version, doc_type, doc_status, copies, pages, print_type, state, print_time, remark, tenant_id, create_dept, create_by, create_time) values (1606, 1502, 'PA-002', 'document', 1001, 'GMP-SOP-002', '洁净区环境监测管理规程', 'V1.0', '900001', 'active', 2, 12, 'docPrint', 'destroyed', sysdate(), '演示数据', '000000', 103, 1, sysdate());

-- 批量打印演示数据
delete from dms_print_apply_batch where id = 1701;
insert into dms_print_apply_batch (id, apply_code, apply_reason, doc_or_record_type, doc_code, doc_name, doc_version, doc_status, status, tenant_id, create_dept, create_by, create_time) values (1701, 'BP-001', '年度文件批量打印', 'document', 'GMP-SOP-001', '生产设备清洁标准操作规程', 'V1.0', 'active', 'approved', '000000', 103, 1, sysdate());
delete from dms_print_apply_batch_detail where id = 1702;
insert into dms_print_apply_batch_detail (id, batch_id, apply_copies, print_type, doc_or_record_type, doc_or_record_id, pages, whether_print_page, doc_code, doc_name, doc_version, doc_dept, doc_type, doc_status, tenant_id, create_dept, create_by, create_time) values (1702, 1701, 3, 'docPrint', 'document', 1001, 12, 'true', 'GMP-SOP-001', '生产设备清洁标准操作规程', 'V1.0', '生产部', '900001', 'active', '000000', 103, 1, sysdate());
delete from dms_print_apply_batch_detail where id = 1703;
insert into dms_print_apply_batch_detail (id, batch_id, apply_copies, print_type, doc_or_record_type, doc_or_record_id, pages, whether_print_page, doc_code, doc_name, doc_version, doc_dept, doc_type, doc_status, tenant_id, create_dept, create_by, create_time) values (1703, 1701, 2, 'recPrint', 'record', 1101, 8, 'false', 'REC-001', '设备清洁记录', 'V1.0', '生产部', '900002', 'active', '000000', 103, 1, sysdate());

-- 借阅演示数据
delete from dms_document_lend_order where id in (1801, 1802);
insert into dms_document_lend_order (id, order_number, doc_lib_id, document_code, document_name, lending_period, is_download, return_date, status, tenant_id, create_dept, create_by, create_time) values (1801, 'LEND-001', 1001, 'GMP-SOP-001', '生产设备清洁标准操作规程', '7', '1', date_add(sysdate(), interval 7 day), 'approved', '000000', 103, 1, sysdate());
insert into dms_document_lend_order (id, order_number, doc_lib_id, document_code, document_name, lending_period, is_download, return_date, status, tenant_id, create_dept, create_by, create_time) values (1802, 'LEND-002', 1002, 'GMP-SOP-002', '洁净区环境监测管理规程', '14', '0', date_add(sysdate(), interval 14 day), 'edit', '000000', 103, 1, sysdate());
delete from dms_document_lend_lib where id = 1811;
insert into dms_document_lend_lib (id, order_id, doc_lib_id, document_code, document_name, lending_period, is_download, lend_date, return_date, status, tenant_id, create_dept, create_by, create_time) values (1811, 1801, 1001, 'GMP-SOP-001', '生产设备清洁标准操作规程', '7', '1', sysdate(), date_add(sysdate(), interval 7 day), 'lending', '000000', 103, 1, sysdate());

-- 操作单演示数据
delete from dms_doc_operate_order where id in (1901, 1902);
insert into dms_doc_operate_order (id, order_number, operate_type, document_type, document_code, document_name, tmpl_source, drafter_id, draft_nick_name, doc_dept_id, status, tenant_id, create_dept, create_by, create_time) values (1901, 'OP-001', 'add', '900001', 'GMP-SOP-010', '培训管理标准操作规程（新增）', 'template', 1, 'admin', 103, 'approved', '000000', 103, 1, sysdate());
insert into dms_doc_operate_order (id, order_number, operate_type, document_type, document_code, document_name, document_lib_id, tmpl_source, drafter_id, draft_nick_name, doc_dept_id, replace_lib_ids, status, tenant_id, create_dept, create_by, create_time) values (1902, 'OP-002', 'revise', '900001', 'GMP-SOP-001', '生产设备清洁标准操作规程（修订）', 1001, 'document', 1, 'admin', 103, '1001', 'edit', '000000', 103, 1, sysdate());

-- 批量修订演示数据
delete from dms_batch_revise_order_main where id in (2001, 2002);
insert into dms_batch_revise_order_main (id, order_number, order_reason, remark, apply_user, status, tenant_id, create_dept, create_by, create_time) values (2001, 'BR-001', '年度文件修订批次', '演示批量修订', 'admin', 'edit', '000000', 103, 1, sysdate());
insert into dms_batch_revise_order_main (id, order_number, order_reason, remark, apply_user, status, tenant_id, create_dept, create_by, create_time) values (2002, 'BR-002', '法规更新修订批次', '演示批量修订', 'admin', 'approved', '000000', 103, 1, sysdate());
delete from dms_batch_revise_order_detail where id = 2011;
insert into dms_batch_revise_order_detail (id, order_id, document_lib_id, document_code, document_name, document_version, document_level, dl_code, dl_name, dl_version, dl_level, drafter_dept_name, issue_dept_name, create_dept_name, user_dept_name, tenant_id, create_dept, create_by, create_time) values (2011, 2002, 1001, 'GMP-SOP-001', '生产设备清洁标准操作规程', 'V1.0', 'A', 'GMP-SOP-001', '生产设备清洁标准操作规程', 'V1.0', 'A', '生产部', '生产部', '研发部门', '生产部', '000000', 103, 1, sysdate());
delete from dms_batch_revise_order_detail where id = 2012;
insert into dms_batch_revise_order_detail (id, order_id, document_lib_id, document_code, document_name, document_version, document_level, dl_code, dl_name, dl_version, dl_level, drafter_dept_name, issue_dept_name, create_dept_name, user_dept_name, tenant_id, create_dept, create_by, create_time) values (2012, 2002, 1002, 'GMP-SOP-002', '洁净区环境监测管理规程', 'V2.0', 'A', 'GMP-SOP-002', '洁净区环境监测管理规程', 'V2.0', 'A', '质量部', '质量部', '研发部门', '质量部', '000000', 103, 1, sysdate());

-- 成册打印演示数据
delete from dms_volumes_print_apply where id in (2101, 2102);
insert into dms_volumes_print_apply (id, volumes_name, apply_code, print_source, state, status, edit_way, tmpl_source, drafter_id, drafter_user_name, document_code, document_name, tenant_id, create_dept, create_by, create_time) values (2101, 'GMP 文件成册（Q4）', 'BOOK-001', 'add', 'approved', 'approved', 'online', 'document', 1, 'admin', 'GMP-SOP-001', '生产设备清洁标准操作规程', '000000', 103, 1, sysdate());
insert into dms_volumes_print_apply (id, volumes_name, apply_code, print_source, status, edit_way, tmpl_source, drafter_id, drafter_user_name, document_code, document_name, tenant_id, create_dept, create_by, create_time) values (2102, 'GMP 文件成册（Q3）', 'BOOK-002', 'add', 'edit', 'online', 'document', 1, 'admin', 'GMP-SOP-002', '洁净区环境监测管理规程', '000000', 103, 1, sysdate());
delete from dms_volumes_print_apply_detail where id = 2111;
insert into dms_volumes_print_apply_detail (id, apply_id, doc_or_record_id, doc_or_record_type, doc_dept_id, issue_dept_id, doc_code, doc_name, doc_level, doc_type, copies, pages, print_type, tenant_id, create_dept, create_by, create_time) values (2111, 2101, 1001, 'document', 103, 103, 'GMP-SOP-001', '生产设备清洁标准操作规程', 'A', '900001', 1, 12, 'docPrint', '000000', 103, 1, sysdate());
delete from dms_volumes_print_apply_detail where id = 2112;
insert into dms_volumes_print_apply_detail (id, apply_id, doc_or_record_id, doc_or_record_type, doc_dept_id, issue_dept_id, doc_code, doc_name, doc_level, doc_type, copies, pages, print_type, tenant_id, create_dept, create_by, create_time) values (2112, 2101, 1002, 'document', 103, 103, 'GMP-SOP-002', '洁净区环境监测管理规程', 'A', '900001', 1, 10, 'docPrint', '000000', 103, 1, sysdate());
delete from dms_volumes_print_info where id = 2121;
insert into dms_volumes_print_info (id, volumes_print_apply_id, apply_code, volumes_name, doc_code, doc_name, pages, copies, state, print_time, tenant_id, create_dept, create_by, create_time) values (2121, 2101, 'BOOK-001', 'GMP 文件成册（Q4）', 'GMP-SOP-001', '生产设备清洁标准操作规程', 12, 1, 'printed', sysdate(), '000000', 103, 1, sysdate());
delete from dms_volumes_print_info where id = 2122;
insert into dms_volumes_print_info (id, volumes_print_apply_id, apply_code, volumes_name, doc_code, doc_name, pages, copies, state, print_time, tenant_id, create_dept, create_by, create_time) values (2122, 2101, 'BOOK-001', 'GMP 文件成册（Q4）', 'GMP-SOP-002', '洁净区环境监测管理规程', 10, 1, 'issued', sysdate(), '000000', 103, 1, sysdate());
delete from dms_volumes_print_info where id = 2123;
insert into dms_volumes_print_info (id, volumes_print_apply_id, apply_code, volumes_name, doc_code, doc_name, pages, copies, state, print_time, tenant_id, create_dept, create_by, create_time) values (2123, 2101, 'BOOK-001', 'GMP 文件成册（Q4）', 'GMP-SOP-001', '生产设备清洁标准操作规程', 12, 1, 'returned', sysdate(), '000000', 103, 1, sysdate());

-- 待组册任务演示数据
delete from dms_volumes_print_pending_task where id = 2201;
insert into dms_volumes_print_pending_task (id, excel_name, row_id, doc_code, doc_name, doc_type, apply_dept_key, apply_dept, source, print_type, pages, apply_copies, whether_print_page, controlled_info, status, tenant_id, create_dept, create_by, create_time) values (2201, '成册打印清单-2026Q4', 'R001', 'GMP-SOP-001', '生产设备清洁标准操作规程', '900001', '103', '生产部', 'active', 'docPrint', 12, 1, 'true', '受控', 'pending', '000000', 103, 1, sysdate());
delete from dms_volumes_print_pending_task where id = 2202;
insert into dms_volumes_print_pending_task (id, excel_name, row_id, doc_code, doc_name, doc_type, apply_dept_key, apply_dept, source, print_type, pages, apply_copies, whether_print_page, controlled_info, status, tenant_id, create_dept, create_by, create_time) values (2202, '成册打印清单-2026Q4', 'R002', 'GMP-SOP-002', '洁净区环境监测管理规程', '900001', '103', '生产部', 'active', 'docPrint', 10, 1, 'true', '受控', 'pending', '000000', 103, 1, sysdate());
delete from dms_volumes_print_pending_task where id = 2203;
insert into dms_volumes_print_pending_task (id, excel_name, row_id, doc_code, doc_name, doc_type, apply_dept_key, apply_dept, source, print_type, pages, apply_copies, whether_print_page, controlled_info, status, tenant_id, create_dept, create_by, create_time) values (2203, '成册打印清单-2026Q4', 'R003', 'GMP-SOP-003', '物料入库验收操作规程', '900001', '104', '质量部', 'active', 'docPrint', 8, 2, 'false', '受控', 'pending', '000000', 103, 1, sysdate());
delete from dms_volumes_print_pending_task where id = 2204;
insert into dms_volumes_print_pending_task (id, excel_name, row_id, doc_code, doc_name, doc_type, apply_dept_key, apply_dept, source, print_type, pages, apply_copies, whether_print_page, controlled_info, status, tenant_id, create_dept, create_by, create_time) values (2204, '成册打印清单-2026Q4', 'R004', 'GMP-SOP-004', '产品放行管理规程', '900004', '104', '质量部', 'active', 'docPrint', 6, 1, 'true', '普通', 'pending', '000000', 103, 1, sysdate());
delete from dms_volumes_print_pending_task where id = 2205;
insert into dms_volumes_print_pending_task (id, excel_name, row_id, doc_code, doc_name, doc_type, apply_dept_key, apply_dept, source, print_type, pages, apply_copies, whether_print_page, controlled_info, status, tenant_id, create_dept, create_by, create_time) values (2205, '成册打印清单-2026Q4', 'R005', 'GMP-REC-001', '产品批生产记录模板', '900002', '103', '生产部', 'active', 'recPrint', 20, 5, 'false', '受控', 'pending', '000000', 103, 1, sysdate());
delete from dms_volumes_print_pending_task where id = 2206;
insert into dms_volumes_print_pending_task (id, excel_name, row_id, doc_code, doc_name, doc_type, apply_dept_key, apply_dept, source, print_type, pages, apply_copies, whether_print_page, controlled_info, status, tenant_id, create_dept, create_by, create_time) values (2206, '成册打印清单-2026Q4', 'R006', 'GMP-SOP-005', '偏差处理标准操作规程', '900001', '103', '设备部', 'unActive', 'docPrint', 9, 1, 'true', '受控', 'pending', '000000', 103, 1, sysdate());
delete from dms_volumes_print_pending_task where id = 2207;
insert into dms_volumes_print_pending_task (id, excel_name, row_id, doc_code, doc_name, doc_type, apply_dept_key, apply_dept, source, print_type, pages, apply_copies, whether_print_page, controlled_info, status, tenant_id, create_dept, create_by, create_time) values (2207, '成册打印清单-2026Q4', 'R007', 'GMP-SOP-006', '变更控制管理规程', '900001', '103', '注册部', 'unActive', 'docPrint', 11, 1, 'true', '受控', 'pending', '000000', 103, 1, sysdate());
delete from dms_volumes_print_pending_task where id = 2208;
insert into dms_volumes_print_pending_task (id, excel_name, row_id, doc_code, doc_name, doc_type, apply_dept_key, apply_dept, source, print_type, pages, apply_copies, whether_print_page, controlled_info, status, tenant_id, create_dept, create_by, create_time) values (2208, '成册打印清单-2026Q4', 'R008', 'GMP-SOP-007', '设备维护保养操作规程', '900001', '103', '生产部', 'unActive', 'docPrint', 7, 1, 'false', '普通', 'pending', '000000', 103, 1, sysdate());

-- 培训责任人/培训记录/浏览历史
delete from dms_training_owner_config where id = 2301;
insert into dms_training_owner_config (id, user_category, user_info, nick_name, drafter_id, draft_user_name, status, tenant_id, create_dept, create_by, create_time) values (2301, 'personnel', '1', 'admin', 1, 'admin', 'active', '000000', 103, 1, sysdate());
delete from dms_training_owner_config where id = 2302;
insert into dms_training_owner_config (id, user_category, user_info, nick_name, drafter_id, draft_user_name, status, tenant_id, create_dept, create_by, create_time) values (2302, 'role', '1', '超级管理员', 1, 'admin', 'active', '000000', 103, 1, sysdate());
delete from dms_training_record where id = 2311;
insert into dms_training_record (id, doc_create_id, document_code, document_name, train_user_name, train_status, train_time, remark, tenant_id, create_dept, create_by, create_time) values (2311, 1401, 'DOC-101', '年度培训计划管理规程', 'admin', 'needTrain', sysdate(), '演示培训记录', '000000', 103, 1, sysdate());
delete from dms_training_record where id = 2312;
insert into dms_training_record (id, doc_create_id, document_code, document_name, train_user_name, train_status, train_time, remark, tenant_id, create_dept, create_by, create_time) values (2312, 1402, 'DOC-102', '数据完整性管理规程', 'admin', 'trained', sysdate(), '演示培训记录', '000000', 103, 1, sysdate());
delete from dms_doc_browse_history where id = 2321;
insert into dms_doc_browse_history (id, document_lib_id, dl_code, dl_name, dl_version, browse_user_name, browse_time, tenant_id, create_dept, create_by, create_time) values (2321, 1001, 'GMP-SOP-001', '生产设备清洁标准操作规程', 'V1.0', 'admin', sysdate(), '000000', 103, 1, sysdate());
delete from dms_doc_browse_history where id = 2322;
insert into dms_doc_browse_history (id, document_lib_id, dl_code, dl_name, dl_version, browse_user_name, browse_time, tenant_id, create_dept, create_by, create_time) values (2322, 1002, 'GMP-SOP-002', '洁净区环境监测管理规程', 'V2.0', 'admin', sysdate(), '000000', 103, 1, sysdate());

-- EQMS 演示数据
delete from dms_eqms_inspection_template where id = 2401;
insert into dms_eqms_inspection_template (id, template_code, template_name, template_type, version, status, remark, tenant_id, create_dept, create_by, create_time) values (2401, 'IT-001', '进厂检验模板', 'incoming', 'V1.0', 'active', '演示模板', '000000', 103, 1, sysdate());
delete from dms_eqms_inspection_template where id = 2402;
insert into dms_eqms_inspection_template (id, template_code, template_name, template_type, version, status, remark, tenant_id, create_dept, create_by, create_time) values (2402, 'IT-002', '周期检验模板', 'periodic', 'V1.0', 'active', '演示模板', '000000', 103, 1, sysdate());
delete from dms_eqms_number_application where id = 2411;
insert into dms_eqms_number_application (id, apply_code, device_name, device_type, applicant, apply_date, status, remark, tenant_id, create_dept, create_by, create_time) values (2411, 'DEV-001', '纯化水系统', 'water_system', 'admin', sysdate(), 'approved', '演示申请', '000000', 103, 1, sysdate());
