package be.casperverswijvelt.unifiedinternetqs.ui.pages

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.data.ShellMethod
import be.casperverswijvelt.unifiedinternetqs.ui.components.AlertDialog
import be.casperverswijvelt.unifiedinternetqs.ui.components.OnLifecycleEvent
import be.casperverswijvelt.unifiedinternetqs.util.AlertDialogData
import be.casperverswijvelt.unifiedinternetqs.util.ShizukuUtil
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShellMethodPage(
    onBackClicked: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val preferences = BITPreferences(context)
    val coroutineScope = rememberCoroutineScope()

    val selectedShellMethod by preferences.getShellMethod.collectAsState(initial = ShellMethod.AUTO)
    var alertDialog by remember { mutableStateOf<AlertDialogData?>(null) }

    val afterPermissionRequested: (ShellMethod) -> Unit = { method ->
        if (method.isGranted()) {
            coroutineScope.launch {
                preferences.setShellMethod(method)
            }
        } else {
            alertDialog = method.alertDialog
        }
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text(stringResource(R.string.shell_method)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = {
                        onBackClicked()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = ""
                        )
                    }
                },
            )
        },
    ) {
        Column (
            modifier = Modifier
                .padding(top = it.calculateTopPadding())
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                listOf(
                    ShellMethod.ROOT,
                    ShellMethod.SHIZUKU
                ).forEach { method ->
                    val selected = method == selectedShellMethod
                    var granted by remember { mutableStateOf(method.isGranted()) }
                    val selectedAndGranted = selected && granted
                    val checkAlpha by animateFloatAsState(
                        targetValue = if (selectedAndGranted) 1f else 0f
                    )

                    OutlinedCard(
                        onClick = {
                            when (method) {
                                ShellMethod.ROOT -> {
                                    Shell.getShell {
                                        afterPermissionRequested(method)
                                    }
                                }
                                ShellMethod.SHIZUKU -> {
                                    ShizukuUtil.requestShizukuPermission {
                                        afterPermissionRequested(method)
                                    }
                                }
                                else -> {}
                            }
                        },
                        colors = if (selectedAndGranted)
                            CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = .15f)
                            )
                        else
                            CardDefaults.outlinedCardColors(),
                        modifier = Modifier.width(300.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1F)
                            ) {
                                Text(
                                    text = stringResource(method.nameResource),
                                    fontSize = 18.sp
                                )
                                method.descriptionResource?.let { descriptionResource ->
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = stringResource(descriptionResource),
                                        fontSize = 14.sp,
                                        modifier = Modifier.alpha(.8f),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = if (selectedAndGranted)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.Transparent,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .2f)),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.baseline_check_24),
                                    contentDescription = "",
                                    contentScale = ContentScale.Inside,
                                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .alpha(checkAlpha)
                                )
                            }
                        }
                    }
                    OnLifecycleEvent { _, event ->
                        when(event) {
                            Lifecycle.Event.ON_RESUME -> {
                                granted = method.isGranted()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    alertDialog?.let {
        AlertDialog(
            alertDialogData = it,
            onDismissRequest = {
                alertDialog = null
            }
        )
    }
}