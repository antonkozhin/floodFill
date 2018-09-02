package kozhin.floodfill

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kozhin.floodfill.view.MainFragment

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mainFragment = MainFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, mainFragment, MainFragment.TAG)
                .commitAllowingStateLoss()
    }

}
