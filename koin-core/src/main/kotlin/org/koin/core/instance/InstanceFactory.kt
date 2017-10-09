package org.koin.core.instance

import org.koin.core.bean.BeanDefinition
import org.koin.core.bean.BeanRegistry
import org.koin.error.BeanDefinitionException
import org.koin.error.BeanInstanceCreationException
import java.util.concurrent.ConcurrentHashMap

/**
 * Instance factory - handle objects creation against BeanRegistry
 * @author - Arnaud GIULIANI
 */
class InstanceFactory(val beanRegistry: BeanRegistry) {

    val instances = ConcurrentHashMap<BeanDefinition<*>, Any>()

    fun <T> resolveInstance(def: BeanDefinition<*>): T = retrieveInstance(def)

    /**
     * Retrieve or create bean instance
     */
    private fun <T> retrieveInstance(def: BeanDefinition<*>): T {
        var instance = findInstance<T>(def)
        if (instance == null) {
            instance = createInstance(def)
        }
        return instance!!
    }

    /**
     * Find existing instance
     */
    private fun <T> findInstance(def: BeanDefinition<*>): T? {
        val existingClass = instances.keys.firstOrNull { it == def }
        return if (existingClass != null) {
            instances[existingClass] as? T
        } else {
            null
        }
    }

    /**
     * create instance for given bean definition
     */
    private fun <T> createInstance(def: BeanDefinition<*>): T {
        val scope = beanRegistry.getScope(def)
        if (scope == null) throw BeanDefinitionException("Can't create bean $def in : $scope -- Scope has not been declared")
        else {
            try {
                val instance = def.definition.invoke() as Any
                instances[def] = instance
                instance as T
                return instance
            } catch (e: Throwable) {
                throw BeanInstanceCreationException("Can't create bean $def due to error : $e")
            }
        }
    }
//
//    fun deleteInstance(vararg kClasses: KClass<*>) {
//        kClasses.forEach { clazz ->
//            val res = instances.keys.filter { it.clazz == clazz }
//            res.forEach { def -> instances.remove(def) }
//        }
//    }
//
//    fun clear() {
//        instances.clear()
//    }
}