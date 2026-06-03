package com.varram.taskquest

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.varram.taskquest.data.local.AppDatabase

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.varram.taskquest.GetRewaredApplication.Companion.db
import com.varram.taskquest.Reciver.ReminderReceiver
import com.varram.taskquest.adapters.MainPagerAdapter
import com.varram.taskquest.adapters.TaskAdapter
import com.varram.taskquest.data.local.TaskEntity
import com.varram.taskquest.data.local.UserStatsEntity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private lateinit var tvPoints: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private lateinit var btnAddTask: Button
    private lateinit var btnViewRewards: Button
    private lateinit var tvStreak: TextView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressedMethod()
        }
    }
    private fun onBackPressedMethod() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            finish()
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()

    }

    private fun initView() {
        viewPager = findViewById(R.id.viewPager)
        bottomNav = findViewById(R.id.bottomNav)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val menuIcon = findViewById<ImageView>(R.id.menu_icon)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        navigationView.setNavigationItemSelectedListener(this)
        navigationView.itemIconTintList = null
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        val drawerHeader = navigationView.getHeaderView(0)
        val username = drawerHeader.findViewById<TextView>(R.id.usrname)
        val email = drawerHeader.findViewById<TextView>(R.id.usremail)
        val profile_image = drawerHeader.findViewById<CircleImageView>(R.id.profileImg)



//
//        val userdata = dbHelper.getImageAndName()
//        username.setText(userdata.first)
//        val emailtext = dbHelper.getAllUsers()
//        email.text = emailtext.get(0).email

//        Glide.with(this@MainActivity)
//            .asBitmap()
//            .load(userdata.second)
//            .placeholder(R.drawable.profilepicture)
//            .into(profile_image)

        //init ViewPager
        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.dashboard -> viewPager.currentItem = 0
                R.id.rewards -> viewPager.currentItem = 1
                R.id.tasks -> viewPager.currentItem = 2
                R.id.stats -> viewPager.currentItem = 3
            }
            true
        }
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNav.menu.getItem(position).isChecked = true
            }
        })
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    private fun observeTasks() {
        lifecycleScope.launch {
            db.taskDao().getAllTasks().collect { tasks ->
                adapter.updateData(tasks)
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        initStatsAndLoad()
    }
    private fun loadStreak() {
        lifecycleScope.launch {
            val stats = db.userStatsDao().getStats()
            runOnUiThread {
                tvStreak.text = "Streak: ${stats?.streak ?: 0}"
            }
        }
    }
    private fun initStatsAndLoad() {
        lifecycleScope.launch {
            var stats = db.userStatsDao().getStats()

            if (stats == null) {
                db.userStatsDao().insertStats(UserStatsEntity())
                stats = db.userStatsDao().getStats()
            }

            runOnUiThread {
                tvPoints.text = "Points: ${stats?.totalPoints ?: 0}"
            }
        }
    }
    private fun completeTask(task: TaskEntity) {
        lifecycleScope.launch {

            if (task.isCompleted) return@launch

            // 1. Mark task complete
            val updatedTask = task.copy(isCompleted = true)
            db.taskDao().updateTask(updatedTask)

            // 2. Add points
            val stats = db.userStatsDao().getStats()

            stats?.let {
                val newPoints = stats.totalPoints + task.points
                db.userStatsDao().updateStats(stats.copy(totalPoints = newPoints))
            }
            initStatsAndLoad()
        }
    }
    fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
    private fun applyDailyPenaltyIfNeeded() {
        lifecycleScope.launch {

            val stats = db.userStatsDao().getStats() ?: return@launch
            val today = getTodayDate()

            if (stats.lastCheckedDate == today) return@launch

            val tasks = db.taskDao().getAllTasksOnce()

            var penalty = 0
            var allCompleted = true

            tasks.forEach { task ->
                if (task.isDaily) {
                    if (!task.isCompleted) {
                        penalty += task.points / 2
                        allCompleted = false
                    }
                }
            }

            val newPoints = (stats.totalPoints - penalty).coerceAtLeast(0)

            val newStreak = if (allCompleted) {
                stats.streak + 1
            } else {
                0
            }

            db.userStatsDao().updateStats(
                stats.copy(
                    totalPoints = newPoints,
                    lastCheckedDate = today,
                    streak = newStreak
                )
            )

            // Reset daily tasks
            tasks.forEach { task ->
                if (task.isDaily) {
                    db.taskDao().updateTask(task.copy(isCompleted = false))
                }
            }

            initStatsAndLoad()
            loadStreak()
        }
    }
    fun scheduleDailyReminder(context: Context) {

        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21) // 9 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        item.isChecked = false

        when (item.itemId) {
            R.id.nav_profile -> {
            }

            R.id.nav_help -> {
                drawerLayout.closeDrawer(GravityCompat.START)
            }

            R.id.nav_savedPost -> {
            }

            R.id.nav_logout -> {
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}