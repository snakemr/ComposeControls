import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SpinEdit(
    value: Int, onEdit: (Int)->Unit,
    modifier: Modifier = Modifier.width(48.dp),
    range: IntRange = Int.MIN_VALUE .. Int.MAX_VALUE
) {
    var edit by remember { mutableStateOf(false) }
    var focused by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    Column(modifier.scrollable(
        rememberScrollableState { delta ->
            if (delta > 0 && value+1 in range) onEdit(value+1)
            else
                if (delta < 0 && value-1 in range) onEdit(value-1)
            delta
        },
        Orientation.Vertical
    ), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.KeyboardArrowUp, "+", Modifier.clickable {
            if (value+1 in range) onEdit(value+1)
        }, tint = MaterialTheme.colors.primary)
        if (edit) BasicTextField(value.toString(), { str ->
            str.toIntOrNull()?.takeIf { it in range } ?.let(onEdit)
        }, Modifier.fillMaxWidth().focusRequester(focus).onFocusChanged {
            if (focused && !it.isFocused) edit = false
            focused = it.isFocused
        }, textStyle = MaterialTheme.typography.body1.copy(textAlign = TextAlign.Center), singleLine = true)
        else
            Text(value.toString(), Modifier.clickable {
                focused = false
                edit = true
            }.draggable(rememberDraggableState { delta ->
                if (delta < 0 && value+1 in range) onEdit(value+1)
                else
                    if (delta > 0 && value-1 in range) onEdit(value-1)
            }, Orientation.Vertical), maxLines = 1)
        Icon(Icons.Default.KeyboardArrowDown, "-", Modifier.clickable {
            if (value-1 in range) onEdit(value-1)
        }, tint = MaterialTheme.colors.primary)
    }
    LaunchedEffect(edit) {
        if (edit) focus.requestFocus()
    }
}