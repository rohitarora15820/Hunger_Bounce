package com.rohit.hungerbounce.activity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar

import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import com.rohit.hungerbounce.R
import com.rohit.hungerbounce.fragment.*
import com.rohit.hungerbounce.utility.SessionManager

class DashboardActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var toolbar: Toolbar
    lateinit var frameLayout: FrameLayout
    lateinit var navigationView: NavigationView
    private lateinit var sessionManager: SessionManager
    private lateinit var sharedPrefs: SharedPreferences
    var previousMenuItem:MenuItem?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        sessionManager = SessionManager(this@DashboardActivity)
        sharedPrefs = this@DashboardActivity.getSharedPreferences(
            sessionManager.PREF_NAME,
            sessionManager.PRIVATE_MODE
        )


        drawerLayout=findViewById(R.id.drawerLayout)
        coordinatorLayout=findViewById(R.id.coordinatorLayout)
        toolbar=findViewById(R.id.toolbar)
        frameLayout=findViewById(R.id.frame)
        navigationView=findViewById(R.id.navigationView)
        val actionBarDrawerToggle= ActionBarDrawerToggle(this@DashboardActivity,drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        openHome()

        navigationView.setNavigationItemSelectedListener {
            if(previousMenuItem!=null){
                previousMenuItem?.isChecked=false
            }
            it.isCheckable=true
            it.isChecked=true
            previousMenuItem=it
          when(it.itemId){
              R.id.Home ->{
                  supportActionBar?.title="Home"
                 supportFragmentManager.beginTransaction()
                     .replace(R.id.frame,HomeFragment())
                     .commit()
                  drawerLayout.closeDrawers()
              }
              R.id.myProfile ->{
                  supportActionBar?.title="Profile"
                  supportFragmentManager.beginTransaction()
                      .replace(R.id.frame,ProfileFragment())
                      .commit()
                  drawerLayout.closeDrawers()

              }
              R.id.favouriteRestaurants ->{
                  supportActionBar?.title="Favourite Restaurant"
                  supportFragmentManager.beginTransaction()
                      .replace(R.id.frame,FavouriteFragment())
                      .commit()
                  drawerLayout.closeDrawers()
              }
              R.id.orderHistory ->{
                  supportActionBar?.title="Order History"
                  supportFragmentManager.beginTransaction()
                      .replace(R.id.frame,RestaurantFragment())
                      .commit()
                  drawerLayout.closeDrawers()
              }
              R.id.faqs ->
              {
                  supportActionBar?.title="FAQs"
                  supportFragmentManager.beginTransaction()
                      .replace(R.id.frame,FaqsFragment())
                      .commit()
                  drawerLayout.closeDrawers()
              }
              R.id.logOut ->
              {
               //   Toast.makeText(this@DashboardActivity,"Clicked on logOut",Toast.LENGTH_SHORT).show()
                  val builder = AlertDialog.Builder(this@DashboardActivity)
                  builder.setTitle("Confirmation")
                      .setMessage("Are you sure you want exit?")
                      .setPositiveButton("Yes") { _, _ ->
                          sessionManager.setLogin(false)
                          sharedPrefs.edit().clear().apply()
                          startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                          Volley.newRequestQueue(this).cancelAll(this::class.java.simpleName)
                          ActivityCompat.finishAffinity(this)
                      }
                      .setNegativeButton("No") { _, _ ->
                          val fragment = HomeFragment()
                          val transaction = supportFragmentManager.beginTransaction()
                          transaction.replace(R.id.frame, fragment)
                          transaction.commit()
                          supportActionBar?.title = "All Restaurants"
                          navigationView.setCheckedItem(R.id.home)
                      }
                      .create()
                      .show()
              }
          }

            return@setNavigationItemSelectedListener true
        }
        setUpToolbar()
    }
    fun setUpToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.title="Toolbar Title"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id=item.itemId
        if (id==android.R.id.home){
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)}
    fun openHome(){
        supportActionBar?.title="Home"
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame,HomeFragment())

            .commit()
        drawerLayout.closeDrawers()
        navigationView.setCheckedItem(R.id.Home)
    }

    override fun onBackPressed() {
        val frag=supportFragmentManager.findFragmentById(R.id.frame)
        when(frag){
            !is HomeFragment->openHome()
        else->
        super.onBackPressed()}
    }
}
