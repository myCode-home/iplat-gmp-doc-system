 
<template>
  <div class="doc">
    <!-- 此div用来加载PageOffice客户端控件，其中div的高宽及位置就决定了控件的大小及位置 -->
    <div style="width:auto; height:800px;" v-html="poHtmlCode"></div>
  </div>
</template>

<script setup>
  import request from '@/utils/request';
  import { ref, onMounted, nextTick } from 'vue'

  const poHtmlCode = ref('');
  const open_params = ref('');
  const responseData = ref({});

  function OnPageOfficeCtrlInit() {
    // PageOffice的初始化事件回调函数，您可以在这里添加自定义按钮
    if(responseData.value.save){
      pageofficectrl.AddCustomToolButton("保存", "Save", 1);//其中"Save"对应function Save()函数，并且需要在onMounted中挂载。
    }
    // pageofficectrl.CustomToolbar = false; //隐藏自定义工具栏
    // pageofficectrl.OfficeToolbars = false; //隐藏Office工具栏
    if(responseData.value.captionTitle){
      pageofficectrl.Caption = responseData.value.captionTitle; //修改标题栏文本
    }
    pageofficectrl.AddCustomToolButton("全屏/还原", "IsFullScreen", 4);
    pageofficectrl.AddCustomToolButton("-", "", 0);
    if(responseData.value.refillLabel){
      pageofficectrl.AddCustomToolButton("回填标签", "postponedLabelSelect()", 0);
    }
    pageofficectrl.AddCustomToolButton("关闭", "Close", 21);
  };

  function Save() {
    //使用SaveFilePage属性设置后端保存方法的Controller路由地址，这个地址必须从"/"开始，并且也可以向此路由地址传递json字符串参数，示例如下：
    let saveFileUrl = "/dms/tmpl/word/saveFile/"+open_params.value.id;
    let paramValue = new URLSearchParams(open_params.value);//为了简单起见，这里直接使用打开时的参数。
    pageofficectrl.SaveFilePage = `${saveFileUrl}?${paramValue.toString()}`;
    //在这里写您保存前的代码
    pageofficectrl.WebSave();
    //在这里写您保存后的代码，比如判断保存结果pageofficectrl.CustomSaveResult
    //alert(pageofficectrl.CustomSaveResult);
  };

  function IsFullScreen() {
    pageofficectrl.FullScreen = !pageofficectrl.FullScreen;
  };

  function Close() {
    pageofficectrl.CloseWindow();
  };

  function AfterDocumentOpened() {
    //在这里写您文档打开后自动触发的代码
    console.log('已打开pageoffice文档')
  };

  function postponedLabelSelect() {
    pageofficectrl.RunMacro( 'openPostponedLabel' ); //运行文档中的无返回值的宏命令.
  };

  function openFile(id) {
    // 发起GET请求到后端Controller的/doc/openFile路由
    return request({
      url: '/dms/tmpl/word/'+id,
      method: 'get',
      params: open_params.value
    });
  };

  onMounted(() => {
    console.log('初始化pageoffice')
    //使用pageofficectrl.WindowParams获取获取父页面(此项目中为：HomeView.vue)中POBrowser.openWindow()方法的第三个参数的值,获取到的值为string类型
    open_params.value = JSON.parse(pageofficectrl.WindowParams);
    // 请求后端打开文件
    openFile(open_params.value.id).then(response => {
      console.log(response)
      responseData.value = response;
      poHtmlCode.value = response.html;
      nextTick(() => {
        window.POPageMounted = { OnPageOfficeCtrlInit, AfterDocumentOpened, Save, IsFullScreen, Close, postponedLabelSelect };//其中OnPageOfficeCtrlInit必须
      });
    });
    //将需要回调的函数挂载到PageOffice控件，例如控件触发的事件、自定义按钮触发的函数。
    // window.POPageMounted = { OnPageOfficeCtrlInit, AfterDocumentOpened, Save, IsFullScreen, Close, postponedLabelSelect };//其中OnPageOfficeCtrlInit必须
  });
 </script>
