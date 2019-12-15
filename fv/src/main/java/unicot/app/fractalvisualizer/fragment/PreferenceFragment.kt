package unicot.app.fractalvisualizer.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import unicot.app.fractalvisualizer.R

class PreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference)

        val acknowledgement = findPreference(getString(R.string.preference_acknowledge_key))
        acknowledgement.setOnPreferenceClickListener {
            activity?.let{
                val intent = Intent()
                intent.setClass( it, OssLicensesMenuActivity::class.java)
                intent.putExtra("title", getString(R.string.preference_acknowledge_title))
                startActivity(intent)
            }
            return@setOnPreferenceClickListener true
        }
        val store = findPreference(getString(R.string.preference_store_key))
        store.setOnPreferenceClickListener {
            val url = Uri.parse(getString(R.string.store_url))
            val intent = Intent(Intent.ACTION_VIEW, url)

            startActivity(intent)

            return@setOnPreferenceClickListener true
        }
    }
}