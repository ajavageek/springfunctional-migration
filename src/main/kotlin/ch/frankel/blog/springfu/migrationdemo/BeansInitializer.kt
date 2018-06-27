package ch.frankel.blog.springfu.migrationdemo

import org.springframework.context.*
import org.springframework.context.support.*

class BeansInitializer: ApplicationContextInitializer<GenericApplicationContext> {

    override fun initialize(context: GenericApplicationContext) {
        beans().initialize(context)
    }
}