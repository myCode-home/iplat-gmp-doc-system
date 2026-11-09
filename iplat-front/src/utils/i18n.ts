// translate router.meta.title, be used in breadcrumb sidebar tagsview
import i18n from '@/lang/index';
import { LanguageEnum } from '@/enums/LanguageEnum';

/**
 * 获取国际化路由，如果不存在则原生返回
 * @param title 路由名称
 * @returns {string}
 */
export const translateRouteTitle = (title: string): string => {
  const hasKey = i18n.global.te('route.' + title);
  if (hasKey) {
    const translatedTitle = i18n.global.t('route.' + title);
    return translatedTitle;
  }
  return title;
};
/**
 * 
 * @param key {zn: 中文名称, en: 英文名称} 或 字符串
 * @returns 对象：返回对应的名称，字符串：返回原字符串
 */
export const tr = (key: any) => {
  if(typeof key === 'object') {
    if(getLanguage() == 'zh_CN'){
      return key['zh']
    }else if(getLanguage() == 'en_US'){
      return key['en']
    }else {
      return key
    }
  }
  return key;
  // return i18n.global.t(key);
};

const getLanguage = () => {
  const language = localStorage.getItem('language');
  if(language){
    return language
  }
  return LanguageEnum.zh_CN;
};
