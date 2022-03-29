import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T> LazyCombo(
    value: T,
    onChange: (T)->Unit,
    modifier: Modifier = Modifier,
    onEdit: ((String)->Boolean)? = null,
    items: List<T> = listOf(),
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    var edit by remember { mutableStateOf(false) }
    var width by remember { mutableStateOf(0) }
    var focused by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    var editValue by remember { mutableStateOf(TextFieldValue()) }
    var selected by remember (items) { mutableStateOf(-1) }

    Box(modifier.onGloballyPositioned { width = it.size.width }) {
        if (edit)
            BasicTextField(editValue, {
                if (onEdit == null || onEdit(it.text))
                    editValue = it
            }, Modifier.focusRequester(focus).onFocusChanged { editor ->
                if (edit && focused && !editor.isFocused) {
                    edit = false
                    items.firstOrNull { it.toString() == editValue.text } ?.let(onChange)
                }
                focused = editor.isFocused
            }.onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) when (event.key) {
                    Key.Enter, Key.NumPadEnter -> {
                        edit = false
                        if (selected in items.indices)
                            onChange(items[selected])
                        else
                            items.firstOrNull { it.toString() == editValue.text } ?.let(onChange)
                        true
                    }
                    Key.F2 -> {
                        edit = false
                        items.firstOrNull { it.toString() == editValue.text } ?.let(onChange)
                        true
                    }
                    Key.Escape -> {
                        edit = false
                        true
                    }
                    Key.DirectionUp -> {
                        if (selected > 0) selected --
                        true
                    }
                    Key.DirectionDown -> {
                        if (selected + 1 < items.size) selected ++
                        true
                    }
                    else -> false
                }   else    false
            }.width(with(LocalDensity.current){ width.toDp() }),
                textStyle = MaterialTheme.typography.body1.copy(color = color, fontSize = fontSize),
                singleLine = true
            )
        else
            Text(value.toString(), Modifier.clickable {
                editValue = TextFieldValue(value.toString(), TextRange(0, value.toString().length) )
                focused = false
                edit = true
            }, color, fontSize, maxLines = 1)

       DropdownMenu(edit, { edit = false },
            false, modifier = Modifier.width(with(LocalDensity.current){ width.toDp() })
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem({
                    edit = false
                    onChange(item)
                },
                    if (index == selected)
                        Modifier.background(MaterialTheme.colors.secondary)
                    else
                        Modifier
                ) {
                    Text(item.toString(), color = color, fontSize = fontSize, maxLines = 1)
                }
            }
        }
    }

    LaunchedEffect(edit) {
        if (edit) {
            focus.requestFocus()
        }
    }
}