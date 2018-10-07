package es.ucm.demoarcore

import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.os.Bundle
import android.content.Intent





class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            val intent = Intent(this, HelloSceneformActivity::class.java)
            startActivity(intent)
        }
    }

}
