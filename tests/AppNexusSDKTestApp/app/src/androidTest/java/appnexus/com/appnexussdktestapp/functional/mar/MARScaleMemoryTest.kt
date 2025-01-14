/*
 *    Copyright 2012 APPNEXUS INC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package appnexus.com.appnexussdktestapp.functional.mar

import android.content.Intent
import android.content.res.Resources
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.runner.AndroidJUnit4
import appnexus.com.appnexussdktestapp.BannerScaleLoadAndDisplayActivity
import appnexus.com.appnexussdktestapp.MARScaleLoadAndDisplayActivity
import appnexus.com.appnexussdktestapp.R
import com.appnexus.opensdk.utils.Clog
import com.microsoft.appcenter.espresso.Factory
import kotlinx.android.synthetic.main.activity_mar_load.*
import org.junit.*

import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class MARScaleMemoryTest {
    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    @get:Rule
    var reportHelper = Factory.getReportHelper()

    @Rule
    @JvmField
    var mActivityTestRule = IntentsTestRule(MARScaleLoadAndDisplayActivity::class.java, false, false)

    lateinit var marActivity: MARScaleLoadAndDisplayActivity

    @Before
    fun setup() {
        IdlingPolicies.setMasterPolicyTimeout(1, TimeUnit.MINUTES)
        IdlingPolicies.setIdlingResourceTimeout(1, TimeUnit.MINUTES)
    }

    @After
    fun destroy() {
        IdlingRegistry.getInstance().unregister(marActivity.idlingResource)
        reportHelper.label("Stopping App")
    }

    /*
    * Test for the Invalid Renderer Url for Banner Native Ad (NativeAssemblyRenderer)
    * */
    @Test
    fun marScaleForNumberOfCountMARs() {

        var intent = Intent()
        var count = 64
        intent.putExtra(BannerScaleLoadAndDisplayActivity.COUNT, count)
        mActivityTestRule.launchActivity(intent)
        marActivity = mActivityTestRule.activity
        IdlingRegistry.getInstance().register(marActivity.idlingResource)

        Thread.sleep(2000)

        Espresso.onView(ViewMatchers.withId(R.id.recyclerListAdView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Clog.e("COUNT: ", marActivity.recyclerListAdView.adapter!!.itemCount.toString())

        (0 until count-1 step 3).forEach { i-> marActivity.recyclerListAdView.smoothScrollToPosition(i)
        Thread.sleep(1000)}

        (count-1 until 0 step 3).forEach { i-> marActivity.recyclerListAdView.smoothScrollToPosition(i)
            Thread.sleep(1000)}

        Espresso.onView(ViewMatchers.withId(R.id.recyclerListAdView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))


    }
}
