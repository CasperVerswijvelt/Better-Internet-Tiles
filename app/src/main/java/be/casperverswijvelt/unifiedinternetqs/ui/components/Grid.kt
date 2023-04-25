package be.casperverswijvelt.unifiedinternetqs.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
    horizontalSpacing: Dp,
    verticalSpacing: Dp,
    content: List<@Composable () -> Unit>
) {
    val splitComposables: State<List<List<@Composable () -> Unit>>> = remember {
        derivedStateOf { content.chunked(columns) }
    }
    Box(modifier) {

        Column(
            verticalArrangement = Arrangement.spacedBy(verticalSpacing)
        ) {
            splitComposables.value.forEach {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
                ) {
                    val weightModifier = Modifier.weight(1f / columns)
                    it.forEach {
                        Box(weightModifier) { it() }
                    }
                    repeat(columns - it.size) {
                        Spacer(weightModifier)
                    }
                }
            }
        }
    }
}