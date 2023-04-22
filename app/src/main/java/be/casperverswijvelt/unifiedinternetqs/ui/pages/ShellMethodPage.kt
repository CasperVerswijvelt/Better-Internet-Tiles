package be.casperverswijvelt.unifiedinternetqs.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.data.ShellMethod
import be.casperverswijvelt.unifiedinternetqs.ui.components.RadioEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShellMethodPage(
    onBackClicked: () -> Unit,
    shellMethod: ShellMethod,
    onShellMethodSelected: (ShellMethod) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()


    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text("Shell method") },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = {
                        onBackClicked()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
            )
        },
    ) {
        Column (
            modifier = Modifier
                .padding(top = it.calculateTopPadding())
                .verticalScroll(rememberScrollState())
        ) {
            RadioEntry(title = stringResource(id = R.string.root), enabled = shellMethod === ShellMethod.ROOT) {
                onShellMethodSelected(ShellMethod.ROOT)
            }
            RadioEntry(title = stringResource(id = R.string.shizuku), enabled = shellMethod === ShellMethod.SHIZUKU){
                onShellMethodSelected(ShellMethod.SHIZUKU)
            }
        }
    }
}