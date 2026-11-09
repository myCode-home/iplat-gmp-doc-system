import { parse, eval as evaluate } from 'expression-eval'

export class InteractionManager {
  constructor(formData, fieldStates, formMethods = {}) {
    this.formData = formData
    this.fieldStates = fieldStates
    this.formMethods = formMethods
    this.rules = []
  }

  // 添加规则
  addRules(rules) {
    this.rules = this.rules.concat(rules)
  }

  // 应用所有规则
  async applyAllRules(formRef) {
    for (const rule of this.rules) {
      await this.applyRule(rule, formRef)
    }
  }

  // 应用单个规则
  async applyRule(rule, formRef) {
    try {
      const conditionMet = await this.checkCondition(rule.condition)
      if (conditionMet) {
        await this.applyActions(rule.actions, formRef)
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

  // 执行动作
  async applyActions(actions, formRef) {
    for (const action of actions) {
      await this.applyAction(action, formRef)
    }
  }

  // 执行单个动作
  async applyAction(action, formRef) {
    const { target, action: actionType, value, expression } = action
    
    switch (actionType) {
      case 'show':
        this.fieldStates[target].visible = true
        break
      case 'hide':
        this.fieldStates[target].visible = false
        this.formData[target] = ''
        await this.clearFieldValidation(target, formRef)
        break
      case 'required':
        this.fieldStates[target].required = true
        break
      case 'optional':
        this.fieldStates[target].required = false
        await this.clearFieldValidation(target, formRef)
        break
      case 'readonly':
        console.log('readonly',target)
        this.fieldStates[target].readonly = true
        break
      case 'editable':
        this.fieldStates[target].readonly = false
        break
      case 'clear':
        this.formData[target] = ''
        await this.clearFieldValidation(target, formRef)
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