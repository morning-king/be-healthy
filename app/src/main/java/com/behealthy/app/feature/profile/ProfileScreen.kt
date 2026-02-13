package com.behealthy.app.feature.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.behealthy.app.ui.theme.ThemeStyle
import com.behealthy.app.ui.theme.getThemeColorScheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.io.File
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val backupList by viewModel.backupList.collectAsState()
    val backupOperationState by viewModel.backupOperationState.collectAsState()
    val context = LocalContext.current
    
    var isEditing by remember { 
        mutableStateOf(false) 
    }
    
    // Avatar Picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.updateAvatar(context, uri)
        }
    }
    
    // Note Image Picker
    val noteImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            viewModel.updateNoteImage(it.toString())
        }
    }
    
    // Date Picker
    var showDatePicker by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLogDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    if (showLogDialog) {
        LogViewerDialog(
            onDismiss = { showLogDialog = false },
            onTriggerSync = { viewModel.triggerSync() }
        )
    }

    if (showBackupDialog) {
        BackupDialog(
            backups = backupList,
            operationState = backupOperationState,
            onDismiss = { showBackupDialog = false },
            onCreateBackup = { viewModel.createBackup() },
            onRestoreBackup = { viewModel.restoreBackup(it) },
            onLoadBackups = { viewModel.loadBackups() }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.themeStyle,
            currentAlpha = uiState.backgroundAlpha,
            pageTransition = uiState.pageTransition ?: "Default",
            zenRotationEnabled = uiState.zenRotationEnabled,
            zenRotationSpeed = uiState.zenRotationSpeed,
            zenRotationDirection = uiState.zenRotationDirection,
            techIntensity = uiState.techIntensity,
            fontColorMode = uiState.fontColorMode,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { viewModel.updateThemeStyle(it) },
            onAlphaChange = { viewModel.updateBackgroundAlpha(it) },
            onPageTransitionChange = { viewModel.updatePageTransition(it) },
            onZenRotationEnabledChange = { viewModel.updateZenRotationEnabled(it) },
            onZenRotationSpeedChange = { viewModel.updateZenRotationSpeed(it) },
            onZenRotationDirectionChange = { viewModel.updateZenRotationDirection(it) },
            onTechIntensityChange = { viewModel.updateTechIntensity(it) },
            onFontColorModeChange = { viewModel.updateFontColorMode(it) }
        )
    }
    
    if (showDatePicker) {
        val initialDateMillis = uiState.birthday?.let { 
            try {
                LocalDate.parse(it, dateFormatter).toEpochDay() * 24 * 60 * 60 * 1000 
            } catch (e: Exception) { null }
        }
        
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        viewModel.updateBirthday(date.format(dateFormatter))
                    }
                    showDatePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Auto-save when exiting edit mode
    BackHandler(enabled = isEditing) {
        isEditing = false
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("我的档案", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    if (!isEditing) {
                        IconButton(onClick = { showThemeDialog = true }) {
                            Icon(Icons.Default.Palette, contentDescription = "主题风格")
                        }
                    }
                },
                actions = {
                    if (!isEditing && !uiState.nickname.isNullOrEmpty()) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
                    } else if (isEditing) {
                        IconButton(onClick = { isEditing = false }) {
                            Icon(Icons.Default.Close, contentDescription = "关闭")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Remove the edit button from bottom right
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (isEditing) {
                ProfileEditView(
                    uiState = uiState,
                    viewModel = viewModel,
                    onAvatarClick = { imagePicker.launch("image/*") },
                    onNicknameChange = { viewModel.updateNickname(it) },
                    onNoteChange = { viewModel.updateNote(it) },
                    onNoteImageClick = { noteImagePicker.launch("image/*") },
                    onBirthdayClick = { showDatePicker = true },
                    onBirthdayReminderChange = { viewModel.setBirthdayReminderEnabled(it) },
                    onAvatarCropChange = { viewModel.setAvatarCropEnabled(it) },
                    onShowBackupDialog = { showBackupDialog = true }
                )
            } else {
                ProfileDisplayView(uiState = uiState, onEditClick = { isEditing = true })
            }
        }
    }
}

@Composable
fun BackupDialog(
    backups: List<File>,
    operationState: String?,
    onDismiss: () -> Unit,
    onCreateBackup: () -> Unit,
    onRestoreBackup: (File) -> Unit,
    onLoadBackups: () -> Unit
) {
    LaunchedEffect(Unit) {
        onLoadBackups()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("数据备份与恢复") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (operationState != null) {
                    Text(
                        text = operationState,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = onCreateBackup,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null) // Ensure Backup icon is available or use generic
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("立即创建备份")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "历史备份列表",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (backups.isEmpty()) {
                    Text(
                        text = "暂无备份记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp).align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(backups) { file ->
                            BackupItem(file = file, onRestore = { onRestoreBackup(file) })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
fun BackupItem(file: File, onRestore: () -> Unit) {
    var showConfirmRestore by remember { mutableStateOf(false) }

    if (showConfirmRestore) {
        AlertDialog(
            onDismissRequest = { showConfirmRestore = false },
            title = { Text("确认恢复") },
            text = { Text("恢复此备份将覆盖当前所有数据，且不可撤销。建议先创建新备份。确定要继续吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmRestore = false
                        onRestore()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("确定恢复")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRestore = false }) {
                    Text("取消")
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            Text(
                text = "${file.length() / 1024} KB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(onClick = { showConfirmRestore = true }) {
            Icon(
                imageVector = Icons.Default.Restore, // Ensure Restore icon is available or use generic
                contentDescription = "恢复",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ProfileDisplayView(uiState: ProfileUiState, onEditClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header: Avatar & Name
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onEditClick)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.avatarUri.isNullOrEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "头像",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(uiState.avatarUri),
                        contentDescription = "头像",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = uiState.nickname ?: "未命名",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "点击编辑资料",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        if (!uiState.note.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        
        if (!uiState.noteImageUri.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = rememberAsyncImagePainter(uiState.noteImageUri),
                contentDescription = "个人简介图片",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(number = uiState.totalWorkoutDays.toString(), label = "健身天数")
            StatItem(number = uiState.totalMoodRecords.toString(), label = "心情记录")
            StatItem(number = uiState.currentStreak.toString(), label = "连续打卡")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Achievements
        Text(
            text = "我的成就",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        AchievementBadges(uiState)
        
        Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
    }
}

@Composable
fun ProfileEditView(
    uiState: ProfileUiState,
    viewModel: ProfileViewModel,
    onAvatarClick: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onNoteImageClick: () -> Unit,
    onBirthdayClick: () -> Unit,
    onBirthdayReminderChange: (Boolean) -> Unit,
    onAvatarCropChange: (Boolean) -> Unit,
    onShowBackupDialog: () -> Unit
) {
    // Use local state for inputs to prevent cursor jumping (Item 15)
    var nickname by remember { mutableStateOf(uiState.nickname ?: "") }
    var note by remember { mutableStateOf(uiState.note ?: "") }
    
    // Update local state when uiState changes, but preserve user input
    LaunchedEffect(uiState.nickname) {
        if (nickname.isEmpty() && !uiState.nickname.isNullOrEmpty()) {
            nickname = uiState.nickname
        }
    }
    LaunchedEffect(uiState.note) {
        if (note.isEmpty() && !uiState.note.isNullOrEmpty()) {
            note = uiState.note
        }
    }

    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.themeStyle,
            currentAlpha = uiState.backgroundAlpha,
            zenRotationEnabled = uiState.zenRotationEnabled,
            zenRotationSpeed = uiState.zenRotationSpeed,
            zenRotationDirection = uiState.zenRotationDirection,
            techIntensity = uiState.techIntensity,
            fontColorMode = uiState.fontColorMode,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { 
                viewModel.updateThemeStyle(it)
                // Dialog stays open to allow adjusting multiple things? 
                // Or close it? Original code closed it.
                // showThemeDialog = false // Let user close it manually if they want to adjust slider?
                // Actually, selecting a theme usually closes dialog.
                // But transparency slider needs dialog to stay open.
                // So let's NOT close on theme select, only on Dismiss/Close button.
            },
            onAlphaChange = { viewModel.updateBackgroundAlpha(it) },
            onZenRotationEnabledChange = { viewModel.updateZenRotationEnabled(it) },
            onZenRotationSpeedChange = { viewModel.updateZenRotationSpeed(it) },
            onZenRotationDirectionChange = { viewModel.updateZenRotationDirection(it) },
            onTechIntensityChange = { viewModel.updateTechIntensity(it) },
            onFontColorModeChange = { viewModel.updateFontColorMode(it) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "编辑个人资料",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable { onAvatarClick() }
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.avatarUri.isNullOrEmpty()) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(50.dp))
            } else {
                Image(
                    painter = rememberAsyncImagePainter(uiState.avatarUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Nickname
        OutlinedTextField(
            value = nickname,
            onValueChange = { 
                nickname = it
                onNicknameChange(it) 
            },
            label = { Text("昵称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = uiState.birthday ?: "",
            onValueChange = {},
            label = { Text("生日") },
            leadingIcon = { Icon(Icons.Default.DateRange, null) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBirthdayClick),
            enabled = false, // Disable typing, but handle click on parent or overlay
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        )
        // Hack to make read-only text field clickable
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // Approximate height of TextField
                .offset(y = (-56).dp)
                .clickable(onClick = onBirthdayClick)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = uiState.note ?: "",
            onValueChange = onNoteChange,
            label = { Text("个人简介 (支持Emoji)") },
            leadingIcon = { Icon(Icons.Default.Edit, null) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Note Image Picker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onNoteImageClick),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.noteImageUri.isNullOrEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("添加个人简介图片", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Image(
                    painter = rememberAsyncImagePainter(uiState.noteImageUri),
                    contentDescription = "个人简介图片",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("生日提醒", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = uiState.birthdayReminderEnabled, onCheckedChange = onBirthdayReminderChange)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("头像裁剪", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = uiState.avatarCropEnabled, onCheckedChange = onAvatarCropChange)
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShowBackupDialog() }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("数据备份与恢复", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "备份数据库以防止数据丢失", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "进入备份",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Auto-save hint
        Text(
            text = "资料将自动保存",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StatItem(number: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AchievementBadges(uiState: ProfileUiState) {
    val badges = listOf(
        Triple("初次体验", uiState.hasFirstWorkoutBadge, Icons.Default.Star),
        Triple("七日连胜", uiState.hasSevenDayStreakBadge, Icons.Default.ThumbUp),
        Triple("月度达人", uiState.hasThirtyDayStreakBadge, Icons.Default.Favorite),
        Triple("百炼成钢", uiState.hasHundredWorkoutsBadge, Icons.Default.Face),
        Triple("积极向上", uiState.hasPositiveWeekBadge, Icons.Default.CheckCircle),
        Triple("心情管家", uiState.hasMoodMonthBadge, Icons.Default.DateRange)
    )
    
    // Using Column/Row instead of LazyVerticalGrid to avoid nested scrolling with parent Scroll
    Column {
        val rows = badges.chunked(3)
        rows.forEach { rowBadges ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowBadges.forEach { (name, unlocked, icon) ->
                    BadgeItem(name, unlocked, icon)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BadgeItem(name: String, unlocked: Boolean, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (unlocked) MaterialTheme.colorScheme.primaryContainer 
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    currentAlpha: Float,
    pageTransition: String = "Default",
    zenRotationEnabled: Boolean = true,
    zenRotationSpeed: Float = 5f,
    zenRotationDirection: String = "Clockwise",
    techIntensity: String = "Standard",
    fontColorMode: String = "Auto",
    onDismiss: () -> Unit,
    onThemeSelected: (String) -> Unit,
    onAlphaChange: (Float) -> Unit,
    onPageTransitionChange: (String) -> Unit = {},
    onZenRotationEnabledChange: (Boolean) -> Unit = {},
    onZenRotationSpeedChange: (Float) -> Unit = {},
    onZenRotationDirectionChange: (String) -> Unit = {},
    onTechIntensityChange: (String) -> Unit = {},
    onFontColorModeChange: (String) -> Unit = {}
) {
    val themes = ThemeStyle.values()
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().heightIn(max = 700.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Text(
                    text = "主题风格设置",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Zen Theme Settings
                if (currentTheme == "Zen") {
                     Text(
                        text = "禅意设置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("禅字旋转", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = zenRotationEnabled,
                            onCheckedChange = onZenRotationEnabledChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                    
                    if (zenRotationEnabled) {
                        Text(
                            text = "旋转速度: ${zenRotationSpeed.toInt()}秒/圈", 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = zenRotationSpeed,
                            onValueChange = onZenRotationSpeedChange,
                            valueRange = 1f..20f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        
                        Text(
                            text = "旋转方向", 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onZenRotationDirectionChange("Clockwise") }
                            ) {
                                RadioButton(
                                    selected = zenRotationDirection == "Clockwise",
                                    onClick = { onZenRotationDirectionChange("Clockwise") }
                                )
                                Text("顺时针", style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onZenRotationDirectionChange("CounterClockwise") }
                            ) {
                                RadioButton(
                                    selected = zenRotationDirection == "CounterClockwise",
                                    onClick = { onZenRotationDirectionChange("CounterClockwise") }
                                )
                                Text("逆时针", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                }
                
                // Tech Theme Settings
                if (currentTheme == "Tech") {
                     Text(
                        text = "科技感设置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "视觉强度", 
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp), 
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                         listOf("Minimal", "Standard", "Vibrant").forEach { intensity ->
                             FilterChip(
                                 selected = techIntensity == intensity,
                                 onClick = { onTechIntensityChange(intensity) },
                                 label = { 
                                     Text(when(intensity) {
                                         "Minimal" -> "极简"
                                         "Standard" -> "标准"
                                         "Vibrant" -> "炫彩"
                                         else -> intensity
                                     }) 
                                 }
                             )
                         }
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                }

                // General Settings (Font Color Mode)
                 Text(
                    text = "显示设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("字体颜色模式", style = MaterialTheme.typography.bodyMedium)
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp), 
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                     listOf("Auto", "Light", "Dark").forEach { mode ->
                         Row(
                             verticalAlignment = Alignment.CenterVertically,
                             modifier = Modifier.clickable { onFontColorModeChange(mode) }
                         ) {
                             RadioButton(
                                 selected = fontColorMode == mode,
                                 onClick = { onFontColorModeChange(mode) }
                             )
                             Text(when(mode) {
                                 "Auto" -> "跟随系统"
                                 "Light" -> "浅色"
                                 "Dark" -> "深色"
                                 else -> mode
                             }, style = MaterialTheme.typography.bodySmall)
                         }
                     }
                }
                
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Transparency Slider
                Text(
                    text = "背景透明度: ${(currentAlpha * 100).toInt()}%", 
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = currentAlpha,
                    onValueChange = onAlphaChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Page Transition Settings
                Text(
                    text = "页面转场动画",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val transitions = listOf("Default", "Fade", "Zoom", "Depth", "Rotate")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(100.dp)
                ) {
                    items(transitions) { transition ->
                        FilterChip(
                            selected = pageTransition == transition,
                            onClick = { onPageTransitionChange(transition) },
                            label = { 
                                Text(when(transition) {
                                    "Default" -> "默认"
                                    "Fade" -> "淡入淡出"
                                    "Zoom" -> "缩放"
                                    "Depth" -> "深度"
                                    "Rotate" -> "旋转"
                                    else -> transition
                                }) 
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Theme Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(300.dp) // Fixed height for grid inside scrollable column
                ) {
                    items(themes) { theme ->
                        ThemeCard(
                            theme = theme,
                            isSelected = theme.name == currentTheme,
                            onClick = { onThemeSelected(theme.name) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("完成")
                }
            }
        }
    }
}

@Composable
fun ThemeCard(theme: ThemeStyle, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            // Preview Background
            com.behealthy.app.ui.DynamicThemeBackground(theme = theme, alpha = 1f)
            
            if (isSelected) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = getThemeName(theme.name),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

fun getThemeName(themeStyleName: String): String {
    return try {
        when (ThemeStyle.valueOf(themeStyleName)) {
            ThemeStyle.Default -> "默认风格"
            ThemeStyle.Tech -> "科技风格"
            ThemeStyle.Sports -> "运动风格"
            ThemeStyle.Cute -> "可爱风格"
            ThemeStyle.Doraemon -> "哆啦A梦"
            ThemeStyle.Minions -> "小黄人"
            ThemeStyle.WallE -> "机器人总动员"
            ThemeStyle.NewYear -> "春节过年"
            ThemeStyle.NBA -> "NBA"
            ThemeStyle.Badminton -> "羽毛球"
            ThemeStyle.FootballWorldCup -> "世界杯"
            ThemeStyle.Zen -> "禅"
            ThemeStyle.Dao -> "道"
        }
    } catch (e: Exception) {
        "默认风格"
    }
}
