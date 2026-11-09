import { parse, eval as evaluate } from 'expression-eval'

export class InteractionManager {
  constructor(formData, fieldStates, formMethods = {}) {
    this.formData = formData
    this.fieldStates = fieldStates
    this.formMethods = formMethods
    this.rules = []
    // 新增一个对象用于存储待执行的动作
    this.pendingActions = {}
  }

  // 添加规则
  addRules(rules) {
    this.rules = this.rules.concat(rules)
  }

  // 应用所有规则
  async applyAllRules(formRef) {
    // console.log('applyAllRulesapplyAllRulesapplyAllRules')
    // console.log(this.rules)
  // 每次执行规则前清空pendingActions
    this.pendingActions = {}
    for (const rule of this.rules) {
      await this.applyRule(rule, formRef)
    }
    // 所有规则执行完毕后，统一处理pendingActions、
    // console.log('executePendingActions')
    // console.log(this.pendingActions)
    await this.executePendingActions(formRef)
  }

  // 应用单个规则
  async applyRule(rule, formRef) {

    try {
      const conditionMet = await this.checkCondition(rule.condition)
      // console.log(rule.condition)
      // console.log('conditionMetconditionMet')
      // console.log(conditionMet)
      if (conditionMet) {
        await this.addActionsList(rule.actions, formRef)
      }
    } catch (e) {
      console.error(`规则[${rule.name}]应用出错:`, e)
    }
  }

  // 检查条件
  async checkCondition(condition) {
    if (typeof condition === 'string') {
      return evaluate(parse(condition), this.formData)  
    }
    
    if (condition.type === 'async') {
      const method = this.formMethods[condition.method]
      return typeof method === 'function' 
        ? await method(this.formData) 
        : false
    }
    
    if (condition.logic) {
      const results = await Promise.all(
        condition.conditions.map(c => this.checkCondition(c))
      )
      return condition.logic === 'AND'
        ? results.every(Boolean)
        : results.some(Boolean)
    }
    
    return false
  }

  // 收集动作到pendingActions而不是立即执行
  async addActionsList(actions, formRef) {
    for (const action of actions) {
      // 不再直接执行applyAction，而是收集到pendingActions中
      const { target, action: actionType } = action
      if (target && target.length) {
        // 初始化动作类型数组
        if (!this.pendingActions[actionType]) {
          this.pendingActions[actionType] = []
        }
        
        // 将目标添加到对应的动作类型数组中
        target.forEach(item => {
          // 检查是否已经在其他动作类型中存在，如果存在则移除
          this.removeFromOtherActions(actionType, item)
          
          // 添加到当前动作类型数组中（避免重复）
          if (!this.pendingActions[actionType].includes(item)) {
            this.pendingActions[actionType].push(item)
          }
        })
      } else if (actionType === 'custom') {
        // 自定义动作仍需要立即执行
        await this.applyAction(action, formRef)
      }
    }
  }

  // 从其他动作类型中移除指定的目标（处理多种互斥关系）
  removeFromOtherActions(currentActionType, targetItem) {
    // 定义互斥关系映射
    const mutexMap = {
      'show': ['hide'],
      'hide': ['show'],
      'required': ['optional'],
      'optional': ['required'],
      'readonly': ['editable'],
      'editable': ['readonly']
    };

    // 获取当前动作类型的互斥动作类型列表
    const mutexTypes = mutexMap[currentActionType] || [];
    
    // 遍历互斥动作类型，从对应列表中移除目标项
    mutexTypes.forEach(mutexType => {
      const index = this.pendingActions[mutexType]?.indexOf(targetItem);
      if (index > -1) {
        this.pendingActions[mutexType].splice(index, 1);
      }
    });
  }

  // 统一执行所有待处理的动作
  async executePendingActions(formRef) {
    for (const [actionType, targets] of Object.entries(this.pendingActions)) {
      if (targets.length > 0) {
        const action = { 
          target: targets, 
          action: actionType 
        }
        await this.applyAction(action, formRef)
      }
    }
    // 执行完毕后清空pendingActions
    this.pendingActions = {}
  }

  // 执行单个动作
  async applyAction(action, formRef) {
    // console.log('applyActionapplyActionapplyAction')
    
    const { target, action: actionType, value, expression } = action
    // console.log(actionType)
    // console.log(target)
    switch (actionType) {
      case 'show':
        if(target&&target.length){
          target.forEach(item=>{
            if(this.fieldStates[item]){
              this.fieldStates[item].visible = true
            }
          })
        }
        break
      case 'hide':
        if(target&&target.length){
          // 使用Promise.all并行处理异步操作
          await Promise.all(target.map(async item => {
            if(this.fieldStates[item]){
              this.fieldStates[item].visible = false
            }
            this.formData[item] = ''
            await this.clearFieldValidation(item, formRef)
          }))
        }
        break
      case 'required':
        if(target&&target.length){
          target.forEach(item=>{
            if(this.fieldStates[item]){
              this.fieldStates[item].required = true
            }
          })
        }
        break
      case 'optional':
        if(target&&target.length){
          // 使用Promise.all并行处理异步操作
          await Promise.all(target.map(async item => {
            if(this.fieldStates[item]){
              this.fieldStates[item].required = false
            }
            await this.clearFieldValidation(item, formRef)
          }))
        }
        break
      case 'readonly':
        if(target&&target.length){
          target.forEach(item=>{
            if(this.fieldStates[item]){
              this.fieldStates[item].readonly = true
            }
          })
        }
        break
      case 'editable':
        if(target&&target.length){
          target.forEach(item=>{
            if(this.fieldStates[item]){
              this.fieldStates[item].readonly = false
              this.fieldStates[item].disabled = false
            }
          })
        }
        break
      case 'disabled':
        if(target&&target.length){
          target.forEach(item=>{
            if(this.fieldStates[item]){
              this.fieldStates[item].disabled = true
            }
          })
        }
        break
      case 'clear':
        if(target&&target.length){
          // 使用Promise.all并行处理异步操作
          await Promise.all(target.map(async item => {
            this.formData[item] = ''
            await this.clearFieldValidation(item, formRef)
          }))
        }
        break
      // case 'setValue':
      //   this.formData[target] = this.formData[target] || value
      //   break
      // case 'setValueByExpression':
      //   this.formData[target] = evaluate(parse(expression), this.formData)
      //   break
      case 'custom':
        const method = this.formMethods[action.method]
        if (typeof method === 'function') {
          await method(target,{...this.formData})
        }
        break
      default:
        console.warn(`未知动作类型: ${actionType}`)
    }
  }

  async clearFieldValidation(field, formRef) {
    if (formRef && field) {
      try {
        await formRef.clearValidate(field)
      } catch (e) {
        console.error(`清除字段[${field}]验证失败:`, e)
      }
    }
  }
}