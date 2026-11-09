
import { createApp, provide  } from 'vue';
import SignatureDialog from './signature-dialog.vue';
import { newSignature, combination } from '@/api/system/electronicsignature/signatureDefine';

function showSignatureDialog(options = {}, confirm, cancel) {
  const buttonLoading = ref(false);
  const app = createApp(SignatureDialog, {
    ...options,
    onConfirm: (data) => {
      confirm(data, (loading) => {
        buttonLoading.value = loading;
      }, function (){
        unmount();
      })
    },
    onCancel: () => {
      cancel({ msg: '已取消签名！' });
      unmount();
    }
  });
  // 提供buttonLoading状态
  app.provide('buttonLoading', buttonLoading);
  // 创建容器并挂载
  const container = document.createElement('div');
  document.body.appendChild(container);
  const unmount = () => {
    app.unmount();
    document.body.removeChild(container);
  };
  app.mount(container);
}

const Signature = (data: Object, config: any) => {
  return new Promise((resolve, reject) => {
    showSignatureDialog(
      data,
      (signature, setLoading, close) =>{
        setLoading(true);
        newSignature(signature).then(res=>{
          console.log('签名成功')
          const users = signature.historyUsers.map(item=>{
            return {
              userName: item.userName,
              nickName: item.nickName,
              sduSort: item.sduSort,
              sduImplication: item.sduImplication,
              sduReason: item.sduReason
            }
          })
          resolve({ es: res.data, users: users });
          close();
        }).catch(error => {
          // reject({ msg: '签名失败' });
          // console.log('签名失败')
        }).finally(()=>{
          setLoading(false);
        });
      },
      (err) =>{
        reject(err);
        console.log('签名取消:', err.msg)
      }
    )
  })
}

const sig = (data: any, config?: any) =>{
  return new Promise((resolve, reject) => {
    if(typeof data === 'function'){
      const funcStr = data.toString();
      
      // 匹配 url 和 method
      const urlMatch = funcStr.match(/url:\s*["'`]([^"'`]+)["'`]/);
      const methodMatch = funcStr.match(/method:\s*["'`]([^"'`]+)["'`]/);
      
      if (!urlMatch || !methodMatch) {
        return reject({ msg: '无法解析函数中的 url 或 method' });
      }
      
      const url = urlMatch[1].replace(/^\/(dms|eqms)/, '');
      const method = methodMatch[1].toUpperCase();
      
      combination({ url, method }).then(res=>{
        if(res.data){
          return resolve(Signature(res, config));
        }else {
          return resolve(false)
        }
      }).catch(error => {
        return reject({ msg: '获取签名配置失败', error });
      })
    }else{
      return resolve(Signature(data, config));
    }
  })
}

export default sig;