-- ----------------------------
-- GMP 文件管理（DMS）业务表
-- 可重复执行（drop 后重建）
-- ----------------------------

DROP TABLE IF EXISTS dms_document_lib;
create table dms_document_lib
(
    id  bigint  not null comment '主键',
    dl_code  varchar(100)  null comment '文件编号',
    dl_name  varchar(255)  null comment '文件名称',
    dl_type  varchar(64)  null comment '文件类型id',
    dl_version  varchar(32)  null comment '版本',
    dl_level  varchar(32)  null comment '文件级别',
    doc_source  varchar(32)  null comment '来源 new/revise',
    doc_operation_source  varchar(32)  null comment '操作来源',
    use_state  varchar(32)  null comment '使用状态 pending/active/invalid',
    deactive_source  varchar(64)  null comment '失效来源',
    deactive_reason  varchar(500)  null comment '失效原因',
    dl_dept_id  bigint  null comment '编制部门id',
    dl_dept_name  varchar(100)  null comment '编制部门',
    issue_dept_id  bigint  null comment '发放部门id',
    issue_dept_name  varchar(100)  null comment '发放部门',
    tmpl_lib_id  bigint  null comment '模板id',
    tmpl_lib_code  varchar(64)  null comment '模板编号',
    tmpl_lib_name  varchar(255)  null comment '模板名称',
    tmpl_source  varchar(16)  null comment '模板来源',
    drafter_id  bigint  null comment '起草人id',
    draft_nick_name  varchar(64)  null comment '起草人',
    distribute_dept_id  varchar(255)  null comment '分发部门',
    ext  text  null comment '扩展属性JSON',
    status  varchar(32)  null comment '业务状态',
    active_status  varchar(32)  null comment '生效状态',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_document_lib 表';

DROP TABLE IF EXISTS dms_document_lib_relate;
create table dms_document_lib_relate
(
    id  bigint  not null comment '主键',
    lib_id  bigint  null comment '文件库id',
    relate_lib_id  bigint  null comment '关联文件id',
    link_type  varchar(8)  null comment '关联类型1单向2双向',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_document_lib_relate 表';

DROP TABLE IF EXISTS dms_document_lib_replace;
create table dms_document_lib_replace
(
    id  bigint  not null comment '主键',
    lib_id  bigint  null comment '新文件id',
    replace_lib_id  bigint  null comment '被替代文件id',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_document_lib_replace 表';

DROP TABLE IF EXISTS dms_document_record_lib;
create table dms_document_record_lib
(
    id  bigint  not null comment '主键',
    dlr_code  varchar(100)  null comment '记录编号',
    dlr_name  varchar(255)  null comment '记录名称',
    dlr_version  varchar(32)  null comment '版本',
    dlr_active_time  datetime  null comment '生效时间',
    dlr_deactive_time  datetime  null comment '失效时间',
    dlr_deactive_reason  varchar(500)  null comment '失效原因',
    document_lib_id  bigint  null comment '所属文件库id',
    document_code  varchar(100)  null comment '所属文件编号',
    document_name  varchar(255)  null comment '所属文件名称',
    document_type  varchar(64)  null comment '文件类型',
    edit_way  varchar(16)  null comment '编制方式',
    ext  text  null comment '扩展属性JSON',
    status  varchar(32)  null comment '业务状态',
    active_status  varchar(32)  null comment '生效状态',
    dl_dept_name  varchar(100)  null comment '所属部门',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_document_record_lib 表';

DROP TABLE IF EXISTS dms_tmpl_lib;
create table dms_tmpl_lib
(
    id  bigint  not null comment '主键',
    tmpl_code  varchar(100)  null comment '模板编号',
    tmpl_name  varchar(255)  null comment '模板名称',
    tmpl_category  varchar(64)  null comment '模板类型',
    tmpl_version  varchar(32)  null comment '版本',
    tmpl_version_minor  varchar(32)  null comment '次版本',
    tmpl_purpose  varchar(16)  null comment '用途',
    tmpl_dept  bigint  null comment '所属部门id',
    tmpl_dept_name  varchar(100)  null comment '所属部门',
    status  varchar(32)  null comment '业务状态',
    active_status  varchar(32)  null comment '生效状态',
    ext  text  null comment '扩展属性JSON',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_tmpl_lib 表';

DROP TABLE IF EXISTS dms_tmpl_preparation;
create table dms_tmpl_preparation
(
    id  bigint  not null comment '主键',
    tmpl_code  varchar(100)  null comment '模板编号',
    tmpl_name  varchar(255)  null comment '模板名称',
    tmpl_category  varchar(64)  null comment '模板类型',
    tmpl_version  varchar(32)  null comment '版本',
    tmpl_version_minor  varchar(32)  null comment '次版本',
    tmpl_purpose  varchar(16)  null comment '用途',
    tmpl_dept  bigint  null comment '所属部门id',
    tmpl_dept_name  varchar(100)  null comment '所属部门',
    status  varchar(32)  null comment '状态',
    active_status  varchar(32)  null comment '生效状态',
    workflow_id  bigint  null comment '流程实例id',
    approve_comments  varchar(500)  null comment '审批意见',
    ext  text  null comment '扩展属性JSON',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_tmpl_preparation 表';

DROP TABLE IF EXISTS dms_tmpl_snapshot;
create table dms_tmpl_snapshot
(
    id  bigint  not null comment '主键',
    biz_id  bigint  null comment '业务id',
    tmpl_code  varchar(100)  null comment '模板编号',
    tmpl_version  varchar(32)  null comment '版本',
    tmpl_version_minor  varchar(32)  null comment '次版本',
    snapshot_name  varchar(255)  null comment '快照名称',
    content  longtext  null comment '快照内容',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_tmpl_snapshot 表';

DROP TABLE IF EXISTS dms_doc_create;
create table dms_doc_create
(
    id  bigint  not null comment '主键',
    document_code  varchar(100)  null comment '文件编号',
    document_name  varchar(255)  null comment '文件名称',
    document_type  varchar(64)  null comment '文件类型',
    document_version  varchar(32)  null comment '版本',
    document_version_minor  varchar(32)  null comment '次版本',
    document_level  varchar(32)  null comment '文件级别',
    document_dept_id  bigint  null comment '编制部门id',
    document_dept_code  varchar(64)  null comment '编制部门编码',
    document_dept_name  varchar(100)  null comment '编制部门',
    edit_way  varchar(16)  null comment '编制方式',
    operate_source  varchar(32)  null comment '操作来源',
    operate_remark  varchar(500)  null comment '操作备注',
    operate_cause  varchar(500)  null comment '操作原因',
    tmpl_source  varchar(16)  null comment '来源',
    tmpl_lib_id  bigint  null comment '模板id',
    tmpl_lib_code  varchar(64)  null comment '模板编号',
    tmpl_lib_name  varchar(255)  null comment '模板名称',
    drafter_id  bigint  null comment '起草人id',
    drafter_name  varchar(64)  null comment '起草人',
    distribute_dept_id  varchar(255)  null comment '分发部门',
    distribute_dept_code  varchar(255)  null comment '分发部门编码',
    distribute_dept_name  varchar(255)  null comment '分发部门名称',
    pd_dept_id  varchar(255)  null comment 'pd部门',
    pd_dept_code  varchar(255)  null comment 'pd部门编码',
    pd_dept_name  varchar(255)  null comment 'pd部门名称',
    doc_apply_id  bigint  null comment '申请单id',
    order_number  varchar(64)  null comment '单号',
    status  varchar(32)  null comment '状态',
    train_status  varchar(32)  null comment '培训状态',
    edit_status  varchar(32)  null comment '离线文件状态',
    workflow_id  bigint  null comment '流程实例id',
    approve_comments  varchar(500)  null comment '审批意见',
    ext  text  null comment '扩展属性JSON',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_doc_create 表';

DROP TABLE IF EXISTS dms_doc_create_supersede;
create table dms_doc_create_supersede
(
    id  bigint  not null comment '主键',
    doc_create_id  bigint  null comment '文档编制id',
    document_lib_id  bigint  null comment '被替代文件库id',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_doc_create_supersede 表';

DROP TABLE IF EXISTS dms_doc_create_link;
create table dms_doc_create_link
(
    id  bigint  not null comment '主键',
    doc_create_id  bigint  null comment '文档编制id',
    document_lib_id  bigint  null comment '关联文件库id',
    link_type  varchar(8)  null comment '关联类型',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_doc_create_link 表';

DROP TABLE IF EXISTS dms_doc_create_snapshot;
create table dms_doc_create_snapshot
(
    id  bigint  not null comment '主键',
    biz_id  bigint  null comment '业务id',
    document_code  varchar(100)  null comment '文件编号',
    document_version  varchar(32)  null comment '版本',
    document_version_minor  varchar(32)  null comment '次版本',
    snapshot_name  varchar(255)  null comment '快照名称',
    content  longtext  null comment '快照内容',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_doc_create_snapshot 表';

DROP TABLE IF EXISTS dms_doc_record_create;
create table dms_doc_record_create
(
    id  bigint  not null comment '主键',
    document_code  varchar(100)  null comment '文件编号',
    document_name  varchar(255)  null comment '文件名称',
    document_record_code  varchar(100)  null comment '记录编号',
    document_record_name  varchar(255)  null comment '记录名称',
    document_record_type  varchar(64)  null comment '记录类型',
    document_record_version  varchar(32)  null comment '记录版本',
    document_record_version_minor  varchar(32)  null comment '次版本',
    document_record_library_id  bigint  null comment '记录库id',
    record_handle_type  varchar(16)  null comment '记录处理类型',
    revise_source  varchar(16)  null comment '修订来源',
    edit_way  varchar(16)  null comment '编制方式',
    edit_status  varchar(32)  null comment '离线文件状态',
    doc_create_id  bigint  null comment '文档编制id',
    tmpl_source  varchar(16)  null comment '来源',
    tmpl_lib_id  bigint  null comment '模板id',
    tmpl_lib_code  varchar(64)  null comment '模板编号',
    tmpl_lib_name  varchar(255)  null comment '模板名称',
    drafter_id  bigint  null comment '起草人id',
    draft_user_name  varchar(64)  null comment '起草人',
    status  varchar(32)  null comment '状态',
    workflow_id  bigint  null comment '流程实例id',
    approve_comments  varchar(500)  null comment '审批意见',
    ext  text  null comment '扩展属性JSON',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_doc_record_create 表';

DROP TABLE IF EXISTS dms_doc_record_snapshot;
create table dms_doc_record_snapshot
(
    id  bigint  not null comment '主键',
    biz_id  bigint  null comment '业务id',
    document_record_code  varchar(100)  null comment '记录编号',
    document_record_version  varchar(32)  null comment '版本',
    document_record_version_minor  varchar(32)  null comment '次版本',
    snapshot_name  varchar(255)  null comment '快照名称',
    content  longtext  null comment '快照内容',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_doc_record_snapshot 表';

DROP TABLE IF EXISTS dms_doc_record_change_order;
create table dms_doc_record_change_order
(
    id  bigint  not null comment '主键',
    order_number  varchar(64)  null comment '单号',
    document_code  varchar(100)  null comment '文件编号',
    document_name  varchar(255)  null comment '文件名称',
    document_record_library_id  bigint  null comment '记录库id',
    document_record_code  varchar(100)  null comment '记录编号',
    document_record_name  varchar(255)  null comment '记录名称',
    document_record_type  varchar(64)  null comment '记录类型',
    edit_way  varchar(16)  null comment '编制方式',
    tmpl_lib_id  bigint  null comment '模板id',
    tmpl_lib_code  varchar(64)  null comment '模板编号',
    tmpl_lib_name  varchar(255)  null comment '模板名称',
    drafter_id  bigint  null comment '起草人id',
    drafter_user_name  varchar(64)  null comment '起草人',
    status  varchar(32)  null comment '状态',
    workflow_id  bigint  null comment '流程实例id',
    approve_comments  varchar(500)  null comment '审批意见',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_doc_record_change_order 表';

DROP TABLE IF EXISTS dms_doc_invalid_order;
create table dms_doc_invalid_order
(
    id  bigint  not null comment '主键',
    order_number  varchar(64)  null comment '单号',
    order_reason  varchar(500)  null comment '失效原因',
    document_lib_id  bigint  null comment '文件库id',
    status  varchar(32)  null comment '状态',
    workflow_id  bigint  null comment '流程实例id',
    approve_comments  varchar(500)  null comment '审批意见',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_doc_invalid_order 表';

DROP TABLE IF EXISTS dms_doc_invalid_order_item;
create table dms_doc_invalid_order_item
(
    id  bigint  not null comment '主键',
    order_id  bigint  null comment '失效单id',
    document_lib_id  bigint  null comment '文件库id',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_doc_invalid_order_item 表';

DROP TABLE IF EXISTS dms_doc_record_failure_order;
create table dms_doc_record_failure_order
(
    id  bigint  not null comment '主键',
    order_number  varchar(64)  null comment '单号',
    document_record_reason  varchar(500)  null comment '失效原因',
    document_record_library_id  bigint  null comment '记录库id',
    document_code  varchar(100)  null comment '文件编号',
    status  varchar(32)  null comment '状态',
    workflow_id  bigint  null comment '流程实例id',
    approve_comments  varchar(500)  null comment '审批意见',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_doc_record_failure_order 表';

DROP TABLE IF EXISTS dms_document_lend_order;
create table dms_document_lend_order
(
    id  bigint  not null comment '主键',
    order_number  varchar(64)  null comment '单号',
    doc_lib_id  bigint  null comment '借阅文件库id',
    document_code  varchar(100)  null comment '文件编号',
    document_name  varchar(255)  null comment '文件名称',
    lending_period  varchar(32)  null comment '借阅期限(天)',
    is_download  char(1)  null comment '是否允许下载',
    return_date  datetime  null comment '归还日期',
    status  varchar(32)  null comment '状态',
    workflow_id  bigint  null comment '流程实例id',
    approve_comments  varchar(500)  null comment '审批意见',
    ext  text  null comment '扩展属性JSON',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_document_lend_order 表';

DROP TABLE IF EXISTS dms_document_lend_lib;
create table dms_document_lend_lib
(
    id  bigint  not null comment '主键',
    order_id  bigint  null comment '借阅单id',
    doc_lib_id  bigint  null comment '文件库id',
    document_code  varchar(100)  null comment '文件编号',
    document_name  varchar(255)  null comment '文件名称',
    lending_period  varchar(32)  null comment '借阅期限(天)',
    is_download  char(1)  null comment '是否允许下载',
    lend_date  datetime  null comment '借阅时间',
    return_date  datetime  null comment '归还日期',
    status  varchar(32)  null comment '状态',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_document_lend_lib 表';

DROP TABLE IF EXISTS dms_doc_operate_order;
create table dms_doc_operate_order
(
    id  bigint  not null comment '主键',
    order_number  varchar(64)  null comment '单号',
    operate_type  varchar(16)  null comment '操作类型 add/revise',
    document_type  varchar(64)  null comment '文件类型',
    document_code  varchar(100)  null comment '文件编号',
    document_name  varchar(255)  null comment '文件名称',
    document_lib_id  bigint  null comment '被修订文件库id',
    tmpl_source  varchar(16)  null comment '来源',
    tmpl_lib_id  bigint  null comment '模板id',
    tmpl_lib_code  varchar(64)  null comment '模板编号',
    tmpl_lib_name  varchar(255)  null comment '模板名称',
    drafter_id  bigint  null comment '起草人id',
    draft_nick_name  varchar(64)  null comment '起草人',
    doc_dept_id  bigint  null comment '编制部门id',
    distribute_dept_id  varchar(255)  null comment '分发部门',
    replace_lib_ids  varchar(500)  null comment '替代文件id列表',
    ext  text  null comment '扩展属性JSON',
    status  varchar(32)  null comment '状态',
    workflow_id  bigint  null comment '流程实例id',
    approve_comments  varchar(500)  null comment '审批意见',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_doc_operate_order 表';

DROP TABLE IF EXISTS dms_document_record_revise;
create table dms_document_record_revise
(
    id  bigint  not null comment '主键',
    da_id  bigint  null comment '操作单id',
    document_code  varchar(100)  null comment '文件编号',
    document_name  varchar(255)  null comment '文件名称',
    document_record_code  varchar(100)  null comment '记录编号',
    document_record_name  varchar(255)  null comment '记录名称',
    document_record_type  varchar(64)  null comment '记录类型',
    tmpl_lib_id  bigint  null comment '模板id',
    tmpl_lib_code  varchar(64)  null comment '模板编号',
    tmpl_lib_name  varchar(255)  null comment '模板名称',
    whether_to_revise  char(1)  null comment '是否修订',
    edit_way  varchar(16)  null comment '编制方式',
    status  varchar(32)  null comment '状态',
    workflow_id  bigint  null comment '流程实例id',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_document_record_revise 表';

DROP TABLE IF EXISTS dms_batch_revise_order_main;
create table dms_batch_revise_order_main
(
    id  bigint  not null comment '主键',
    order_number  varchar(64)  null comment '单号',
    order_reason  varchar(500)  null comment '修订原因',
    remark  varchar(500)  null comment '备注',
    apply_user  varchar(64)  null comment '申请人',
    status  varchar(32)  null comment '状态',
    workflow_id  bigint  null comment '流程实例id',
    approve_comments  varchar(500)  null comment '审批意见',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_batch_revise_order_main 表';

DROP TABLE IF EXISTS dms_batch_revise_order_detail;
create table dms_batch_revise_order_detail
(
    id  bigint  not null comment '主键',
    order_id  bigint  null comment '批量修订单id',
    document_lib_id  bigint  null comment '文件库id',
    document_code  varchar(100)  null comment '文件编号',
    document_name  varchar(255)  null comment '文件名称',
    document_version  varchar(32)  null comment '版本',
    document_level  varchar(32)  null comment '文件级别',
    dl_code  varchar(100)  null comment '文件编号',
    dl_name  varchar(255)  null comment '文件名称',
    dl_version  varchar(32)  null comment '版本',
    dl_level  varchar(32)  null comment '文件级别',
    drafter_dept_name  varchar(100)  null comment '起草部门',
    issue_dept_name  varchar(100)  null comment '发放部门',
    create_dept_name  varchar(100)  null comment '创建部门',
    user_dept_name  varchar(100)  null comment '用户部门',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_batch_revise_order_detail 表';

DROP TABLE IF EXISTS dms_print_apply;
create table dms_print_apply
(
    id  bigint  not null comment '主键',
    apply_code  varchar(64)  null comment '申请单号',
    apply_reason  varchar(500)  null comment '申请原因',
    doc_or_record_type  varchar(16)  null comment '文件/记录',
    doc_or_record_id  bigint  null comment '文件库/记录库id',
    doc_code  varchar(100)  null comment '文件编号',
    doc_name  varchar(255)  null comment '文件名称',
    doc_version  varchar(32)  null comment '版本',
    doc_status  varchar(32)  null comment '文件状态',
    tmpl_source  varchar(16)  null comment '来源',
    print_type  varchar(16)  null comment '打印类型',
    copies  int  null comment '打印份数',
    pages  int  null comment '页数',
    print_source  varchar(16)  null comment '打印来源',
    status  varchar(32)  null comment '状态',
    workflow_id  bigint  null comment '流程实例id',
    approve_comments  varchar(500)  null comment '审批意见',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_print_apply 表';

DROP TABLE IF EXISTS dms_print_doc_info;
create table dms_print_doc_info
(
    id  bigint  not null comment '主键',
    print_apply_id  bigint  null comment '打印申请id',
    apply_code  varchar(64)  null comment '申请单号',
    doc_or_record_type  varchar(16)  null comment '文件/记录',
    doc_or_record_id  bigint  null comment '文件库/记录库id',
    doc_code  varchar(100)  null comment '文件编号',
    doc_name  varchar(255)  null comment '文件名称',
    doc_version  varchar(32)  null comment '版本',
    doc_type  varchar(64)  null comment '文件类型',
    doc_status  varchar(32)  null comment '文件状态',
    copies  int  null comment '份数',
    pages  int  null comment '页数',
    print_type  varchar(16)  null comment '打印类型',
    state  varchar(32)  null comment '状态',
    print_time  datetime  null comment '打印时间',
    issue_user_id  varchar(64)  null comment '发放人id',
    issue_user_name  varchar(64)  null comment '发放人',
    issue_time  datetime  null comment '发放时间',
    receive_user_name  varchar(64)  null comment '领用人',
    receive_time  datetime  null comment '领用时间',
    return_user_name  varchar(64)  null comment '交回人',
    return_time  datetime  null comment '交回时间',
    confirm_user_id  varchar(64)  null comment '确认人id',
    confirm_user_name  varchar(64)  null comment '确认人',
    close_user_name  varchar(64)  null comment '关闭人',
    close_time  datetime  null comment '关闭时间',
    destroy_user_name  varchar(64)  null comment '销毁人',
    confirm_destroy_user_name  varchar(64)  null comment '监销人',
    destroy_action  varchar(64)  null comment '销毁方式',
    destroy_place  varchar(255)  null comment '销毁地点',
    destroy_time  datetime  null comment '销毁时间',
    abnormal_reason  varchar(500)  null comment '异常原因',
    fail_reason  varchar(500)  null comment '失败原因',
    remark  varchar(500)  null comment '备注',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_print_doc_info 表';

DROP TABLE IF EXISTS dms_print_apply_batch;
create table dms_print_apply_batch
(
    id  bigint  not null comment '主键',
    apply_code  varchar(64)  null comment '申请单号',
    apply_reason  varchar(500)  null comment '申请原因',
    doc_or_record_type  varchar(16)  null comment '文件/记录',
    doc_code  varchar(100)  null comment '文件编号',
    doc_name  varchar(255)  null comment '文件名称',
    doc_version  varchar(32)  null comment '版本',
    doc_status  varchar(32)  null comment '文件状态',
    status  varchar(32)  null comment '状态',
    workflow_id  bigint  null comment '流程实例id',
    approve_comments  varchar(500)  null comment '审批意见',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_print_apply_batch 表';

DROP TABLE IF EXISTS dms_print_apply_batch_detail;
create table dms_print_apply_batch_detail
(
    id  bigint  not null comment '主键',
    batch_id  bigint  null comment '批量打印申请id',
    apply_copies  int  null comment '申请份数',
    print_type  varchar(16)  null comment '打印类型',
    doc_or_record_type  varchar(16)  null comment '文件/记录',
    doc_or_record_id  bigint  null comment '文件/记录id',
    pages  int  null comment '页数',
    whether_print_page  varchar(8)  null comment '是否打印页',
    doc_code  varchar(100)  null comment '文件编号',
    doc_name  varchar(255)  null comment '文件名称',
    doc_version  varchar(32)  null comment '版本',
    doc_dept  varchar(100)  null comment '文件部门',
    doc_type  varchar(64)  null comment '文件类型',
    doc_status  varchar(32)  null comment '文件状态',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_print_apply_batch_detail 表';

DROP TABLE IF EXISTS dms_volumes_print_pending_task;
create table dms_volumes_print_pending_task
(
    id  bigint  not null comment '主键',
    excel_name  varchar(128)  null comment '批次名称',
    row_id  varchar(64)  null comment '行号',
    doc_code  varchar(100)  null comment '文件编号',
    doc_name  varchar(255)  null comment '文件名称',
    doc_type  varchar(64)  null comment '文件类型',
    apply_dept_key  varchar(64)  null comment '申请部门key',
    apply_dept  varchar(100)  null comment '申请部门',
    source  varchar(16)  null comment '来源 active/unActive',
    print_type  varchar(16)  null comment '打印类型',
    pages  int  null comment '页数',
    apply_copies  int  null comment '申请份数',
    whether_print_page  varchar(8)  null comment '是否打印页',
    controlled_info  varchar(64)  null comment '受控信息',
    status  varchar(16)  null comment '状态 pending/accepted/rejected',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_volumes_print_pending_task 表';

DROP TABLE IF EXISTS dms_volumes_print_apply;
create table dms_volumes_print_apply
(
    id  bigint  not null comment '主键',
    volumes_name  varchar(255)  null comment '成册名称',
    apply_code  varchar(64)  null comment '申请单号',
    print_source  varchar(16)  null comment '打印来源',
    state  varchar(32)  null comment '打印件状态',
    status  varchar(32)  null comment '状态',
    workflow_id  bigint  null comment '流程实例id',
    edit_way  varchar(16)  null comment '编制方式',
    tmpl_source  varchar(16)  null comment '来源',
    tmpl_lib_id  bigint  null comment '模板id',
    tmpl_lib_code  varchar(64)  null comment '模板编号',
    tmpl_lib_name  varchar(255)  null comment '模板名称',
    drafter_id  bigint  null comment '起草人id',
    drafter_user_name  varchar(64)  null comment '起草人',
    document_record_library_id  varchar(64)  null comment '记录库id',
    document_record_code  varchar(100)  null comment '记录编号',
    document_record_name  varchar(255)  null comment '记录名称',
    document_record_type  varchar(64)  null comment '记录类型',
    document_code  varchar(100)  null comment '文件编号',
    document_name  varchar(255)  null comment '文件名称',
    approve_comments  varchar(500)  null comment '审批意见',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_volumes_print_apply 表';

DROP TABLE IF EXISTS dms_volumes_print_apply_detail;
create table dms_volumes_print_apply_detail
(
    id  bigint  not null comment '主键',
    apply_id  bigint  null comment '成册申请id',
    doc_or_record_id  bigint  null comment '文件/记录id',
    doc_or_record_type  varchar(16)  null comment '文件/记录',
    doc_dept_id  bigint  null comment '文件部门id',
    issue_dept_id  bigint  null comment '发放部门id',
    doc_code  varchar(100)  null comment '文件编号',
    doc_name  varchar(255)  null comment '文件名称',
    doc_level  varchar(32)  null comment '文件级别',
    doc_type  varchar(64)  null comment '文件类型',
    copies  int  null comment '份数',
    pages  int  null comment '页数',
    print_type  varchar(16)  null comment '打印类型',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_volumes_print_apply_detail 表';

DROP TABLE IF EXISTS dms_volumes_print_info;
create table dms_volumes_print_info
(
    id  bigint  not null comment '主键',
    volumes_print_apply_id  bigint  null comment '成册申请id',
    apply_code  varchar(64)  null comment '申请单号',
    volumes_name  varchar(255)  null comment '成册名称',
    doc_code  varchar(100)  null comment '文件编号',
    doc_name  varchar(255)  null comment '文件名称',
    pages  int  null comment '页数',
    copies  int  null comment '份数',
    state  varchar(32)  null comment '状态',
    print_time  datetime  null comment '打印时间',
    issue_user_name  varchar(64)  null comment '发放人',
    issue_time  datetime  null comment '发放时间',
    receive_user_name  varchar(64)  null comment '领用人',
    receive_time  datetime  null comment '领用时间',
    return_user_name  varchar(64)  null comment '交回人',
    return_time  datetime  null comment '交回时间',
    confirm_user_name  varchar(64)  null comment '确认人',
    destroy_user_name  varchar(64)  null comment '销毁人',
    confirm_destroy_user_name  varchar(64)  null comment '监销人',
    destroy_action  varchar(64)  null comment '销毁方式',
    destroy_place  varchar(255)  null comment '销毁地点',
    destroy_time  datetime  null comment '销毁时间',
    abnormal_reason  varchar(500)  null comment '异常原因',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_volumes_print_info 表';

DROP TABLE IF EXISTS dms_training_record;
create table dms_training_record
(
    id  bigint  not null comment '主键',
    doc_create_id  bigint  null comment '文档编制id',
    document_code  varchar(100)  null comment '文件编号',
    document_name  varchar(255)  null comment '文件名称',
    train_user_name  varchar(64)  null comment '培训人',
    train_status  varchar(32)  null comment '培训状态',
    train_time  datetime  null comment '培训时间',
    remark  varchar(500)  null comment '备注',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_training_record 表';

DROP TABLE IF EXISTS dms_training_owner_config;
create table dms_training_owner_config
(
    id  bigint  not null comment '主键',
    user_category  varchar(16)  null comment '目标类型',
    user_info  varchar(64)  null comment '目标id',
    nick_name  varchar(64)  null comment '目标名称',
    drafter_id  bigint  null comment '起草人id',
    draft_user_name  varchar(64)  null comment '起草人',
    status  varchar(32)  null comment '状态',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_training_owner_config 表';

DROP TABLE IF EXISTS dms_document_type;
create table dms_document_type
(
    id  varchar(64)  not null comment '主键',
    parent_id  varchar(64)  null comment '父节点id 顶层为root',
    parent_name  varchar(100)  null comment '父节点名称',
    sort_code  varchar(64)  null comment '分类代码',
    sort_name  varchar(100)  null comment '分类名称',
    status  varchar(16)  null comment '状态 active/failure',
    remark  varchar(500)  null comment '备注',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_document_type 表';

DROP TABLE IF EXISTS dms_document_type_attribute;
create table dms_document_type_attribute
(
    id  bigint  not null comment '主键',
    sort_id  varchar(64)  null comment '文件类型id',
    attribute_code  varchar(64)  null comment '属性代码',
    attribute_name  varchar(100)  null comment '属性名称',
    if_required  varchar(8)  null comment '是否必填',
    value_type  varchar(16)  null comment '值类型',
    applicable_mark  varchar(32)  null comment '适用标记',
    status  varchar(16)  null comment '状态',
    sort  int  null comment '排序',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_document_type_attribute 表';

DROP TABLE IF EXISTS dms_oss_biz;
create table dms_oss_biz
(
    id  bigint  not null comment '主键',
    biz_id  varchar(64)  null comment '业务id',
    biz_type  varchar(64)  null comment '业务类型',
    biz_second_type  varchar(64)  null comment '子类型',
    oss_id  bigint  null comment 'oss表id',
    covert_type  varchar(64)  null comment '转换类型',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_oss_biz 表';

DROP TABLE IF EXISTS dms_file_open_record;
create table dms_file_open_record
(
    id  bigint  not null comment '主键',
    file_business_id  varchar(64)  null comment '业务id',
    file_business_type  varchar(64)  null comment '业务类型',
    file_business_name  varchar(255)  null comment '业务名称',
    open_user_name  varchar(64)  null comment '打开人',
    open_time  datetime  null comment '打开时间',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_file_open_record 表';

DROP TABLE IF EXISTS dms_doc_browse_history;
create table dms_doc_browse_history
(
    id  bigint  not null comment '主键',
    document_lib_id  bigint  null comment '文件库id',
    dl_code  varchar(100)  null comment '文件编号',
    dl_name  varchar(255)  null comment '文件名称',
    dl_version  varchar(32)  null comment '版本',
    browse_user_name  varchar(64)  null comment '浏览人',
    browse_time  datetime  null comment '浏览时间',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_doc_browse_history 表';

DROP TABLE IF EXISTS dms_flow_business;
create table dms_flow_business
(
    id  bigint  not null comment '主键',
    business_id  varchar(64)  null comment '业务id',
    business_type  varchar(64)  null comment '业务类型',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_flow_business 表';

DROP TABLE IF EXISTS sys_page_config;
create table sys_page_config
(
    page_config_id  bigint  not null comment '主键',
    page_code  varchar(128)  null comment '页面编码',
    page_config_name  varchar(128)  null comment '页面名称',
    page_config_align  varchar(16)  null comment '对齐方式',
    page_config_size  varchar(16)  null comment '尺寸',
    page_config_display  varchar(8)  null comment '表单列数',
    remark  varchar(500)  null comment '备注',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'sys_page_config 表';

DROP TABLE IF EXISTS sys_page_config_attribute;
create table sys_page_config_attribute
(
    id  bigint  not null comment '主键',
    page_config_id  bigint  null comment '页面配置id',
    page_code  varchar(128)  null comment '页面编码',
    attribute_json  longtext  null comment '属性JSON',
    page_config_attribute_sort  int  null comment '排序',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'sys_page_config_attribute 表';

DROP TABLE IF EXISTS sys_page_config_rule;
create table sys_page_config_rule
(
    id  bigint  not null comment '主键',
    page_config_id  bigint  null comment '页面配置id',
    page_code  varchar(128)  null comment '页面编码',
    page_config_rule_name  varchar(128)  null comment '规则名称',
    page_config_rule_enable  varchar(8)  null comment '是否启用',
    page_config_rule_condition  longtext  null comment '条件JSON',
    page_config_rule_action  longtext  null comment '动作JSON',
    page_config_rule_sort  int  null comment '排序',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'sys_page_config_rule 表';

DROP TABLE IF EXISTS dms_eqms_inspection_template;
create table dms_eqms_inspection_template
(
    id  bigint  not null comment '主键',
    template_code  varchar(64)  null comment '模板编号',
    template_name  varchar(255)  null comment '模板名称',
    template_type  varchar(64)  null comment '模板类型',
    version  varchar(32)  null comment '版本',
    status  varchar(16)  null comment '状态',
    remark  varchar(500)  null comment '备注',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_eqms_inspection_template 表';

DROP TABLE IF EXISTS dms_eqms_number_application;
create table dms_eqms_number_application
(
    id  bigint  not null comment '主键',
    apply_code  varchar(64)  null comment '申请单号',
    device_name  varchar(255)  null comment '设备名称',
    device_type  varchar(64)  null comment '设备类型',
    applicant  varchar(64)  null comment '申请人',
    apply_date  datetime  null comment '申请日期',
    status  varchar(32)  null comment '状态',
    remark  varchar(500)  null comment '备注',
  tenant_id        varchar(20)   default '000000' comment '租户编号',
  create_dept      bigint        null comment '创建部门',
  create_by        bigint        null comment '创建者',
  create_time      datetime      null comment '创建时间',
  update_by        bigint        null comment '更新者',
  update_time      datetime      null comment '更新时间' 
) engine=innodb comment = 'dms_eqms_number_application 表';
