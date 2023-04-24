package be.casperverswijvelt.unifiedinternetqs.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun VerticalGrid(
    modifier: Modifier = Modifier,
    columns: Int = 2,
    spacing: Dp,
    content: List<@Composable () -> Unit>
) {
    val splitComposables: State<List<List<@Composable () -> Unit>>> = remember {
        derivedStateOf { content.chunked(columns) }
    }
    Box(modifier) {

        Column(
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            splitComposables.value.forEach {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    it.forEach {
                        Box(Modifier.weight(1f / columns)) {
                            it()
                        }
                    }
                }
            }
        }
    }
}