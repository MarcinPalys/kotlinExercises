package pl.wsei.pam.lab03

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pl.wsei.pam.lab01.R
import java.util.Random
import android.media.MediaPlayer
import android.view.Menu
import android.view.MenuItem

class Lab03Activity : AppCompatActivity() {
    private lateinit var completionPlayer: MediaPlayer
    private lateinit var negativePlayer: MediaPlayer
    private lateinit var mBoard: GridLayout
    private lateinit var mBoardModel: MemoryBoardView
    private var isBoardLocked = false
    @Volatile
    private var isSound = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lab03)

        mBoard = findViewById(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(mBoard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val size = intent.getIntArrayExtra("size") ?: intArrayOf(3, 3)
        val rows = size[0]
        val cols = size[1]

        mBoard.columnCount = cols
        mBoard.rowCount = rows

        mBoardModel = MemoryBoardView(mBoard, cols, rows)

        if (savedInstanceState != null) {
            val boardState = savedInstanceState.getIntArray("boardState")
                ?: IntArray(cols * rows) { -1 }
            mBoardModel.setState(boardState)
        }

        mBoardModel.setOnGameChangeListener { e ->
            when (e.state) {
                GameStates.Matching -> {
                    if (isBoardLocked) return@setOnGameChangeListener
                    e.tiles.forEach { it.revealed = true }
                }
                GameStates.Match -> {
                    isBoardLocked = true
                    if (isSound) completionPlayer.start()
                    e.tiles.forEach { it.revealed = true }
                    e.tiles.forEach { tile ->
                        animatePairedButton(tile.button) {
                            isBoardLocked = false
                        }
                    }
                }
                GameStates.NoMatch -> {
                    isBoardLocked = true
                    if (isSound) negativePlayer.start()
                    e.tiles.forEach { it.revealed = true }
                    e.tiles.forEach { tile ->
                        animateUnpairedButton(tile.button) {
                            runOnUiThread {
                                e.tiles.forEach { it.revealed = false }
                                isBoardLocked = false
                            }
                        }
                    }
                }
                GameStates.Finished -> {
                    e.tiles.forEach { it.revealed = true }
                    Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun animatePairedButton(button: ImageButton, action: Runnable) {
        val set = AnimatorSet()
        val random = Random()
        button.pivotX = random.nextFloat() * 200f
        button.pivotY = random.nextFloat() * 200f

        val rotation = ObjectAnimator.ofFloat(button, "rotation", 1080f)
        val scallingX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 4f)
        val scallingY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 4f)
        val fade = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f)

        set.startDelay = 500
        set.duration = 2000
        set.interpolator = DecelerateInterpolator()
        set.playTogether(rotation, scallingX, scallingY, fade)

        set.addListener(object : AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                button.scaleX = 1f
                button.scaleY = 1f
                button.alpha = 0.0f
                action.run()
            }
            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        set.start()
    }

    private fun animateUnpairedButton(button: ImageButton, action: Runnable) {
        val set = AnimatorSet()
        val rotation = ObjectAnimator.ofFloat(button, "rotation", 0f, -15f, 15f, -15f, 15f, 0f)

        set.duration = 600
        set.interpolator = DecelerateInterpolator()
        set.play(rotation)

        set.addListener(object : AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                button.rotation = 0f
                action.run()
            }
            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        set.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray("boardState", mBoardModel.getState())
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d("SOUND", "onResume called, isSound = $isSound")
        completionPlayer = MediaPlayer.create(applicationContext, R.raw.completion)
        negativePlayer = MediaPlayer.create(applicationContext, R.raw.negative_guitar)
    }

    override fun onPause() {
        super.onPause()
        android.util.Log.d("SOUND", "onPause called, isSound = $isSound")
        completionPlayer.release()
        negativePlayer.release()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.board_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.board_activity_sound -> {
                if (isSound) {
                    Toast.makeText(this, "Sound off", Toast.LENGTH_SHORT).show()
                    item.setIcon(R.drawable.baseline_volume_off_24)
                    isSound = false
                } else {
                    Toast.makeText(this, "Sound on", Toast.LENGTH_SHORT).show()
                    item.setIcon(R.drawable.baseline_volume_up_24)
                    isSound = true
                }
            }
        }
        return true
    }
}