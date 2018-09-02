package kozhin.floodfill.di

import dagger.Component
import kozhin.floodfill.presenter.MainPresenter
import kozhin.floodfill.view.MainFragment
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {

    fun inject(mainActivity: MainFragment)

    fun presenter(): MainPresenter

}