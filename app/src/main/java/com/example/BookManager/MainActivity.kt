package com.example.BookManager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var bookViewModel: BookViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private lateinit var filterPrefs: FilterPreferences
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val drawerToggle = findViewById<ImageButton>(R.id.drawerToggle)

        // Handle drawer toggle manually
        drawerToggle.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            initApp()
        } else {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    initApp()
                } else {
                    Toast.makeText(this, "Authentication failed. Using local mode.", Toast.LENGTH_LONG).show()
                    initApp()
                }
            }
        }
    }

    private fun initApp() {
        filterPrefs = FilterPreferences(this)
        bookViewModel = ViewModelProvider(this)[BookViewModel::class.java]

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookAdapter = BookAdapter(
            onEditClick = { book -> showEditDialog(book) },
            onDeleteClick = { book -> bookViewModel.delete(book) }
        )
        recyclerView.adapter = bookAdapter

        applySavedFilter()

        val addButton: FloatingActionButton = findViewById(R.id.fabAdd)
        addButton.setOnClickListener { showAddDialog() }

        val filterButton: FloatingActionButton = findViewById(R.id.fabFilter)
        filterButton.setOnClickListener {
            val dialog = FilterDialogFragment(
                onFilterApplied = { type, value -> applyFilter(type, value) },
                onClearFilter = {
                    filterPrefs.clearFilter()
                    bookViewModel.allBooks.observe(this) {
                        bookAdapter.setBooks(it)
                    }
                }
            )
            dialog.show(supportFragmentManager, "FilterDialog")
        }

        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.nav_logout) {
                // Sign out and go to login
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            } else false
        }

        val menu = navigationView.menu
        val logoutItem = menu.findItem(R.id.nav_logout)
        logoutItem?.let {
            val span = android.text.SpannableString(it.title)
            span.setSpan(
                android.text.style.ForegroundColorSpan(android.graphics.Color.RED),
                0,
                span.length,
                0
            )
            it.title = span
        }

        val syncFab: FloatingActionButton = findViewById(R.id.fabSync)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.isAnonymous) {
            syncFab.isEnabled = false
            syncFab.alpha = 0.5f // faded to show it's disabled
        }

        syncFab.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null || user.isAnonymous) {
                Toast.makeText(this, "Please sign in to sync with cloud.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Syncing with cloud...", Toast.LENGTH_SHORT).show()

            bookViewModel.syncWithCloud {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Sync completed!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Initial sync with cloud if user is authenticated (not anonymous)
        if (currentUser != null && !currentUser.isAnonymous) {
            try {
                bookViewModel.syncWithCloud {
                    Log.d("MainActivity", "Initial cloud sync completed")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Initial cloud sync failed", e)
                Toast.makeText(this, "Cloud sync failed. Running locally.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applySavedFilter() {
        val (type, value) = filterPrefs.getFilter()
        if (type != null) {
            when (type) {
                "FINISHED" -> bookViewModel.getFinishedBooks().observe(this) {
                    bookAdapter.setBooks(it)
                }
                "UNDER" -> bookViewModel.getBooksUnderPercentage(value!!.toInt()).observe(this) {
                    bookAdapter.setBooks(it)
                }
                "ABOVE" -> bookViewModel.getBooksAbovePercentage(value!!.toInt()).observe(this) {
                    bookAdapter.setBooks(it)
                }
                "AUTHOR" -> bookViewModel.getBooksByAuthor(value!!).observe(this) {
                    bookAdapter.setBooks(it)
                }
                "TITLE" -> bookViewModel.getBooksByTitle(value!!).observe(this) {
                    bookAdapter.setBooks(it)
                }
            }
        } else {
            bookViewModel.allBooks.observe(this) {
                bookAdapter.setBooks(it)
            }
        }
    }

    private fun applyFilter(type: FilterDialogFragment.FilterType, value: String?) {
        when (type) {
            FilterDialogFragment.FilterType.FINISHED -> {
                filterPrefs.saveFilter("FINISHED", null)
                bookViewModel.getFinishedBooks().observe(this) {
                    bookAdapter.setBooks(it)
                }
            }
            FilterDialogFragment.FilterType.UNDER_PERCENTAGE -> {
                filterPrefs.saveFilter("UNDER", value)
                bookViewModel.getBooksUnderPercentage(value!!.toInt()).observe(this) {
                    bookAdapter.setBooks(it)
                }
            }
            FilterDialogFragment.FilterType.ABOVE_PERCENTAGE -> {
                filterPrefs.saveFilter("ABOVE", value)
                bookViewModel.getBooksAbovePercentage(value!!.toInt()).observe(this) {
                    bookAdapter.setBooks(it)
                }
            }
            FilterDialogFragment.FilterType.AUTHOR -> {
                filterPrefs.saveFilter("AUTHOR", value)
                bookViewModel.getBooksByAuthor(value!!).observe(this) {
                    bookAdapter.setBooks(it)
                }
            }
            FilterDialogFragment.FilterType.TITLE -> {
                filterPrefs.saveFilter("TITLE", value)
                bookViewModel.getBooksByTitle(value!!).observe(this) {
                    bookAdapter.setBooks(it)
                }
            }
        }
    }

    private fun showAddDialog() {
        val dialog = AddEditBookDialogFragment.newInstance(null)
        dialog.show(supportFragmentManager, "AddBookDialog")
    }

    private fun showEditDialog(book: Book) {
        val dialog = AddEditBookDialogFragment.newInstance(book)
        dialog.show(supportFragmentManager, "EditBookDialog")
    }
}