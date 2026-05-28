package com.varram.taskquest

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.varram.taskquest.Reciver.ReminderReceiver
import com.varram.taskquest.adapters.MainPagerAdapter
import com.varram.taskquest.adapters.TaskAdapter
import com.varram.taskquest.data.local.TaskEntity
import com.varram.taskquest.data.local.UserStatsEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var db: AppDatabase
    }
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


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val MIGRATION_3_4 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE tasks ADD COLUMN category TEXT NOT NULL DEFAULT 'Other'"
                )
            }
        }
        com.varram.taskquest.MainActivity.Companion.db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "task_db"
        )
            .addMigrations(MIGRATION_3_4)
            .build()

       /* setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.rvTasks)
        btnAddTask = findViewById(R.id.btnAddTask)
        btnViewRewards = findViewById(R.id.btnRewards)
        tvStreak = findViewById(R.id.tvStreak)
        tvPoints = findViewById(R.id.tvPoints)
        adapter = TaskAdapter(emptyList()) { task ->
            completeTask(task)
        }
        scheduleDailyReminder(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAddTask.setOnClickListener {
            startActivity(Intent(this, AddTaskScreen::class.java))
        }
        btnViewRewards.setOnClickListener {
            startActivity(Intent(this, RewardActivity::class.java))
        }

        observeTasks()
        initStatsAndLoad()
        loadStreak()
        applyDailyPenaltyIfNeeded()*/

            setContentView(R.layout.activity_main)
            viewPager = findViewById(R.id.viewPager)
            bottomNav = findViewById(R.id.bottomNav)
            drawerLayout = findViewById(R.id.drawer_layout)
            navView = findViewById(R.id.nav_view)

        // Setup toggle
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { /* handle */ true }
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
}