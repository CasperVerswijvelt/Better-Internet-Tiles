package be.casperverswijvelt.unifiedinternetqs.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.casperverswijvelt.unifiedinternetqs.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@Composable
fun HomePage() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                modifier = Modifier.background(Color.Red),
                scrollBehavior = scrollBehavior
            )
        },
    ) {
        Column (Modifier.padding(it)) {
            Card(
                Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.padding(10.dp),
                    text = stringResource(R.string.about),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    modifier = Modifier.padding(10.dp),
                    text = stringResource(R.string.app_description_about)
                )
            }
        }
//        LazyColumn(Modifier.padding(it).fillMaxWidth()) {
//            listOf(
//                "Test", "Test3", "Test3", "Test3", "Test3", "Test3",
//                "Test3"
//            ).forEach { text ->
//                item {
//                    Card(
//                        Modifier
//                            .padding(10.dp)
//                            .fillMaxWidth()
//                            .height(100.dp)
//                    ) {
//                        Text(
//                            text = text
//                        )
//                    }
//                }
//            }
//        }
    }
}