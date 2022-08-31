import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import java.awt.event.MouseEvent

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PointerEventButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
//    enabled: Boolean,
//    interactionSource: MutableInteractionSource,
//    elevation: ButtonElevation?,
//    shape: Shape,
//    border: BorderStroke?,
//    colors: ButtonColors,
//    contentPadding: PaddingValues,
    onDoubleClick: (() -> Unit)? = null,
    onRightClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) = OutlinedButton({}, modifier.onPointerEvent(PointerEventType.Press) {
    it.awtEventOrNull?.run {
        if (button == MouseEvent.BUTTON1)
            if (clickCount == 2) onDoubleClick?.invoke() else onClick()
        else if (button == MouseEvent.BUTTON3)
            onRightClick?.invoke()
    }},
    content = content
)
