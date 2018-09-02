package kozhin.floodfill.di

import android.app.Application
import dagger.Module
import kozhin.floodfill.presenter.MainPresenter
import dagger.Provides
import kozhin.floodfill.presenter.MainPresenterImpl
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: Application) {

    @Provides
    @Singleton
    fun presenter(): MainPresenter {
        return MainPresenterImpl()
    }

}