import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.time.LocalDate
import java.time.LocalTime

@Composable
@Preview
fun App() {
    var value by remember { mutableStateOf(42) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.now()) }
    var precision by remember { mutableStateOf(7) }
    var code by remember { mutableStateOf("007") }
    var search by remember { mutableStateOf("007") }
    val all = remember { List(50) { it.toString().padStart(3,'0') } }
    val items by remember { derivedStateOf {
        all.filter { it.contains(search) }
    } }

    MaterialTheme {
        Column {
            LazyCombo(code, {
                code = it
                search = it
            }, Modifier.width(100.dp), {
                search = it
                true
            }, items, fontSize = 36.sp, color = MaterialTheme.colors.secondaryVariant)

            DragEdit(value, { value = it }, Modifier.width(60.dp),
                fontSize = 48.sp, color = MaterialTheme.colors.primaryVariant, textAlign = TextAlign.Center
            )
            DateEdit(date, { date = it },
                range = LocalDate.of(2022, 2, 22) .. LocalDate.of(2037, 5, 5),
                color = MaterialTheme.colors.primaryVariant, fontSize = 48.sp   //, autoWidth = false
                //dayTwoDigits = true, monthNames = true, shortMonths = true, yearFourDigits = true
                //dayTwoDigits = true, monthNames = false, shortMonths = false, yearFourDigits = false
            )
            TimeEdit(time, { time = it },
                color = MaterialTheme.colors.secondaryVariant, fontSize = 48.sp, seconds = true
            )
            SpinEdit(precision, { precision = it }, Modifier.width(24.dp), 0..72)

            PointerEventButton({ println(1) }, onRightClick = { println(-1) }, onDoubleClick = { println(2) } ) { Text("click me") }


            var d by remember { mutableStateOf(LocalDate.now()) }
            Calendar(d, { d = it })
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Custom Controls") {
        App()
    }
}
