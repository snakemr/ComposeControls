import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.math.abs


@Composable
fun Calendar(date: LocalDate, change: (LocalDate)->Unit) {
    val months = remember { listOf("янв","фев","мар","апр","май","июн","июл","авг","сен","окт","ноя","дек") }
    val days = remember { listOf("пн","вт","ср","чт","пт","сб","вс") }
    var year by remember { mutableStateOf(0) }
    var month by remember { mutableStateOf(0) }
    val state = rememberLazyListState()
    val state2 = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var size by remember { mutableStateOf(DpSize(1.dp, 1.dp)) }
    var yearWidth by remember { mutableStateOf(1.dp) }
    var yearHeight by remember { mutableStateOf(1.dp) }
    var fullHalfWidth by remember { mutableStateOf(0) }
    var halfWidth by remember { mutableStateOf(0) }
    var yearSize by remember { mutableStateOf(1.sp) }
    var monthSize by remember { mutableStateOf(1.sp) }

    Box(Modifier.onGloballyPositioned {
        size = with(density) {
            yearSize = it.size.width.toSp() / 20
            monthSize = it.size.width.toSp() / 30
            DpSize(it.size.width.toDp(), it.size.height.toDp())
        }
        yearWidth = size.width / 7
        halfWidth = it.size.width / 14
        if (fullHalfWidth == 0) scope.launch { state2.scrollToItem(1) }
        fullHalfWidth = it.size.width / 2
    }) {
        Box(Modifier.size(yearWidth, yearHeight).background(MaterialTheme.colors.secondary).align(Alignment.TopCenter))
        Column {
            LazyRow(Modifier.draggable(rememberDraggableState {
                scope.launch {
                    state.scrollBy(-it)
                    year = state.firstVisibleItemIndex + if (state.firstVisibleItemScrollOffset > halfWidth) 4 else 3
                }
            }, Orientation.Horizontal, onDragStopped = {
                scope.launch {
                    if (state.firstVisibleItemScrollOffset != 0) state.animateScrollToItem(year - 3)
                    if (year != date.year)
                        change(date.withYear(year))
                }
            }).onGloballyPositioned {
                yearHeight = with(density) { it.size.height.toDp() }
            }, state) {
                items(4000) {
                    Text(
                        (it).toString(),
                        Modifier.width(yearWidth).clickable {
                            if (it != year && it >= 3) scope.launch {
                                year = it
                                state.animateScrollToItem(year - 3)
                                change(date.withYear(year))
                            }
                        },
                        textAlign = TextAlign.Center,
                        fontSize = yearSize,
                        color = when (abs(year - it)) {
                            3 -> Color.LightGray
                            2 -> Color.Gray
                            1 -> Color.DarkGray
                            0 -> Color.Black
                            else -> Color.White
                        }
                    )
                }
            }
            Row(Modifier.height(yearHeight), verticalAlignment = Alignment.CenterVertically) {
                months.forEachIndexed { i, m ->
                    Text(m, Modifier.weight(1f).background(
                            if (i+1 == month) MaterialTheme.colors.secondary else Color.Transparent
                        ).clickable {
                            if (i+1 != month) {
                                month = i+1
                                change(date.withMonth(month))
                            }
                        },
                        if (i+1 == month) Color.Black else Color.Gray,
                        textAlign = TextAlign.Center, fontSize = monthSize)
                }
            }

            LazyRow(Modifier.draggable(rememberDraggableState {
                scope.launch {
                    state2.scrollBy(-it)
                }
            }, Orientation.Horizontal, onDragStopped = {
                scope.launch {
                    val new = state2.firstVisibleItemIndex +
                            if (state2.firstVisibleItemScrollOffset > fullHalfWidth) 1 else 0
                    if (state2.firstVisibleItemScrollOffset != 0) state2.animateScrollToItem(new)
                    if (new != 1) {
                        change(date.plusMonths(new-1L))
                        state2.scrollToItem(1)
                    }
                }
            }), state2) { items(3) { variant ->
                Column(Modifier.width(size.width)) {
                    Row {
                        days.forEachIndexed { i, d ->
                            Text(d, Modifier.weight(1f),
                                if (i > 4) MaterialTheme.colors.error else Color.Gray,
                                textAlign = TextAlign.Center, fontSize = yearSize)
                        }
                    }
                    var tmp = date.withDayOfMonth(1).plusMonths(variant-1L)
                    val m = tmp.monthValue
                    if (tmp.dayOfWeek != DayOfWeek.MONDAY) tmp = tmp.with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
                    do {
                        Row {
                            repeat(7) {
                                val d = tmp
                                Text(
                                    d.dayOfMonth.toString(), Modifier.weight(1f).background(
                                        if (d == date) MaterialTheme.colors.secondary else Color.Transparent
                                    ).clickable {
                                        if (d != date) change(d)
                                    },
                                    if (d.monthValue != date.monthValue)
                                        Color.LightGray
                                    else if (d.dayOfWeek > DayOfWeek.FRIDAY)
                                        MaterialTheme.colors.error
                                    else
                                        Color.Black,
                                    textAlign = TextAlign.Center, fontSize = yearSize
                                )
                                tmp = tmp.plusDays(1)
                            }
                        }
                    } while (tmp.monthValue == m)
                }
            } }
        }
    }
    LaunchedEffect(date) {
        year = date.year
        month = date.monthValue
        if (year >= 3) state.scrollToItem(year - 3)
    }
}
