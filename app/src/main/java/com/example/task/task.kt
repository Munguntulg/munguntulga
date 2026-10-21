
package com.example.task

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.task.ui.theme.TaskTheme
import com.example.task.voice.Narrator
import com.example.task.voice.VoiceStudioClient
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var narrator: Narrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        narrator = Narrator(
            context = this,
            client = VoiceStudioClient(BuildConfig.VOICESTUDIO_URL, BuildConfig.VOICESTUDIO_API_KEY),
            voice = BuildConfig.VOICESTUDIO_VOICE,
        )
        setContent {
            TaskTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val scope = rememberCoroutineScope()
                    var engine by remember { mutableStateOf<Narrator.Engine?>(null) }
                    val narration = stringResource(R.string.all_task_completed) + ". " +
                        stringResource(R.string.nice_work)
                    TaskCompletedScreen(
                        engine = engine,
                        onReadAloud = { scope.launch { engine = narrator.speak(narration) } },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        narrator.release()
        super.onDestroy()
    }
}

@Composable
fun TaskCompletedScreen(
    engine: Narrator.Engine? = null,
    onReadAloud: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val image = painterResource(R.drawable.ic_task_completed)
        Image(painter = image, contentDescription = null)
        Text(
            text = stringResource(R.string.all_task_completed),
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.nice_work),
            fontSize = 16.sp
        )
        Button(
            onClick = onReadAloud,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(text = stringResource(R.string.read_aloud))
        }
        if (engine != null) {
            Text(
                text = stringResource(
                    when (engine) {
                        Narrator.Engine.VoiceStudio -> R.string.spoken_by_voicestudio
                        Narrator.Engine.Device -> R.string.spoken_by_device
                    }
                ),
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskCompletedPreview() {
    TaskTheme {
        TaskCompletedScreen()
    }
}