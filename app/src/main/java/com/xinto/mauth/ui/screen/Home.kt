package com.xinto.mauth.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.ui.component.MaterialBottomSheetDialog
import com.xinto.mauth.ui.navigation.AddAccountParams
import com.xinto.mauth.ui.navigation.MauthDestination
import com.xinto.mauth.ui.navigation.MauthNavigator
import com.xinto.mauth.ui.viewmodel.HomeViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeScreen(
    navigator: MauthNavigator,
    viewModel: HomeViewModel = getViewModel(),
) {
    var showAddAccount by remember { mutableStateOf(false) }

    val timer by animateFloatAsState(viewModel.timerProgress, animationSpec = tween(500))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.app_name))
                }
            )
        },
        bottomBar = {
            Column {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = timer
                )
                BottomAppBar(
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            showAddAccount = true
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                imageVector = Icons.Rounded.Sort,
                                contentDescription = null
                            )
                        }
                    },
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(viewModel.accounts) { account ->
                var visible by remember { mutableStateOf(false) }
                val code = viewModel.codes[account.secret]
                Account(
                    onCopyClick = {
                        viewModel.copyCodeToClipboard(account.label, code)
                    },
                    onVisibleChange = {
                        visible = !visible
                    },
                    visible = visible,
                    name = { Text(account.label) },
                    icon = {
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(MaterialTheme.shapes.large)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    },
                    code = {
                        AnimatedContent(
                            targetState = code,
                            transitionSpec = {
                                slideIntoContainer(
                                    towards = AnimatedContentScope.SlideDirection.Up,
                                    animationSpec = tween(500)
                                ) + fadeIn() with
                                    slideOutOfContainer(
                                        towards = AnimatedContentScope.SlideDirection.Up,
                                        animationSpec = tween(500)
                                    ) + fadeOut()
                            }
                        ) { animatedCode ->
                            if (animatedCode != null) {
                                if (visible) {
                                    Text(animatedCode)
                                } else {
                                    Text("*".repeat(animatedCode.length))
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    if (showAddAccount) {
        val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val qrCode = viewModel.decodeQrCodeFromImageUri(uri)
                if (qrCode != null) {
                    val params = viewModel.parseOtpUri(qrCode)
                    if (params != null) {
                        navigator.push(MauthDestination.AddAccount(params))
                    }
                }
            }
        }
        MaterialBottomSheetDialog(
            onDismissRequest = {
                showAddAccount = false
            },
            title = {
                Text(stringResource(R.string.home_addaccount_title))
            },
            subtitle = {
                Text(stringResource(R.string.home_addaccount_subtitle))
            },
        ) {
            Column(
                modifier = Modifier.clip(MaterialTheme.shapes.large),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                AddAccountType(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navigator.push(MauthDestination.QrScanner)
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.QrCodeScanner,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(stringResource(R.string.home_addaccount_data_scanqr))
                    },
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                AddAccountType(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Image,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(stringResource(R.string.home_addaccount_data_imageqr))
                    },
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                )
                AddAccountType(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        showAddAccount = false
                        navigator.push(MauthDestination.AddAccount(AddAccountParams()))
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Password,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(stringResource(R.string.home_addaccount_data_manual))
                    },
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }
    }
}

@Composable
private fun Account(
    onCopyClick: () -> Unit,
    onVisibleChange: (Boolean) -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier,
    name: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    code: @Composable () -> Unit,
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                icon()
                ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                    name()
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {

                }) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null
                    )
                }
            }
            Divider(Modifier.padding(vertical = 12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                    code()
                }
                Spacer(Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconToggleButton(checked = visible, onCheckedChange = onVisibleChange) {
                        Icon(
                            imageVector = if (visible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = null
                        )
                    }
                    FilledTonalIconButton(onClick = onCopyClick) {
                        Icon(
                            imageVector = Icons.Rounded.CopyAll,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddAccountType(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        color = color,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
                text()
            }
        }
    }
}