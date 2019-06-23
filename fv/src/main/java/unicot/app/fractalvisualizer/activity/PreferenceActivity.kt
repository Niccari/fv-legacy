package unicot.app.fractalvisualizer.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import unicot.app.fractalvisualizer.fragment.PreferenceFragment

open class PreferenceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, PreferenceFragment())
                .commit()
    }
}
