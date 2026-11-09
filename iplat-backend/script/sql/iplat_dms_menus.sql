-- ----------------------------
-- 演示用 DMS 菜单（iplat-front 前端业务模块）
-- 仅用于本地演示环境，可重复执行
-- ----------------------------
DELETE FROM sys_menu WHERE menu_id BETWEEN 2000 AND 2199;

-- 文件管理
INSERT INTO sys_menu VALUES (2000, '文件管理', 0, 6, 'dms', NULL, '', 1, 0, 'M', '0', '0', '', 'documentation', 103, 1, sysdate(), NULL, NULL, '文件管理目录');

-- 文件库
INSERT INTO sys_menu VALUES (2010, '文件库', 2000, 1, 'fileLib', 'ParentView', '', 1, 0, 'M', '0', '0', '', 'category', 103, 1, sysdate(), NULL, NULL, '文件库目录');
INSERT INTO sys_menu VALUES (2011, '生效文件库', 2010, 1, 'activeFileLib', 'dms/fileLib/activeFileLib/index', '', 1, 0, 'C', '0', '0', '', 'documentation', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2012, '未生效文件库', 2010, 2, 'notActiveFileLib', 'dms/fileLib/notActiveFileLib/index', '', 1, 0, 'C', '0', '0', '', 'documentation', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2013, '失效文件库', 2010, 3, 'invalidFileLib', 'dms/fileLib/invalidFileLib/index', '', 1, 0, 'C', '0', '0', '', 'documentation', 103, 1, sysdate(), NULL, NULL, '');

-- 记录库
INSERT INTO sys_menu VALUES (2020, '记录库', 2000, 2, 'recordLibrary', 'ParentView', '', 1, 0, 'M', '0', '0', '', 'list', 103, 1, sysdate(), NULL, NULL, '记录库目录');
INSERT INTO sys_menu VALUES (2021, '生效记录库', 2020, 1, 'activeRecordLibrary', 'dms/recordLibrary/activeRecordLibrary/index', '', 1, 0, 'C', '0', '0', '', 'list', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2022, '历史记录库', 2020, 2, 'historyRecordLibrary', 'dms/recordLibrary/historyRecordLibrary/index', '', 1, 0, 'C', '0', '0', '', 'list', 103, 1, sysdate(), NULL, NULL, '');

-- 模板管理
INSERT INTO sys_menu VALUES (2030, '模板管理', 2000, 3, 'templateManagement', 'ParentView', '', 1, 0, 'M', '0', '0', '', 'form', 103, 1, sysdate(), NULL, NULL, '模板管理目录');
INSERT INTO sys_menu VALUES (2031, '生效模板库', 2030, 1, 'templateActiveLibrary', 'dms/templateManagement/templateActiveLibrary/index', '', 1, 0, 'C', '0', '0', '', 'form', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2032, '历史模板库', 2030, 2, 'historyTemplateLibrary', 'dms/templateManagement/historyTemplateLibrary/index', '', 1, 0, 'C', '0', '0', '', 'form', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2033, '模板编制', 2030, 3, 'templateEdit', 'dms/templateManagement/templateEdit/index', '', 1, 0, 'C', '0', '0', '', 'edit', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2034, '模板审批', 2030, 4, 'templateApprove', 'dms/templateManagement/templateApprove/index', '', 1, 0, 'C', '0', '0', '', 'edit', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2035, '模板追踪', 2030, 5, 'templateTrack', 'dms/templateManagement/templateTrack/index', '', 1, 0, 'C', '0', '0', '', 'edit', 103, 1, sysdate(), NULL, NULL, '');

-- 文档创建
INSERT INTO sys_menu VALUES (2040, '文档创建', 2000, 4, 'docCreate', 'ParentView', '', 1, 0, 'M', '0', '0', '', 'edit', 103, 1, sysdate(), NULL, NULL, '文档创建目录');
INSERT INTO sys_menu VALUES (2041, '文档编制', 2040, 1, 'docEedit', 'dms/docCreate/docEedit/index', '', 1, 0, 'C', '0', '0', '', 'edit', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2042, '文档审批', 2040, 2, 'docApprove', 'dms/docCreate/docApprove/index', '', 1, 0, 'C', '0', '0', '', 'edit', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2043, '文档追踪', 2040, 3, 'docTrack', 'dms/docCreate/docTrack/index', '', 1, 0, 'C', '0', '0', '', 'edit', 103, 1, sysdate(), NULL, NULL, '');

-- 记录变更
INSERT INTO sys_menu VALUES (2050, '记录变更', 2000, 5, 'recordAlteration', 'ParentView', '', 1, 0, 'M', '0', '0', '', 'list', 103, 1, sysdate(), NULL, NULL, '记录变更目录');
INSERT INTO sys_menu VALUES (2051, '变更申请', 2050, 1, 'recordAlterationApply', 'dms/recordAlteration/recordAlterationApply/index', '', 1, 0, 'C', '0', '0', '', 'list', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2052, '变更审批', 2050, 2, 'recordAlterationApprove', 'dms/recordAlteration/recordAlterationApprove/index', '', 1, 0, 'C', '0', '0', '', 'list', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2053, '变更追踪', 2050, 3, 'recordAlterationTrack', 'dms/recordAlteration/recordAlterationTrack/index', '', 1, 0, 'C', '0', '0', '', 'list', 103, 1, sysdate(), NULL, NULL, '');

-- 记录修订
INSERT INTO sys_menu VALUES (2060, '记录修订', 2000, 6, 'recordAlterationEdit', 'ParentView', '', 1, 0, 'M', '0', '0', '', 'edit', 103, 1, sysdate(), NULL, NULL, '记录修订目录');
INSERT INTO sys_menu VALUES (2061, '记录编制', 2060, 1, 'docRecordEdit', 'dms/recordAlterationEdit/docRecordEdit/index', '', 1, 0, 'C', '0', '0', '', 'edit', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2062, '记录审批', 2060, 2, 'docRecordApprove', 'dms/recordAlterationEdit/docRecordApprove/index', '', 1, 0, 'C', '0', '0', '', 'edit', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2063, '记录追踪', 2060, 3, 'docRecordTrack', 'dms/recordAlterationEdit/docRecordTrack/index', '', 1, 0, 'C', '0', '0', '', 'edit', 103, 1, sysdate(), NULL, NULL, '');

-- 文档失效
INSERT INTO sys_menu VALUES (2070, '文档失效', 2000, 7, 'documentInvalidation', 'ParentView', '', 1, 0, 'M', '0', '0', '', 'close', 103, 1, sysdate(), NULL, NULL, '文档失效目录');
INSERT INTO sys_menu VALUES (2071, '失效申请', 2070, 1, 'docInvalidationApply', 'dms/documentInvalidation/docInvalidationApply/index', '', 1, 0, 'C', '0', '0', '', 'close', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2072, '失效审批', 2070, 2, 'docInvalidationApprove', 'dms/documentInvalidation/docInvalidationApprove/index', '', 1, 0, 'C', '0', '0', '', 'close', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2073, '失效追踪', 2070, 3, 'docInvalidationTrack', 'dms/documentInvalidation/docInvalidationTrack/index', '', 1, 0, 'C', '0', '0', '', 'close', 103, 1, sysdate(), NULL, NULL, '');

-- 记录失效
INSERT INTO sys_menu VALUES (2080, '记录失效', 2000, 8, 'recordInvalidation', 'ParentView', '', 1, 0, 'M', '0', '0', '', 'close', 103, 1, sysdate(), NULL, NULL, '记录失效目录');
INSERT INTO sys_menu VALUES (2081, '失效申请', 2080, 1, 'InvalidationApply', 'dms/recordInvalidation/InvalidationApply/index', '', 1, 0, 'C', '0', '0', '', 'close', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2082, '失效审批', 2080, 2, 'InvalidationApprove', 'dms/recordInvalidation/InvalidationApprove/index', '', 1, 0, 'C', '0', '0', '', 'close', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2083, '失效追踪', 2080, 3, 'InvalidationTrack', 'dms/recordInvalidation/InvalidationTrack/index', '', 1, 0, 'C', '0', '0', '', 'close', 103, 1, sysdate(), NULL, NULL, '');

-- 文件借阅
INSERT INTO sys_menu VALUES (2090, '文件借阅', 2000, 9, 'fileLendOrder', 'ParentView', '', 1, 0, 'M', '0', '0', '', 'email', 103, 1, sysdate(), NULL, NULL, '文件借阅目录');
INSERT INTO sys_menu VALUES (2091, '借阅申请', 2090, 1, 'lendApply', 'dms/fileLendOrder/lendApply/index', '', 1, 0, 'C', '0', '0', '', 'email', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2092, '借阅审批', 2090, 2, 'lendApprove', 'dms/fileLendOrder/lendApprove/index', '', 1, 0, 'C', '0', '0', '', 'email', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2093, '借阅追踪', 2090, 3, 'lendTrack', 'dms/fileLendOrder/lendTrack/index', '', 1, 0, 'C', '0', '0', '', 'email', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2094, '借阅库', 2090, 4, 'lendlibrary', 'dms/fileLendOrder/lendlibrary/index', '', 1, 0, 'C', '0', '0', '', 'email', 103, 1, sysdate(), NULL, NULL, '');

-- 批量修订
INSERT INTO sys_menu VALUES (2100, '批量修订', 2000, 10, 'batchRevision', 'ParentView', '', 1, 0, 'M', '0', '0', '', 'edit', 103, 1, sysdate(), NULL, NULL, '批量修订目录');
INSERT INTO sys_menu VALUES (2101, '修订申请', 2100, 1, 'batchApply', 'dms/batchRevision/batchApply/index', '', 1, 0, 'C', '0', '0', '', 'edit', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2102, '修订审批', 2100, 2, 'batchApprove', 'dms/batchRevision/batchApprove/index', '', 1, 0, 'C', '0', '0', '', 'edit', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2103, '修订追踪', 2100, 3, 'batchTrack', 'dms/batchRevision/batchTrack/index', '', 1, 0, 'C', '0', '0', '', 'edit', 103, 1, sysdate(), NULL, NULL, '');

-- 批量打印
INSERT INTO sys_menu VALUES (2110, '批量打印', 2000, 11, 'bulkPrint', 'ParentView', '', 1, 0, 'M', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '批量打印目录');
INSERT INTO sys_menu VALUES (2111, '打印申请', 2110, 1, 'bulkPrintApply', 'dms/bulkPrint/bulkPrintApply/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2112, '打印审批', 2110, 2, 'bulkPrintApprove', 'dms/bulkPrint/bulkPrintApprove/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2113, '打印追踪', 2110, 3, 'bulkPrintTrack', 'dms/bulkPrint/bulkPrintTrack/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');

-- 成册打印
INSERT INTO sys_menu VALUES (2120, '成册打印', 2000, 12, 'bookPrint', 'ParentView', '', 1, 0, 'M', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '成册打印目录');
INSERT INTO sys_menu VALUES (2121, '成册申请', 2120, 1, 'bookPrintApply', 'dms/bookPrint/bookPrintApply/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2122, '成册审批', 2120, 2, 'bookPrintApprove', 'dms/bookPrint/bookPrintApprove/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2123, '成册组成', 2120, 3, 'bookPrintConsist', 'dms/bookPrint/bookPrintConsist/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2124, '成册打印', 2120, 4, 'bookPrintPage', 'dms/bookPrint/bookPrintPage/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2125, '扫描分发', 2120, 5, 'bookPrintScanIssue', 'dms/bookPrint/bookPrintScanIssue/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2126, '扫描回收', 2120, 6, 'bookPrintScanRecycle', 'dms/bookPrint/bookPrintScanRecycle/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2127, '成册销毁', 2120, 7, 'bookPrintDestruction', 'dms/bookPrint/bookPrintDestruction/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2128, '成册追踪', 2120, 8, 'bookPrintTrack', 'dms/bookPrint/bookPrintTrack/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2129, '打印追踪', 2120, 9, 'bookPrintoutTrack', 'dms/bookPrint/bookPrintoutTrack/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');

-- 打印分发
INSERT INTO sys_menu VALUES (2130, '打印分发', 2000, 13, 'printDistribution', 'ParentView', '', 1, 0, 'M', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '打印分发目录');
INSERT INTO sys_menu VALUES (2131, '打印申请', 2130, 1, 'printApply', 'dms/printDistribution/printApply/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2132, '打印审批', 2130, 2, 'printApplyApprove', 'dms/printDistribution/printApplyApprove/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2133, '文件追踪', 2130, 3, 'printApplyDocumentTracking', 'dms/printDistribution/printApplyDocumentTracking/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2134, '打印页面', 2130, 4, 'printApplyPage', 'dms/printDistribution/printApplyPage/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2135, '扫描分发', 2130, 5, 'printApplyScanDistribute', 'dms/printDistribution/printApplyScanDistribute/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2136, '扫描回收', 2130, 6, 'printApplyScanRecycling', 'dms/printDistribution/printApplyScanRecycling/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2137, '扫描关闭', 2130, 7, 'printApplyScanClose', 'dms/printDistribution/printApplyScanClose/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2138, '销毁确认', 2130, 8, 'printApplyDestructionConfirmation', 'dms/printDistribution/printApplyDestructionConfirmation/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2139, '审批追踪', 2130, 9, 'printApplyApproveTracking', 'dms/printDistribution/printApplyApproveTracking/index', '', 1, 0, 'C', '0', '0', '', 'printer', 103, 1, sysdate(), NULL, NULL, '');

-- 培训
INSERT INTO sys_menu VALUES (2140, '培训', 2000, 14, 'training', 'ParentView', '', 1, 0, 'M', '0', '0', '', 'education', 103, 1, sysdate(), NULL, NULL, '培训目录');
INSERT INTO sys_menu VALUES (2141, '培训确认', 2140, 1, 'trainingConfirmation', 'dms/training/trainingConfirmation/index', '', 1, 0, 'C', '0', '0', '', 'education', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2142, '培训生效', 2140, 2, 'trainingEffective', 'dms/training/trainingEffective/index', '', 1, 0, 'C', '0', '0', '', 'education', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2143, '培训追踪', 2140, 3, 'trainingTrack', 'dms/training/trainingTrack/index', '', 1, 0, 'C', '0', '0', '', 'education', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2144, '培训人员配置', 2140, 4, 'trainingUserConfig', 'dms/training/trainingUserConfig/index', '', 1, 0, 'C', '0', '0', '', 'education', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2145, '待生效文件', 2140, 5, 'waitActiveFile', 'dms/training/waitActiveFile/index', '', 1, 0, 'C', '0', '0', '', 'education', 103, 1, sysdate(), NULL, NULL, '');

-- 操作单
INSERT INTO sys_menu VALUES (2150, '操作单', 2000, 15, 'operateOrder', 'ParentView', '', 1, 0, 'M', '0', '0', '', 'form', 103, 1, sysdate(), NULL, NULL, '操作单目录');
INSERT INTO sys_menu VALUES (2151, '新增操作单', 2150, 1, 'addOperateOrder', 'dms/operateOrder/addOperateOrder/index', '', 1, 0, 'C', '0', '0', '', 'form', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2152, '编辑操作单', 2150, 2, 'editOperateOrder', 'dms/operateOrder/editOperateOrder/index', '', 1, 0, 'C', '0', '0', '', 'form', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2153, '操作单复核', 2150, 3, 'operateOrderReview', 'dms/operateOrder/operateOrderReview/index', '', 1, 0, 'C', '0', '0', '', 'form', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2154, '操作单跟进', 2150, 4, 'operateOrderFollow', 'dms/operateOrder/operateOrderFollow/index', '', 1, 0, 'C', '0', '0', '', 'form', 103, 1, sysdate(), NULL, NULL, '');

-- 文件系统选项
INSERT INTO sys_menu VALUES (2160, '文件系统选项', 2000, 16, 'fileSystemOption', 'ParentView', '', 1, 0, 'M', '0', '0', '', 'dict', 103, 1, sysdate(), NULL, NULL, '文件系统选项目录');
INSERT INTO sys_menu VALUES (2161, '文件类型', 2160, 1, 'fileType', 'dms/fileSystemOption/fileType/index', '', 1, 0, 'C', '0', '0', '', 'dict', 103, 1, sysdate(), NULL, NULL, '');

-- 设备管理
INSERT INTO sys_menu VALUES (2170, '设备管理', 0, 7, 'eqms', NULL, '', 1, 0, 'M', '0', '0', '', 'monitor', 103, 1, sysdate(), NULL, NULL, '设备管理目录');
INSERT INTO sys_menu VALUES (2171, '设备管理配置', 2170, 1, 'deviceManagementCconfig', 'eqms/deviceManagementCconfig/numberApplication/index', '', 1, 0, 'C', '0', '0', '', 'monitor', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES (2172, '检验模板', 2170, 2, 'inspectionTemplate', 'eqms/inspectionTemplate/index', '', 1, 0, 'C', '0', '0', '', 'monitor', 103, 1, sysdate(), NULL, NULL, '');
