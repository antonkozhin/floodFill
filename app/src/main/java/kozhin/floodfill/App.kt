package kozhin.floodfill

import android.app.Application
import kozhin.floodfill.di.ApplicationComponent
import kozhin.floodfill.di.ApplicationModule
import kozhin.floodfill.di.DaggerApplicationComponent

class App: Application() {

    companion object {
        var applicationComponent: ApplicationComponent? = null
    }

    override fun onCreate() {
        super.onCreate()
        applicationComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(ApplicationModule(this))
                .build()
    }
}