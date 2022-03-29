import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.sign

@Composable
fun TimeEdit(
    time: LocalTime,
    onChange: (LocalTime)->Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    seconds: Boolean = false
){
    val hours = remember { List(24) { it.toString().padStart(2, '0') } }
    val minutes = remember { List(60) { it.toString().padStart(2, '0') } }
    Row(modifier) {
        DragEdit(time.hour.toString().padStart(2, '0'), hours.reversed(), {
            onChange(time.withHour(hours.indexOf(it)))
        }, Modifier, color, fontSize)
        Text(":", color = color, fontSize = fontSize)
        DragEdit(time.minute.toString().padStart(2, '0'), minutes.reversed(), {
            onChange(time.withMinute(minutes.indexOf(it)))
        }, Modifier, color, fontSize)
        if (seconds) {
            Text(".", color = color, fontSize = fontSize)
            DragEdit(time.second.toString().padStart(2, '0'), minutes.reversed(), {
                onChange(time.withSecond(minutes.indexOf(it)))
            }, Modifier, color, fontSize)
        }
    }
}

@Composable
fun DateEdit(
    date: LocalDate,
    onChange: (LocalDate)->Unit,
    modifier: Modifier = Modifier,
    range: ClosedRange<LocalDate> =
        LocalDate.of(1,1,1) .. LocalDate.of(3000, 12, 31),
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    dayTwoDigits: Boolean = false,
    monthNames: Boolean = true,
    shortMonths: Boolean = false,
    yearFourDigits: Boolean = true,
    autoWidth: Boolean = true
) {
    val months by remember { derivedStateOf { if (monthNames)
        listOf("января", "февраля", "марта", "апреля", "мая", "июня",
            "июля", "августа", "сентября", "октября", "ноября", "декабря").run {
            if (shortMonths) map { it.substring(0, 3) } else this
        }
    else List(12) { (it+1).toString().run { if (shortMonths) this else padStart(2, '0') } }
    } }
    val delimiter by remember { derivedStateOf { if (monthNames && !shortMonths) " " else "." } }
    val longest by remember { derivedStateOf { if (monthNames) if (shortMonths) 1 else 8 else 11 } }

    Row(modifier) {
        MeasureTextWidth("00", fontSize) { width ->
            if (dayTwoDigits) DragEdit(
                date.dayOfMonth.toString().padStart(2, '0'),
                (date.lengthOfMonth() downTo 1).filter { date.withDayOfMonth(it) in range }.map {
                    it.toString().padStart(2, '0')
                }, onChange = { day ->
                    onChange(date.withDayOfMonth(day.toInt()))
                }, if (autoWidth) Modifier.width(width) else Modifier,
                color, fontSize, TextAlign.Center
            ) else DragEdit(
                date.dayOfMonth,
                (date.lengthOfMonth() downTo 1).filter { date.withDayOfMonth(it) in range },
                onChange = { day ->
                    onChange(date.withDayOfMonth(day))
                }, if (autoWidth) Modifier.width(width) else Modifier,
                color, fontSize, TextAlign.Center
            )
        }

        Text(delimiter, color = color, fontSize = fontSize)

        MeasureTextWidth(months[longest], fontSize) { width ->
            DragEdit(
                months[date.monthValue - 1], months.filterIndexed { index, _ ->
                    date.withMonth(index+1) in range
                } .reversed(), { month ->
                    onChange(date.withMonth(months.indexOf(month) + 1))
                }, if (autoWidth) Modifier.width(width) else Modifier,
                color, fontSize, TextAlign.Center
            )
        }

        Text(delimiter, color = color, fontSize = fontSize)

        MeasureTextWidth("00", fontSize) { width ->
            if (yearFourDigits) DragEdit(
                date.year,
                (range.endInclusive.year downTo range.start.year).filter { date.withYear(it) in range },
                onChange = { year ->
                    onChange(date.withYear(year))
                }, if (autoWidth) Modifier.width(width * 2) else Modifier,
                color, fontSize, TextAlign.Center
            ) else DragEdit(
                date.year.toString().takeLast(2),
                (range.endInclusive.year downTo range.start.year).filter { date.withYear(it) in range }.map {
                    it.toString().takeLast(2)
                },
                onChange = { year ->
                    onChange(date.withYear(year.toInt() + 2000))
                }, if (autoWidth) Modifier.width(width) else Modifier,
                color, fontSize, TextAlign.Center
            )
        }
    }
}

@Composable
fun DragEdit(
    value: Int,
    onChange: (Int)->Unit,
    modifier: Modifier = Modifier,
    range: IntProgression = 99 downTo 0,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null
) = DragEdit(value, range.toList(), onChange, modifier, color, fontSize, textAlign)

@Composable
fun <T> DragEdit(
    value: T,
    range: List<T>,
    onChange: (T)->Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null
) {
    val scope = rememberCoroutineScope()
    val lazyState = rememberLazyListState()
    val dragState = rememberDraggableState {
        scope.launch { lazyState.scrollBy(-it) }
    }
    var dragging by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(IntSize(0,0)) }

    Box(modifier) {
        Text(
            value.toString(),
            modifier
                .onGloballyPositioned { size = it.size }
                .draggable(dragState, Orientation.Vertical, onDragStarted = {
                    lazyState.scrollToItem(range.indexOf(value))
                    dragging = true
                }, onDragStopped = {
                    dragging = false
                    lazyState.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.let { size ->
                        onChange(range.elementAt(lazyState.firstVisibleItemIndex +
                                if(lazyState.firstVisibleItemScrollOffset > size/2) 1 else 0))
                    }
                })
                .scrollable(rememberScrollableState { delta ->
                    if (!lazyState.isScrollInProgress) {
                        val offset = sign(delta).toInt()
                        if (offset != 0) scope.launch {
                            lazyState.scrollToItem(range.indexOf(value))
                            val next = lazyState.firstVisibleItemIndex - offset
                            if (next >= 0 && next < range.count()) {
                                lazyState.animateScrollToItem(next)
                                onChange(range.elementAt(next))
                            }
                        }
                    }
                    delta
                }, Orientation.Vertical),
            color = color, fontSize = fontSize, textAlign = textAlign, maxLines = 1
        )
        if (dragging || lazyState.isScrollInProgress) LazyColumn(
            modifier
                .background(Color.White)
                .draggable(dragState, Orientation.Vertical)
                .size( with (LocalDensity.current) { DpSize(size.width.toDp(), size.height.toDp()) } ),
            state = lazyState
        ) {
            items(range.count()) {
                Text(
                    range.elementAt(it).toString(),
                    modifier, color = color, fontSize = fontSize, textAlign = textAlign, maxLines = 1
                )
                if (it < range.count()-1) Divider()
            }
        }
    }
}

@Composable
fun MeasureTextWidth(sample: String, fontSize: TextUnit = TextUnit.Unspecified, content: @Composable (width: Dp)->Unit)
        = SubcomposeLayout { constraints ->
    val textWidth = subcompose("sampleText") {
        Text(sample, fontSize = fontSize)
    }[0].measure(Constraints()).width.toDp()

    val contentPlaceable = subcompose("content") {
        content(textWidth)
    }[0].measure(constraints)
    layout(contentPlaceable.width, contentPlaceable.height) {
        contentPlaceable.place(0, 0)
    }
}
