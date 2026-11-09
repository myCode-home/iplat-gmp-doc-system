import { defineStore } from 'pinia';
import { getPageConfigPage } from '@/api/system/pageConfig';

export const usePageConfigStore = defineStore('pageConfig', () => {
  const pageConfig = ref<Map<string, []>>(new Map());

  /**
   * 获取页面配置
   * @param _key 页面配置key
   */
  const getPageConfig = async (_key: string) => {
    if (!_key) {
      return null;
    }
    const config = pageConfig.value.get(_key) || null
    if(!config){
      const res = await getPageConfigPage(_key)
      pageConfig.value.set(_key, res.data || null)
    }
    return JSON.parse(JSON.stringify(pageConfig.value.get(_key))) || null;
  };

  /**
   * 设置页面配置
   * @param _key 页面配置key
   * @param _value 页面配置value
   */
  const setPageConfig = (_key: string, _value: []) => {
    if (!_key) {
      return false;
    }
    try {
      pageConfig.value.set(_key, _value);
      return true;
    } catch (e) {
      console.error('Error in setPageConfig:', e);
      return false;
    }
  };

  /**
   * 删除页面配置
   * @param _key
   */
  const removePageConfig = (_key: string): boolean => {
    if (!_key) {
      return false;
    }
    try {
      return pageConfig.value.delete(_key);
    } catch (e) {
      console.error('Error in removePageConfig:', e);
      return false;
    }
  };

  /**
   * 清空页面配置
   */
  const cleanPageConfig = (): void => {
    pageConfig.value.clear();
  };

  return {
    pageConfig,
    getPageConfig,
    setPageConfig,
    removePageConfig,
    cleanPageConfig
  };
});
