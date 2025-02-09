package eu.kanade.tachiyomi.ui.reader

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import com.chargemap.compose.numberpicker.ListItemPicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import uy.kohesive.injekt.injectLazy

class AutoPlayDialogFragment : DialogFragment() {

    private var positiveListener: ((Int) -> Unit)? = null

    private val preferences: PreferencesHelper by injectLazy()

    @SuppressLint("ResourceType")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val possibleValues = (1..15).map {
            "$it S"
        }
        val state = mutableStateOf(possibleValues[0])

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(requireContext().getString(R.string.auto_play))
            .setView(
                ComposeView(requireContext()).apply {
                    setContent {
                        val isDarkTheme = when (AppCompatDelegate.getDefaultNightMode()) {
                            AppCompatDelegate.MODE_NIGHT_YES -> true
                            AppCompatDelegate.MODE_NIGHT_NO -> false
                            else -> isSystemInDarkTheme() // You can define this function to check system theme preference if needed
                        }
                        MaterialTheme {
                            ProvideTextStyle(
                                value = LocalTextStyle.current.copy(
                                    color = if (isDarkTheme) Color.White else Color.Black,
                                ),
                            ) {
                                AutoPlayDialogContent(
                                    state,
                                    possibleValues,
                                    preferences.useAutoPlayProgress().asFlow()
                                        .collectAsState(
                                            initial = preferences.useAutoPlayProgress().get(),
                                        ),
                                ) {
                                    preferences.useAutoPlayProgress().set(it)
                                }
                            }
                        }
                    }
                },
            ).apply {
                setPositiveButton(requireContext().getString(R.string.start)) { _, _ ->
                    val position = possibleValues.indexOf(state.value) + 1
                    positiveListener?.invoke(position * 1000)
                }
                setNegativeButton(requireContext().getString(R.string.cancel)) { _, _ ->
                }
            }
            .create()
    }

    fun setPositiveListener(listener: (Int) -> Unit) {
        positiveListener = listener
    }
}

@Composable
fun AutoPlayDialogContent(
    number: MutableState<String>,
    possibleValues: List<String>,
    useProgress: State<Boolean>,
    onProgressEnable: (Boolean) -> Unit = {},
) {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        var state by remember { number }
        val isUseProgress by remember { useProgress }
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ListItemPicker(
                label = { it },
                value = state,
                onValueChange = { state = it },
                dividersColor = MaterialTheme.colorScheme.primary,
                list = possibleValues,
                textStyle = LocalTextStyle.current,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onProgressEnable.invoke(useProgress.value.not())
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material3.Text(
                    text = LocalContext.current.getString(R.string.use_auto_play_progress),
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                )
                Switch(
                    checked = isUseProgress,
                    onCheckedChange = {
                        onProgressEnable.invoke(it)
                    },
                )
            }
        }
    }
}
