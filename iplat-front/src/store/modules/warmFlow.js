import {setTokenName, setToken} from "@/utils/warmFlow/auth.js";
import {config} from "@/api/warmFlow/anony.js";

const useWarmFlowStore = defineStore(
  'warmFlow',
  {
    state: () => ({
      appParams: null
    }),
    actions: {
      async fetchTokenName() {
        const urlParams = new URLSearchParams(window.location.search);
        const params = {};
        let tokenNames = []
        await config().then(res => {
          if (res.code === 200 && res.data && res.data.tokenNameList && res.data.tokenNameList.length > 0) {
            tokenNames = res.data.tokenNameList
            console.log('tokenNamestokenNamestokenNamestokenNames')
            console.log(tokenNames)
            setTokenName(res.data.tokenNameList)
          }
        }).catch(() => {});
        this.appParams= null
        console.log('urlParamsurlParamsurlParams')
        console.log(urlParams)
        console.log(urlParams.entries())
        for (const [key, value] of urlParams.entries()) {
          console.log(key)
          console.log(value)
          if (value && 'undefined' !== value) {
            if (tokenNames && tokenNames.length > 0 && tokenNames.includes(key)) {
              setToken(key, value);
            }
            params[key] = value;
          }
        }
        this.appParams = params;
      }
    }
  })

export default useWarmFlowStore;